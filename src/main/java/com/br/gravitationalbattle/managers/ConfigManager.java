package com.br.gravitationalbattle.managers;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.br.gravitationalbattle.GravitationalBattle;

public class ConfigManager {

    private final GravitationalBattle plugin;
    private FileConfiguration config;
    private File configFile;
    private Location lobbyLocation;
    private int startCountdown;
    private int gameTime;
    private int maxPlayers;

    public ConfigManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Carrega a configuração do plugin
     */
    public void loadConfig() {
        // Salva a configuração padrão se não existir
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveDefaultConfig();
        }

        // Recarrega a configuração
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Carregar lobby location
        if (config.isSet("lobby.world")) {
            String worldName = config.getString("lobby.world");
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                double x = config.getDouble("lobby.x");
                double y = config.getDouble("lobby.y");
                double z = config.getDouble("lobby.z");
                float yaw = (float) config.getDouble("lobby.yaw");
                float pitch = (float) config.getDouble("lobby.pitch");

                lobbyLocation = new Location(world, x, y, z, yaw, pitch);
                plugin.getLogger().info("Lobby location loaded: " + worldName + ", " + x + ", " + y + ", " + z);
            } else {
                plugin.getLogger().warning("Could not find world: " + worldName + " for lobby location!");
                lobbyLocation = null;
            }
        } else {
            plugin.getLogger().info("No lobby location set in config!");
            lobbyLocation = null;
        }

        // Carregar configurações do jogo
        startCountdown = config.getInt("game.start-countdown", 30);
        gameTime = config.getInt("game.max-time", 600); // 10 minutos por padrão
        maxPlayers = config.getInt("game.max-players", 16); // 16 jogadores por padrão

        // Carregar outras configurações
        loadArenaConfig();
    }

    /**
     * Carrega a configuração das arenas
     */
    private void loadArenaConfig() {
        File arenaFile = new File(plugin.getDataFolder(), "arenas.yml");

        if (!arenaFile.exists()) {
            try {
                arenaFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create arenas.yml file!");
                e.printStackTrace();
            }
        }

        configFile = arenaFile;
    }

    /**
     * Obtém a configuração das arenas
     *
     * @return Configuração das arenas
     */
    public FileConfiguration getArenaConfig() {
        if (configFile == null) {
            loadArenaConfig();
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Salva a configuração das arenas
     */
    public void saveArenaConfig() {
        if (configFile == null) {
            loadArenaConfig();
        }

        try {
            getArenaConfig().save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save arenas.yml file!");
            e.printStackTrace();
        }
    }

    /**
     * Define a localização do lobby
     *
     * @param location Localização do lobby
     */
    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;

        // Salvar no arquivo de configuração
        config.set("lobby.world", location.getWorld().getName());
        config.set("lobby.x", location.getX());
        config.set("lobby.y", location.getY());
        config.set("lobby.z", location.getZ());
        config.set("lobby.yaw", location.getYaw());
        config.set("lobby.pitch", location.getPitch());

        plugin.saveConfig();
        plugin.getLogger().info("Lobby location set to: " +
                location.getWorld().getName() + ", " +
                location.getX() + ", " +
                location.getY() + ", " +
                location.getZ());
    }

    /**
     * Obtém a localização do lobby
     *
     * @return Localização do lobby
     */
    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    /**
     * Obtém o tempo de contagem regressiva para iniciar o jogo
     *
     * @return Tempo em segundos
     */
    public int getStartCountdown() {
        return startCountdown;
    }

    /**
     * Obtém o tempo máximo de jogo
     *
     * @return Tempo em segundos
     */
    public int getGameTime() {
        return gameTime;
    }

    /**
     * Verifica se o jogador deve ser teleportado para o lobby ao entrar no servidor
     *
     * @return true se deve teleportar, false caso contrário
     */
    public boolean shouldTeleportToLobbyOnJoin() {
        return config.getBoolean("settings.teleport-to-lobby-on-join", true);
    }

    /**
     * Obtém o número máximo de jogadores permitido por arena
     *
     * @return Número máximo de jogadores
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Define o número máximo de jogadores permitido por arena
     *
     * @param maxPlayers Número máximo de jogadores
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        config.set("game.max-players", maxPlayers);
        plugin.saveConfig();
    }
}