package com.br.gravitationalbattle.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

public class JoinCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;

    public JoinCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.join")) {
            MessageUtil.sendMessage(player, "&cVocê não tem permissão para usar este comando.");
            return true;
        }

        String arenaName = null;
        if (args.length > 0) {
            arenaName = args[0];
        }

        Game game = plugin.getGameManager().joinGame(player, arenaName);
        if (game == null) {
            // Error message is sent by the GameManager
            return true;
        }

        // Success message handled by the Game class when adding player
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        if (!sender.hasPermission("gravitationalbattle.join")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // List available arenas
            for (Arena arena : plugin.getArenaManager().getAllArenas()) {
                if (arena.isAvailable()) {
                    completions.add(arena.getName());
                }
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