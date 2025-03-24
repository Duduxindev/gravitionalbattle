package com.br.gravitationalbattle.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.arena.Arena;
import com.br.gravitationalbattle.utils.LocationUtil;

/**
 * Manages all arenas in the plugin
 */
public class ArenaManager {

    private GravitationalBattle plugin;
    private Map<String, Arena> arenas;

    /**
     * Creates a new arena manager
     * @param plugin The plugin instance
     */
    public ArenaManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
    }

    /**
     * Loads all arenas from config
     */
    public void loadArenas() {
        arenas.clear();

        ConfigurationSection arenaSection = plugin.getConfig().getConfigurationSection("arenas");
        if (arenaSection == null) {
            plugin.getConfig().createSection("arenas");
            plugin.saveConfig();
            return;
        }

        Set<String> arenaNames = arenaSection.getKeys(false);

        for (String name : arenaNames) {
            try {
                String worldName = plugin.getConfig().getString("arenas." + name + ".world");
                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    plugin.getLogger().warning("Failed to load arena '" + name + "': World '" + worldName + "' not found");
                    continue;
                }

                // Load lobby location
                Location lobbyLocation = LocationUtil.stringToLocation(
                        plugin.getConfig().getString("arenas." + name + ".lobby"));

                if (lobbyLocation == null) {
                    plugin.getLogger().warning("Failed to load arena '" + name + "': Invalid lobby location");
                    continue;
                }

                // Create arena with your existing constructor
                Arena arena = new Arena(name, lobbyLocation);

                // Load spawn points
                List<String> spawnPointStrings = plugin.getConfig().getStringList("arenas." + name + ".spawnpoints");
                for (String spawnString : spawnPointStrings) {
                    Location spawnLocation = LocationUtil.stringToLocation(spawnString);
                    if (spawnLocation != null) {
                        arena.addSpawnPoint(spawnLocation);
                    }
                }

                // Load settings
                if (plugin.getConfig().contains("arenas." + name + ".min_players")) {
                    arena.setMinPlayers(plugin.getConfig().getInt("arenas." + name + ".min_players"));
                }

                if (plugin.getConfig().contains("arenas." + name + ".max_players")) {
                    arena.setMaxPlayers(plugin.getConfig().getInt("arenas." + name + ".max_players"));
                }

                if (plugin.getConfig().contains("arenas." + name + ".countdown_time")) {
                    arena.setCountdownTime(plugin.getConfig().getInt("arenas." + name + ".countdown_time"));
                }

                if (plugin.getConfig().contains("arenas." + name + ".enabled")) {
                    arena.setEnabled(plugin.getConfig().getBoolean("arenas." + name + ".enabled"));
                }

                // Add to the map
                arenas.put(name.toLowerCase(), arena);
                plugin.getLogger().info("Loaded arena: " + name);

            } catch (Exception e) {
                plugin.getLogger().warning("Error loading arena '" + name + "': " + e.getMessage());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("Loaded " + arenas.size() + " arenas");
    }

    /**
     * Saves all arenas to config
     */
    public void saveArenas() {
        for (Arena arena : arenas.values()) {
            saveArena(arena);
        }
        plugin.saveConfig();
    }

    /**
     * Saves a specific arena to config
     * @param arena The arena to save
     */
    public void saveArena(Arena arena) {
        String path = "arenas." + arena.getName();

        // Save basic information
        plugin.getConfig().set(path + ".world", arena.getLobbyLocation().getWorld().getName());
        plugin.getConfig().set(path + ".lobby", LocationUtil.locationToString(arena.getLobbyLocation()));

        // Save spawn points
        List<String> spawnPointStrings = new ArrayList<>();
        for (Location loc : arena.getSpawnPoints()) {
            spawnPointStrings.add(LocationUtil.locationToString(loc));
        }
        plugin.getConfig().set(path + ".spawnpoints", spawnPointStrings);

        // Save settings
        plugin.getConfig().set(path + ".min_players", arena.getMinPlayers());
        plugin.getConfig().set(path + ".max_players", arena.getMaxPlayers());
        plugin.getConfig().set(path + ".countdown_time", arena.getCountdownTime());
        plugin.getConfig().set(path + ".enabled", arena.isEnabled());

        // Save the config
        plugin.saveConfig();
    }

    /**
     * Creates a new arena
     * @param name The arena name
     * @param lobbyLocation The lobby location
     * @return The created arena, or null if an arena with that name already exists
     */
    public Arena createArena(String name, Location lobbyLocation) {
        String lowerName = name.toLowerCase();

        if (arenas.containsKey(lowerName)) {
            return null; // Arena already exists
        }

        Arena arena = new Arena(name, lobbyLocation);
        arenas.put(lowerName, arena);
        saveArena(arena);

        return arena;
    }

    /**
     * Gets an arena by name
     * @param name The arena name
     * @return The arena, or null if not found
     */
    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    /**
     * Deletes an arena
     * @param name The arena name
     * @return true if the arena was deleted, false if it didn't exist
     */
    public boolean deleteArena(String name) {
        String lowerName = name.toLowerCase();

        if (!arenas.containsKey(lowerName)) {
            return false;
        }

        arenas.remove(lowerName);
        plugin.getConfig().set("arenas." + name, null);
        plugin.saveConfig();

        return true;
    }

    /**
     * Gets a list of all arena names
     * @return List of arena names
     */
    public List<String> getArenaNames() {
        return new ArrayList<>(arenas.keySet());
    }

    /**
     * Gets a list of all arenas
     * @return List of arenas
     */
    public List<Arena> getArenas() {
        return new ArrayList<>(arenas.values());
    }

    /**
     * Gets a list of enabled arenas
     * @return List of enabled arenas
     */
    public List<Arena> getEnabledArenas() {
        List<Arena> enabledArenas = new ArrayList<>();

        for (Arena arena : arenas.values()) {
            if (arena.isEnabled()) {
                enabledArenas.add(arena);
            }
        }

        return enabledArenas;
    }

    /**
     * Checks if an arena exists
     * @param name The arena name
     * @return true if the arena exists
     */
    public boolean arenaExists(String name) {
        return arenas.containsKey(name.toLowerCase());
    }
}