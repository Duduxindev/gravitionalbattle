package com.br.gravitationalbattle.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Gerencia NPCs no plugin
 */
public class NPCManager {

    private final GravitationalBattle plugin;
    private final Map<String, ArmorStand> npcs;

    public NPCManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.npcs = new HashMap<>();

        // Carregar NPCs
        loadNPCs();
    }

    /**
     * Carrega os NPCs do arquivo de configuração
     */
    private void loadNPCs() {
        // Implementar carregamento de NPCs do arquivo de configuração
    }

    /**
     * Salva os NPCs no arquivo de configuração
     */
    public void saveNPCs() {
        // Implementar salvamento de NPCs no arquivo de configuração
    }

    /**
     * Cria um NPC no local especificado
     *
     * @param name Nome do NPC
     * @param location Local onde o NPC será criado
     * @return true se o NPC foi criado com sucesso
     */
    public boolean createNPC(String name, Location location) {
        if (name == null || location == null) {
            return false;
        }

        // Verificar se já existe um NPC com esse nome
        if (npcs.containsKey(name.toLowerCase())) {
            return false;
        }

        // Criar ArmorStand para representar o NPC
        ArmorStand npc = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        npc.setCustomName(MessageUtil.colorize("&6&l" + name));
        npc.setCustomNameVisible(true);
        npc.setVisible(true);
        npc.setGravity(false);
        npc.setSmall(false);
        npc.setBasePlate(false);
        npc.setArms(true);

        // Salvar NPC no mapa
        npcs.put(name.toLowerCase(), npc);

        // Salvar NPCs
        saveNPCs();

        return true;
    }

    /**
     * Remove um NPC
     *
     * @param name Nome do NPC
     * @return true se o NPC foi removido com sucesso
     */
    public boolean removeNPC(String name) {
        if (name == null) {
            return false;
        }

        ArmorStand npc = npcs.remove(name.toLowerCase());
        if (npc != null) {
            npc.remove();
            saveNPCs();
            return true;
        }

        return false;
    }

    /**
     * Verifica se um NPC existe
     *
     * @param name Nome do NPC
     * @return true se o NPC existir
     */
    public boolean npcExists(String name) {
        if (name == null) {
            return false;
        }
        return npcs.containsKey(name.toLowerCase());
    }
}