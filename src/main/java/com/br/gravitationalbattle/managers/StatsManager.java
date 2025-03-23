package com.br.gravitationalbattle.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.br.gravitationalbattle.GravitationalBattle;

public class StatsManager {

    private final GravitationalBattle plugin;
    private final File statsFile;
    private final Map<UUID, PlayerStats> playerStats;

    public StatsManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        this.playerStats = new HashMap<>();

        loadStats();
    }

    public void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo de estatísticas!");
                e.printStackTrace();
            }
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(statsFile);
        ConfigurationSection statsSection = config.getConfigurationSection("stats");

        if (statsSection == null) {
            return;
        }

        for (String uuidStr : statsSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection playerSection = statsSection.getConfigurationSection(uuidStr);

                if (playerSection != null) {
                    int kills = playerSection.getInt("kills", 0);
                    int deaths = playerSection.getInt("deaths", 0);
                    int wins = playerSection.getInt("wins", 0);
                    int gamesPlayed = playerSection.getInt("gamesPlayed", 0);

                    PlayerStats stats = new PlayerStats(uuid);
                    stats.setKills(kills);
                    stats.setDeaths(deaths);
                    stats.setWins(wins);
                    stats.setGamesPlayed(gamesPlayed);

                    playerStats.put(uuid, stats);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID inválido encontrado no arquivo de estatísticas: " + uuidStr);
            }
        }

        plugin.getLogger().info("Estatísticas carregadas para " + playerStats.size() + " jogadores.");
    }

    public void saveStats() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(statsFile);

        ConfigurationSection statsSection = config.createSection("stats");

        for (Map.Entry<UUID, PlayerStats> entry : playerStats.entrySet()) {
            String uuidStr = entry.getKey().toString();
            PlayerStats stats = entry.getValue();

            ConfigurationSection playerSection = statsSection.createSection(uuidStr);
            playerSection.set("kills", stats.getKills());
            playerSection.set("deaths", stats.getDeaths());
            playerSection.set("wins", stats.getWins());
            playerSection.set("gamesPlayed", stats.getGamesPlayed());
        }

        try {
            config.save(statsFile);
            plugin.getLogger().info("Estatísticas salvas para " + playerStats.size() + " jogadores.");
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar o arquivo de estatísticas!");
            e.printStackTrace();
        }
    }

    public PlayerStats getPlayerStats(UUID playerUUID) {
        if (!playerStats.containsKey(playerUUID)) {
            playerStats.put(playerUUID, new PlayerStats(playerUUID));
        }

        return playerStats.get(playerUUID);
    }

    public void incrementKills(UUID playerUUID) {
        getPlayerStats(playerUUID).incrementKills();
    }

    public void incrementDeaths(UUID playerUUID) {
        getPlayerStats(playerUUID).incrementDeaths();
    }

    public void incrementWins(UUID playerUUID) {
        getPlayerStats(playerUUID).incrementWins();
    }

    public void incrementGamesPlayed(UUID playerUUID) {
        getPlayerStats(playerUUID).incrementGamesPlayed();
    }

    public int getPlayerKills(UUID playerUUID) {
        return getPlayerStats(playerUUID).getKills();
    }

    public int getPlayerDeaths(UUID playerUUID) {
        return getPlayerStats(playerUUID).getDeaths();
    }

    public int getPlayerWins(UUID playerUUID) {
        return getPlayerStats(playerUUID).getWins();
    }

    public int getPlayerGamesPlayed(UUID playerUUID) {
        return getPlayerStats(playerUUID).getGamesPlayed();
    }

    // Convenience overloaded methods for Player objects
    public int getPlayerKills(org.bukkit.entity.Player player) {
        return getPlayerKills(player.getUniqueId());
    }

    public int getPlayerDeaths(org.bukkit.entity.Player player) {
        return getPlayerDeaths(player.getUniqueId());
    }

    public int getPlayerWins(org.bukkit.entity.Player player) {
        return getPlayerWins(player.getUniqueId());
    }

    public int getPlayerGamesPlayed(org.bukkit.entity.Player player) {
        return getPlayerGamesPlayed(player.getUniqueId());
    }

    public class PlayerStats {
        private final UUID playerUUID;
        private int kills;
        private int deaths;
        private int wins;
        private int gamesPlayed;

        public PlayerStats(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.kills = 0;
            this.deaths = 0;
            this.wins = 0;
            this.gamesPlayed = 0;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public int getKills() {
            return kills;
        }

        public void setKills(int kills) {
            this.kills = kills;
        }

        public void incrementKills() {
            this.kills++;
        }

        public int getDeaths() {
            return deaths;
        }

        public void setDeaths(int deaths) {
            this.deaths = deaths;
        }

        public void incrementDeaths() {
            this.deaths++;
        }

        public int getWins() {
            return wins;
        }

        public void setWins(int wins) {
            this.wins = wins;
        }

        public void incrementWins() {
            this.wins++;
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }

        public void setGamesPlayed(int gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }

        public void incrementGamesPlayed() {
            this.gamesPlayed++;
        }
    }
}