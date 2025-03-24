package com.br.gravitationalbattle.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Menu para seleção de mapas/arenas
 */
public class MapSelectorMenu extends Menu {

    private static final int MENU_SIZE = 54; // 6 linhas

    public MapSelectorMenu(GravitationalBattle plugin) {
        this(plugin, null);
    }

    public MapSelectorMenu(GravitationalBattle plugin, Player owner) {
        super(plugin, owner != null ? owner : plugin.getServer().getOnlinePlayers().iterator().next(),
                "&6&lSelecionar Arena", MENU_SIZE);
        initializeItems();
    }

    @Override
    public void open(Player player) {
        // Atualiza os itens antes de abrir
        initializeItems();
        super.open(player);
    }

    @Override
    public void initializeItems() {
        // Limpa o inventário
        inventory.clear();
        clickHandlers.clear();

        // Adiciona arenas disponíveis
        List<Arena> arenas = new ArrayList<>(plugin.getArenaManager().getAllArenas());
        int slot = 0;

        for (Arena arena : arenas) {
            if (slot >= 45) break; // Limita a 5 linhas de itens

            // Pula arenas em manutenção
            if (arena.getState() == GameState.MAINTENANCE) continue;

            // Verifica se a arena tem um jogo ativo
            Game game = plugin.getArenaManager().getGame(arena.getName());
            int playerCount = 0;
            GameState gameState = GameState.AVAILABLE;

            if (game != null) {
                playerCount = game.getPlayers().size();
                gameState = game.getState();
            }

            // Cria o item
            Material material = Material.GRASS_BLOCK;
            if (gameState == GameState.INGAME) {
                material = Material.REDSTONE_BLOCK;
            } else if (gameState == GameState.COUNTDOWN || gameState == GameState.WAITING) {
                material = Material.EMERALD_BLOCK;
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(MessageUtil.colorize("&e" + arena.getDisplayName()));

            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.colorize("&7Spawns: &f" + arena.getSpawnPointCount()));

            if (game != null) {
                lore.add(MessageUtil.colorize("&7Estado: &f" + getStateDisplayName(gameState)));
                lore.add(MessageUtil.colorize("&7Jogadores: &f" + playerCount + "/" +
                        plugin.getConfigManager().getMaxPlayers()));

                if (gameState == GameState.COUNTDOWN) {
                    lore.add(MessageUtil.colorize("&7Iniciando em: &f" + game.getCountdown() + "s"));
                } else if (gameState == GameState.INGAME) {
                    lore.add(MessageUtil.colorize("&7Tempo restante: &f" + formatTime(game.getGameTime())));
                    lore.add(MessageUtil.colorize("&cJogo em andamento"));
                }
            } else {
                lore.add(MessageUtil.colorize("&7Estado: &fDisponível"));
                lore.add(MessageUtil.colorize("&7Jogadores: &f0/" +
                        plugin.getConfigManager().getMaxPlayers()));
                lore.add(MessageUtil.colorize("&aClique para jogar!"));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            // Adiciona o item ao menu
            final String arenaName = arena.getName();
            setItem(slot, item, () -> {
                Player player = plugin.getServer().getPlayer(ownerUUID);
                if (player != null && player.isOnline()) {
                    player.closeInventory();
                    plugin.getGameManager().joinGame(player, arenaName);
                }
            });

            slot++;
        }

        // Item para fechar o menu
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(MessageUtil.colorize("&c&lFechar"));
        closeItem.setItemMeta(closeMeta);

        setItem(49, closeItem, () -> {
            Player player = plugin.getServer().getPlayer(ownerUUID);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
        });
    }

    /**
     * Formata um tempo em segundos para minutos:segundos
     *
     * @param seconds Tempo em segundos
     * @return Tempo formatado
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /**
     * Obtém o nome de exibição para um estado de jogo
     *
     * @param state Estado do jogo
     * @return Nome de exibição
     */
    private String getStateDisplayName(GameState state) {
        switch (state) {
            case AVAILABLE:
                return "Disponível";
            case MAINTENANCE:
                return "Manutenção";
            case WAITING:
                return "Aguardando";
            case COUNTDOWN:
                return "Iniciando";
            case INGAME:
                return "Em jogo";
            case ENDING:
                return "Finalizando";
            default:
                return "Desconhecido";
        }
    }
}