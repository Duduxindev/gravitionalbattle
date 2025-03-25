package com.br.gravitationalbattle.commands;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Comando para acessar a loja do jogo com GUI
 */
public class AbrirLojaCommand implements CommandExecutor {

    private final GravitationalBattle plugin;

    public AbrirLojaCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;
        openMainShopMenu(player);
        return true;
    }

    /**
     * Abre o menu principal da loja para o jogador
     *
     * @param player O jogador
     */
    public void openMainShopMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 45, "§6§lLoja - Batalha Gravitacional");

        // Informações sobre moedas do jogador
        ItemStack infoItem = createItem(Material.EMERALD, "§e§lSuas Moedas",
                "§7Você tem §e" + plugin.getRewardManager().getPlayerTokens(player) + " §7moedas");
        menu.setItem(4, infoItem);

        // Separador decorativo
        ItemStack separator = createItem(Material.NETHER_STAR, "§r", (byte) 7);
        for (int i = 9; i < 18; i++) {
            menu.setItem(i, separator);
        }

        // Categoria: Habilidades
        ItemStack abilities = createItem(Material.NETHER_STAR, "§b§lHabilidades",
                "§7Adquira habilidades especiais",
                "§7para usar durante o jogo.",
                "",
                "§a• §eSalto Duplo - §61200 moedas",
                "§a• §eEscudo Protetor - §61500 moedas",
                "§a• §eTeleporte Rápido - §61800 moedas",
                "§a• §eBola de Fogo - §61600 moedas",
                "",
                "§eClique para visualizar!"
        );
        menu.setItem(20, abilities);

        // Categoria: Títulos
        ItemStack titles = createItem(Material.NAME_TAG, "§d§lTítulos",
                "§7Títulos exclusivos para",
                "§7exibir durante o jogo.",
                "",
                "§a• §eVencedor §7- §6800 moedas",
                "§a• §eGuerreiro §7- §6800 moedas",
                "§a• §eAssassino §7- §61000 moedas",
                "§a• §eLenda §7- §61500 moedas",
                "",
                "§eClique para visualizar!"
        );
        menu.setItem(22, titles);

        // Categoria: Efeitos de Morte
        ItemStack deathEffects = createItem(Material.REDSTONE, "§c§lEfeitos de Morte",
                "§7Efeitos especiais que acontecem",
                "§7quando você elimina um jogador.",
                "",
                "§a• §eExplosão §7- §6500 moedas",
                "§a• §eRaio §7- §6500 moedas",
                "§a• §ePartículas §7- §6400 moedas",
                "",
                "§eClique para visualizar!"
        );
        menu.setItem(24, deathEffects);

        // Categoria: Efeitos de Vitória
        ItemStack victoryEffects = createItem(Material.STICK, "§6§lEfeitos de Vitória",
                "§7Efeitos especiais que acontecem",
                "§7quando você vence uma partida.",
                "",
                "§a• §eFogos de Artifício §7- §6300 moedas",
                "§a• §eConfeti §7- §6300 moedas",
                "",
                "§eClique para visualizar!"
        );
        menu.setItem(30, victoryEffects);

        // Categoria: Kits
        ItemStack kits = createItem(Material.CHEST, "§e§lKits",
                "§7Conjuntos de itens especiais",
                "§7para usar durante o jogo.",
                "",
                "§cDisponível em breve!",
                "",
                "§eClique para visualizar!"
        );
        menu.setItem(32, kits);

        // Item para fechar o menu
        ItemStack closeItem = createItem(Material.BARRIER, "§c§lFechar",
                "§7Clique para fechar o menu");
        menu.setItem(40, closeItem);

        // Decoração
        ItemStack decoration = createItem(Material.GLASS, " ", (byte) 1);
        for (int slot : Arrays.asList(0, 1, 7, 8, 36, 37, 43, 44)) {
            menu.setItem(slot, decoration);
        }

        player.openInventory(menu);
    }

    /**
     * Abre o menu de uma categoria específica
     *
     * @param player O jogador
     * @param category Nome da categoria
     */
    public void openCategoryMenu(Player player, String category) {
        String title;
        int size;
        List<ItemStack> items = new ArrayList<>();

        switch (category) {
            case "habilidades":
                title = "§b§lHabilidades";
                size = 36;

                items.add(createShopItem(Material.FEATHER, "§e§lSalto Duplo",
                        "§7Permite fazer um segundo salto",
                        "§7enquanto estiver no ar.",
                        "ability.doublejump", 1200, player));

                items.add(createShopItem(Material.IRON_INGOT, "§e§lEscudo Protetor",
                        "§7Ativa um escudo protetor que",
                        "§7reduz o dano recebido por 5 segundos.",
                        "ability.shield", 1500, player));

                items.add(createShopItem(Material.ENDER_PEARL, "§e§lTeleporte Rápido",
                        "§7Teleporta você instantaneamente",
                        "§7na direção em que está olhando.",
                        "ability.teleport", 1800, player));

                items.add(createShopItem(Material.STICK, "§e§lBola de Fogo",
                        "§7Lança uma poderosa bola de fogo",
                        "§7que explode ao atingir um alvo.",
                        "ability.fireball", 1600, player));

                break;

            case "titulos":
                title = "§d§lTítulos";
                size = 36;

                items.add(createShopItem(Material.NAME_TAG, "§e§lVencedor",
                        "§7Título exclusivo para",
                        "§7jogadores vitoriosos.",
                        "title.winner", 800, player));

                items.add(createShopItem(Material.IRON_SWORD, "§e§lGuerreiro",
                        "§7Título para jogadores que",
                        "§7amam o combate.",
                        "title.warrior", 800, player));

                items.add(createShopItem(Material.DIAMOND_SWORD, "§e§lAssassino",
                        "§7Título para jogadores com",
                        "§7muitas eliminações.",
                        "title.assassin", 1000, player));

                items.add(createShopItem(Material.DIAMOND, "§e§lLenda",
                        "§7Título para verdadeiras lendas",
                        "§7da Batalha Gravitacional.",
                        "title.legend", 1500, player));

                break;

            case "efeitos_morte":
                title = "§c§lEfeitos de Morte";
                size = 36;

                items.add(createShopItem(Material.TNT, "§e§lExplosão",
                        "§7Causa uma explosão quando",
                        "§7você elimina um jogador.",
                        "death.explosion", 500, player));

                items.add(createShopItem(Material.QUARTZ, "§e§lRaio",
                        "§7Um raio cai quando você",
                        "§7elimina um jogador.",
                        "death.lightning", 500, player));

                items.add(createShopItem(Material.BLAZE_POWDER, "§e§lPartículas",
                        "§7Exibe partículas coloridas",
                        "§7quando você elimina um jogador.",
                        "death.particles", 400, player));

                break;

            case "efeitos_vitoria":
                title = "§6§lEfeitos de Vitória";
                size = 36;

                items.add(createShopItem(Material.STICK, "§e§lFogos de Artifício",
                        "§7Lança fogos de artifício",
                        "§7quando você vence uma partida.",
                        "victory.fireworks", 300, player));

                items.add(createShopItem(Material.SUGAR, "§e§lConfeti",
                        "§7Lança confeti colorido",
                        "§7quando você vence uma partida.",
                        "victory.confetti", 300, player));

                break;

            case "kits":
                title = "§e§lKits";
                size = 36;

                // Kits ainda não disponíveis
                items.add(createItem(Material.BARRIER, "§c§lEm Breve",
                        "§7Os kits estarão disponíveis",
                        "§7em uma atualização futura."));

                break;

            default:
                // Não deveria chegar aqui
                player.sendMessage("§cCategoria inválida!");
                return;
        }

        // Criar o inventário
        Inventory menu = Bukkit.createInventory(null, size, title);

        // Preencher com os itens
        for (int i = 0; i < items.size(); i++) {
            menu.setItem(10 + i * 2, items.get(i));
        }

        // Item para voltar ao menu principal
        ItemStack backItem = createItem(Material.ARROW, "§a§lVoltar",
                "§7Voltar para o menu principal");
        menu.setItem(31, backItem);

        // Abrir o menu
        player.openInventory(menu);
    }

    /**
     * Cria um item de loja com verificação se o jogador já possui
     *
     * @param material Material do item
     * @param name Nome do item
     * @param description1 Primeira linha de descrição
     * @param description2 Segunda linha de descrição
     * @param itemId ID interno do item
     * @param cost Custo em moedas
     * @param player Jogador para verificar se já possui
     * @return ItemStack configurado
     */
    private ItemStack createShopItem(Material material, String name, String description1,
                                     String description2, String itemId, int cost, Player player) {

        boolean hasItem = plugin.getRewardManager().hasItem(player, itemId);
        List<String> lore = new ArrayList<>();

        lore.add("");
        lore.add("§7" + description1);
        lore.add("§7" + description2);
        lore.add("");

        if (hasItem) {
            lore.add("§a§lJÁ ADQUIRIDO");
            lore.add("§7Você já possui este item");
            lore.add("");
            lore.add("§7ID: " + itemId);
        } else {
            lore.add("§6Preço: §e" + cost + " moedas");

            int playerTokens = plugin.getRewardManager().getPlayerTokens(player);
            if (playerTokens >= cost) {
                lore.add("§aVocê tem moedas suficientes!");
                lore.add("§eClique para comprar!");
            } else {
                lore.add("§cVocê não tem moedas suficientes!");
                lore.add("§7Faltam §e" + (cost - playerTokens) + " §7moedas");
            }

            lore.add("");
            lore.add("§7ID: " + itemId);
        }

        return createItem(material, name, lore);
    }

    /**
     * Utilitário para criar um item com meta
     *
     * @param material Material do item
     * @param name Nome do item
     * @param loreLines Linhas de descrição
     * @return ItemStack configurado
     */
    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (loreLines.length > 0) {
            meta.setLore(Arrays.asList(loreLines));
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Utilitário para criar um item com meta e dados
     *
     * @param material Material do item
     * @param name Nome do item
     * @param data Valor de dados/durabilidade
     * @param loreLines Linhas de descrição
     * @return ItemStack configurado
     */
    private ItemStack createItem(Material material, String name, byte data, String... loreLines) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (loreLines.length > 0) {
            meta.setLore(Arrays.asList(loreLines));
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Utilitário para criar um item com meta e lista de descrição
     *
     * @param material Material do item
     * @param name Nome do item
     * @param lore Lista de linhas de descrição
     * @return ItemStack configurado
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }
}