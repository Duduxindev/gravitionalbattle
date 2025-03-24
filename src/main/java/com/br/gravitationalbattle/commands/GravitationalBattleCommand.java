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

public class GravitationalBattleCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;

    public GravitationalBattleCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendMessage(sender, "&6===== &eGravitational Battle &6=====");
            MessageUtil.sendMessage(sender, "&7Plugin desenvolvido por &bDuduxindev");
            MessageUtil.sendMessage(sender, "&7Versão: &a" + plugin.getDescription().getVersion());
            MessageUtil.sendMessage(sender, "&7Comandos disponíveis: &e/gb help");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMessage(sender);
                break;

            case "reload":
                if (!sender.hasPermission("gravitationalbattle.admin")) {
                    MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
                    return true;
                }

                // Reload the plugin configuration
                plugin.getConfigManager().loadConfig();
                MessageUtil.sendMessage(sender, "&aConfigurações recarregadas com sucesso!");
                break;

            case "admin":
                if (!sender.hasPermission("gravitationalbattle.admin")) {
                    MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
                    return true;
                }

                if (args.length < 2) {
                    MessageUtil.sendMessage(sender, "&cUso: /gb admin [forcestart|forceend|reload]");
                    return true;
                }

                AdminCommands adminCmds = new AdminCommands(plugin);
                String adminSubCmd = args[1].toLowerCase();
                String[] subArgs = new String[args.length - 2];
                System.arraycopy(args, 2, subArgs, 0, args.length - 2);

                switch (adminSubCmd) {
                    case "forcestart":
                        return adminCmds.forceStart(sender, subArgs);
                    case "forceend":
                        return adminCmds.forceEnd(sender, subArgs);
                    case "reload":
                        return adminCmds.reloadConfig(sender);
                    case "setlobby":
                        return adminCmds.setMainLobby(sender);
                    default:
                        MessageUtil.sendMessage(sender, "&cComando administrativo desconhecido.");
                        return false;
                }

            default:
                MessageUtil.sendMessage(sender, "&cComando desconhecido. Use &e/gb help &cpara ver a lista de comandos.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        MessageUtil.sendMessage(sender, "&6===== &eComandos do Gravitational Battle &6=====");
        MessageUtil.sendMessage(sender, "&e/gb &7- Mostra informações sobre o plugin");
        MessageUtil.sendMessage(sender, "&e/gb help &7- Mostra esta mensagem de ajuda");

        if (sender.hasPermission("gravitationalbattle.admin")) {
            MessageUtil.sendMessage(sender, "&e/gb reload &7- Recarrega as configurações do plugin");
            MessageUtil.sendMessage(sender, "&e/arena &7- Comandos de gerenciamento de arenas");
            MessageUtil.sendMessage(sender, "&e/setlobby &7- Define o lobby principal");
        }

        MessageUtil.sendMessage(sender, "&e/join [arena] &7- Entra em um jogo");
        MessageUtil.sendMessage(sender, "&e/leave &7- Sai do jogo atual");
        MessageUtil.sendMessage(sender, "&e/stats [jogador] &7- Mostra estatísticas");
        MessageUtil.sendMessage(sender, "&e/spectate <arena|jogador> &7- Assiste a um jogo");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("help");
            if (sender.hasPermission("gravitationalbattle.admin")) {
                completions.add("reload");
                completions.add("admin");
            }
            return filterCompletions(completions, args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            if (sender.hasPermission("gravitationalbattle.admin")) {
                completions.add("forcestart");
                completions.add("forceend");
                completions.add("reload");
                completions.add("setlobby");
            }
            return filterCompletions(completions, args[1]);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin") &&
                (args[1].equalsIgnoreCase("forcestart") || args[1].equalsIgnoreCase("forceend"))) {
            // Adicionar nomes das arenas disponíveis
            for (String arena : plugin.getArenaManager().getArenaNames()) {
                completions.add(arena);
            }
            return filterCompletions(completions, args[2]);
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