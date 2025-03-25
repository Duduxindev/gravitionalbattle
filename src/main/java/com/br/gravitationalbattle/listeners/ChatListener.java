package com.br.gravitationalbattle.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;

/**
 * Listener for game chat events
 */
public class ChatListener implements Listener {

    private final GravitationalBattle plugin;

    public ChatListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        // If player is in a game, send chat only to players in same game
        if (game != null) {
            // Cancel the original event
            event.setCancelled(true);

            // Create a game-specific chat format
            String gamePrefix = ChatColor.GOLD + "[Game] ";
            String playerName = (game.isSpectator(player) ?
                    ChatColor.GRAY + "[Spectator] " : "") +
                    player.getDisplayName();
            String message = event.getMessage();

            // Format the message
            String formattedMessage = gamePrefix + playerName + ChatColor.WHITE + ": " + message;

            // Send to all players in the game
            game.broadcastMessage(formattedMessage);
        }
        // Otherwise, let the regular chat system handle it
    }
}