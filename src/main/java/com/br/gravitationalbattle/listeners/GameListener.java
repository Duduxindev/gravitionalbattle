package com.br.gravitationalbattle.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Listener for game-related events
 */
public class GameListener implements Listener {

    private final GravitationalBattle plugin;

    public GameListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null) {
            // Cancel death message
            event.setDeathMessage(null);

            // Mark player as dead in the game
            game.playerDied(player);

            // Update killer's stats if applicable
            Player killer = player.getKiller();
            if (killer != null) {
                plugin.getStatsManager().addKill(killer);
                game.recordKill(killer);
            }

            // Update death stats
            plugin.getStatsManager().addDeath(player);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null) {
            // Only allow damage if game is in progress
            if (game.getState() != com.br.gravitationalbattle.game.GameState.INGAME) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        // Handle NPC damage
        if (event.getEntity().hasMetadata("NPC")) {
            event.setCancelled(true);
            return;
        }

        // Handle player damage
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player target = (Player) event.getEntity();

            Game damagerGame = plugin.getGameManager().getPlayerGame(damager);
            Game targetGame = plugin.getGameManager().getPlayerGame(target);

            // If both in same game and game is in progress, allow PVP
            if (damagerGame != null && damagerGame == targetGame &&
                    damagerGame.getState() == com.br.gravitationalbattle.game.GameState.INGAME) {
                // Allow damage - PvP is enabled
            } else {
                // Cancel damage
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isPlayerInGame(player)) {
            // Prevent block breaking in games
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().isPlayerInGame(player)) {
            // Prevent block placing in games
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove player from any game they're in
        if (plugin.getGameManager().isPlayerInGame(player)) {
            plugin.getGameManager().leaveGame(player);
        }

        // Clean up scoreboard
        plugin.getScoreboardManager().removePlayer(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null) {
            // Check if player fell out of the world
            if (event.getTo().getY() < 0) {
                player.damage(1000.0); // Kill player
                MessageUtil.broadcastMessage("&c" + player.getName() + " &7fell out of the world!");
            }

            // Only restrict movement in waiting/starting states
            if ((game.getState() == com.br.gravitationalbattle.game.GameState.WAITING ||
                    game.getState() == com.br.gravitationalbattle.game.GameState.STARTING) &&
                    (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                            event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {

                // Cancel horizontal movement in pre-game
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null) {
            // Cancel the regular chat event
            event.setCancelled(true);

            try {
                // Use game-specific chat
                plugin.getGameManager().sendGameChatMessage(game, player, event.getMessage());
            } catch (Exception e) {
                plugin.getLogger().severe("Error processing game chat: " + e.getMessage());
                e.printStackTrace();
                // Send a message to the player that their chat message failed
                MessageUtil.sendMessage(player, "&cError sending chat message. Please try again!");
            }
        }
    }
}