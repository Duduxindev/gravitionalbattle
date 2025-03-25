package com.br.gravitationalbattle.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Listener for NPC-related events
 */
public class NPCListener implements Listener {

    private final GravitationalBattle plugin;

    public NPCListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (plugin.getNPCManager().isNPC(entity)) {
            String npcId = plugin.getNPCManager().getNPCId(entity);

            if (npcId != null) {
                handleNPCInteraction(player, npcId);
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles player interaction with an NPC
     *
     * @param player The player
     * @param npcId The NPC ID
     */
    private void handleNPCInteraction(Player player, String npcId) {
        switch (npcId) {
            case "join":
                // Show arena selection menu
                MessageUtil.sendMessage(player, "&eOpening arena selection...");
                showArenaSelectionMenu(player);
                break;

            case "stats":
                // Show player stats
                showPlayerStats(player);
                break;

            default:
                MessageUtil.sendMessage(player, "&eInteracted with NPC: &a" + npcId);
                break;
        }
    }

    /**
     * Shows the arena selection menu
     *
     * @param player The player
     */
    private void showArenaSelectionMenu(Player player) {
        // In 1.8.8, you might want to use a custom inventory menu
        // For now, just list arenas in chat
        MessageUtil.sendMessage(player, "&6===== &eAvailable Arenas &6=====");

        for (String arenaName : plugin.getGameManager().getAvailableArenas()) {
            int players = plugin.getGameManager().getPlayersInArena(arenaName);
            int maxPlayers = plugin.getGameManager().getMaxPlayersInArena(arenaName);
            String state = plugin.getGameManager().getArenaState(arenaName);

            MessageUtil.sendMessage(player, "&a" + arenaName + " &7- &fPlayers: &e" +
                    players + "/" + maxPlayers + " &7- &fStatus: &e" + state);
        }

        MessageUtil.sendMessage(player, "&eUse &a/join <arena> &eto join a game.");
    }

    /**
     * Shows player stats
     *
     * @param player The player
     */
    private void showPlayerStats(Player player) {
        int kills = plugin.getStatsManager().getPlayerKills(player);
        int deaths = plugin.getStatsManager().getPlayerDeaths(player);
        int wins = plugin.getStatsManager().getPlayerWins(player);
        int gamesPlayed = plugin.getStatsManager().getPlayerGamesPlayed(player);

        MessageUtil.sendMessage(player, "&6===== &eYour Stats &6=====");
        MessageUtil.sendMessage(player, "&eKills: &f" + kills);
        MessageUtil.sendMessage(player, "&eDeaths: &f" + deaths);
        MessageUtil.sendMessage(player, "&eK/D Ratio: &f" + (deaths > 0 ? String.format("%.2f", (double)kills/deaths) : kills));
        MessageUtil.sendMessage(player, "&eWins: &f" + wins);
        MessageUtil.sendMessage(player, "&eGames Played: &f" + gamesPlayed);
        MessageUtil.sendMessage(player, "&eWin Rate: &f" + (gamesPlayed > 0 ? String.format("%.2f%%", (double)wins/gamesPlayed*100) : "0.00%"));
    }
}