package com.br.gravitationalbattle.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Comando para acessar a loja do jogo (versão simplificada)
 */
public class ShopCommand implements CommandExecutor {

    private final GravitationalBattle plugin;

    public ShopCommand(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtil.sendMessage(sender, "&cApenas jogadores podem usar este comando!");
            return true;
        }

        Player player = (Player) sender;

        // Mostrar mensagem temporária
        MessageUtil.sendMessage(player, "&6&l=========================");
        MessageUtil.sendMessage(player, "&6&lLoja - Batalha Gravitacional");
        MessageUtil.sendMessage(player, "&6&l=========================");
        MessageUtil.sendMessage(player, "");
        MessageUtil.sendMessage(player, "&eVocê tem &f" + plugin.getRewardManager().getPlayerTokens(player) + " &emoedas");
        MessageUtil.sendMessage(player, "");
        MessageUtil.sendMessage(player, "&7Categorias disponíveis:");
        MessageUtil.sendMessage(player, "&a• &eHabilidades");
        MessageUtil.sendMessage(player, "&a• &eTítulos");
        MessageUtil.sendMessage(player, "&a• &eEfeitos de Morte");
        MessageUtil.sendMessage(player, "&a• &eEfeitos de Vitória");
        MessageUtil.sendMessage(player, "");
        MessageUtil.sendMessage(player, "&cA loja está em manutenção. Funcionalidade completa em breve!");
        MessageUtil.sendMessage(player, "&6&l=========================");

        return true;
    }
}