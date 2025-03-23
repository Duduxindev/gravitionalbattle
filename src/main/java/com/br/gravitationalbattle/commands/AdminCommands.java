package com.br.gravitationalbattle.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Classe que contém os comandos administrativos do plugin
 */
public class AdminCommands {

    private final GravitationalBattle plugin;

    public AdminCommands(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    /**
     * Força o início de um jogo
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean forceStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.admin.forcestart")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(sender, "&cUso: /admin forcestart <arena>");
            return false;
        }

        String arenaName = args[0];
        Game game = plugin.getArenaManager().getGame(arenaName);

        if (game == null) {
            MessageUtil.sendMessage(sender, "&cNão há jogo ativo nesta arena.");
            return false;
        }

        if (game.getState() == GameState.INGAME) {
            MessageUtil.sendMessage(sender, "&cEste jogo já está em andamento.");
            return false;
        }

        // Adicione este método na classe Game
        game.forceStart();
        MessageUtil.sendMessage(sender, "&aJogo iniciado à força com sucesso!");
        return true;
    }

    /**
     * Força o fim de um jogo
     *
     * @param sender Quem enviou o comando
     * @param args Argumentos do comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean forceEnd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("gravitationalbattle.admin.forceend")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        if (args.length < 1) {
            MessageUtil.sendMessage(sender, "&cUso: /admin forceend <arena>");
            return false;
        }

        String arenaName = args[0];
        Game game = plugin.getArenaManager().getGame(arenaName);

        if (game == null) {
            MessageUtil.sendMessage(sender, "&cNão há jogo ativo nesta arena.");
            return false;
        }

        // Adicione este método na classe Game
        game.forceEnd();
        MessageUtil.sendMessage(sender, "&aJogo encerrado à força com sucesso!");
        return true;
    }

    /**
     * Recarrega as configurações do plugin
     *
     * @param sender Quem enviou o comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("gravitationalbattle.admin.reload")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        plugin.getConfigManager().loadConfig();
        plugin.getArenaManager().loadArenas();

        MessageUtil.sendMessage(sender, "&aConfigurações recarregadas com sucesso!");
        return true;
    }

    /**
     * Define o lobby principal do plugin
     *
     * @param sender Quem enviou o comando
     * @return true se o comando foi executado com sucesso
     */
    public boolean setMainLobby(CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cEste comando só pode ser usado por jogadores.");
            return false;
        }

        if (!sender.hasPermission("gravitationalbattle.admin.setlobby")) {
            MessageUtil.sendMessage(sender, "&cVocê não tem permissão para usar este comando.");
            return false;
        }

        Player player = (Player) sender;
        plugin.getConfigManager().setLobbyLocation(player.getLocation());

        MessageUtil.sendMessage(player, "&aLobby principal definido com sucesso!");
        return true;
    }
}