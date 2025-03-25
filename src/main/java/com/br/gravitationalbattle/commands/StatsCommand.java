package com.br.gravitationalbattle.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

public class StatsCommand implements CommandExecutor {

    private final GravitationalBattle plugin;

    public StatsCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.stats")) {
            MessageUtil.sendMessage(sender, "&cYou don't have permission to use this command!");
            return true;
        }

        Player target;

        if (args.length > 0) {
            // Check other player's stats
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

        String targetName = (sender == target) ? "Your" : target.getName() + "'s";

        MessageUtil.sendMessage(sender, "&6===== &e" + targetName + " Stats &6=====");
        MessageUtil.sendMessage(sender, "&eKills: &f" + kills);
        MessageUtil.sendMessage(sender, "&eDeaths: &f" + deaths);
        MessageUtil.sendMessage(sender, "&eK/D Ratio: &f" + (deaths > 0 ? String.format("%.2f", (double)kills/deaths) : kills));
        MessageUtil.sendMessage(sender, "&eWins: &f" + wins);
        MessageUtil.sendMessage(sender, "&eGames Played: &f" + gamesPlayed);
        MessageUtil.sendMessage(sender, "&eWin Rate: &f" + (gamesPlayed > 0 ? String.format("%.2f%%", (double)wins/gamesPlayed*100) : "0.00%"));

        return true;
    }
}