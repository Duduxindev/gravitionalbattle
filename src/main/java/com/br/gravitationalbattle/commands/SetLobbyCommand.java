package com.br.gravitationalbattle.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

public class SetLobbyCommand implements CommandExecutor {

    private final GravitationalBattle plugin;

    public SetLobbyCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.admin.setlobby")) {
            MessageUtil.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }

        plugin.getConfigManager().setLobbyLocation(player.getLocation());
        MessageUtil.sendMessage(player, "&aLobby location has been set to your current position!");

        return true;
    }
}