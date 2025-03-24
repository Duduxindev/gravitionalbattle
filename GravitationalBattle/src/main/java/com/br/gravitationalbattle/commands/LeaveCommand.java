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
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        boolean left = plugin.getGameManager().leaveGame(player);
        if (!left) {
            // Error message is sent by the GameManager
            return true;
        }

        // Success message handled by the Game class when removing player
        return true;
    }
}