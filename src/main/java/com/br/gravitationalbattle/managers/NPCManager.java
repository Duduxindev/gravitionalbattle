package com.br.gravitationalbattle.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

public class NPCManager {

    private final GravitationalBattle plugin;
    private Map<String, Entity> npcs;
    private Map<String, Location> npcLocations;

    public NPCManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.npcs = new HashMap<>();
        this.npcLocations = new HashMap<>();
        loadNPCs();
    }

    /**
     * Loads all NPCs from configuration
     */
    public void loadNPCs() {
        // Implementation will depend on your NPC system
        plugin.getLogger().info("Loading NPCs...");
    }

    /**
     * Creates a new NPC at the player's location
     *
     * @param player The player executing the command
     * @param name Name of the NPC
     * @param skinName Optional skin name for the NPC
     * @return true if successful
     */
    public boolean createNPC(Player player, String name, String skinName) {
        Location location = player.getLocation();

        // Store NPC location for respawning if needed
        npcLocations.put(name, location);

        // This is a simple implementation - in reality, you'd use a library like Citizens
        try {
            // Create NPC entity
            Entity npc = location.getWorld().spawnEntity(location, org.bukkit.entity.EntityType.PLAYER);
            npc.setCustomName(MessageUtil.colorize("&e" + name));
            npc.setCustomNameVisible(true);

            // Mark as NPC to avoid damage, etc.
            npc.setMetadata("NPC", new FixedMetadataValue(plugin, true));
            npc.setMetadata("NPC_ID", new FixedMetadataValue(plugin, name));

            // Save NPC
            npcs.put(name, npc);

            // Save to config
            saveNPCToConfig(name, location, skinName);

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating NPC: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the ID of an NPC entity
     *
     * @param entity The entity to check
     * @return The NPC ID or null if not an NPC
     */
    public String getNPCId(Entity entity) {
        if (entity == null || !entity.hasMetadata("NPC_ID")) {
            return null;
        }

        for (MetadataValue value : entity.getMetadata("NPC_ID")) {
            if (value.getOwningPlugin() == plugin) {
                return value.asString();
            }
        }

        return null;
    }

    /**
     * Removes an NPC by name
     *
     * @param name Name of the NPC
     * @return true if successful
     */
    public boolean removeNPC(String name) {
        Entity npc = npcs.get(name);
        if (npc != null && !npc.isDead()) {
            npc.remove();
            npcs.remove(name);
            npcLocations.remove(name);
            removeNPCFromConfig(name);
            return true;
        }
        return false;
    }

    /**
     * Teleports an NPC to a new location
     *
     * @param name Name of the NPC
     * @param location New location
     * @return true if successful
     */
    public boolean teleportNPC(String name, Location location) {
        Entity npc = npcs.get(name);
        if (npc != null && !npc.isDead()) {
            npc.teleport(location);
            npcLocations.put(name, location);
            updateNPCLocationInConfig(name, location);
            return true;
        }
        return false;
    }

    /**
     * Gets an NPC by name
     *
     * @param name Name of the NPC
     * @return The NPC entity, or null if not found
     */
    public Entity getNPC(String name) {
        return npcs.get(name);
    }

    /**
     * Gets all NPCs
     *
     * @return Map of NPC names to entities
     */
    public Map<String, Entity> getAllNPCs() {
        return new HashMap<>(npcs);
    }

    /**
     * Checks if a entity is an NPC
     *
     * @param entity The entity to check
     * @return true if entity is an NPC
     */
    public boolean isNPC(Entity entity) {
        return entity != null && entity.hasMetadata("NPC");
    }

    /**
     * Respawns all NPCs (useful after server restart)
     */
    public void respawnAllNPCs() {
        for (String name : npcLocations.keySet()) {
            Location location = npcLocations.get(name);
            if (location != null) {
                // Logic to respawn NPC
            }
        }
    }

    // Private helper methods

    private void saveNPCToConfig(String name, Location location, String skinName) {
        // Implementation will depend on your configuration system
    }

    private void removeNPCFromConfig(String name) {
        // Implementation will depend on your configuration system
    }

    private void updateNPCLocationInConfig(String name, Location location) {
        // Implementation will depend on your configuration system
    }
}