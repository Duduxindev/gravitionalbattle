package com.br.gravitationalbattle.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.commands.AbrirLojaCommand;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Listener para eventos de menu
 */
public class MenuListener implements Listener {

    private final GravitationalBattle plugin;

    public MenuListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Verificar se é um de nossos menus
        if (title.equals("§6§lLoja - Batalha Gravitacional") ||
                title.equals("§b§lHabilidades") ||
                title.equals("§d§lTítulos") ||
                title.equals("§c§lEfeitos de Morte") ||
                title.equals("§6§lEfeitos de Vitória")) {

            event.setCancelled(true); // Cancelar o clique para evitar mover itens

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            // Verificar se clicou em um item válido
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            // Obter o nome do item (se houver)
            String itemName = "";
            if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                itemName = clickedItem.getItemMeta().getDisplayName();
            }

            // Menu principal da loja
            if (title.equals("§6§lLoja - Batalha Gravitacional")) {
                handleMainShopClick(player, clickedItem, itemName);
                return;
            }

            // Menus de categorias da loja
            if (title.equals("§b§lHabilidades") ||
                    title.equals("§d§lTítulos") ||
                    title.equals("§c§lEfeitos de Morte") ||
                    title.equals("§6§lEfeitos de Vitória")) {

                handleCategoryMenuClick(player, clickedItem, itemName, title);
                return;
            }
        }
    }

    /**
     * Processa cliques no menu principal
     */
    private void handleMainShopClick(Player player, ItemStack clickedItem, String itemName) {
        // Botão para fechar
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Menu de habilidades
        if (clickedItem.getType() == Material.NETHER_STAR) {
            AbrirLojaCommand shopCommand = new AbrirLojaCommand(plugin);
            shopCommand.openCategoryMenu(player, "habilidades");
            return;
        }

        // Menu de títulos
        if (clickedItem.getType() == Material.NAME_TAG) {
            AbrirLojaCommand shopCommand = new AbrirLojaCommand(plugin);
            shopCommand.openCategoryMenu(player, "titulos");
            return;
        }

        // Menu de efeitos de morte
        if (clickedItem.getType() == Material.REDSTONE) {
            AbrirLojaCommand shopCommand = new AbrirLojaCommand(plugin);
            shopCommand.openCategoryMenu(player, "efeitos_morte");
            return;
        }

        // Menu de efeitos de vitória
    }

    /**
     * Processa cliques nos menus de categoria
     */
    private void handleCategoryMenuClick(Player player, ItemStack clickedItem, String itemName, String menuTitle) {
        // Botão para voltar
        if (clickedItem.getType() == Material.ARROW && itemName.equals("§a§lVoltar")) {
            AbrirLojaCommand shopCommand = new AbrirLojaCommand(plugin);
            shopCommand.openMainShopMenu(player);
            return;
        }

        // Itens compráveis
        if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore()) {
            ItemMeta meta = clickedItem.getItemMeta();
            String itemId = extractItemId(meta.getLore());

            if (itemId != null) {
                // Verificar se o jogador já possui
                if (plugin.getRewardManager().hasItem(player, itemId)) {
                    MessageUtil.sendMessage(player, "&cVocê já possui este item!");
                } else {
                    // Tentar comprar
                    int cost = plugin.getRewardManager().getItemCost(itemId);
                    if (cost > 0) {
                        if (plugin.getRewardManager().purchaseItem(player, itemId)) {
                            MessageUtil.sendMessage(player, "&aVocê adquiriu &e" + itemName + " &acom sucesso!");
                            // Atualizar o menu
                            player.closeInventory();

                            // Reabrindo o menu correto
                            AbrirLojaCommand shopCommand = new AbrirLojaCommand(plugin);
                            if (menuTitle.equals("§b§lHabilidades")) {
                                shopCommand.openCategoryMenu(player, "habilidades");
                            } else if (menuTitle.equals("§d§lTítulos")) {
                                shopCommand.openCategoryMenu(player, "titulos");
                            } else if (menuTitle.equals("§c§lEfeitos de Morte")) {
                                shopCommand.openCategoryMenu(player, "efeitos_morte");
                            } else if (menuTitle.equals("§6§lEfeitos de Vitória")) {
                                shopCommand.openCategoryMenu(player, "efeitos_vitoria");
                            }
                        } else {
                            MessageUtil.sendMessage(player, "&cVocê não tem moedas suficientes para comprar este item!");
                        }
                    }
                }
            }
        }
    }

    /**
     * Extrai o ID do item de sua descrição
     */
    private String extractItemId(java.util.List<String> lore) {
        for (String line : lore) {
            if (line.startsWith("§7ID: ")) {
                return line.substring(6); // Remove "§7ID: "
            }
        }
        return null;
    }
}