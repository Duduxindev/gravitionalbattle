package com.br.gravitationalbattle.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

import java.util.UUID;

/**
 * Commands available for moderators
 */
public class ModeratorCommands {

    private final GravitationalBattle plugin;

    public ModeratorCommands(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleports a player to an active game
     *
     * @param sender Command sender
     * @param args Command arguments
     * @return true if command executed successfully
     */
    public boolean teleportToGame(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.mod.teleport")) {
            MessageUtil.sendMessage(player, "&cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(player, "&cUsage: /gbmod teleport <arena>");
            return true;
        }

        String arenaName = args[0];
        Game game = plugin.getArenaManager().getGame(arenaName);

        if (game == null) {
            MessageUtil.sendMessage(player, "&cNo active game found in arena '" + arenaName + "'!");
            return true;
        }

        // Teleport to first player in game or to arena spawn
        boolean teleported = false;
        for (UUID playerId : game.getPlayers()) {
            Player gamePlayer = Bukkit.getPlayer(playerId); // Corrigido aqui usando Bukkit.getPlayer(UUID)
            if (gamePlayer != null) {
                player.teleport(gamePlayer.getLocation());
                teleported = true;
                break;
            }
        }

        if (!teleported) {
            // Teleport to arena spawn
            player.teleport(game.getArena().getSpawnPoints().get(0));
        }

        MessageUtil.sendMessage(player, "&aTeleported to game in arena &e" + arenaName + "&a.");
        return true;
    }

    /**
     * Kicks a player from their current game
     *
     * @param sender Command sender
     * @param args Command arguments
     * @return true if command executed successfully
     */
    public boolean kickPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.mod.kick")) {
            MessageUtil.sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(sender, "&cUsage: /gbmod kick <player> [reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(sender, "&cPlayer not found!");
            return true;
        }

        Game game = plugin.getGameManager().getPlayerGame(target);
        if (game == null) {
            MessageUtil.sendMessage(sender, "&cThat player is not in a game!");
            return true;
        }

        // Get reason if provided
        String reason = "No reason provided.";
        if (args.length > 1) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) reasonBuilder.append(" ");
                reasonBuilder.append(args[i]);
            }
            reason = reasonBuilder.toString();
        }

        // Kick player
        boolean kicked = plugin.getGameManager().leaveGame(target);

        if (kicked) {
            MessageUtil.sendMessage(target, "&cYou were kicked from the game. Reason: &7" + reason);
            MessageUtil.sendMessage(sender, "&aSuccessfully kicked &e" + target.getName() + " &afrom their game!");
        } else {
            MessageUtil.sendMessage(sender, "&cFailed to kick player from game!");
        }

        return true;
    }

    /**
     * Checks the status of all active games
     *
     * @param sender Command sender
     * @return true if command executed successfully
     */
    public boolean checkGames(CommandSender sender) {
        if (!sender.hasPermission("gravitationalbattle.mod.check")) {
            MessageUtil.sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        MessageUtil.sendMessage(sender, "&6===== &eActive Games &6=====");
        int gameCount = 0;

        for (Game game : plugin.getArenaManager().getActiveGames()) {
            gameCount++;
            MessageUtil.sendMessage(sender, "&eArena: &f" + game.getArena().getDisplayName() +
                    " &7| &eState: &f" + game.getState() +
                    " &7| &ePlayers: &f" + game.getPlayerCount() + "/" + game.getArena().getMaxPlayers() +
                    " &7| &eAlive: &f" + game.getAliveCount());

            // List players in this game
            StringBuilder players = new StringBuilder("&7Players: ");
            for (UUID playerId : game.getPlayers()) {
                Player gamePlayer = Bukkit.getPlayer(playerId); // Corrigido aqui usando Bukkit.getPlayer(UUID)
                if (gamePlayer != null) {
                    players.append("&f").append(gamePlayer.getName()).append("&7, ");
                }
            }

            // Remove trailing comma and space
            if (players.length() > 11) {
                players.setLength(players.length() - 2);
            }

            MessageUtil.sendMessage(sender, players.toString());
        }

        if (gameCount == 0) {
            MessageUtil.sendMessage(sender, "&7No active games found.");
        }

        MessageUtil.sendMessage(sender, "&6==========================");

        return true;
    }
}