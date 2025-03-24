package com.br.gravitationalbattle.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;

public class ScoreboardManager {
    private final GravitationalBattle plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager;

    public ScoreboardManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
        this.bukkitScoreboardManager = Bukkit.getScoreboardManager();
    }

    /**
     * Cria um novo scoreboard para um jogador
     * @param player Jogador para criar o scoreboard
     */
    public void setScoreboard(Player player) {
        Scoreboard board = bukkitScoreboardManager.getNewScoreboard();
        Objective objective = board.registerNewObjective("gbboard", "dummy",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Gravitational Battle");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
        playerScoreboards.put(player.getUniqueId(), board);

        // Atualiza o scoreboard com informações iniciais
        updateScoreboard(player);
    }

    /**
     * Atualiza o scoreboard de um jogador
     * @param player Jogador para atualizar o scoreboard
     */
    public void updateScoreboard(Player player) {
        if (!playerScoreboards.containsKey(player.getUniqueId())) {
            setScoreboard(player);
            return;
        }

        Scoreboard board = playerScoreboards.get(player.getUniqueId());
        Objective objective = board.getObjective("gbboard");

        if (objective == null) {
            // Se o objetivo não existir por algum motivo, cria um novo
            setScoreboard(player);
            return;
        }

        // Limpa o scoreboard atual
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        // Verifica se o jogador está em um jogo
        Game game = null;
        try {
            game = plugin.getGameManager().getPlayerGame(player);
        } catch (Exception e) {
            // Ignora erros se o método não existir ou lançar exceção
        }

        if (game == null) {
            // Jogador não está em um jogo, mostra o scoreboard do lobby
            setLobbyScoreboard(player, objective);
        } else {
            // Jogador está em um jogo, mostra o scoreboard do jogo
            setGameScoreboard(player, objective, game);
        }
    }

    /**
     * Define o scoreboard do lobby para um jogador
     * @param player Jogador
     * @param objective Objetivo do scoreboard
     */
    private void setLobbyScoreboard(Player player, Objective objective) {
        int line = 10;

        setScore(objective, "&r", line--);
        setScore(objective, "&e&lSEU PERFIL", line--);
        setScore(objective, "&fNome: &a" + player.getName(), line--);

        // Estatísticas (padrão zero, pode ser atualizado depois)
        int kills = 0;
        int deaths = 0;
        int wins = 0;

        // Tenta obter estatísticas do StatsManager, mas não falha se der erro
        if (plugin.getStatsManager() != null) {
            try {
                kills = plugin.getStatsManager().getKills(player.getUniqueId());
                deaths = plugin.getStatsManager().getDeaths(player.getUniqueId());
                wins = plugin.getStatsManager().getWins(player.getUniqueId());
            } catch (Exception e) {
                // Ignora erros
            }
        }

        setScore(objective, "&fVitórias: &a" + wins, line--);
        setScore(objective, "&fAbates: &a" + kills, line--);
        setScore(objective, "&fMortes: &c" + deaths, line--);
        setScore(objective, "&r&r", line--);
        setScore(objective, "&e&lSERVIDOR", line--);
        setScore(objective, "&fJogadores: &a" + Bukkit.getOnlinePlayers().size(), line--);

        int arenaCount = 0;
        try {
            arenaCount = plugin.getArenaManager().getAllArenas().size();
        } catch (Exception e) {
            // Ignora erros
        }

        setScore(objective, "&fArenas: &a" + arenaCount, line--);
        setScore(objective, "&r&r&r", line--);
        setScore(objective, "&7gravitationalbattle.com.br", line);
    }

    /**
     * Define o scoreboard do jogo para um jogador
     * @param player Jogador
     * @param objective Objetivo do scoreboard
     * @param game Jogo
     */
    private void setGameScoreboard(Player player, Objective objective, Game game) {
        int line = 10;

        setScore(objective, "&r", line--);
        setScore(objective, "&e&lARENA", line--);

        String displayName = "Desconhecida";
        try {
            displayName = game.getArena().getDisplayName();
        } catch (Exception e) {
            // Ignora erros
        }

        setScore(objective, "&f" + displayName, line--);
        setScore(objective, "&r&r", line--);
        setScore(objective, "&e&lESTADO", line--);

        GameState state = GameState.WAITING;
        try {
            state = game.getState();
        } catch (Exception e) {
            // Ignora erros
        }

        String stateColor = "&c";
        if (state == GameState.WAITING) {
            stateColor = "&a";
        } else if (state == GameState.STARTING) {
            stateColor = "&e";
        } else if (state == GameState.INGAME) {
            stateColor = "&c";
        }

        setScore(objective, stateColor + state.toString(), line--);
        setScore(objective, "&r&r&r", line--);
        setScore(objective, "&e&lJOGADORES", line--);

        int playerCount = 0;
        int maxPlayers = 0;
        try {
            playerCount = game.getPlayerCount();
            maxPlayers = game.getArena().getMaxPlayers();
        } catch (Exception e) {
            // Ignora erros
        }

        setScore(objective, "&f" + playerCount + "/" + maxPlayers, line--);
        setScore(objective, "&r&r&r&r", line--);
        setScore(objective, "&7gravitationalbattle.com.br", line);
    }

    /**
     * Define uma linha no scoreboard
     * @param objective Objetivo do scoreboard
     * @param text Texto da linha
     * @param line Número da linha
     */
    private void setScore(Objective objective, String text, int line) {
        Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', text));
        score.setScore(line);
    }

    /**
     * Remove o scoreboard de um jogador
     * @param player Jogador para remover o scoreboard
     */
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(bukkitScoreboardManager.getNewScoreboard());
    }
}