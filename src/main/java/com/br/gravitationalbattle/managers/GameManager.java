package com.br.gravitationalbattle.managers;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;
import com.br.gravitationalbattle.utils.MessageUtil;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {

    private final GravitationalBattle plugin;
    private final Random random;

    public GameManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Gets the game a player is currently in
     *
     * @param player The player to check
     * @return Game object or null if not in a game
     */
    public Game getPlayerGame(Player player) {
        return plugin.getArenaManager().getPlayerGame(player);
    }

    /**
     * Checks if a player is in any game
     *
     * @param player Player to check
     * @return true if player is in a game
     */
    public boolean isPlayerInGame(Player player) {
        return getPlayerGame(player) != null;
    }

    /**
     * Checks if an arena exists
     *
     * @param arenaName Arena name
     * @return true if arena exists
     */
    public boolean arenaExists(String arenaName) {
        return plugin.getArenaManager().getArena(arenaName) != null;
    }

    /**
     * Joins a player to a game
     *
     * @param player Player joining
     * @param arenaName Arena to join
     * @return true if successfully joined
     */
    public boolean joinGame(Player player, String arenaName) {
        // Check if player is already in a game
        if (isPlayerInGame(player)) {
            // Force leave previous game first
            leaveGame(player);
        }

        // Get arena
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            return false;
        }

        // Get or create game
        Game game = plugin.getArenaManager().getGame(arenaName);

        if (game == null) {
            // Create new game
            game = new Game(plugin, arena);
            plugin.getArenaManager().registerGame(game);
        }

        // Try to join
        boolean joined = game.addPlayer(player);

        if (joined) {
            plugin.getArenaManager().registerPlayer(player, game);
            plugin.getScoreboardManager().updateGameScoreboard(player, game);
        }

        return joined;
    }

    /**
     * Joins a player to a random available game
     *
     * @param player Player joining
     * @return true if successfully joined
     */
    public boolean joinRandomGame(Player player) {
        // Check if player is already in a game
        if (isPlayerInGame(player)) {
            // Force leave previous game first
            leaveGame(player);
        }

        // Get all available arenas
        List<Arena> availableArenas = new ArrayList<>();

        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            if (arena.isEnabled()) {
                Game game = plugin.getArenaManager().getGame(arena.getName());

                // If no game exists for this arena, or the game has space and is joinable
                if (game == null || (game.getPlayerCount() < arena.getMaxPlayers() &&
                        (game.getState() == GameState.WAITING || game.getState() == GameState.STARTING))) {
                    availableArenas.add(arena);
                }
            }
        }

        // If no arenas are available
        if (availableArenas.isEmpty()) {
            MessageUtil.sendMessage(player, "&cNo available games found!");
            return false;
        }

        // Choose a random arena
        Arena chosenArena = availableArenas.get(random.nextInt(availableArenas.size()));

        // Join the game
        boolean joined = joinGame(player, chosenArena.getName());

        if (joined) {
            MessageUtil.sendMessage(player, "&aYou have joined a random game in &e" + chosenArena.getDisplayName() + "&a!");
        } else {
            MessageUtil.sendMessage(player, "&cFailed to join a random game. Please try again!");
        }

        return joined;
    }

    /**
     * Makes a player leave their current game
     *
     * @param player Player leaving
     * @return true if player was in a game and left
     */
    public boolean leaveGame(Player player) {
        Game game = getPlayerGame(player);

        if (game == null) {
            return false;
        }

        boolean removed = game.removePlayer(player);

        if (removed) {
            plugin.getArenaManager().unregisterPlayer(player);
            plugin.getScoreboardManager().setLobbyScoreboard(player);

            // If game is now empty, unregister it
            if (game.getPlayerCount() == 0) {
                plugin.getArenaManager().unregisterGame(game);
            }
        }

        return removed;
    }

    /**
     * Gets all available arenas
     *
     * @return List of arena names
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
     * Gets player count in an arena
     *
     * @param arenaName Arena name
     * @return Number of players
     */
    public int getPlayersInArena(String arenaName) {
        Game game = plugin.getArenaManager().getGame(arenaName);
        return game != null ? game.getPlayerCount() : 0;
    }

    /**
     * Gets max players allowed in an arena
     *
     * @param arenaName Arena name
     * @return Max players
     */
    public int getMaxPlayersInArena(String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        return arena != null ? arena.getMaxPlayers() : 0;
    }

    /**
     * Gets state of an arena
     *
     * @param arenaName Arena name
     * @return State as string
     */
    public String getArenaState(String arenaName) {
        Game game = plugin.getArenaManager().getGame(arenaName);

        if (game != null) {
            return game.getState().toString();
        }

        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena != null) {
            return arena.getState().toString();
        }

        return "UNKNOWN";
    }

    /**
     * Gets total number of players in all games
     *
     * @return Total player count
     */
    public int getTotalPlayersInGames() {
        int count = 0;

        for (Game game : plugin.getArenaManager().getActiveGames()) {
            count += game.getPlayerCount();
        }

        return count;
    }

    /**
     * Sends a game chat message to all players in a game
     *
     * @param game The game
     * @param sender The sending player
     * @param message The message
     */
    public void sendGameChatMessage(Game game, Player sender, String message) {
        if (game == null) {
            plugin.getLogger().warning("Attempted to send game chat message with null game!");
            return;
        }

        if (sender == null || message == null) {
            plugin.getLogger().warning("Invalid sender or message for game chat!");
            return;
        }

        // Format message with proper prefixes depending on if player is regular or spectator
        String prefix = game.isSpectator(sender) ? "&7[&8SPECTATOR&7] " : "";
        String formattedMessage = "&7[&bGame&7] " + prefix + sender.getName() + "&f: " + message;

        // Send to all players in the game
        try {
            game.broadcastMessage(formattedMessage);
        } catch (Exception e) {
            plugin.getLogger().severe("Error broadcasting game chat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}