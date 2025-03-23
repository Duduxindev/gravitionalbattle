package com.br.gravitationalbattle.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.menus.MapSelectorMenu;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Classe que contém os comandos para jogadores comuns do plugin
 */
public class PlayerCommands {

    private final GravitationalBattle plugin;

    public PlayerCommands(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    /**
     * Abre o menu de seleção de mapas
     *
     * @param sender Quem enviou o comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean openMapMenu(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.play")) {
            MessageUtil.sendMessage(player, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        // Verifica se o jogador já está em um jogo
        Game currentGame = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (currentGame != null) {
            MessageUtil.sendMessage(player, "&cVocê já está em um jogo. Use /leave para sair.");
            return false;
        }

        // Abre o menu de seleção de mapas
        MapSelectorMenu menu = new MapSelectorMenu(plugin);
        menu.open(player);

        return true;
    }

    /**
     * Entra em um jogo
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean joinGame(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gravitationalbattle.play")) {
            MessageUtil.sendMessage(player, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        // Verifica se o jogador já está em um jogo
        Game currentGame = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (currentGame != null) {
            MessageUtil.sendMessage(player, "&cVocê já está em um jogo. Use /leave para sair.");
            return false;
        }

        String arenaName = null;
        if (args.length > 0) {
            arenaName = args[0];
        }

        Game game = plugin.getGameManager().joinGame(player, arenaName);
        return game != null;
    }

    /**
     * Sai de um jogo
     *
     * @param sender Quem enviou o comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean leaveGame(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return false;
        }

        Player player = (Player) sender;
        return plugin.getGameManager().leaveGame(player);
    }

    /**
     * Mostra as estatísticas do jogador
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean showStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.stats")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        Player target;

        if (args.length > 0) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                MessageUtil.sendMessage(sender, "&cJogador não encontrado.");
                return false;
            }
        } else {
            if (!(sender instanceof Player)) {
                MessageUtil.sendMessage(sender, "&cUso para console: /stats <jogador>");
                return false;
            }
            target = (Player) sender;
        }

        // Obtém as estatísticas do jogador
        int kills = plugin.getStatsManager().getPlayerKills(target);
        int deaths = plugin.getStatsManager().getPlayerDeaths(target);
        int wins = plugin.getStatsManager().getPlayerWins(target);
        int gamesPlayed = plugin.getStatsManager().getPlayerGamesPlayed(target);

        // Calcula estatísticas derivadas
        double kdr = deaths > 0 ? (double) kills / deaths : kills;
        String formattedKdr = String.format("%.2f", kdr);

        double winRate = gamesPlayed > 0 ? (double) wins / gamesPlayed * 100 : 0;
        String formattedWinRate = String.format("%.2f", winRate);

        // Mostra as estatísticas
        MessageUtil.sendMessage(sender, "&6===== &eEstatísticas de &f" + target.getName() + " &6=====");
        MessageUtil.sendMessage(sender, "&eVitórias: &f" + wins);
        MessageUtil.sendMessage(sender, "&eEliminações: &f" + kills);
        MessageUtil.sendMessage(sender, "&eMortes: &f" + deaths);
        MessageUtil.sendMessage(sender, "&eK/D: &f" + formattedKdr);
        MessageUtil.sendMessage(sender, "&ePartidas Jogadas: &f" + gamesPlayed);
        MessageUtil.sendMessage(sender, "&eTaxa de Vitória: &f" + formattedWinRate + "%");

        return true;
    }
}