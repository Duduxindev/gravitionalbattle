package com.br.gravitationalbattle.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private GravitationalBattle plugin;
    private Map<UUID, Scoreboard> playerScoreboards;

    public ScoreboardManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
    }

    /**
     * Sets the waiting scoreboard for a player
     * @param player The player
     * @param game The game instance
     */
    public void setWaitingScoreboard(Player player, Game game) {
        // Use fully qualified name for Bukkit's ScoreboardManager
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = bukkitManager.getNewScoreboard();

        // Fix for 1.8.8 - Use two parameters instead of three
        Objective objective = scoreboard.registerNewObjective("waiting", "dummy");
        objective.setDisplayName(ChatColor.GOLD + "§l§6Arena Battle");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Create score entries with unique strings to prevent collisions
        Score spacer1 = objective.getScore(ChatColor.YELLOW + "");
        spacer1.setScore(10);

        Score playerCount = objective.getScore(ChatColor.WHITE + "Players: " + ChatColor.GREEN + game.getPlayers().size() + "/" + game.getMaxPlayers());
        playerCount.setScore(9);

        Score spacer2 = objective.getScore(ChatColor.YELLOW + " ");
        spacer2.setScore(8);

        Score statusLine;
        if (game.isStarting()) {
            statusLine = objective.getScore(ChatColor.WHITE + "Starting in: " + ChatColor.GREEN + game.getCountdown() + "s");
        } else {
            statusLine = objective.getScore(ChatColor.WHITE + "Waiting for players...");
        }
        statusLine.setScore(7);

        Score spacer3 = objective.getScore(ChatColor.YELLOW + "  ");
        spacer3.setScore(6);

        Score minPlayersLine = objective.getScore(ChatColor.WHITE + "Min Players: " + ChatColor.GREEN + game.getMinPlayers());
        minPlayersLine.setScore(5);

        Score spacer4 = objective.getScore(ChatColor.YELLOW + "   ");
        spacer4.setScore(4);

        Score serverLine = objective.getScore(ChatColor.YELLOW + "§7play.yourserver.com");
        serverLine.setScore(3);

        // Store the scoreboard for this player
        playerScoreboards.put(player.getUniqueId(), scoreboard);

        // Set the scoreboard for the player
        player.setScoreboard(scoreboard);
    }

    /**
     * Sets the game scoreboard for a player
     * @param player The player
     * @param game The game instance
     */
    public void setGameScoreboard(Player player, Game game) {
        // Use fully qualified name for Bukkit's ScoreboardManager
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = bukkitManager.getNewScoreboard();

        // Fix for 1.8.8 - Use two parameters instead of three
        Objective objective = scoreboard.registerNewObjective("game", "dummy");
        objective.setDisplayName(ChatColor.GOLD + "§l§6Arena Battle");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Create score entries with unique strings
        Score spacer1 = objective.getScore(ChatColor.YELLOW + "");
        spacer1.setScore(9);

        Score playersLine = objective.getScore(ChatColor.WHITE + "Players alive: " + ChatColor.GREEN + game.getAlivePlayers().size());
        playersLine.setScore(8);

        Score spacer2 = objective.getScore(ChatColor.YELLOW + " ");
        spacer2.setScore(7);

        Score killsLine = objective.getScore(ChatColor.WHITE + "Your kills: " + ChatColor.RED + game.getPlayerKills(player));
        killsLine.setScore(6);

        Score spacer3 = objective.getScore(ChatColor.YELLOW + "  ");
        spacer3.setScore(5);

        // Show top killer if available
        Player topKiller = game.getTopKiller();
        if (topKiller != null) {
            Score topKillerLine = objective.getScore(ChatColor.WHITE + "Top: " + ChatColor.GOLD + topKiller.getName() + " (" + game.getPlayerKills(topKiller) + ")");
            topKillerLine.setScore(4);

            Score spacer4 = objective.getScore(ChatColor.YELLOW + "   ");
            spacer4.setScore(3);
        }

        Score serverLine = objective.getScore(ChatColor.YELLOW + "§7play.yourserver.com");
        serverLine.setScore(2);

        // Store the scoreboard for this player
        playerScoreboards.put(player.getUniqueId(), scoreboard);

        // Set the scoreboard for the player
        player.setScoreboard(scoreboard);
    }

    /**
     * Sets the end game scoreboard for a player
     * @param player The player
     * @param game The game instance
     * @param winner The winning player, or null if no winner
     */
    public void setEndGameScoreboard(Player player, Game game, Player winner) {
        // Use fully qualified name for Bukkit's ScoreboardManager
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = bukkitManager.getNewScoreboard();

        // Fix for 1.8.8 - Use two parameters instead of three
        Objective objective = scoreboard.registerNewObjective("endgame", "dummy");
        objective.setDisplayName(ChatColor.GOLD + "§l§6Game Over");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Create score entries with unique strings
        Score spacer1 = objective.getScore(ChatColor.YELLOW + "");
        spacer1.setScore(9);

        if (winner != null) {
            Score winnerLine = objective.getScore(ChatColor.WHITE + "Winner: " + ChatColor.GREEN + winner.getName());
            winnerLine.setScore(8);

            Score winnerKillsLine = objective.getScore(ChatColor.WHITE + "Kills: " + ChatColor.RED + game.getPlayerKills(winner));
            winnerKillsLine.setScore(7);
        } else {
            Score noWinnerLine = objective.getScore(ChatColor.RED + "No winner!");
            noWinnerLine.setScore(8);
        }

        Score spacer2 = objective.getScore(ChatColor.YELLOW + " ");
        spacer2.setScore(6);

        Score yourKillsLine = objective.getScore(ChatColor.WHITE + "Your kills: " + ChatColor.RED + game.getPlayerKills(player));
        yourKillsLine.setScore(5);

        Score spacer3 = objective.getScore(ChatColor.YELLOW + "  ");
        spacer3.setScore(4);

        Score restartLine = objective.getScore(ChatColor.WHITE + "Next game in: " + ChatColor.GREEN + "10s");
        restartLine.setScore(3);

        Score spacer4 = objective.getScore(ChatColor.YELLOW + "   ");
        spacer4.setScore(2);

        Score serverLine = objective.getScore(ChatColor.YELLOW + "§7play.yourserver.com");
        serverLine.setScore(1);

        // Store the scoreboard for this player
        playerScoreboards.put(player.getUniqueId(), scoreboard);

        // Set the scoreboard for the player
        player.setScoreboard(scoreboard);
    }

    /**
     * Updates the waiting scoreboard for all players
     * @param game The game instance
     */
    public void updateWaitingScoreboard(Game game) {
        for (Player player : game.getPlayers()) {
            setWaitingScoreboard(player, game);
        }
    }

    /**
     * Updates the game scoreboard for all players
     * @param game The game instance
     */
    public void updateGameScoreboard(Game game) {
        for (Player player : game.getPlayers()) {
            if (game.getAlivePlayers().contains(player)) {
                setGameScoreboard(player, game);
            }
        }
    }

    /**
     * Updates the end game scoreboard for all players
     * @param game The game instance
     * @param winner The winning player, or null if no winner
     */
    public void updateEndGameScoreboard(Game game, Player winner) {
        for (Player player : game.getPlayers()) {
            setEndGameScoreboard(player, game, winner);
        }
    }

    /**
     * Updates the countdown timer on the waiting scoreboard
     * @param game The game instance
     */
    public void updateCountdown(Game game) {
        for (Player player : game.getPlayers()) {
            Scoreboard board = playerScoreboards.get(player.getUniqueId());
            if (board != null) {
                // We can't update individual lines easily in 1.8.8, so we recreate the scoreboard
                setWaitingScoreboard(player, game);
            }
        }
    }

    /**
     * Removes a player's scoreboard
     * @param player The player
     */
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        // Use fully qualified name for Bukkit's ScoreboardManager
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    /**
     * Clears all stored scoreboards
     */
    public void clearScoreboards() {
        playerScoreboards.clear();
    }
}