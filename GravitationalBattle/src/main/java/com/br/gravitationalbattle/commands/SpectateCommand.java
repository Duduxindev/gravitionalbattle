package com.br.gravitationalbattle.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;
import com.br.gravitationalbattle.utils.MessageUtil;

public class SpectateCommand implements CommandExecutor, TabCompleter {

    private final GravitationalBattle plugin;

    public SpectateCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.spectate")) {
            MessageUtil.sendMessage(player, "&cVocê não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(player, "&cUso: /spectate <arena|jogador>");
            return true;
        }

        // Check if player is already in a game
        Game currentGame = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (currentGame != null) {
            MessageUtil.sendMessage(player, "&cVocê precisa sair do jogo atual antes de assistir a outro. Use /leave.");
            return true;
        }

        String target = args[0];

        // First try to find an arena with this name
        Arena arena = plugin.getArenaManager().getArena(target);
        if (arena != null) {
            Game game = plugin.getArenaManager().getGame(arena.getName());

            if (game != null && game.getState() == GameState.INGAME) {
                game.addSpectator(player);
                return true;
            } else {
                MessageUtil.sendMessage(player, "&cNão há jogo em andamento nesta arena.");
                return true;
            }
        }

        // If not an arena, try to find a player
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            Game game = plugin.getGameManager().getPlayerGame(targetPlayer.getUniqueId());

            if (game != null && game.getState() == GameState.INGAME) {
                game.addSpectator(player);
                return true;
            } else {
                MessageUtil.sendMessage(player, "&cEste jogador não está em um jogo.");
                return true;
            }
        }

        MessageUtil.sendMessage(player, "&cJogador ou arena não encontrado.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        if (!sender.hasPermission("gravitationalbattle.spectate")) {
            return null;
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Add active arenas with games
            for (Game game : plugin.getArenaManager().getActiveGames()) {
                if (game.getState() == GameState.INGAME) {
                    completions.add(game.getArena().getName());
                }
            }

            // Add players in games
            for (Player player : Bukkit.getOnlinePlayers()) {
                Game playerGame = plugin.getGameManager().getPlayerGame(player.getUniqueId());
                if (playerGame != null && playerGame.getState() == GameState.INGAME) {
                    completions.add(player.getName());
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