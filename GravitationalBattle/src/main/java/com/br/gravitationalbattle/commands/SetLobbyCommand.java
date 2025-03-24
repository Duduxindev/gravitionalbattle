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
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.admin")) {
            MessageUtil.sendMessage(player, "&cVocê não tem permissão para usar este comando.");
            return true;
        }

        // Set lobby location to the player's current location
        plugin.getConfigManager().setLobbyLocation(player.getLocation());

        MessageUtil.sendMessage(player, "&aLobby definido com sucesso na sua localização atual!");
        return true;
    }
}