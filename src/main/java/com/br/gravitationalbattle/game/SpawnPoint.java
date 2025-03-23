package com.br.gravitationalbattle.game;

import org.bukkit.Location;

public class SpawnPoint {

    private Location location;
    private boolean inUse;

    public SpawnPoint(Location location) {
        this.location = location;
        this.inUse = false;
    }

    public Location getLocation() {
        return location.clone();
    }

    /**
     * Define se este spawn está em uso
     *
     * @param inUse true se o spawn estiver em uso, false caso contrário
     */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /**
     * Reseta o estado deste spawn para não utilizado
     */
    public void reset() {
        this.inUse = false;
    }

    /**
     * Verifica se este spawn está em uso
     *
     * @return true se o spawn estiver em uso, false caso contrário
     */
    public boolean isInUse() {
        return inUse;
    }
}