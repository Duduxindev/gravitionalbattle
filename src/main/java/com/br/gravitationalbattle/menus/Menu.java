package com.br.gravitationalbattle.menus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Classe base para menus do plugin
 */
public abstract class Menu implements InventoryHolder, Listener {

    protected final GravitationalBattle plugin;
    protected final Inventory inventory;
    protected final Map<Integer, Runnable> clickHandlers;
    protected final UUID ownerUUID;

    /**
     * Construtor para um menu
     *
     * @param plugin Instância do plugin
     * @param owner Jogador que abrirá o menu
     * @param title Título do menu
     * @param size Tamanho do menu (múltiplo de 9)
     */
    public Menu(GravitationalBattle plugin, Player owner, String title, int size) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, size, MessageUtil.colorize(title));
        this.clickHandlers = new HashMap<>();
        this.ownerUUID = owner.getUniqueId();

        // Registra os eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Abre o menu para o jogador
     *
     * @param player Jogador que verá o menu
     */
    public void open(Player player) {
        // Garante que o menu só é aberto pelo dono
        if (player.getUniqueId().equals(ownerUUID)) {
            player.openInventory(inventory);
        }
    }

    /**
     * Fecha o menu para o jogador
     */
    public void close() {
        Player player = Bukkit.getPlayer(ownerUUID);
        if (player != null && player.isOnline()) {
            player.closeInventory();
        }
    }

    /**
     * Define um item no menu
     *
     * @param slot Slot onde o item será colocado
     * @param item Item a ser colocado
     * @param clickHandler Ação a ser executada ao clicar no item
     */
    public void setItem(int slot, ItemStack item, Runnable clickHandler) {
        inventory.setItem(slot, item);
        if (clickHandler != null) {
            clickHandlers.put(slot, clickHandler);
        }
    }

    /**
     * Define um item no menu sem ação
     *
     * @param slot Slot onde o item será colocado
     * @param item Item a ser colocado
     */
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, null);
    }

    /**
     * Método que deve ser sobrescrito para inicializar os itens do menu
     */
    public abstract void initializeItems();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        if (!player.getUniqueId().equals(ownerUUID)) return;

        int slot = event.getRawSlot();
        if (clickHandlers.containsKey(slot)) {
            Runnable handler = clickHandlers.get(slot);
            if (handler != null) {
                // Execute o handler em uma task síncrona
                Bukkit.getScheduler().runTask(plugin, handler);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() != this) return;

        Player player = (Player) event.getPlayer();
        if (!player.getUniqueId().equals(ownerUUID)) return;

        // Desregistra os eventos quando o inventário é fechado
        HandlerList.unregisterAll(this);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}