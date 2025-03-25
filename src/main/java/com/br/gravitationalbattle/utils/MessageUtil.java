package com.br.gravitationalbattle.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility class for handling messages with color codes
 */
public class MessageUtil {

    /**
     * Sends a colored message to a command sender
     *
     * @param sender The command sender
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Sends a colored message to a player
     *
     * @param player The player
     * @param message The message to send
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(colorize(message));
    }

    /**
     * Sends a colored title to a player
     *
     * @param player The player
     * @param title The title text
     * @param subtitle The subtitle text
     */
    public static void sendTitle(Player player, String title, String subtitle) {
        // In 1.8.8 we need to use the player connection directly or a plugin for titles
        // This is a simple implementation - you might want to use a title API plugin
        player.sendMessage(colorize("&l" + title));
        if (subtitle != null && !subtitle.isEmpty()) {
            player.sendMessage(colorize(subtitle));
        }
    }

    /**
     * Broadcasts a colored message to all players
     *
     * @param message The message to broadcast
     */
    public static void broadcastMessage(String message) {
        org.bukkit.Bukkit.broadcastMessage(colorize(message));
    }

    /**
     * Converts color codes in a string
     *
     * @param text The text to colorize
     * @return Colorized text
     */
    public static String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Strips color codes from a string
     *
     * @param text The text to strip colors from
     * @return Text without color codes
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(text);
    }
}