package com.br.gravitationalbattle.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.managers.ArenaManager;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Comando para gerenciar arenas do Gravitational Battle
 */
public class ArenaCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;
    private final ArenaManager arenaManager;

    public ArenaCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser executado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.admin")) {
            MessageUtil.sendMessage(player, "&cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    MessageUtil.sendMessage(player, "&cUso correto: /arena create <nome>");
                    return true;
                }
                handleCreateArena(player, args[1]);
                break;

            case "delete":
                if (args.length < 2) {
                    MessageUtil.sendMessage(player, "&cUso correto: /arena delete <nome>");
                    return true;
                }
                handleDeleteArena(player, args[1]);
                break;

            case "list":
                handleListArenas(player);
                break;

            case "setspawn":
                handleSetSpawnPoint(player);
                break;

            case "setlobby":
                handleSetLobby(player);
                break;

            case "info":
                if (args.length < 2) {
                    MessageUtil.sendMessage(player, "&cUso correto: /arena info <nome>");
                    return true;
                }
                handleArenaInfo(player, args[1]);
                break;

            case "setminplayers":
                if (args.length < 3) {
                    MessageUtil.sendMessage(player, "&cUso correto: /arena setminplayers <nome> <quantidade>");
                    return true;
                }
                handleSetMinPlayers(player, args[1], args[2]);
                break;

            case "setmaxplayers":
                if (args.length < 3) {
                    MessageUtil.sendMessage(player, "&cUso correto: /arena setmaxplayers <nome> <quantidade>");
                    return true;
                }
                handleSetMaxPlayers(player, args[1], args[2]);
                break;

            case "setmode":
                if (args.length < 3) {
                    MessageUtil.sendMessage(player, "&cUso correto: /arena setmode <nome> <modo>");
                    return true;
                }
                handleSetGameMode(player, args[1], args[2]);
                break;

            default:
                MessageUtil.sendMessage(player, "&cSubcomando desconhecido. Use /arena para ver os comandos disponíveis.");
                break;
        }

        return true;
    }

    /**
     * Mostra a ajuda do comando arena
     *
     * @param player O jogador
     */
    private void showHelp(Player player) {
        MessageUtil.sendMessage(player, "&6===== &eComandos de Arena &6=====");
        MessageUtil.sendMessage(player, "&e/arena create <nome> &7- Cria uma nova arena");
        MessageUtil.sendMessage(player, "&e/arena delete <nome> &7- Remove uma arena");
        MessageUtil.sendMessage(player, "&e/arena list &7- Lista todas as arenas");
        MessageUtil.sendMessage(player, "&e/arena setspawn &7- Adiciona um ponto de spawn na sua localização");
        MessageUtil.sendMessage(player, "&e/arena setlobby &7- Define o lobby da arena");
        MessageUtil.sendMessage(player, "&e/arena info <nome> &7- Mostra informações de uma arena");
        MessageUtil.sendMessage(player, "&e/arena setminplayers <nome> <quantidade> &7- Define o número mínimo de jogadores");
        MessageUtil.sendMessage(player, "&e/arena setmaxplayers <nome> <quantidade> &7- Define o número máximo de jogadores");
        MessageUtil.sendMessage(player, "&e/arena setmode <nome> <modo> &7- Define o modo de jogo (SOLO, DUOS, SQUADS, etc)");
    }

    /**
     * Manipula o comando para criar uma arena
     *
     * @param player O jogador que executou o comando
     * @param name Nome da arena a ser criada
     */
    private void handleCreateArena(Player player, String name) {
        if (arenaManager.arenaExists(name)) {
            MessageUtil.sendMessage(player, "&cJá existe uma arena com o nome '" + name + "'.");
            return;
        }

        Arena arena = arenaManager.createArena(name, player.getWorld().getUID());

        if (arena != null) {
            MessageUtil.sendMessage(player, "&aArena '" + name + "' criada com sucesso!");
            MessageUtil.sendMessage(player, "&7Use &e/arena setspawn &7para adicionar pontos de spawn.");
        } else {
            MessageUtil.sendMessage(player, "&cOcorreu um erro ao criar a arena.");
        }
    }

    /**
     * Manipula o comando para deletar uma arena
     *
     * @param player O jogador que executou o comando
     * @param name Nome da arena a ser deletada
     */
    private void handleDeleteArena(Player player, String name) {
        if (!arenaManager.arenaExists(name)) {
            MessageUtil.sendMessage(player, "&cNão existe uma arena com o nome '" + name + "'.");
            return;
        }

        arenaManager.deleteArena(name);
        MessageUtil.sendMessage(player, "&aArena '" + name + "' deletada com sucesso!");
    }

    /**
     * Manipula o comando para listar todas as arenas
     *
     * @param player O jogador que executou o comando
     */
    private void handleListArenas(Player player) {
        List<Arena> arenas = arenaManager.getAllArenas();

        if (arenas.isEmpty()) {
            MessageUtil.sendMessage(player, "&cNenhuma arena foi criada ainda.");
            return;
        }

        MessageUtil.sendMessage(player, "&6===== &eArenas Disponíveis &6=====");

        for (Arena arena : arenas) {
            String status = arenaManager.isArenaInUse(arena.getName()) ? "&c(Em Uso)" : "&a(Disponível)";
            MessageUtil.sendMessage(player, "&e" + arena.getName() + " &7- " + status);
        }
    }

    /**
     * Manipula o comando para definir um ponto de spawn na arena
     *
     * @param player O jogador que executou o comando
     */
    private void handleSetSpawnPoint(Player player) {
        Arena arena = arenaManager.getArenaByWorld(player.getWorld().getUID());

        if (arena == null) {
            MessageUtil.sendMessage(player, "&cVocê não está em um mundo de arena.");
            MessageUtil.sendMessage(player, "&7Use &e/arena create <nome> &7para criar uma arena neste mundo.");
            return;
        }

        arena.addSpawnPoint(player.getLocation().clone());
        arenaManager.saveArenas();

        int spawnCount = arena.getSpawnPointCount();
        MessageUtil.sendMessage(player, "&aPonto de spawn #" + spawnCount + " adicionado à arena '" + arena.getName() + "'!");
    }

    /**
     * Manipula o comando para definir o lobby de uma arena
     *
     * @param player O jogador que executou o comando
     */
    private void handleSetLobby(Player player) {
        Arena arena = arenaManager.getArenaByWorld(player.getWorld().getUID());

        if (arena == null) {
            MessageUtil.sendMessage(player, "&cVocê não está em um mundo de arena.");
            MessageUtil.sendMessage(player, "&7Use &e/arena create <nome> &7para criar uma arena neste mundo.");
            return;
        }

        arena.setLobbyLocation(player.getLocation().clone());
        arenaManager.saveArenas();

        MessageUtil.sendMessage(player, "&aLobby da arena '" + arena.getName() + "' definido com sucesso!");
    }

    /**
     * Manipula o comando para mostrar informações de uma arena
     *
     * @param player O jogador que executou o comando
     * @param name Nome da arena
     */
    private void handleArenaInfo(Player player, String name) {
        Arena arena = arenaManager.getArena(name);

        if (arena == null) {
            MessageUtil.sendMessage(player, "&cNão existe uma arena com o nome '" + name + "'.");
            return;
        }

        MessageUtil.sendMessage(player, "&6===== &eInformações da Arena: &a" + arena.getName() + " &6=====");
        MessageUtil.sendMessage(player, "&eNome: &7" + arena.getName());
        MessageUtil.sendMessage(player, "&eNome de exibição: &7" + arena.getDisplayName());
        MessageUtil.sendMessage(player, "&eMundo: &7" + arena.getWorldUUID());
        MessageUtil.sendMessage(player, "&ePontos de spawn: &7" + arena.getSpawnPointCount());
        MessageUtil.sendMessage(player, "&eJogadores: &7" + arena.getMinPlayers() + "-" + arena.getMaxPlayers());
        MessageUtil.sendMessage(player, "&eModo de jogo: &7" + arena.getDefaultGameMode().getDisplayName());
        MessageUtil.sendMessage(player, "&eLobby configurado: &7" + (arena.getLobbyLocation() != null ? "Sim" : "Não"));
        MessageUtil.sendMessage(player, "&eStatus: &7" + (arenaManager.isArenaInUse(name) ? "Em uso" : "Disponível"));
    }

    /**
     * Manipula o comando para definir o número mínimo de jogadores
     *
     * @param player O jogador que executou o comando
     * @param name Nome da arena
     * @param minPlayersStr Número mínimo de jogadores
     */
    private void handleSetMinPlayers(Player player, String name, String minPlayersStr) {
        Arena arena = arenaManager.getArena(name);

        if (arena == null) {
            MessageUtil.sendMessage(player, "&cNão existe uma arena com o nome '" + name + "'.");
            return;
        }

        try {
            int minPlayers = Integer.parseInt(minPlayersStr);

            if (minPlayers < 2) {
                MessageUtil.sendMessage(player, "&cO número mínimo de jogadores deve ser pelo menos 2.");
                return;
            }

            if (minPlayers > arena.getMaxPlayers()) {
                MessageUtil.sendMessage(player, "&cO número mínimo de jogadores não pode ser maior que o máximo (" + arena.getMaxPlayers() + ").");
                return;
            }

            arena.setMinPlayers(minPlayers);
            arenaManager.saveArenas();

            MessageUtil.sendMessage(player, "&aNúmero mínimo de jogadores da arena '" + name + "' definido para " + minPlayers + "!");
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(player, "&cO valor '" + minPlayersStr + "' não é um número válido.");
        }
    }

    /**
     * Manipula o comando para definir o número máximo de jogadores
     *
     * @param player O jogador que executou o comando
     * @param name Nome da arena
     * @param maxPlayersStr Número máximo de jogadores
     */
    private void handleSetMaxPlayers(Player player, String name, String maxPlayersStr) {
        Arena arena = arenaManager.getArena(name);

        if (arena == null) {
            MessageUtil.sendMessage(player, "&cNão existe uma arena com o nome '" + name + "'.");
            return;
        }

        try {
            int maxPlayers = Integer.parseInt(maxPlayersStr);

            if (maxPlayers < arena.getMinPlayers()) {
                MessageUtil.sendMessage(player, "&cO número máximo de jogadores não pode ser menor que o mínimo (" + arena.getMinPlayers() + ").");
                return;
            }

            if (maxPlayers > 64) {
                MessageUtil.sendMessage(player, "&cO número máximo de jogadores não pode ser maior que 64.");
                return;
            }

            arena.setMaxPlayers(maxPlayers);
            arenaManager.saveArenas();

            MessageUtil.sendMessage(player, "&aNúmero máximo de jogadores da arena '" + name + "' definido para " + maxPlayers + "!");
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(player, "&cO valor '" + maxPlayersStr + "' não é um número válido.");
        }
    }

    /**
     * Manipula o comando para definir o modo de jogo da arena
     *
     * @param player O jogador que executou o comando
     * @param name Nome da arena
     * @param gameModeStr Nome do modo de jogo
     */
    private void handleSetGameMode(Player player, String name, String gameModeStr) {
        Arena arena = arenaManager.getArena(name);

        if (arena == null) {
            MessageUtil.sendMessage(player, "&cNão existe uma arena com o nome '" + name + "'.");
            return;
        }

        try {
            com.br.gravitationalbattle.game.GameMode gameMode = com.br.gravitationalbattle.game.GameMode.valueOf(gameModeStr.toUpperCase());

            arena.setDefaultGameMode(gameMode);
            arenaManager.saveArenas();

            MessageUtil.sendMessage(player, "&aModo de jogo da arena '" + name + "' definido para " + gameMode.getDisplayName() + "!");
        } catch (IllegalArgumentException e) {
            MessageUtil.sendMessage(player, "&cModo de jogo inválido: '" + gameModeStr + "'.");
            MessageUtil.sendMessage(player, "&7Modos disponíveis: SOLO, DUOS, SQUADS, TEAM_VS_TEAM, CAPTURE_THE_FLAG, DOMINATION, RACE");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("delete");
            completions.add("list");
            completions.add("setspawn");
            completions.add("setlobby");
            completions.add("info");
            completions.add("setminplayers");
            completions.add("setmaxplayers");
            completions.add("setmode");

            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("delete") || subCommand.equals("info") ||
                    subCommand.equals("setminplayers") || subCommand.equals("setmaxplayers") ||
                    subCommand.equals("setmode")) {

                return filterCompletions(
                        arenaManager.getAllArenas().stream()
                                .map(Arena::getName)
                                .collect(Collectors.toList()),
                        args[1]
                );
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("setmode")) {
            List<String> modes = new ArrayList<>();
            modes.add("SOLO");
            modes.add("DUOS");
            modes.add("SQUADS");
            modes.add("TEAM_VS_TEAM");
            modes.add("CAPTURE_THE_FLAG");
            modes.add("DOMINATION");
            modes.add("RACE");

            return filterCompletions(modes, args[2]);
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) return completions;

        String lowerInput = input.toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}