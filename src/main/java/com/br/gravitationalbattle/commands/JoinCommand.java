package com.br.gravitationalbattle.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

public class JoinCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;

    public JoinCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            MessageUtil.sendMessage(player, "&cUsage: /join <arena> or /join random");
            return true;
        }

        String arenaName = args[0];

        // Check for "random" parameter
        if (arenaName.equalsIgnoreCase("random")) {
            return plugin.getPlayerCommands().joinRandomGame(sender);
        }

        // Check if arena exists
        if (!plugin.getGameManager().arenaExists(arenaName)) {
            MessageUtil.sendMessage(player, "&cArena '" + arenaName + "' does not exist!");
            return true;
        }

        // Try to join the arena
        boolean joined = plugin.getGameManager().joinGame(player, arenaName);

        if (!joined) {
            MessageUtil.sendMessage(player, "&cFailed to join arena '" + arenaName + "'. It may be full or in progress.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Add "random" option
            completions.add("random");

            // Add available arenas
            completions.addAll(plugin.getGameManager().getAvailableArenas());

            // Filter by current input
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