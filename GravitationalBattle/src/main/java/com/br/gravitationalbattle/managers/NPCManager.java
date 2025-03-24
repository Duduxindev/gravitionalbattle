package com.br.gravitationalbattle.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.LocationUtil;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Gerencia os NPCs do plugin
 */
public class NPCManager {

    private final GravitationalBattle plugin;
    private final Map<String, UUID> npcs;

    public NPCManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.npcs = new HashMap<>();

        loadNPCs();
    }

    /**
     * Carrega os NPCs salvos na configuração
     */
    public void loadNPCs() {
        if (!plugin.getConfig().contains("npcs")) {
            return;
        }

        for (String key : plugin.getConfig().getConfigurationSection("npcs").getKeys(false)) {
            String locationStr = plugin.getConfig().getString("npcs." + key + ".location");
            if (locationStr == null) continue;

            Location location = LocationUtil.stringToLocation(locationStr);
            if (location == null) continue;

            // Verifica se o chunk está carregado
            if (!location.getChunk().isLoaded()) {
                location.getChunk().load();
            }

            // Cria o NPC
            Villager npc = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
            npc.setCustomName(MessageUtil.colorize("&6&l" + key));
            npc.setCustomNameVisible(true);
            npc.setInvulnerable(true);
            npc.setPersistent(true);
            npc.setAI(false);

            npcs.put(key, npc.getUniqueId());

            plugin.getLogger().info("NPC " + key + " carregado em " + location.getWorld().getName() + " " +
                    location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
        }
    }

    /**
     * Salva os NPCs na configuração
     */
    public void saveNPCs() {
        // Remove seção anterior
        plugin.getConfig().set("npcs", null);

        // Salva os NPCs
        for (Map.Entry<String, UUID> entry : npcs.entrySet()) {
            String npcName = entry.getKey();
            UUID npcUUID = entry.getValue();

            Villager npc = null;
            for (Villager entity : Bukkit.getWorlds().get(0).getEntitiesByClass(Villager.class)) {
                if (entity.getUniqueId().equals(npcUUID)) {
                    npc = entity;
                    break;
                }
            }

            if (npc == null) continue;

            Location npcLoc = npc.getLocation();
            String locationStr = LocationUtil.locationToString(npcLoc);

            plugin.getConfig().set("npcs." + npcName + ".location", locationStr);
        }

        // Salva a configuração
        plugin.saveConfig();
    }

    /**
     * Cria um novo NPC
     *
     * @param name Nome do NPC
     * @param location Local onde o NPC será criado
     * @return true se o NPC foi criado com sucesso
     */
    public boolean createNPC(String name, Location location) {
        if (npcs.containsKey(name)) {
            return false;
        }

        // Cria o NPC
        Villager npc = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        npc.setCustomName(MessageUtil.colorize("&6&l" + name));
        npc.setCustomNameVisible(true);
        npc.setInvulnerable(true);
        npc.setPersistent(true);
        npc.setAI(false);

        npcs.put(name, npc.getUniqueId());

        // Salva na configuração
        String locationStr = LocationUtil.locationToString(location);
        plugin.getConfig().set("npcs." + name + ".location", locationStr);
        plugin.saveConfig();

        return true;
    }

    /**
     * Remove um NPC
     *
     * @param name Nome do NPC a ser removido
     * @return true se o NPC foi removido com sucesso
     */
    public boolean removeNPC(String name) {
        if (!npcs.containsKey(name)) {
            return false;
        }

        UUID npcUUID = npcs.get(name);
        npcs.remove(name);

        // Remove da configuração
        plugin.getConfig().set("npcs." + name, null);
        plugin.saveConfig();

        // Remove a entidade do mundo
        for (Villager entity : Bukkit.getWorlds().get(0).getEntitiesByClass(Villager.class)) {
            if (entity.getUniqueId().equals(npcUUID)) {
                entity.remove();
                break;
            }
        }

        return true;
    }

    /**
     * Verifica se um UUID é de um NPC
     *
     * @param entityUUID UUID da entidade
     * @return true se for um NPC
     */
    public boolean isNPC(UUID entityUUID) {
        return npcs.containsValue(entityUUID);
    }

    /**
     * Obtém o nome de um NPC pelo UUID
     *
     * @param npcUUID UUID do NPC
     * @return Nome do NPC ou null se não existir
     */
    public String getNPCName(UUID npcUUID) {
        for (Map.Entry<String, UUID> entry : npcs.entrySet()) {
            if (entry.getValue().equals(npcUUID)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Processa a interação de um jogador com um NPC
     *
     * @param player Jogador que interagiu
     * @param npcUUID UUID do NPC
     */
    public void processNPCInteraction(Player player, UUID npcUUID) {
        String npcName = getNPCName(npcUUID);
        if (npcName == null) return;

        switch (npcName.toLowerCase()) {
            case "join":
                // Abre o menu de seleção de arena
                new com.br.gravitationalbattle.menus.MapSelectorMenu(plugin).open(player);
                break;

            case "stats":
                // Mostra as estatísticas do jogador
                plugin.getServer().dispatchCommand(player, "stats");
                break;

            case "spectate":
                // Lista jogos em andamento para assistir
                MessageUtil.sendMessage(player, "&eJogos em andamento para assistir:");
                for (com.br.gravitationalbattle.game.Game game : plugin.getArenaManager().getActiveGames()) {
                    if (game.getState() == com.br.gravitationalbattle.game.GameState.INGAME) {
                        MessageUtil.sendMessage(player, "&a" + game.getArena().getDisplayName() + " &7- &fJogadores: " +
                                game.getAliveCount() + " &7- &e/spectate " + game.getArena().getName());
                    }
                }
                break;

            default:
                // NPC desconhecido
                MessageUtil.sendMessage(player, "&eVocê interagiu com o NPC " + npcName);
                break;
        }
    }

    /**
     * Teleporta todos os NPCs para o spawn
     */
    public void teleportNPCsToSpawn() {
        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();

        for (Map.Entry<String, UUID> entry : npcs.entrySet()) {
            UUID npcUUID = entry.getValue();

            for (Villager entity : Bukkit.getWorlds().get(0).getEntitiesByClass(Villager.class)) {
                if (entity.getUniqueId().equals(npcUUID)) {
                    entity.teleport(spawnLocation, TeleportCause.PLUGIN);
                    break;
                }
            }
        }
    }
}