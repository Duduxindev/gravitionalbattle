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

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;
    private final AdminCommands adminCommands;

    public AdminCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.adminCommands = new AdminCommands(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.admin")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sendAdminHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);

        switch (subCommand) {
            case "forcestart":
                return adminCommands.forceStart(sender, subArgs);
            case "forceend":
                return adminCommands.forceEnd(sender, subArgs);
            case "reload":
                return adminCommands.reloadConfig(sender);
            case "setlobby":
                return adminCommands.setMainLobby(sender);
            case "help":
                sendAdminHelpMessage(sender);
                return true;
            default:
                MessageUtil.sendMessage(sender, "&cComando administrativo desconhecido.");
                sendAdminHelpMessage(sender);
                return false;
        }
    }

    private void sendAdminHelpMessage(CommandSender sender) {
        MessageUtil.sendMessage(sender, "&6===== &eComandos Administrativos &6=====");
        MessageUtil.sendMessage(sender, "&e/admin forcestart <arena> &7- Força o início de um jogo");
        MessageUtil.sendMessage(sender, "&e/admin forceend <arena> &7- Força o fim de um jogo");
        MessageUtil.sendMessage(sender, "&e/admin reload &7- Recarrega as configurações");
        MessageUtil.sendMessage(sender, "&e/admin setlobby &7- Define o lobby principal");
        MessageUtil.sendMessage(sender, "&e/admin help &7- Mostra esta mensagem de ajuda");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("gravitationalbattle.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.add("forcestart");
            completions.add("forceend");
            completions.add("reload");
            completions.add("setlobby");
            completions.add("help");
            return filterCompletions(completions, args[0]);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("forcestart") || args[0].equalsIgnoreCase("forceend"))) {
            // Adicionar nomes das arenas disponíveis
            for (String arena : plugin.getArenaManager().getArenaNames()) {
                completions.add(arena);
            }
            return filterCompletions(completions, args[1]);
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