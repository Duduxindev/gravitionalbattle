package com.br.gravitationalbattle.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameMode;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Gerencia arenas e jogadores
 */
public class ArenaManager {

    private final GravitationalBattle plugin;
    private final Map<String, Arena> arenas;
    private final Map<String, Game> activeGames;
    private final Map<UUID, String> playerArena;
    private File arenaFile;
    private FileConfiguration arenaConfig;

    public ArenaManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.activeGames = new HashMap<>();
        this.playerArena = new HashMap<>();

        // Configurar arquivo de arenas
        setupArenaFile();

        // Carregar arenas existentes
        loadArenas();
    }

    /**
     * Configura o arquivo de arenas
     */
    private void setupArenaFile() {
        arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenaFile.exists()) {
            try {
                arenaFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo arenas.yml!");
                e.printStackTrace();
            }
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
    }

    /**
     * Carrega as arenas do arquivo de configuração
     */
    public void loadArenas() {
        arenas.clear();

        ConfigurationSection arenasSection = arenaConfig.getConfigurationSection("arenas");
        if (arenasSection == null) {
            return;
        }

        for (String key : arenasSection.getKeys(false)) {
            try {
                ConfigurationSection arenaSection = arenasSection.getConfigurationSection(key);

                // Carregar dados básicos
                String name = key;
                String displayName = arenaSection.getString("display-name", name);
                String worldId = arenaSection.getString("world");
                int minPlayers = arenaSection.getInt("min-players", 2);
                int maxPlayers = arenaSection.getInt("max-players", 16);

                // Converter UUID do mundo
                UUID worldUUID = null;
                try {
                    worldUUID = UUID.fromString(worldId);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("UUID de mundo inválido para arena " + name + ": " + worldId);
                    continue;
                }

                // Verificar se o mundo existe
                World world = Bukkit.getWorld(worldUUID);
                if (world == null) {
                    plugin.getLogger().warning("Mundo não encontrado para arena " + name + ": " + worldUUID);
                    continue;
                }

                // Criar arena
                Arena arena = new Arena(name, worldUUID);
                arena.setDisplayName(displayName);
                arena.setMinPlayers(minPlayers);
                arena.setMaxPlayers(maxPlayers);

                // Carregar modo de jogo padrão
                String gameModeName = arenaSection.getString("game-mode", "SOLO");
                try {
                    GameMode gameMode = GameMode.valueOf(gameModeName);
                    arena.setDefaultGameMode(gameMode);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Modo de jogo inválido para arena " + name + ": " + gameModeName);
                    arena.setDefaultGameMode(GameMode.SOLO);
                }

                // Carregar localização do lobby
                if (arenaSection.contains("lobby")) {
                    ConfigurationSection lobbySection = arenaSection.getConfigurationSection("lobby");
                    World lobbyWorld = Bukkit.getWorld(UUID.fromString(lobbySection.getString("world")));
                    if (lobbyWorld != null) {
                        Location lobbyLoc = new Location(
                                lobbyWorld,
                                lobbySection.getDouble("x"),
                                lobbySection.getDouble("y"),
                                lobbySection.getDouble("z"),
                                (float) lobbySection.getDouble("yaw"),
                                (float) lobbySection.getDouble("pitch")
                        );
                        arena.setLobbyLocation(lobbyLoc);
                    }
                }

                // Carregar pontos de spawn
                if (arenaSection.contains("spawn-points")) {
                    ConfigurationSection spawnSection = arenaSection.getConfigurationSection("spawn-points");
                    for (String spawnId : spawnSection.getKeys(false)) {
                        ConfigurationSection pointSection = spawnSection.getConfigurationSection(spawnId);
                        World spawnWorld = Bukkit.getWorld(UUID.fromString(pointSection.getString("world")));
                        if (spawnWorld != null) {
                            Location spawnLoc = new Location(
                                    spawnWorld,
                                    pointSection.getDouble("x"),
                                    pointSection.getDouble("y"),
                                    pointSection.getDouble("z"),
                                    (float) pointSection.getDouble("yaw"),
                                    (float) pointSection.getDouble("pitch")
                            );
                            arena.addSpawnPoint(spawnLoc);
                        }
                    }
                }

                arenas.put(name.toLowerCase(), arena);

            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao carregar arena " + key + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Carregadas " + arenas.size() + " arenas.");
    }

    /**
     * Salva as arenas no arquivo de configuração
     */
    public void saveArenas() {
        // Limpar configuração atual
        arenaConfig.set("arenas", null);

        // Criar seção para arenas
        ConfigurationSection arenasSection = arenaConfig.createSection("arenas");

        // Salvar cada arena
        for (Arena arena : arenas.values()) {
            ConfigurationSection arenaSection = arenasSection.createSection(arena.getName());

            // Salvar dados básicos
            arenaSection.set("display-name", arena.getDisplayName());
            arenaSection.set("world", arena.getWorldUUID().toString());
            arenaSection.set("min-players", arena.getMinPlayers());
            arenaSection.set("max-players", arena.getMaxPlayers());
            arenaSection.set("game-mode", arena.getDefaultGameMode().name());

            // Salvar localização do lobby
            Location lobbyLoc = arena.getLobbyLocation();
            if (lobbyLoc != null && lobbyLoc.getWorld() != null) {
                ConfigurationSection lobbySection = arenaSection.createSection("lobby");
                lobbySection.set("world", lobbyLoc.getWorld().getUID().toString());
                lobbySection.set("x", lobbyLoc.getX());
                lobbySection.set("y", lobbyLoc.getY());
                lobbySection.set("z", lobbyLoc.getZ());
                lobbySection.set("yaw", lobbyLoc.getYaw());
                lobbySection.set("pitch", lobbyLoc.getPitch());
            }

            // Salvar pontos de spawn
            List<Location> spawnPoints = arena.getSpawnPoints();
            if (spawnPoints != null && !spawnPoints.isEmpty()) {
                ConfigurationSection spawnSection = arenaSection.createSection("spawn-points");
                for (int i = 0; i < spawnPoints.size(); i++) {
                    Location spawnLoc = spawnPoints.get(i);
                    if (spawnLoc != null && spawnLoc.getWorld() != null) {
                        ConfigurationSection pointSection = spawnSection.createSection("spawn-" + i);
                        pointSection.set("world", spawnLoc.getWorld().getUID().toString());
                        pointSection.set("x", spawnLoc.getX());
                        pointSection.set("y", spawnLoc.getY());
                        pointSection.set("z", spawnLoc.getZ());
                        pointSection.set("yaw", spawnLoc.getYaw());
                        pointSection.set("pitch", spawnLoc.getPitch());
                    }
                }
            }
        }

        // Salvar arquivo
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar arenas.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Verifica se uma arena com o nome especificado existe
     *
     * @param name Nome da arena
     * @return true se a arena existir
     */
    public boolean arenaExists(String name) {
        if (name == null) {
            return false;
        }
        return arenas.containsKey(name.toLowerCase());
    }

    /**
     * Cria uma nova arena
     *
     * @param name Nome da arena
     * @param worldUUID UUID do mundo da arena
     * @return A arena criada ou null se houver falha
     */
    public Arena createArena(String name, UUID worldUUID) {
        if (name == null || worldUUID == null) {
            return null;
        }

        // Verificar se já existe uma arena com esse nome
        if (arenaExists(name)) {
            return null;
        }

        // Verificar se o mundo existe
        World world = Bukkit.getWorld(worldUUID);
        if (world == null) {
            return null;
        }

        // Criar arena
        Arena arena = new Arena(name, worldUUID);
        arena.setDisplayName(name); // Nome de exibição padrão é igual ao nome

        // Adicionar arena à lista
        arenas.put(name.toLowerCase(), arena);

        // Salvar arenas
        saveArenas();

        return arena;
    }

    /**
     * Obtém uma arena pelo nome
     *
     * @param name Nome da arena
     * @return A arena ou null se não existir
     */
    public Arena getArena(String name) {
        if (name == null) {
            return null;
        }
        return arenas.get(name.toLowerCase());
    }

    /**
     * Obtém uma arena pelo UUID do mundo
     *
     * @param worldUUID UUID do mundo
     * @return A arena que usa esse mundo ou null se não existir
     */
    public Arena getArenaByWorld(UUID worldUUID) {
        if (worldUUID == null) {
            return null;
        }

        for (Arena arena : arenas.values()) {
            if (arena.getWorldUUID().equals(worldUUID)) {
                return arena;
            }
        }

        return null;
    }

    /**
     * Exclui uma arena
     *
     * @param name Nome da arena
     * @return true se a arena foi excluída com sucesso
     */
    public boolean deleteArena(String name) {
        if (name == null) {
            return false;
        }

        // Verificar se a arena está em uso
        if (isArenaInUse(name)) {
            return false;
        }

        // Remover arena da lista
        Arena removed = arenas.remove(name.toLowerCase());

        // Salvar arenas
        if (removed != null) {
            saveArenas();
            return true;
        }

        return false;
    }

    /**
     * Obtém todas as arenas cadastradas
     *
     * @return Lista de arenas
     */
    public List<Arena> getAllArenas() {
        return new ArrayList<>(arenas.values());
    }

    /**
     * Verifica se uma arena está em uso
     *
     * @param arenaName Nome da arena
     * @return true se há um jogo ativo nessa arena
     */
    public boolean isArenaInUse(String arenaName) {
        if (arenaName == null) {
            return false;
        }
        return activeGames.containsKey(arenaName.toLowerCase());
    }

    /**
     * Cria um novo jogo em uma arena
     *
     * @param arenaName Nome da arena
     * @return O jogo criado ou null se falhar
     */
    public Game createGame(String arenaName) {
        Arena arena = getArena(arenaName);
        if (arena == null) {
            return null;
        }

        // Verificar se já existe um jogo nessa arena
        if (isArenaInUse(arenaName)) {
            return null;
        }

        // Criar novo jogo
        Game game = new Game(plugin, arena);
        game.setGameMode(arena.getDefaultGameMode());

        // Registrar jogo
        activeGames.put(arenaName.toLowerCase(), game);

        return game;
    }

    /**
     * Obtém um jogo ativo por nome da arena
     *
     * @param arenaName Nome da arena
     * @return O jogo ou null se não existir
     */
    public Game getGame(String arenaName) {
        if (arenaName == null) {
            return null;
        }
        return activeGames.get(arenaName.toLowerCase());
    }

    /**
     * Obtém um jogo pelo jogador
     *
     * @param player O jogador
     * @return O jogo em que o jogador está ou null
     */
    public Game getPlayerGame(Player player) {
        if (player == null) {
            return null;
        }

        String arenaName = playerArena.get(player.getUniqueId());
        if (arenaName == null) {
            return null;
        }

        return getGame(arenaName);
    }

    /**
     * Registra um jogador em um jogo
     *
     * @param player O jogador
     * @param game O jogo
     */
    public void registerPlayer(Player player, Game game) {
        if (player == null || game == null) {
            return;
        }

        playerArena.put(player.getUniqueId(), game.getArena().getName().toLowerCase());
    }

    /**
     * Remove o registro de um jogador de qualquer jogo
     *
     * @param player O jogador
     */
    public void unregisterPlayer(Player player) {
        if (player == null) {
            return;
        }

        playerArena.remove(player.getUniqueId());
    }

    /**
     * Remove o registro de um jogo
     *
     * @param game O jogo
     */
    public void unregisterGame(Game game) {
        if (game == null) {
            return;
        }

        activeGames.remove(game.getArena().getName().toLowerCase());
    }

    /**
     * Obtém todos os jogos ativos
     *
     * @return Lista de jogos ativos
     */
    public List<Game> getAllGames() {
        return new ArrayList<>(activeGames.values());
    }

    /**
     * Obtém uma arena aleatória disponível
     *
     * @return Uma arena disponível ou null se não houver
     */
    public Arena getRandomAvailableArena() {
        List<Arena> availableArenas = new ArrayList<>();

        for (Arena arena : arenas.values()) {
            if (!isArenaInUse(arena.getName())) {
                availableArenas.add(arena);
            }
        }

        if (availableArenas.isEmpty()) {
            return null;
        }

        // Escolher uma arena aleatória
        int index = (int)(Math.random() * availableArenas.size());
        return availableArenas.get(index);
    }

    /**
     * Tenta adicionar um jogador a um jogo
     *
     * @param player O jogador
     * @param arenaName Nome da arena (ou "random" para aleatório)
     * @return true se o jogador foi adicionado com sucesso
     */
    public boolean joinGame(Player player, String arenaName) {
        if (player == null) {
            return false;
        }

        // Verificar se o jogador já está em um jogo
        if (playerArena.containsKey(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "&cVocê já está em um jogo! Use /leave para sair.");
            return false;
        }

        Game game = null;

        // Tratar entrada em arena aleatória
        if ("random".equalsIgnoreCase(arenaName)) {
            Arena randomArena = getRandomAvailableArena();
            if (randomArena == null) {
                MessageUtil.sendMessage(player, "&cNão há arenas disponíveis no momento.");
                return false;
            }

            arenaName = randomArena.getName();
        }

        // Verificar se a arena existe
        if (!arenaExists(arenaName)) {
            MessageUtil.sendMessage(player, "&cA arena &e" + arenaName + " &cnão existe!");
            return false;
        }

        // Verificar se já existe um jogo na arena
        game = getGame(arenaName);

        // Se não existir, criar um novo
        if (game == null) {
            game = createGame(arenaName);
            if (game == null) {
                MessageUtil.sendMessage(player, "&cNão foi possível criar um jogo na arena &e" + arenaName + "&c!");
                return false;
            }
        }

        // Adicionar jogador ao jogo
        if (game.addPlayer(player)) {
            registerPlayer(player, game);
            return true;
        }

        return false;
    }

    /**
     * Remove um jogador de seu jogo atual
     *
     * @param player O jogador
     * @return true se o jogador foi removido com sucesso
     */
    public boolean leaveGame(Player player) {
        if (player == null) {
            return false;
        }

        Game game = getPlayerGame(player);
        if (game == null) {
            MessageUtil.sendMessage(player, "&cVocê não está em nenhum jogo!");
            return false;
        }

        if (game.removePlayer(player)) {
            unregisterPlayer(player);
            return true;
        }

        return false;
    }
}