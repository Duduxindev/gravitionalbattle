package com.br.gravitationalbattle.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Classe que contém comandos para moderadores do plugin
 */
public class ModeratorCommands {

    private final GravitationalBattle plugin;

    public ModeratorCommands(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    /**
     * Expulsa um jogador de um jogo
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean kickPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.mod.kick")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(sender, "&cUso: /gbmod kick <jogador> [motivo]");
            return false;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            MessageUtil.sendMessage(sender, "&cJogador não encontrado.");
            return false;
        }

        Game game = plugin.getGameManager().getPlayerGame(target.getUniqueId());

        if (game == null) {
            MessageUtil.sendMessage(sender, "&cEste jogador não está em nenhum jogo.");
            return false;
        }

        // Montando o motivo
        String reason = "Você foi expulso do jogo.";
        if (args.length > 1) {
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            reason = "Você foi expulso do jogo: " + reasonBuilder.toString().trim();
        }

        // Enviar mensagem para o jogador
        MessageUtil.sendMessage(target, "&c" + reason);

        // Remover o jogador do jogo
        plugin.getGameManager().leaveGame(target);

        // Informar ao moderador
        MessageUtil.sendMessage(sender, "&aJogador &e" + target.getName() + " &afoi expulso do jogo.");
        return true;
    }

    /**
     * Muta um jogador no chat do jogo
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean mutePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.mod.mute")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(sender, "&cUso: /gbmod mute <jogador>");
            return false;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            MessageUtil.sendMessage(sender, "&cJogador não encontrado.");
            return false;
        }

        // Implemente sua lógica de mute aqui
        // Por exemplo, você pode armazenar jogadores mutados em um conjunto

        MessageUtil.sendMessage(sender, "&aJogador &e" + target.getName() + " &afoi mutado no chat do jogo.");
        MessageUtil.sendMessage(target, "&cVocê foi mutado no chat do jogo por um moderador.");
        return true;
    }

    /**
     * Desmuta um jogador no chat do jogo
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean unmutePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.mod.mute")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(sender, "&cUso: /gbmod unmute <jogador>");
            return false;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            MessageUtil.sendMessage(sender, "&cJogador não encontrado.");
            return false;
        }

        // Implemente sua lógica de unmute aqui

        MessageUtil.sendMessage(sender, "&aJogador &e" + target.getName() + " &afoi desmutado no chat do jogo.");
        MessageUtil.sendMessage(target, "&aVocê foi desmutado no chat do jogo.");
        return true;
    }

    /**
     * Entra em modo espectador em um jogo específico
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean spectateGame(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return false;
        }

        if (!sender.hasPermission("gravitationalbattle.mod.spectate")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(sender, "&cUso: /gbmod spectate <arena|jogador>");
            return false;
        }

        Player player = (Player) sender;
        String target = args[0];

        // Se for um jogador
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            Game game = plugin.getGameManager().getPlayerGame(targetPlayer.getUniqueId());
            if (game != null) {
                game.addSpectator(player);
                return true;
            } else {
                MessageUtil.sendMessage(player, "&cEste jogador não está em um jogo.");
                return false;
            }
        }

        // Se for uma arena
        Game game = plugin.getArenaManager().getGame(target);
        if (game != null) {
            game.addSpectator(player);
            return true;
        }

        MessageUtil.sendMessage(player, "&cArena ou jogador não encontrado.");
        return false;
    }
}