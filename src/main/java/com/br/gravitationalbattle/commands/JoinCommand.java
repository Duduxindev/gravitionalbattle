package com.br.gravitationalbattle.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.managers.GameManager;

public class JoinCommand implements CommandExecutor {

    private GravitationalBattle plugin;
    private GameManager gameManager;

    /**
     * Creates a new join command
     * @param plugin The plugin instance
     */
    public JoinCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.gameManager = plugin.getGameManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        // Check if player has permission
        if (!player.hasPermission("gravitationalbattle.join")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return true;
        }

        // Handle command without arguments - list available arenas
        if (args.length == 0) {
            listAvailableArenas(player);
            return true;
        }

        String arenaName = args[0].toLowerCase();

        // Check if player is already in a game
        if (gameManager.isPlayerInGame(player)) {
            player.sendMessage(ChatColor.RED + "Você já está em um jogo! Use /leave para sair do jogo atual.");
            return true;
        }

        // Check if arena exists
        if (!gameManager.arenaExists(arenaName)) {
            player.sendMessage(ChatColor.RED + "A arena '" + arenaName + "' não existe!");
            listAvailableArenas(player);
            return true;
        }

        // Try to join the game
        if (gameManager.joinGame(player, arenaName)) {
            // Success - message handled by Game class
        } else {
            player.sendMessage(ChatColor.RED + "Não foi possível entrar na arena '" + arenaName + "'! Ela pode estar cheia ou em andamento.");
        }

        return true;
    }

    /**
     * Lists all available arenas to a player
     * @param player The player to send the list to
     */
    private void listAvailableArenas(Player player) {
        player.sendMessage(ChatColor.GOLD + "========= Arenas Disponíveis =========");
        player.sendMessage(ChatColor.YELLOW + "Uso: " + ChatColor.WHITE + "/join <arena>");
        player.sendMessage("");

        for (String arena : gameManager.getAvailableArenas()) {
            int players = gameManager.getPlayersInArena(arena);
            int maxPlayers = gameManager.getMaxPlayersInArena(arena);
            String state = gameManager.getArenaState(arena);

            if (state.equals("WAITING") || state.equals("STARTING")) {
                player.sendMessage(ChatColor.GREEN + arena + ChatColor.WHITE +
                        " - Jogadores: " + ChatColor.YELLOW + players + "/" + maxPlayers +
                        ChatColor.WHITE + " - Status: " +
                        (state.equals("WAITING") ? ChatColor.GREEN + "Aguardando" : ChatColor.GOLD + "Iniciando"));
            } else {
                player.sendMessage(ChatColor.RED + arena + ChatColor.WHITE +
                        " - Jogadores: " + ChatColor.YELLOW + players + "/" + maxPlayers +
                        ChatColor.WHITE + " - Status: " + ChatColor.RED + "Em Andamento");
            }
        }

        player.sendMessage(ChatColor.GOLD + "==================================");
    }
}