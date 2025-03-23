package com.br.gravitationalbattle.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;

public class MessageUtil {

    private static GravitationalBattle plugin = GravitationalBattle.getInstance();

    /**
     * Sends a colored message to a command sender
     *
     * @param sender The recipient of the message
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Broadcasts a message to all online players
     *
     * @param message The message to broadcast
     */
    public static void broadcastMessage(String message) {
        Bukkit.broadcastMessage(colorize(message));
    }

    /**
     * Sends a configured message to a command sender with placeholders
     *
     * @param sender The recipient of the message
     * @param key The message key in the config
     * @param placeholders Placeholder replacements in pairs (placeholder, value)
     */
    public static void sendMessage(CommandSender sender, String key, String... placeholders) {
        String message = plugin.getConfigManager().getMessage(key);

        // Replace plugin-specific placeholders
        message = message.replace("%prefix%", plugin.getConfigManager().getMessage("prefix"));

        // Replace custom placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "%" + placeholders[i] + "%";
                String value = placeholders[i + 1];
                message = message.replace(placeholder, value);
            }
        }

        sender.sendMessage(colorize(message));
    }

    /**
     * Broadcasts a configured message to all online players with placeholders
     *
     * @param key The message key in the config
     * @param placeholders Placeholder replacements in pairs (placeholder, value)
     */
    public static void broadcastMessage(String key, String... placeholders) {
        String message = plugin.getConfigManager().getMessage(key);

        // Replace plugin-specific placeholders
        message = message.replace("%prefix%", plugin.getConfigManager().getMessage("prefix"));

        // Replace custom placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "%" + placeholders[i] + "%";
                String value = placeholders[i + 1];
                message = message.replace(placeholder, value);
            }
        }

        Bukkit.broadcastMessage(colorize(message));
    }

    /**
     * Broadcasts a message to all players in a game
     *
     * @param game The game to broadcast to
     * @param key The message key in the config
     * @param placeholders Placeholder replacements in pairs
     */
    public static void broadcastMessageToGame(Game game, String key, String... placeholders) {
        String message = plugin.getConfigManager().getMessage(key);

        // Replace plugin-specific placeholders
        message = message.replace("%prefix%", plugin.getConfigManager().getMessage("prefix"));

        // Replace custom placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "%" + placeholders[i] + "%";
                String value = placeholders[i + 1];
                message = message.replace(placeholder, value);
            }
        }

        String finalMessage = colorize(message);

        for (UUID uuid : game.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(finalMessage);
            }
        }
    }

    /**
     * Sends a title and subtitle to a player
     *
     * @param player The recipient of the title
     * @param title The title to display
     * @param subtitle The subtitle to display
     * @param fadeIn Fade in time in ticks
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
    }

    /**
     * Sends a title and subtitle to a player with default timings
     *
     * @param player The recipient of the title
     * @param title The title to display
     * @param subtitle The subtitle to display
     */
    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    /**
     * Sends an action bar message to a player
     *
     * @param player The recipient of the action bar
     * @param message The message to display
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(colorize(message)));
    }

    /**
     * Colorizes a string with color codes
     *
     * @param message The string to colorize
     * @return The colorized string
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}