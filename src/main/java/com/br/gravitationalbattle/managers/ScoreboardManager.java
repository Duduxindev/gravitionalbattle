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

public class ScoreboardManager {

    private final GravitationalBattle plugin;
    private final org.bukkit.scoreboard.ScoreboardManager bukkitManager;
    private final Map<UUID, Scoreboard> playerScoreboards;

    public ScoreboardManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.bukkitManager = Bukkit.getScoreboardManager();
        this.playerScoreboards = new HashMap<>();
    }

    public void setLobbyScoreboard(Player player) {
        Scoreboard board = bukkitManager.getNewScoreboard();
        Objective obj = board.registerNewObjective("lobby", "dummy", ChatColor.GOLD + "Gravitational Battle");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Get player stats
        int wins = plugin.getStatsManager().getPlayerWins(player);
        int kills = plugin.getStatsManager().getPlayerKills(player);
        int deaths = plugin.getStatsManager().getPlayerDeaths(player);
        int games = plugin.getStatsManager().getPlayerGamesPlayed(player);

        // Calculate KDR
        double kdr = (deaths > 0) ? (double) kills / deaths : kills;
        String formattedKdr = String.format("%.2f", kdr);

        // Set scores
        int line = 12;
        setScore(obj, "&r ", line--);
        setScore(obj, "&eJogador: &f" + player.getName(), line--);
        setScore(obj, "&r  ", line--);
        setScore(obj, "&eVitórias: &f" + wins, line--);
        setScore(obj, "&eEliminações: &f" + kills, line--);
        setScore(obj, "&eMortes: &f" + deaths, line--);
        setScore(obj, "&eK/D: &f" + formattedKdr, line--);
        setScore(obj, "&ePartidas: &f" + games, line--);
        setScore(obj, "&r   ", line--);
        setScore(obj, "&eJogos ativos: &f" + plugin.getArenaManager().getActiveGames().size(), line--);
        setScore(obj, "&r    ", line--);
        setScore(obj, "&6play.servidor.com", line);

        player.setScoreboard(board);
        playerScoreboards.put(player.getUniqueId(), board);
    }

    public void setWaitingScoreboard(Player player, Game game) {
        Scoreboard board = bukkitManager.getNewScoreboard();
        Objective obj = board.registerNewObjective("waiting", "dummy", ChatColor.GOLD + "Gravitational Battle");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int line = 12;
        setScore(obj, "&r ", line--);
        setScore(obj, "&eArena: &f" + game.getArena().getDisplayName(), line--);
        setScore(obj, "&eEstado: &fEsperando", line--);
        setScore(obj, "&r  ", line--);
        setScore(obj, "&eJogadores: &f" + game.getPlayers().size() + "/" + plugin.getConfigManager().getMaxPlayers(), line--);
        setScore(obj, "&r   ", line--);

        if (game.getState() == com.br.gravitationalbattle.game.GameState.COUNTDOWN) {
            setScore(obj, "&eIniciando em: &f" + game.getCountdown() + "s", line--);
            setScore(obj, "&r    ", line--);
        }

        setScore(obj, "&fMín. jogadores: &e" + plugin.getConfigManager().getMinPlayers(), line--);
        setScore(obj, "&r     ", line--);
        setScore(obj, "&6play.servidor.com", line);

        player.setScoreboard(board);
        playerScoreboards.put(player.getUniqueId(), board);
    }

    public void updateWaitingScoreboard(Player player, Game game) {
        Scoreboard board = player.getScoreboard();

        if (board == null || board.getObjective("waiting") == null) {
            setWaitingScoreboard(player, game);
            return;
        }

        Objective obj = board.getObjective("waiting");

        // Update player count
        board.resetScores(ChatColor.translateAlternateColorCodes('&',
                "&eJogadores: &f" + (game.getPlayers().size() - 1) + "/" + plugin.getConfigManager().getMaxPlayers()));
        setScore(obj, "&eJogadores: &f" + game.getPlayers().size() + "/" + plugin.getConfigManager().getMaxPlayers(), 7);

        // Update countdown if in countdown state
        if (game.getState() == com.br.gravitationalbattle.game.GameState.COUNTDOWN) {
            // Remove all possible countdowns
            for (int i = 0; i <= 60; i++) {
                board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eIniciando em: &f" + i + "s"));
            }
            setScore(obj, "&eIniciando em: &f" + game.getCountdown() + "s", 5);
        }
    }

    public void setGameScoreboard(Player player, Game game) {
        Scoreboard board = bukkitManager.getNewScoreboard();
        Objective obj = board.registerNewObjective("game", "dummy", ChatColor.GOLD + "Gravitational Battle");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Format time remaining
        int timeRemaining = game.getGameTime();
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);

        // Get gravity direction name
        String gravityDirection;
        switch (game.getGravityDirection()) {
            case 0: gravityDirection = "Normal"; break;
            case 1: gravityDirection = "Invertida"; break;
            case 2: gravityDirection = "Alta"; break;
            case 3: gravityDirection = "Baixa"; break;
            default: gravityDirection = "Normal"; break;
        }

        int line = 12;
        setScore(obj, "&r ", line--);
        setScore(obj, "&eArena: &f" + game.getArena().getDisplayName(), line--);
        setScore(obj, "&eTempo: &f" + timeStr, line--);
        setScore(obj, "&r  ", line--);
        setScore(obj, "&eJogadores vivos: &f" + game.getAliveCount(), line--);
        setScore(obj, "&eSuas eliminações: &f" + game.getKills(player.getUniqueId()), line--);
        setScore(obj, "&r   ", line--);
        setScore(obj, "&eGravidade: &f" + gravityDirection, line--);
        setScore(obj, "&ePróx. mudança: &f" + game.getTimeUntilGravityChange() + "s", line--);
        setScore(obj, "&r    ", line--);
        setScore(obj, "&6play.servidor.com", line);

        player.setScoreboard(board);
        playerScoreboards.put(player.getUniqueId(), board);
    }

    public void updateGameScoreboard(Player player, Game game) {
        Scoreboard board = player.getScoreboard();

        if (board == null || board.getObjective("game") == null) {
            setGameScoreboard(player, game);
            return;
        }

        Objective obj = board.getObjective("game");

        // Update time remaining
        int timeRemaining = game.getGameTime();
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);

        // Clear all possible times
        for (int m = 0; m < 60; m++) {
            for (int s = 0; s < 60; s++) {
                String format = String.format("%02d:%02d", m, s);
                board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eTempo: &f" + format));
            }
        }

        setScore(obj, "&eTempo: &f" + timeStr, 9);

        // Update player count
        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eJogadores vivos: &f" + (game.getAliveCount() + 1)));
        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eJogadores vivos: &f" + (game.getAliveCount() - 1)));
        setScore(obj, "&eJogadores vivos: &f" + game.getAliveCount(), 7);

        // Update kills
        int kills = game.getKills(player.getUniqueId());
        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eSuas eliminações: &f" + (kills - 1)));
        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eSuas eliminações: &f" + (kills + 1)));
        setScore(obj, "&eSuas eliminações: &f" + kills, 6);

        // Update gravity direction
        String gravityDirection;
        switch (game.getGravityDirection()) {
            case 0: gravityDirection = "Normal"; break;
            case 1: gravityDirection = "Invertida"; break;
            case 2: gravityDirection = "Alta"; break;
            case 3: gravityDirection = "Baixa"; break;
            default: gravityDirection = "Normal"; break;
        }

        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eGravidade: &fNormal"));
        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eGravidade: &fInvertida"));
        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eGravidade: &fAlta"));
        board.resetScores(ChatColor.translateAlternateColorCodes('&', "&eGravidade: &fBaixa"));
        setScore(obj, "&eGravidade: &f" + gravityDirection, 4);

        // Update next gravity change
        for (int i = 0; i <= 60; i++) {
            board.resetScores(ChatColor.translateAlternateColorCodes('&', "&ePróx. mudança: &f" + i + "s"));
        }
        setScore(obj, "&ePróx. mudança: &f" + game.getTimeUntilGravityChange() + "s", 3);
    }

    private void setScore(Objective objective, String text, int score) {
        Score scoreObj = objective.getScore(ChatColor.translateAlternateColorCodes('&', text));
        scoreObj.setScore(score);
    }

    // Call this on plugin disable to clean up
    public void cleanup() {
        playerScoreboards.clear();
    }
}