package com.br.gravitationalbattle.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena; // IMPORTAÇÃO CORRIGIDA
import com.br.gravitationalbattle.utils.MessageUtil;


public class ArenaCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;

    public ArenaCommand(GravitationalBattle plugin) {
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

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 3) {
                    MessageUtil.sendMessage(player, "&cUso: /arena create <nome> <displayName>");
                    return true;
                }
                String name = args[1];
                StringBuilder displayNameBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (i > 2) displayNameBuilder.append(" ");
                    displayNameBuilder.append(args[i]);
                }
                String displayName = displayNameBuilder.toString();

                boolean created = plugin.getArenaManager().createArena(name, displayName, player);
                if (created) {
                    MessageUtil.sendMessage(player, "&aArena '" + displayName + "' criada com sucesso!");
                }
                break;

            case "delete":
                if (args.length < 2) {
                    MessageUtil.sendMessage(player, "&cUso: /arena delete <nome>");
                    return true;
                }
                String arenaToDelete = args[1];
                boolean deleted = plugin.getArenaManager().deleteArena(arenaToDelete, player);
                if (deleted) {
                    MessageUtil.sendMessage(player, "&aArena removida com sucesso!");
                }
                break;

// Dentro do case "list":
            case "list":
                MessageUtil.sendMessage(player, "&6===== &eArenas Disponíveis &6=====");
                for (Arena arena : plugin.getArenaManager().getAllArenas()) {
                    String status = arena.isEnabled() ? "DISPONÍVEL" : "MANUTENÇÃO";
                    String statusColor = arena.isEnabled() ? "&a" : "&c";
                    MessageUtil.sendMessage(player, "&e" + arena.getName() + " &7- &f" + arena.getDisplayName()
                            + " &7(" + statusColor + status + "&7) &7- &fSpawns: " + arena.getSpawnPointCount());
                }
                break;

            case "addspawn":
                if (args.length < 2) {
                    MessageUtil.sendMessage(player, "&cUso: /arena addspawn <nome>");
                    return true;
                }
                String arenaName = args[1];
                boolean added = plugin.getArenaManager().addSpawnPoint(arenaName, player);
                if (added) {
                    MessageUtil.sendMessage(player, "&aPonto de spawn adicionado com sucesso para a arena &e" + arenaName + "&a!");
                }
                break;

            case "tp":
                if (args.length < 2) {
                    MessageUtil.sendMessage(player, "&cUso: /arena tp <nome>");
                    return true;
                }
                Arena arenaToTp = plugin.getArenaManager().getArena(args[1]);
                if (arenaToTp == null) {
                    MessageUtil.sendMessage(player, "&cArena não encontrada.");
                    return true;
                }

                player.teleport(arenaToTp.getLobbyLocation());
                MessageUtil.sendMessage(player, "&aTeleportado para a arena &e" + arenaToTp.getName() + "&a.");
                break;

// Dentro do case "setstate":
            case "setstate":
                if (args.length < 3) {
                    MessageUtil.sendMessage(player, "&cUso: /arena setstate <nome> <estado>");
                    MessageUtil.sendMessage(player, "&cEstados válidos: ENABLED, DISABLED");
                    return true;
                }
                Arena arenaToSetState = plugin.getArenaManager().getArena(args[1]);
                if (arenaToSetState == null) {
                    MessageUtil.sendMessage(player, "&cArena não encontrada.");
                    return true;
                }

                String state = args[2].toUpperCase();
                if (state.equals("ENABLED")) {
                    arenaToSetState.setEnabled(true);
                    MessageUtil.sendMessage(player, "&aEstado da arena alterado para &eENABLED&a.");
                } else if (state.equals("DISABLED")) {
                    arenaToSetState.setEnabled(false);
                    MessageUtil.sendMessage(player, "&aEstado da arena alterado para &cDISABLED&a.");
                } else {
                    MessageUtil.sendMessage(player, "&cEstado inválido. Use ENABLED ou DISABLED.");
                }
                break;

            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        MessageUtil.sendMessage(player, "&6===== &eComandos da Arena &6=====");
        MessageUtil.sendMessage(player, "&e/arena create <nome> <displayName> &7- Cria uma nova arena");
        MessageUtil.sendMessage(player, "&e/arena delete <nome> &7- Remove uma arena");
        MessageUtil.sendMessage(player, "&e/arena list &7- Lista todas as arenas");
        MessageUtil.sendMessage(player, "&e/arena addspawn <nome> &7- Adiciona um ponto de spawn na sua localização");
        MessageUtil.sendMessage(player, "&e/arena tp <nome> &7- Teleporta para a arena");
        MessageUtil.sendMessage(player, "&e/arena setstate <nome> <estado> &7- Define o estado da arena");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("delete");
            completions.add("list");
            completions.add("addspawn");
            completions.add("tp");
            completions.add("setstate");
            return filterCompletions(completions, args[0]);
        } else if (args.length == 2) {
            if (!args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("list")) {
                for (Arena arena : plugin.getArenaManager().getAllArenas()) {
                    completions.add(arena.getName());
                }
                return filterCompletions(completions, args[1]);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setstate")) {
                completions.add("ENABLED");
                completions.add("DISABLED");
                return filterCompletions(completions, args[2]);
            }
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