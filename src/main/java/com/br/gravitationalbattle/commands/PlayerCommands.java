package com.br.gravitationalbattle.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

import java.util.UUID;

/**
 * Player-focused commands
 */
public class PlayerCommands {

    private final GravitationalBattle plugin;

    public PlayerCommands(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    /**
     * Allows a player to join a random game
     *
     * @param sender Command sender
     * @return true if command executed successfully
     */
    public boolean joinRandomGame(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        boolean success = plugin.getGameManager().joinRandomGame(player);
        if (!success) {
            MessageUtil.sendMessage(player, "&cCould not find any available games to join.");
        }

        return true;
    }

    /**
     * Shows a player's stats
     *
     * @param sender Command sender
     * @param args Command arguments
     * @return true if command executed successfully
     */
    public boolean showStats(CommandSender sender, String[] args) {
        Player target;

        if (args.length > 0) {
            // Check stats of specified player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtil.sendMessage(sender, "&cPlayer not found!");
                return true;
            }
        } else if (sender instanceof Player) {
            // Check own stats
            target = (Player) sender;
        } else {
            MessageUtil.sendMessage(sender, "&cUsage: /stats <player>");
            return true;
        }

        int kills = plugin.getStatsManager().getPlayerKills(target);
        int deaths = plugin.getStatsManager().getPlayerDeaths(target);
        int wins = plugin.getStatsManager().getPlayerWins(target);
        int gamesPlayed = plugin.getStatsManager().getPlayerGamesPlayed(target);

        String targetName = sender == target ? "Your" : target.getName() + "'s";

        MessageUtil.sendMessage(sender, "&6===== &e" + targetName + " Stats &6=====");
        MessageUtil.sendMessage(sender, "&eKills: &f" + kills);
        MessageUtil.sendMessage(sender, "&eDeaths: &f" + deaths);
        MessageUtil.sendMessage(sender, "&eK/D Ratio: &f" + (deaths > 0 ? String.format("%.2f", (double) kills / deaths) : kills));
        MessageUtil.sendMessage(sender, "&eWins: &f" + wins);
        MessageUtil.sendMessage(sender, "&eGames Played: &f" + gamesPlayed);
        MessageUtil.sendMessage(sender, "&eWin Rate: &f" + (gamesPlayed > 0 ? String.format("%.2f%%", (double) wins / gamesPlayed * 100) : "0.00%"));

        return true;
    }

    /**
     * Lists all active games
     *
     * @param sender Command sender
     * @return true if command executed successfully
     */
    public boolean listGames(CommandSender sender) {
        MessageUtil.sendMessage(sender, "&6===== &eActive Games &6=====");
        int gameCount = 0;

        for (Game game : plugin.getArenaManager().getActiveGames()) {
            gameCount++;
            String state = game.getState().toString();
            String stateColor = "&a"; // Default green

            // Colorize status
            switch (game.getState()) {
                case INGAME:
                    stateColor = "&c"; // Red for in-progress
                    break;
                case STARTING:
                case COUNTDOWN:
                    stateColor = "&e"; // Yellow for starting
                    break;
                case WAITING:
                    stateColor = "&a"; // Green for waiting
                    break;
                default:
                    break;
            }

            MessageUtil.sendMessage(sender, "&e" + game.getArena().getDisplayName() +
                    " &7| &eStatus: " + stateColor + state +
                    " &7| &ePlayers: &f" + game.getPlayerCount() + "/" + game.getArena().getMaxPlayers());
        }

        if (gameCount == 0) {
            MessageUtil.sendMessage(sender, "&7No active games found.");
        }

        MessageUtil.sendMessage(sender, "&6==========================");
        return true;
    }
}