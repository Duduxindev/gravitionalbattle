package com.br.gravitationalbattle.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

public class SpectateCommand implements CommandExecutor {

    private final GravitationalBattle plugin;

    public SpectateCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.spectate")) {
            MessageUtil.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendMessage(player, "&cUsage: /spectate <arena|player>");
            return true;
        }

        String target = args[0];

        // Check if player is already in a game
        if (plugin.getGameManager().isPlayerInGame(player)) {
            MessageUtil.sendMessage(player, "&cYou must leave your current game before spectating another! Use /leave first.");
            return true;
        }

        // Try to find arena with that name
        Game game = plugin.getArenaManager().getGame(target);

        // If not found, try to find player with that name
        if (game == null) {
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null) {
                game = plugin.getGameManager().getPlayerGame(targetPlayer);
            }
        }

        // If still not found or game not in progress
        if (game == null) {
            MessageUtil.sendMessage(player, "&cCouldn't find an active game for '" + target + "'!");
            return true;
        }

        if (game.getState() != com.br.gravitationalbattle.game.GameState.INGAME) {
            MessageUtil.sendMessage(player, "&cThis game is not in progress yet!");
            return true;
        }

        // Add player as spectator
        boolean added = game.addSpectator(player);

        if (!added) {
            MessageUtil.sendMessage(player, "&cCouldn't add you as a spectator!");
        }

        return true;
    }
}