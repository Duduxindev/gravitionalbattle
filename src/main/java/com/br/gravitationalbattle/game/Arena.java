package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

/**
 * Representa uma arena do jogo
 */
public class Arena {

    private String name;
    private String displayName;
    private UUID worldUUID;
    private List<Location> spawnPoints;
    private Location lobbyLocation;
    private int minPlayers = 2;
    private int maxPlayers = 16;
    private GameMode defaultGameMode;
    private GameState state;

    /**
     * Cria uma nova arena
     *
     * @param name Nome da arena
     * @param worldUUID UUID do mundo da arena
     */
    public Arena(String name, UUID worldUUID) {
        this.name = name;
        this.displayName = name; // Por padrão, o nome de exibição é igual ao nome
        this.worldUUID = worldUUID;
        this.spawnPoints = new ArrayList<>();
        this.defaultGameMode = GameMode.SOLO; // Modo padrão
        this.state = GameState.AVAILABLE; // Estado padrão
    }

    /**
     * Obtém o nome da arena
     *
     * @return Nome da arena
     */
    public String getName() {
        return name;
    }

    /**
     * Obtém o nome de exibição da arena
     *
     * @return Nome de exibição
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Define o nome de exibição da arena
     *
     * @param displayName Novo nome de exibição
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtém o UUID do mundo desta arena
     *
     * @return UUID do mundo
     */
    public UUID getWorldUUID() {
        return worldUUID;
    }

    /**
     * Adiciona um ponto de spawn à arena
     *
     * @param location Localização do spawn
     */
    public void addSpawnPoint(Location location) {
        if (location != null) {
            spawnPoints.add(location.clone());
        }
    }

    /**
     * Remove um ponto de spawn da arena
     *
     * @param index Índice do ponto de spawn
     * @return true se o ponto foi removido com sucesso
     */
    public boolean removeSpawnPoint(int index) {
        if (index >= 0 && index < spawnPoints.size()) {
            spawnPoints.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Obtém todos os pontos de spawn da arena
     *
     * @return Lista de pontos de spawn
     */
    public List<Location> getSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }

    /**
     * Obtém a quantidade de pontos de spawn na arena
     *
     * @return Quantidade de spawn points
     */
    public int getSpawnPointCount() {
        return spawnPoints.size();
    }

    /**
     * Define a localização do lobby da arena
     *
     * @param location Localização do lobby
     */
    public void setLobbyLocation(Location location) {
        if (location != null) {
            this.lobbyLocation = location.clone();
        }
    }

    /**
     * Obtém a localização do lobby da arena
     *
     * @return Localização do lobby
     */
    public Location getLobbyLocation() {
        return lobbyLocation != null ? lobbyLocation.clone() : null;
    }

    /**
     * Define o número mínimo de jogadores
     *
     * @param minPlayers Número mínimo de jogadores
     */
    public void setMinPlayers(int minPlayers) {
        if (minPlayers > 0) {
            this.minPlayers = minPlayers;
        }
    }

    /**
     * Define o número máximo de jogadores
     *
     * @param maxPlayers Número máximo de jogadores
     */
    public void setMaxPlayers(int maxPlayers) {
        if (maxPlayers > 0) {
            this.maxPlayers = maxPlayers;
        }
    }

    /**
     * Obtém o número mínimo de jogadores
     *
     * @return Número mínimo de jogadores
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * Obtém o número máximo de jogadores
     *
     * @return Número máximo de jogadores
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Define o modo de jogo padrão da arena
     *
     * @param gameMode Modo de jogo
     */
    public void setDefaultGameMode(GameMode gameMode) {
        this.defaultGameMode = gameMode;
    }

    /**
     * Obtém o modo de jogo padrão da arena
     *
     * @return Modo de jogo
     */
    public GameMode getDefaultGameMode() {
        return defaultGameMode;
    }

    /**
     * Define o estado atual da arena
     *
     * @param state Novo estado
     */
    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Obtém o estado atual da arena
     *
     * @return Estado da arena
     */
    public GameState getState() {
        return state;
    }

    /**
     * Verifica se a arena está disponível para jogo
     *
     * @return true se a arena estiver disponível
     */
    public boolean isAvailable() {
        return state == GameState.AVAILABLE;
    }
}