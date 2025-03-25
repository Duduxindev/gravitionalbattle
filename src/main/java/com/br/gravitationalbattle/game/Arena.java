package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Arena {
    private String name;
    private String displayName;
    private UUID worldUUID;
    private GameState state;
    private List<Location> spawnPoints;
    private boolean enabled;
    private int maxPlayers;
    private int minPlayers;
    private Location lobbyLocation;

    public Arena(String name, String displayName, UUID worldUUID) {
        this.name = name;
        this.displayName = displayName;
        this.worldUUID = worldUUID;
        this.state = GameState.AVAILABLE;
        this.spawnPoints = new ArrayList<>();
        this.enabled = true;
        this.maxPlayers = 16; // Default value
        this.minPlayers = 2;  // Default value

        // Default lobby location is the spawn of the arena world
        World world = Bukkit.getWorld(worldUUID);
        if (world != null) {
            this.lobbyLocation = world.getSpawnLocation();
        }
    }

    /**
     * Gets the lobby location for this arena
     *
     * @return Lobby location or null if not set
     */
    public Location getLobbyLocation() {
        return lobbyLocation != null ? lobbyLocation.clone() : null;
    }

    /**
     * Sets the lobby location for this arena
     *
     * @param location Lobby location
     */
    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location.clone();
    }

    /**
     * Checks if the arena is enabled
     *
     * @return true if the arena is enabled
     */
    public boolean isEnabled() {
        return enabled && state == GameState.AVAILABLE;
    }

    /**
     * Gets the maximum number of players allowed in this arena
     *
     * @return Maximum player count
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum number of players for this arena
     *
     * @param maxPlayers Maximum player count
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    /**
     * Gets the minimum number of players required to start a game
     *
     * @return Minimum player count
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Sets the minimum number of players required to start a game
     *
     * @param minPlayers Minimum player count
     */
    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    /**
     * Sets whether the arena is enabled
     *
     * @param enabled Whether arena is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the name of this arena
     *
     * @return Arena name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the display name of this arena
     *
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the UUID of the world for this arena
     *
     * @return World UUID
     */
    public UUID getWorldUUID() {
        return worldUUID;
    }

    /**
     * Gets the current state of this arena
     *
     * @return Arena state
     */
    public GameState getState() {
        return state;
    }

    /**
     * Sets the state of this arena
     *
     * @param state New state
     */
    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Checks if the arena is available
     *
     * @return true if available
     */
    public boolean isAvailable() {
        return state == GameState.AVAILABLE;
    }

    /**
     * Gets all spawn points for this arena
     *
     * @return List of spawn locations
     */
    public List<Location> getSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }

    /**
     * Adds a spawn point to this arena
     *
     * @param location Spawn location
     */
    public void addSpawnPoint(Location location) {
        spawnPoints.add(location);
    }

    /**
     * Gets the number of spawn points
     *
     * @return Spawn point count
     */
    public int getSpawnPointCount() {
        return spawnPoints.size();
    }
}