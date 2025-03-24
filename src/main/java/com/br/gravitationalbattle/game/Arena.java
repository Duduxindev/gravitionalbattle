package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;

/**
 * Represents a battle arena
 */
public class Arena {

    private static final Random RANDOM = new Random();

    private String name;
    private String displayName; // Campo adicionado
    private Location lobbyLocation;
    private List<Location> spawnPoints;
    private int minPlayers;
    private int maxPlayers;
    private int countdownTime;
    private boolean enabled;
    private UUID worldUUID; // Campo adicionado para compatibilidade

    /**
     * Creates a new arena
     * @param name The arena name
     * @param lobbyLocation The lobby location
     */
    public Arena(String name, Location lobbyLocation) {
        this.name = name;
        this.displayName = name; // Por padrão, displayName é igual ao name
        this.lobbyLocation = lobbyLocation;
        this.spawnPoints = new ArrayList<>();
        this.minPlayers = 2;
        this.maxPlayers = 16;
        this.countdownTime = 60;
        this.enabled = true;
        this.worldUUID = lobbyLocation.getWorld().getUID();

        // Add lobby as default spawn point
        this.spawnPoints.add(lobbyLocation);
    }

    /**
     * Gets the arena name
     * @return The arena name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the display name
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name
     * @param displayName The display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the lobby location
     * @return The lobby location
     */
    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    /**
     * Sets the lobby location
     * @param lobbyLocation The lobby location
     */
    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    /**
     * Adds a spawn point
     * @param location The spawn point location
     */
    public void addSpawnPoint(Location location) {
        spawnPoints.add(location);
    }

    /**
     * Gets all spawn points
     * @return List of spawn points
     */
    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    /**
     * Removes a spawn point
     * @param index The spawn point index
     * @return true if removed, false if index was out of bounds
     */
    public boolean removeSpawnPoint(int index) {
        if (index < 0 || index >= spawnPoints.size()) {
            return false;
        }

        spawnPoints.remove(index);
        return true;
    }

    /**
     * Gets a random spawn point
     * @return A random spawn point, or the lobby location if no spawn points exist
     */
    public Location getRandomSpawnPoint() {
        if (spawnPoints.isEmpty()) {
            return lobbyLocation;
        }

        return spawnPoints.get(RANDOM.nextInt(spawnPoints.size()));
    }

    /**
     * Gets the minimum players required
     * @return The minimum players
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Sets the minimum players required
     * @param minPlayers The minimum players
     */
    public void setMinPlayers(int minPlayers) {
        this.minPlayers = Math.max(2, minPlayers);
    }

    /**
     * Gets the maximum players allowed
     * @return The maximum players
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the maximum players allowed
     * @param maxPlayers The maximum players
     */
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = Math.max(this.minPlayers, maxPlayers);
    }

    /**
     * Gets the countdown time in seconds
     * @return The countdown time
     */
    public int getCountdownTime() {
        return countdownTime;
    }

    /**
     * Sets the countdown time in seconds
     * @param countdownTime The countdown time
     */
    public void setCountdownTime(int countdownTime) {
        this.countdownTime = Math.max(10, countdownTime);
    }

    /**
     * Checks if the arena is enabled
     * @return true if the arena is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Alias para isEnabled() para compatibilidade com outras partes do código
     * @return true if the arena is available
     */
    public boolean isAvailable() {
        return enabled;
    }

    /**
     * Sets whether the arena is enabled
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if the arena has enough spawn points for the maximum number of players
     * @return true if there are enough spawn points
     */
    public boolean hasEnoughSpawnPoints() {
        return spawnPoints.size() >= maxPlayers;
    }

    /**
     * Gets the number of spawn points
     * @return The number of spawn points
     */
    public int getSpawnPointCount() {
        return spawnPoints.size();
    }

    /**
     * Gets the world UUID
     * @return The world UUID
     */
    public UUID getWorldUUID() {
        return worldUUID;
    }

    /**
     * Sets the state of the arena (compatibilidade com código existente)
     * @param state O estado da arena (GameState.AVAILABLE ou GameState.MAINTENANCE)
     */
    public void setState(com.br.gravitationalbattle.game.GameState state) {
        this.enabled = (state == com.br.gravitationalbattle.game.GameState.AVAILABLE);
    }

    /**
     * Gets the state of the arena (compatibilidade com código existente)
     * @return O estado da arena
     */
    public com.br.gravitationalbattle.game.GameState getState() {
        return this.enabled ?
                com.br.gravitationalbattle.game.GameState.AVAILABLE :
                com.br.gravitationalbattle.game.GameState.MAINTENANCE;
    }
}