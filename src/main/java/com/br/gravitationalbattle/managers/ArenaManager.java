package com.br.gravitationalbattle.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

public class ArenaManager {

    private final GravitationalBattle plugin;
    private final Map<String, Arena> arenas;
    private final Map<String, Game> activeGames;

    public ArenaManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.activeGames = new HashMap<>();
    }

    public void loadArenas() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection arenasSection = config.getConfigurationSection("arenas");

        if (arenasSection == null) {
            arenasSection = config.createSection("arenas");
        }

        int count = 0;
        for (String arenaName : arenasSection.getKeys(false)) {
            ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaName);
            Arena arena = Arena.loadFromConfig(arenaName, arenaSection);

            if (arena != null) {
                arenas.put(arenaName.toLowerCase(), arena);
                count++;
            }
        }

        plugin.getLogger().info("Carregadas " + count + " arenas.");
    }

    public void saveArenas() {
        FileConfiguration config = plugin.getConfig();

        // Clear existing arenas first
        config.set("arenas", null);

        // Create new section
        ConfigurationSection arenasSection = config.createSection("arenas");

        for (Map.Entry<String, Arena> entry : arenas.entrySet()) {
            ConfigurationSection arenaSection = arenasSection.createSection(entry.getKey());
            entry.getValue().saveToConfig(arenaSection);
        }

        plugin.saveConfig();
        plugin.getLogger().info("Salvas " + arenas.size() + " arenas.");
    }

    public boolean createArena(String name, String displayName, Player creator) {
        name = name.toLowerCase();

        if (arenas.containsKey(name)) {
            MessageUtil.sendMessage(creator, "&cJá existe uma arena com este nome!");
            return false;
        }

        World world = creator.getWorld();
        Arena arena = new Arena(name, displayName, world.getUID());

        arenas.put(name, arena);
        saveArenas();

        return true;
    }

    public boolean deleteArena(String name, Player player) {
        name = name.toLowerCase();

        if (!arenas.containsKey(name)) {
            MessageUtil.sendMessage(player, "&cArena não encontrada!");
            return false;
        }

        // Check if arena is in use
        if (activeGames.containsKey(name)) {
            MessageUtil.sendMessage(player, "&cEsta arena está sendo usada atualmente!");
            return false;
        }

        arenas.remove(name);
        saveArenas();

        return true;
    }

    public boolean addSpawnPoint(String arenaName, Player player) {
        arenaName = arenaName.toLowerCase();

        Arena arena = arenas.get(arenaName);

        if (arena == null) {
            MessageUtil.sendMessage(player, "&cArena não encontrada!");
            return false;
        }

        // Check if player is in the correct world
        if (!player.getWorld().getUID().equals(arena.getWorldUUID())) {
            MessageUtil.sendMessage(player, "&cVocê precisa estar no mundo da arena para adicionar um spawn point!");
            return false;
        }

        arena.addSpawnPoint(player.getLocation());
        saveArenas();

        MessageUtil.sendMessage(player, "&aSpawn point adicionado com sucesso! Total: " + arena.getSpawnPointCount());
        return true;
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getAllArenas() {
        return arenas.values();
    }

    public Game createGame(Arena arena) {
        String arenaName = arena.getName().toLowerCase();

        // Check if game already exists for this arena
        if (activeGames.containsKey(arenaName)) {
            return activeGames.get(arenaName);
        }

        Game game = new Game(plugin, arena);
        activeGames.put(arenaName, game);

        return game;
    }

    public void removeGame(Game game) {
        String arenaName = game.getArena().getName().toLowerCase();
        activeGames.remove(arenaName);
    }

    public Game getGame(String arenaName) {
        return activeGames.get(arenaName.toLowerCase());
    }

    public Collection<Game> getActiveGames() {
        return activeGames.values();
    }

    public Game getPlayerGame(UUID playerUUID) {
        for (Game game : activeGames.values()) {
            if (game.getPlayers().contains(playerUUID)) {
                return game;
            }
        }
        return null;
    }
}