package com.br.gravitationalbattle.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.managers.StatsManager.PlayerStats;
import com.br.gravitationalbattle.utils.MessageUtil;

public class StatsCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;

    public StatsCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.stats")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return true;
        }

        Player target;

        if (args.length > 0) {
            // Check stats for another player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtil.sendMessage(sender, "&cJogador não encontrado ou offline.");
                return true;
            }
        } else {
            // Check own stats
            if (!(sender instanceof Player)) {
                MessageUtil.sendMessage(sender, "&cUso para console: /stats <jogador>");
                return true;
            }
            target = (Player) sender;
        }

        UUID targetUUID = target.getUniqueId();
        PlayerStats stats = plugin.getStatsManager().getPlayerStats(targetUUID);

        // Calculate KDR (Kill/Death Ratio)
        double kdr = stats.getDeaths() > 0 ? (double) stats.getKills() / stats.getDeaths() : stats.getKills();

        // Format KDR to 2 decimal places
        String formattedKdr = String.format("%.2f", kdr);

        // Calculate win rate
        double winRate = stats.getGamesPlayed() > 0 ?
                (double) stats.getWins() / stats.getGamesPlayed() * 100 : 0;

        // Format win rate to 2 decimal places
        String formattedWinRate = String.format("%.2f", winRate);

        // Display stats
        MessageUtil.sendMessage(sender, "&6===== &eEstatísticas de &f" + target.getName() + " &6=====");
        MessageUtil.sendMessage(sender, "&eVitórias: &f" + stats.getWins());
        MessageUtil.sendMessage(sender, "&eEliminações: &f" + stats.getKills());
        MessageUtil.sendMessage(sender, "&eMortes: &f" + stats.getDeaths());
        MessageUtil.sendMessage(sender, "&eK/D: &f" + formattedKdr);
        MessageUtil.sendMessage(sender, "&ePartidas Jogadas: &f" + stats.getGamesPlayed());
        MessageUtil.sendMessage(sender, "&eTaxa de Vitória: &f" + formattedWinRate + "%");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.stats")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Add online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }

            return filterCompletions(completions, args[0]);
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) return completions;

        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(completion);
            }
        }

        return filtered;
    }
}