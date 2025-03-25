package com.br.gravitationalbattle.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final GravitationalBattle plugin;
    private File statsFile;
    private FileConfiguration statsConfig;

    private Map<UUID, Integer> playerKills;
    private Map<UUID, Integer> playerDeaths;
    private Map<UUID, Integer> playerWins;
    private Map<UUID, Integer> playerGamesPlayed;

    public StatsManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.playerKills = new HashMap<>();
        this.playerDeaths = new HashMap<>();
        this.playerWins = new HashMap<>();
        this.playerGamesPlayed = new HashMap<>();

        loadStats();
    }

    /**
     * Adds kills to a player's statistics
     *
     * @param player The player
     * @param kills Number of kills to add
     */
    public void addKills(Player player, int kills) {
        if (player == null || kills <= 0) return;

        UUID uuid = player.getUniqueId();
        int currentKills = playerKills.getOrDefault(uuid, 0);
        playerKills.put(uuid, currentKills + kills);
    }

    /**
     * Adds a kill to a player's statistics
     *
     * @param player The player
     */
    public void addKill(Player player) {
        addKills(player, 1);
    }

    /**
     * Adds a death to a player's statistics
     *
     * @param player The player
     */
    public void addDeath(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        int currentDeaths = playerDeaths.getOrDefault(uuid, 0);
        playerDeaths.put(uuid, currentDeaths + 1);
    }

    /**
     * Adds a win to a player's statistics
     *
     * @param player The player
     */
    public void addWin(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        int currentWins = playerWins.getOrDefault(uuid, 0);
        playerWins.put(uuid, currentWins + 1);
    }

    /**
     * Adds a game played to a player's statistics
     *
     * @param player The player
     */
    public void addGamePlayed(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        int currentGamesPlayed = playerGamesPlayed.getOrDefault(uuid, 0);
        playerGamesPlayed.put(uuid, currentGamesPlayed + 1);
    }

    /**
     * Gets the number of kills for a player
     *
     * @param player The player
     * @return Number of kills
     */
    public int getPlayerKills(Player player) {
        if (player == null) return 0;

        UUID uuid = player.getUniqueId();
        return playerKills.getOrDefault(uuid, 0);
    }

    /**
     * Gets the number of deaths for a player
     *
     * @param player The player
     * @return Number of deaths
     */
    public int getPlayerDeaths(Player player) {
        if (player == null) return 0;

        UUID uuid = player.getUniqueId();
        return playerDeaths.getOrDefault(uuid, 0);
    }

    /**
     * Gets the number of wins for a player
     *
     * @param player The player
     * @return Number of wins
     */
    public int getPlayerWins(Player player) {
        if (player == null) return 0;

        UUID uuid = player.getUniqueId();
        return playerWins.getOrDefault(uuid, 0);
    }

    /**
     * Gets the number of games played by a player
     *
     * @param player The player
     * @return Number of games played
     */
    public int getPlayerGamesPlayed(Player player) {
        if (player == null) return 0;

        UUID uuid = player.getUniqueId();
        return playerGamesPlayed.getOrDefault(uuid, 0);
    }

    /**
     * Calculates K/D ratio for a player
     *
     * @param player The player
     * @return K/D ratio, or kills if deaths is 0
     */
    public double getPlayerKDRatio(Player player) {
        if (player == null) return 0.0;

        int kills = getPlayerKills(player);
        int deaths = getPlayerDeaths(player);

        if (deaths == 0) {
            return kills; // Avoid division by zero
        }

        return (double) kills / deaths;
    }

    /**
     * Loads player stats from file
     */
    public void loadStats() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");

        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create stats.yml file!");
                e.printStackTrace();
                return;
            }
        }

        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        // Load player stats
        if (statsConfig.contains("players")) {
            for (String uuidString : statsConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    playerKills.put(uuid, statsConfig.getInt("players." + uuidString + ".kills", 0));
                    playerDeaths.put(uuid, statsConfig.getInt("players." + uuidString + ".deaths", 0));
                    playerWins.put(uuid, statsConfig.getInt("players." + uuidString + ".wins", 0));
                    playerGamesPlayed.put(uuid, statsConfig.getInt("players." + uuidString + ".gamesPlayed", 0));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in stats.yml: " + uuidString);
                }
            }
        }
    }

    /**
     * Saves player stats to file
     */
    public void saveStats() {
        if (statsConfig == null || statsFile == null) {
            loadStats();
        }

        // Clear current stats in config
        statsConfig.set("players", null);

        // Save kills
        for (Map.Entry<UUID, Integer> entry : playerKills.entrySet()) {
            statsConfig.set("players." + entry.getKey().toString() + ".kills", entry.getValue());
        }

        // Save deaths
        for (Map.Entry<UUID, Integer> entry : playerDeaths.entrySet()) {
            statsConfig.set("players." + entry.getKey().toString() + ".deaths", entry.getValue());
        }

        // Save wins
        for (Map.Entry<UUID, Integer> entry : playerWins.entrySet()) {
            statsConfig.set("players." + entry.getKey().toString() + ".wins", entry.getValue());
        }

        // Save games played
        for (Map.Entry<UUID, Integer> entry : playerGamesPlayed.entrySet()) {
            statsConfig.set("players." + entry.getKey().toString() + ".gamesPlayed", entry.getValue());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats to stats.yml!");
            e.printStackTrace();
        }
    }
}