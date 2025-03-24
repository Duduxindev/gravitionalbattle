package com.br.gravitationalbattle.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game .Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;

public class GameManager {
    private final GravitationalBattle plugin;

    // Mapeia nomes de arenas para instâncias de jogos
    private final Map<String, Game> games;

    // Mapeia jogadores (UUID) para nomes de arenas
    private final Map<String, String> playerArenas;

    public GameManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.games = new HashMap<>();
        this.playerArenas = new HashMap<>();
    }

    /**
     * Obtém o jogo em que um jogador está
     * @param player Jogador para verificar
     * @return O jogo em que o jogador está, ou null se não estiver em nenhum jogo
     */
    public Game getPlayerGame(Player player) {
        String arenaName = playerArenas.get(player.getUniqueId().toString());
        if (arenaName == null) {
            return null;
        }
        return games.get(arenaName);
    }

    /**
     * Cria um novo jogo para uma arena
     * @param arena A arena para a qual criar o jogo
     * @return O jogo criado
     */
    public Game createGame(Arena arena) {
        // Verifica se já existe um jogo para esta arena
        if (games.containsKey(arena.getName())) {
            return games.get(arena.getName());
        }

        // Cria um novo jogo
        Game game = new Game(plugin, arena);
        games.put(arena.getName(), game);
        return game;
    }

    /**
     * Verifica se um jogador está em um jogo
     * @param player Jogador para verificar
     * @return true se o jogador estiver em um jogo
     */
    public boolean isPlayerInGame(Player player) {
        return playerArenas.containsKey(player.getUniqueId().toString());
    }

    /**
     * Verifica se uma arena existe
     * @param arenaName Nome da arena
     * @return true se a arena existir
     */
    public boolean arenaExists(String arenaName) {
        return plugin.getArenaManager().getArena(arenaName) != null;
    }

    /**
     * Adiciona um jogador a um jogo
     * @param player Jogador para adicionar
     * @param arenaName Nome da arena
     * @return true se o jogador foi adicionado com sucesso
     */
    public boolean joinGame(Player player, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            return false;
        }

        Game game = games.get(arenaName);
        if (game == null) {
            game = createGame(arena);
        }

        if (game.addPlayer(player)) {
            playerArenas.put(player.getUniqueId().toString(), arenaName);
            return true;
        }

        return false;
    }

    /**
     * Remove um jogador de seu jogo atual
     * @param player Jogador para remover
     */
    public void leaveGame(Player player) {
        Game game = getPlayerGame(player);
        if (game != null) {
            game.removePlayer(player);
            playerArenas.remove(player.getUniqueId().toString());
        }
    }

    /**
     * Obtém uma lista de arenas disponíveis
     * @return Lista de nomes de arenas disponíveis
     */
    public List<String> getAvailableArenas() {
        List<String> available = new ArrayList<>();
        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            if (arena.isEnabled()) {
                available.add(arena.getName());
            }
        }
        return available;
    }

    /**
     * Obtém o número de jogadores em uma arena
     * @param arenaName Nome da arena
     * @return Número de jogadores
     */
    public int getPlayersInArena(String arenaName) {
        Game game = games.get(arenaName);
        return game == null ? 0 : game.getPlayerCount();
    }

    /**
     * Obtém o número máximo de jogadores em uma arena
     * @param arenaName Nome da arena
     * @return Número máximo de jogadores
     */
    public int getMaxPlayersInArena(String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        return arena == null ? 0 : arena.getMaxPlayers();
    }

    /**
     * Obtém o estado de uma arena em formato de string
     * @param arenaName Nome da arena
     * @return Estado da arena
     */
    public String getArenaState(String arenaName) {
        Game game = games.get(arenaName);
        if (game == null) {
            return "WAITING";
        }
        return game.getState().toString();
    }
}