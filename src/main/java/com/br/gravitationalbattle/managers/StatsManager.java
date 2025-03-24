package com.br.gravitationalbattle.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;

public class StatsManager {
    private final GravitationalBattle plugin;
    private final File statsFile;
    private FileConfiguration statsConfig;

    // Cache das estatísticas em memória para acesso rápido
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();
    private final Map<UUID, Integer> wins = new HashMap<>();
    private final Map<UUID, Integer> gamesPlayed = new HashMap<>();

    public StatsManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        loadStats();
    }

    /**
     * Classe para armazenar estatísticas de jogadores
     */
    public class PlayerStats {
        private int kills;
        private int deaths;
        private int wins;
        private int gamesPlayed;
        private double kdr;

        public PlayerStats(int kills, int deaths, int wins, int gamesPlayed) {
            this.kills = kills;
            this.deaths = deaths;
            this.wins = wins;
            this.gamesPlayed = gamesPlayed;
            this.kdr = deaths > 0 ? (double) kills / deaths : kills;
        }

        public int getKills() {
            return kills;
        }

        public int getDeaths() {
            return deaths;
        }

        public int getWins() {
            return wins;
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }

        public double getKDR() {
            return kdr;
        }
    }

    /**
     * Obtém todas as estatísticas de um jogador
     * @param uuid UUID do jogador
     * @return Objeto PlayerStats com as estatísticas
     */
    public PlayerStats getPlayerStats(UUID uuid) {
        int playerKills = getKills(uuid);
        int playerDeaths = getDeaths(uuid);
        int playerWins = getWins(uuid);
        int playerGamesPlayed = getGamesPlayed(uuid);

        return new PlayerStats(playerKills, playerDeaths, playerWins, playerGamesPlayed);
    }

    /**
     * Obtém todas as estatísticas de um jogador
     * @param player Jogador
     * @return Objeto PlayerStats com as estatísticas
     */
    public PlayerStats getPlayerStats(Player player) {
        return getPlayerStats(player.getUniqueId());
    }

    /**
     * Carrega as estatísticas do arquivo
     */
    private void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo stats.yml!");
                e.printStackTrace();
                return;
            }
        }

        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        // Carrega os dados em cache
        for (String uuidStr : statsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);

                kills.put(uuid, statsConfig.getInt(uuidStr + ".kills", 0));
                deaths.put(uuid, statsConfig.getInt(uuidStr + ".deaths", 0));
                wins.put(uuid, statsConfig.getInt(uuidStr + ".wins", 0));
                gamesPlayed.put(uuid, statsConfig.getInt(uuidStr + ".gamesPlayed", 0));
            } catch (IllegalArgumentException e) {
                // UUID inválido, ignora
            }
        }
    }

    /**
     * Salva as estatísticas no arquivo
     */
    public void saveStats() {
        if (statsConfig == null) return;

        for (Map.Entry<UUID, Integer> entry : kills.entrySet()) {
            statsConfig.set(entry.getKey().toString() + ".kills", entry.getValue());
        }

        for (Map.Entry<UUID, Integer> entry : deaths.entrySet()) {
            statsConfig.set(entry.getKey().toString() + ".deaths", entry.getValue());
        }

        for (Map.Entry<UUID, Integer> entry : wins.entrySet()) {
            statsConfig.set(entry.getKey().toString() + ".wins", entry.getValue());
        }

        for (Map.Entry<UUID, Integer> entry : gamesPlayed.entrySet()) {
            statsConfig.set(entry.getKey().toString() + ".gamesPlayed", entry.getValue());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar o arquivo stats.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Obtém o número de mortes de um jogador
     * @param uuid UUID do jogador
     * @return Número de mortes
     */
    public int getDeaths(UUID uuid) {
        return deaths.getOrDefault(uuid, 0);
    }

    /**
     * Obtém o número de mortes de um jogador
     * @param player Jogador
     * @return Número de mortes
     */
    public int getDeaths(Player player) {
        return getDeaths(player.getUniqueId());
    }

    /**
     * Obtém o número de abates de um jogador
     * @param uuid UUID do jogador
     * @return Número de abates
     */
    public int getKills(UUID uuid) {
        return kills.getOrDefault(uuid, 0);
    }

    /**
     * Obtém o número de abates de um jogador
     * @param player Jogador
     * @return Número de abates
     */
    public int getKills(Player player) {
        return getKills(player.getUniqueId());
    }

    /**
     * Obtém o número de vitórias de um jogador
     * @param uuid UUID do jogador
     * @return Número de vitórias
     */
    public int getWins(UUID uuid) {
        return wins.getOrDefault(uuid, 0);
    }

    /**
     * Obtém o número de vitórias de um jogador
     * @param player Jogador
     * @return Número de vitórias
     */
    public int getWins(Player player) {
        return getWins(player.getUniqueId());
    }

    /**
     * Obtém o número de jogos jogados por um jogador
     * @param uuid UUID do jogador
     * @return Número de jogos jogados
     */
    public int getGamesPlayed(UUID uuid) {
        return gamesPlayed.getOrDefault(uuid, 0);
    }

    /**
     * Obtém o número de jogos jogados por um jogador
     * @param player Jogador
     * @return Número de jogos jogados
     */
    public int getGamesPlayed(Player player) {
        return getGamesPlayed(player.getUniqueId());
    }

    /**
     * Adiciona uma morte ao jogador
     * @param uuid UUID do jogador
     */
    public void addDeath(UUID uuid) {
        int current = getDeaths(uuid);
        deaths.put(uuid, current + 1);
    }

    /**
     * Adiciona um abate ao jogador
     * @param uuid UUID do jogador
     */
    public void addKill(UUID uuid) {
        int current = getKills(uuid);
        kills.put(uuid, current + 1);
    }

    /**
     * Adiciona uma vitória ao jogador
     * @param uuid UUID do jogador
     */
    public void addWin(UUID uuid) {
        int current = getWins(uuid);
        wins.put(uuid, current + 1);
    }

    /**
     * Adiciona um jogo jogado ao jogador
     * @param uuid UUID do jogador
     */
    public void addGamePlayed(UUID uuid) {
        int current = getGamesPlayed(uuid);
        gamesPlayed.put(uuid, current + 1);
    }

    /**
     * Adiciona uma morte ao jogador
     * @param player Jogador
     */
    public void addDeath(Player player) {
        addDeath(player.getUniqueId());
    }

    /**
     * Adiciona um abate ao jogador
     * @param player Jogador
     */
    public void addKill(Player player) {
        addKill(player.getUniqueId());
    }

    /**
     * Adiciona uma vitória ao jogador
     * @param player Jogador
     */
    public void addWin(Player player) {
        addWin(player.getUniqueId());
    }

    /**
     * Adiciona um jogo jogado ao jogador
     * @param player Jogador
     */
    public void addGamePlayed(Player player) {
        addGamePlayed(player.getUniqueId());
    }

    /**
     * Obtém a taxa de KDR (Kill/Death Ratio) de um jogador
     * @param uuid UUID do jogador
     * @return Taxa KDR
     */
    public double getKDR(UUID uuid) {
        int playerKills = getKills(uuid);
        int playerDeaths = getDeaths(uuid);

        if (playerDeaths == 0) {
            return playerKills;
        }

        return (double) playerKills / playerDeaths;
    }

    /**
     * Obtém a taxa de KDR (Kill/Death Ratio) de um jogador
     * @param player Jogador
     * @return Taxa KDR
     */
    public double getKDR(Player player) {
        return getKDR(player.getUniqueId());
    }
}