package com.br.gravitationalbattle.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

public class LeaveCommand implements CommandExecutor {

    private final GravitationalBattle plugin;

    public LeaveCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.leave")) {
            MessageUtil.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }

        if (!plugin.getGameManager().isPlayerInGame(player)) {
            MessageUtil.sendMessage(player, "&cYou are not in a game!");
            return true;
        }

        boolean left = plugin.getGameManager().leaveGame(player);

        if (left) {
            MessageUtil.sendMessage(player, "&aYou have left the game.");
        } else {
            MessageUtil.sendMessage(player, "&cCouldn't leave the game for some reason.");
        }

        return true;
    }
}