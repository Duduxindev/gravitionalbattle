package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 * Representa uma equipe no jogo
 */
public class Team {

    private String id;
    private String name;
    private ChatColor chatColor;
    private Color armorColor;
    private List<UUID> members;
    private int score;

    public Team(String id, String name, ChatColor chatColor, Color armorColor) {
        this.id = id;
        this.name = name;
        this.chatColor = chatColor;
        this.armorColor = armorColor;
        this.members = new ArrayList<>();
        this.score = 0;
    }

    /**
     * Obtém o ID da equipe
     *
     * @return ID da equipe
     */
    public String getId() {
        return id;
    }

    /**
     * Obtém o nome da equipe
     *
     * @return Nome da equipe
     */
    public String getName() {
        return name;
    }

    /**
     * Obtém o nome colorido da equipe
     *
     * @return Nome com cor
     */
    public String getColoredName() {
        return chatColor + name;
    }

    /**
     * Obtém a cor do chat da equipe
     *
     * @return Cor do chat
     */
    public ChatColor getChatColor() {
        return chatColor;
    }

    /**
     * Obtém a cor da armadura da equipe
     *
     * @return Cor da armadura
     */
    public Color getArmorColor() {
        return armorColor;
    }

    /**
     * Adiciona um jogador à equipe
     *
     * @param player O jogador
     */
    public void addMember(Player player) {
        if (player != null && !members.contains(player.getUniqueId())) {
            members.add(player.getUniqueId());
        }
    }

    /**
     * Remove um jogador da equipe
     *
     * @param player O jogador
     */
    public void removeMember(Player player) {
        if (player != null) {
            members.remove(player.getUniqueId());
        }
    }

    /**
     * Verifica se um jogador pertence à equipe
     *
     * @param player O jogador
     * @return true se for membro
     */
    public boolean isMember(Player player) {
        return player != null && members.contains(player.getUniqueId());
    }

    /**
     * Obtém a lista de membros da equipe
     *
     * @return Lista de UUIDs
     */
    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    /**
     * Obtém o número de membros na equipe
     *
     * @return Contagem de membros
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * Obtém a pontuação da equipe
     *
     * @return Pontuação atual
     */
    public int getScore() {
        return score;
    }

    /**
     * Adiciona pontos à equipe
     *
     * @param points Pontos a adicionar
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * Redefine a pontuação da equipe para zero
     */
    public void resetScore() {
        this.score = 0;
    }

    /**
     * Cria um item de armadura com a cor da equipe
     *
     * @param type Tipo de armadura (LEATHER_HELMET, LEATHER_CHESTPLATE, etc)
     * @return ItemStack com a cor da equipe
     */
    public ItemStack createArmorItem(Material type) {
        if (type != Material.LEATHER_HELMET && type != Material.LEATHER_CHESTPLATE &&
                type != Material.LEATHER_LEGGINGS && type != Material.LEATHER_BOOTS) {
            return new ItemStack(type);
        }

        ItemStack armor = new ItemStack(type);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();

        meta.setColor(armorColor);
        meta.setDisplayName(getColoredName() + " " + getArmorName(type));

        armor.setItemMeta(meta);
        return armor;
    }

    /**
     * Obtém o nome da peça de armadura
     *
     * @param material Material da armadura
     * @return Nome da peça
     */
    private String getArmorName(Material material) {
        switch (material) {
            case LEATHER_HELMET:
                return "Capacete";
            case LEATHER_CHESTPLATE:
                return "Peitoral";
            case LEATHER_LEGGINGS:
                return "Calças";
            case LEATHER_BOOTS:
                return "Botas";
            default:
                return material.name();
        }
    }

    /**
     * Equipa todos os membros da equipe com armadura colorida
     */
    public void equipTeamMembers() {
        for (UUID uuid : members) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.getInventory().setHelmet(createArmorItem(Material.LEATHER_HELMET));
                player.getInventory().setChestplate(createArmorItem(Material.LEATHER_CHESTPLATE));
                player.getInventory().setLeggings(createArmorItem(Material.LEATHER_LEGGINGS));
                player.getInventory().setBoots(createArmorItem(Material.LEATHER_BOOTS));

                player.sendMessage(chatColor + "Você está na equipe " + getColoredName() + "!");
            }
        }
    }
}