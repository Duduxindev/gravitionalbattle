package com.br.gravitationalbattle.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.br.gravitationalbattle.GravitationalBattle;

/**
 * Listener para eventos relacionados aos NPCs
 */
public class NPCListener implements Listener {

    private final GravitationalBattle plugin;

    public NPCListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    /**
     * Trata a interação de jogadores com NPCs
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        // Verifica se é um NPC
        if (entity instanceof Villager) {
            if (plugin.getNPCManager().isNPC(entity.getUniqueId())) {
                event.setCancelled(true);

                Player player = event.getPlayer();
                plugin.getNPCManager().processNPCInteraction(player, entity.getUniqueId());
            }
        }
    }
}