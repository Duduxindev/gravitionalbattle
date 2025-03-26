package com.br.gravitationalbattle.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;
import com.br.gravitationalbattle.utils.MessageUtil;

/**
 * Gerencia os jogos ativos
 */
public class GameManager {

    private final GravitationalBattle plugin;
    private final ArenaManager arenaManager;

    public GameManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    /**
     * Obtém a quantidade total de jogadores em jogos
     *
     * @return Quantidade de jogadores
     */
    public int getTotalPlayersInGames() {
        int total = 0;

        for (Game game : arenaManager.getAllGames()) {
            total += game.getPlayerCount();
        }

        return total;
    }

    /**
     * Verifica se um jogador está em algum jogo
     *
     * @param player O jogador
     * @return true se o jogador estiver em um jogo
     */
    public boolean isPlayerInGame(Player player) {
        return arenaManager.getPlayerGame(player) != null;
    }

    /**
     * Faz um jogador entrar em um jogo
     *
     * @param player O jogador
     * @param arenaName Nome da arena
     * @return true se o jogador entrou com sucesso
     */
    public boolean joinGame(Player player, String arenaName) {
        return arenaManager.joinGame(player, arenaName);
    }

    /**
     * Faz um jogador sair do jogo atual
     *
     * @param player O jogador
     * @return true se o jogador saiu com sucesso
     */
    public boolean leaveGame(Player player) {
        return arenaManager.leaveGame(player);
    }

    /**
     * Verifica se uma arena existe
     *
     * @param arenaName Nome da arena
     * @return true se a arena existir
     */
    public boolean arenaExists(String arenaName) {
        return arenaManager.arenaExists(arenaName);
    }

    /**
     * Obtém uma lista de nomes de arenas disponíveis
     *
     * @return Lista de nomes de arenas
     */
    public List<String> getAvailableArenas() {
        List<String> arenaNames = new ArrayList<>();

        for (Arena arena : arenaManager.getAllArenas()) {
            arenaNames.add(arena.getName());
        }

        return arenaNames;
    }

    /**
     * Obtém o número de jogadores em uma arena
     *
     * @param arenaName Nome da arena
     * @return Quantidade de jogadores ou 0 se a arena não existir/não tiver jogo ativo
     */
    public int getPlayersInArena(String arenaName) {
        Game game = arenaManager.getGame(arenaName);
        return game != null ? game.getPlayerCount() : 0;
    }

    /**
     * Obtém o número máximo de jogadores em uma arena
     *
     * @param arenaName Nome da arena
     * @return Número máximo de jogadores ou 0 se a arena não existir
     */
    public int getMaxPlayersInArena(String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        return arena != null ? arena.getMaxPlayers() : 0;
    }

    /**
     * Obtém o estado atual de uma arena em formato de string
     *
     * @param arenaName Nome da arena
     * @return Estado da arena ou "UNAVAILABLE" se a arena não existir
     */
    public String getArenaState(String arenaName) {
        Game game = arenaManager.getGame(arenaName);
        if (game != null) {
            return game.getState().toString();
        }

        Arena arena = arenaManager.getArena(arenaName);
        if (arena != null) {
            return arena.getState().toString();
        }

        return "UNAVAILABLE";
    }

    /**
     * Adiciona um jogador como espectador em um jogo
     *
     * @param player O jogador
     * @param targetArenaOrPlayer Nome da arena ou jogador para assistir
     * @return true se o jogador foi adicionado como espectador
     */
    public boolean addSpectator(Player player, String targetArenaOrPlayer) {
        // Verificar se o jogador já está em um jogo
        if (isPlayerInGame(player)) {
            MessageUtil.sendMessage(player, "&cVocê já está em um jogo! Use /leave para sair.");
            return false;
        }

        // Tentar encontrar por arena
        Game game = arenaManager.getGame(targetArenaOrPlayer);

        // Se não encontrar por arena, tentar por jogador
        if (game == null) {
            Player targetPlayer = plugin.getServer().getPlayer(targetArenaOrPlayer);
            if (targetPlayer != null) {
                game = arenaManager.getPlayerGame(targetPlayer);
            }
        }

        // Verificar se encontrou um jogo
        if (game == null) {
            MessageUtil.sendMessage(player, "&cNão foi possível encontrar um jogo para assistir.");
            return false;
        }

        // Verificar se o jogo está em andamento
        if (game.getState() != GameState.INGAME) {
            MessageUtil.sendMessage(player, "&cEste jogo ainda não começou ou já terminou.");
            return false;
        }

        // Adicionar como espectador
        if (game.addSpectator(player)) {
            return true;
        } else {
            return false;
        }
    }
}