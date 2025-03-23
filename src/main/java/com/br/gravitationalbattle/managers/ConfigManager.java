package com.br.gravitationalbattle.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.LocationUtil;

public class ConfigManager {

    private final GravitationalBattle plugin;
    private Location lobbyLocation;
    private Map<String, String> messages;
    private Map<String, Integer> rewards;
    private int minPlayers;
    private int maxPlayers;
    private int gameStartDelay;
    private int gameDuration;

    public ConfigManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.rewards = new HashMap<>();
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Load lobby location
        String lobbyLocationStr = config.getString("lobby-location");
        if (lobbyLocationStr != null && !lobbyLocationStr.isEmpty()) {
            lobbyLocation = LocationUtil.stringToLocation(lobbyLocationStr);
        }

        // Load game settings
        minPlayers = config.getInt("game-settings.min-players", 2);
        maxPlayers = config.getInt("game-settings.max-players", 8);
        gameStartDelay = config.getInt("game-settings.start-countdown", 30);
        gameDuration = config.getInt("game-settings.duration", 300);

        // Load messages
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                messages.put(key, messagesSection.getString(key, ""));
            }
        }

        // Load rewards
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            for (String key : rewardsSection.getKeys(false)) {
                rewards.put(key, rewardsSection.getInt(key, 0));
            }
        }

        // Create default values if not exist
        if (messages.isEmpty()) {
            createDefaultMessages();
        }

        if (rewards.isEmpty()) {
            createDefaultRewards();
        }

        saveConfig();
    }

    public void saveConfig() {
        FileConfiguration config = plugin.getConfig();

        // Save lobby location
        if (lobbyLocation != null) {
            config.set("lobby-location", LocationUtil.locationToString(lobbyLocation));
        }

        // Save game settings
        config.set("game-settings.min-players", minPlayers);
        config.set("game-settings.max-players", maxPlayers);
        config.set("game-settings.start-countdown", gameStartDelay);
        config.set("game-settings.duration", gameDuration);

        // Save messages
        ConfigurationSection messagesSection = config.createSection("messages");
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            messagesSection.set(entry.getKey(), entry.getValue());
        }

        // Save rewards
        ConfigurationSection rewardsSection = config.createSection("rewards");
        for (Map.Entry<String, Integer> entry : rewards.entrySet()) {
            rewardsSection.set(entry.getKey(), entry.getValue());
        }

        plugin.saveConfig();
    }

    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;
        saveConfig();
    }

    public Location getLobbyLocation() {
        return lobbyLocation != null ? lobbyLocation.clone() : null;
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cMensagem não encontrada: " + key);
    }

    public int getReward(String key) {
        return rewards.getOrDefault(key, 0);
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getGameStartDelay() {
        return gameStartDelay;
    }

    public int getGameDuration() {
        return gameDuration;
    }

    private void createDefaultMessages() {
        messages.put("prefix", "&8[&6GB&8] &r");
        messages.put("player-joined", "%prefix% &e%player% &ajuntou-se ao jogo! &7(%current%/%max%)");
        messages.put("player-left", "%prefix% &e%player% &csaiu do jogo. &7(%current%/%max%)");
        messages.put("game-starting", "%prefix% &aO jogo começará em &e%time% &asegundos!");
        messages.put("countdown-cancelled", "%prefix% &cContagem regressiva cancelada. Aguardando mais jogadores...");
        messages.put("game-started", "%prefix% &a&lO jogo começou! Boa sorte!");
        messages.put("player-killed", "%prefix% &e%player% &7foi eliminado por &e%killer%&7!");
        messages.put("player-died", "%prefix% &e%player% &7foi eliminado!");
        messages.put("game-ended", "%prefix% &a&lFim de jogo! &e%winner% &avenceu!");
        messages.put("game-ended-no-winner", "%prefix% &a&lFim de jogo! &cNão houve vencedor!");
        messages.put("spectate-join", "%prefix% &7Você agora está assistindo à partida. Use /leave para sair.");
    }

    private void createDefaultRewards() {
        rewards.put("win", 100);
        rewards.put("kill", 25);
        rewards.put("participation", 10);
    }
}