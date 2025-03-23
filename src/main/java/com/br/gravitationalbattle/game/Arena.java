package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.br.gravitationalbattle.utils.LocationUtil;

public class Arena {

    private final String name;
    private final String displayName;
    private final UUID worldUUID;
    private GameState state;
    private final List<SpawnPoint> spawnPoints;
    private final Random random;

    public Arena(String name, String displayName, UUID worldUUID) {
        this.name = name;
        this.displayName = displayName;
        this.worldUUID = worldUUID;
        this.state = GameState.AVAILABLE;
        this.spawnPoints = new ArrayList<>();
        this.random = new Random();
    }

    public static Arena loadFromConfig(String name, ConfigurationSection section) {
        if (section == null) return null;

        String displayName = section.getString("displayName", name);
        UUID worldUUID;

        try {
            worldUUID = UUID.fromString(section.getString("worldUUID"));
        } catch (IllegalArgumentException e) {
            return null;
        }

        Arena arena = new Arena(name, displayName, worldUUID);

        // Load state
        String stateStr = section.getString("state", "AVAILABLE");
        try {
            arena.state = GameState.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            arena.state = GameState.AVAILABLE;
        }

        // Load spawn points
        ConfigurationSection spawnSection = section.getConfigurationSection("spawns");
        if (spawnSection != null) {
            for (String key : spawnSection.getKeys(false)) {
                String locationStr = spawnSection.getString(key);
                Location location = LocationUtil.stringToLocation(locationStr);

                if (location != null) {
                    arena.addSpawnPoint(location);
                }
            }
        }

        return arena;
    }

    public void saveToConfig(ConfigurationSection section) {
        section.set("displayName", displayName);
        section.set("worldUUID", worldUUID.toString());
        section.set("state", state.name());

        // Save spawn points
        ConfigurationSection spawnSection = section.createSection("spawns");
        for (int i = 0; i < spawnPoints.size(); i++) {
            Location location = spawnPoints.get(i).getLocation();
            spawnSection.set(String.valueOf(i), LocationUtil.locationToString(location));
        }
    }

    public void addSpawnPoint(Location location) {
        spawnPoints.add(new SpawnPoint(location));
    }

    public SpawnPoint getRandomSpawnPoint() {
        if (spawnPoints.isEmpty()) {
            return null;
        }

        return spawnPoints.get(random.nextInt(spawnPoints.size()));
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getWorldUUID() {
        return worldUUID;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public int getSpawnPointCount() {
        return spawnPoints.size();
    }

    public List<SpawnPoint> getSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }

    public boolean isAvailable() {
        return state == GameState.AVAILABLE;
    }
}