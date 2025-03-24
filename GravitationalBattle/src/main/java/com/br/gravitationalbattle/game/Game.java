package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.managers.ScoreboardManager;
import com.br.gravitationalbattle.utils.LocationUtil;

public class Game {

    private GravitationalBattle plugin;
    private String arenaName;
    private GameState state;

    // Locations
    private Location spawnLocation;
    private List<Location> spawnPoints;

    // Player tracking
    private List<Player> players;
    private List<Player> alivePlayers;
    private List<Player> spectators;
    private Map<UUID, Integer> playerKills;
    private Map<UUID, BukkitTask> respawnTasks;

    // Game settings
    private int maxPlayers = 16;
    private int minPlayers = 2;
    private int countdown = 60;
    private boolean isStarting = false;
    private int taskId = -1;
    private int gameTime = 0;
    private int gameTimeTaskId = -1;
    private int endGameCountdown = 10;

    // Managers
    private ScoreboardManager scoreboardManager;

    /**
     * Creates a new game instance
     * @param plugin The plugin instance
     * @param arenaName The arena name
     * @param spawnLocation The arena lobby location
     */
    public Game(GravitationalBattle plugin, String arenaName, Location spawnLocation) {
        this.plugin = plugin;
        this.arenaName = arenaName;
        this.spawnLocation = spawnLocation;
        this.state = GameState.WAITING;

        this.players = new ArrayList<>();
        this.alivePlayers = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.playerKills = new HashMap<>();
        this.respawnTasks = new HashMap<>();
        this.spawnPoints = new ArrayList<>();

        // Default spawn point is the main spawn location
        this.spawnPoints.add(spawnLocation);

        this.scoreboardManager = plugin.getScoreboardManager();
    }

    /**
     * Adds a spawn point to the arena
     * @param location The spawn point location
     */
    public void addSpawnPoint(Location location) {
        spawnPoints.add(location);
    }

    /**
     * Sets the spawn points for the arena
     * @param locations The list of spawn point locations
     */
    public void setSpawnPoints(List<Location> locations) {
        this.spawnPoints = locations;
    }

    /**
     * Gets a random spawn point for a player
     * @return A randomly selected spawn point
     */
    public Location getRandomSpawnPoint() {
        if (spawnPoints.size() <= 1) {
            return spawnLocation;
        }
        return spawnPoints.get((int) (Math.random() * spawnPoints.size()));
    }

    /**
     * Adds a player to the game
     * @param player The player to add
     * @return true if the player was added successfully, false otherwise
     */
    public boolean addPlayer(Player player) {
        if (players.size() >= maxPlayers) {
            player.sendMessage(ChatColor.RED + "This arena is full!");
            return false;
        }

        if (state != GameState.WAITING && state != GameState.STARTING) {
            player.sendMessage(ChatColor.RED + "This game is already in progress!");
            return false;
        }

        // Check if player is already in a game
        if (players.contains(player)) {
            player.sendMessage(ChatColor.RED + "You are already in this game!");
            return false;
        }

        players.add(player);
        alivePlayers.add(player);
        playerKills.put(player.getUniqueId(), 0);

        // Teleport player to spawn
        player.teleport(spawnLocation);

        // Clear inventory and set game mode
        resetPlayer(player);

        // Set waiting scoreboard
        scoreboardManager.setWaitingScoreboard(player, this);

        // Play join sound
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);

        // Broadcast join message
        broadcast(ChatColor.GREEN + player.getName() + " has joined the game! " +
                ChatColor.YELLOW + "(" + players.size() + "/" + maxPlayers + ")");

        // Check if minimum players are met to start countdown
        if (players.size() >= minPlayers && !isStarting && state == GameState.WAITING) {
            startCountdown();
        }

        return true;
    }

    /**
     * Resets a player's state
     * @param player The player to reset
     */
    private void resetPlayer(Player player) {
        // Clear inventory and status effects
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setGameMode(GameMode.ADVENTURE);

        // Clear all potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Starts the countdown for the game to begin
     */
    public void startCountdown() {
        isStarting = true;
        state = GameState.STARTING;

        // Update scoreboard for all players
        scoreboardManager.updateWaitingScoreboard(this);

        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                // Check if enough players
                if (players.size() < minPlayers) {
                    broadcast(ChatColor.RED + "Not enough players! Countdown cancelled.");
                    isStarting = false;
                    state = GameState.WAITING;
                    countdown = 60;
                    scoreboardManager.updateWaitingScoreboard(Game.this);
                    cancel();
                    taskId = -1;
                    return;
                }

                // Announce countdown at specific intervals and play sounds
                if (countdown == 60 || countdown == 30 || countdown == 15 ||
                        countdown == 10 || countdown <= 5 && countdown > 0) {
                    broadcast(ChatColor.YELLOW + "Game starting in " + ChatColor.RED + countdown +
                            ChatColor.YELLOW + " second" + (countdown == 1 ? "" : "s") + "!");

                    // Play countdown sound to all players
                    for (Player player : players) {
                        if (countdown <= 5) {
                            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                        } else {
                            player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                        }
                    }
                }

                // Update scoreboard
                scoreboardManager.updateWaitingScoreboard(Game.this);

                // When countdown reaches 0
                if (countdown <= 0) {
                    startGame();
                    cancel();
                    taskId = -1;
                    return;
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L).getTaskId();
    }

    /**
     * Starts the game
     */
    public void startGame() {
        state = GameState.PLAYING;
        isStarting = false;
        countdown = 0;

        broadcast(ChatColor.GOLD + "============================================");
        broadcast(ChatColor.GREEN + "              Battle has begun!            ");
        broadcast(ChatColor.YELLOW + "Fight until only one player remains!");
        broadcast(ChatColor.GOLD + "============================================");

        // Teleport players to random spawn points and give them their gear
        for (Player player : alivePlayers) {
            player.teleport(getRandomSpawnPoint());
            givePlayerKit(player);
            player.setGameMode(GameMode.SURVIVAL);

            // Play start sound
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1.0f, 1.0f);

            // Add temporary invulnerability
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 5)); // 5 second invulnerability
        }

        // Update scoreboard
        scoreboardManager.updateGameScoreboard(this);

        // Start game timer
        gameTime = 0;
        gameTimeTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                gameTime++;

                // Update scoreboards every 10 seconds
                if (gameTime % 10 == 0) {
                    scoreboardManager.updateGameScoreboard(Game.this);
                }

                // Game length limit (optional) - end game after 10 minutes
                if (gameTime >= 600) { // 600 seconds = 10 minutes
                    broadcast(ChatColor.GOLD + "Time limit reached! Game ending...");
                    endGame();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L).getTaskId();
    }

    /**
     * Gives a player their battle kit
     * @param player The player to give the kit to
     */
    private void givePlayerKit(Player player) {
        // Clear inventory first
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        // Give iron armor
        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        // Give diamond sword
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        player.getInventory().addItem(sword);

        // Give golden apples
        ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 3);
        player.getInventory().addItem(goldenApple);

        // Give bow with infinity
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        bow.setItemMeta(bowMeta);
        player.getInventory().addItem(bow);

        // Give one arrow (required for infinity)
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        player.getInventory().addItem(arrow);
    }

    /**
     * Removes a player from the game
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        // Cancel any respawn task for this player
        if (respawnTasks.containsKey(player.getUniqueId())) {
            respawnTasks.get(player.getUniqueId()).cancel();
            respawnTasks.remove(player.getUniqueId());
        }

        boolean wasAlive = alivePlayers.contains(player);

        players.remove(player);
        alivePlayers.remove(player);
        spectators.remove(player);

        // Reset player state
        resetPlayer(player);
        scoreboardManager.removeScoreboard(player);

        // Return player to main server lobby
        // Replace with your lobby teleport location
        // player.teleport(plugin.getLobbyLocation());

        // Broadcast leave message
        if (state == GameState.WAITING || state == GameState.STARTING) {
            broadcast(ChatColor.RED + player.getName() + " has left the game! " +
                    ChatColor.YELLOW + "(" + players.size() + "/" + maxPlayers + ")");

            // Check if minimum players are still met
            if (players.size() < minPlayers && isStarting) {
                cancelCountdown();
            }

            // Update waiting scoreboard
            scoreboardManager.updateWaitingScoreboard(this);
        } else if (state == GameState.PLAYING) {
            broadcast(ChatColor.RED + player.getName() + " has left the game!");

            // Update game scoreboard
            scoreboardManager.updateGameScoreboard(this);

            // Check if game should end
            if (wasAlive) {
                checkGameEnd();
            }
        }
    }

    /**
     * Handles player death
     * @param player The player who died
     * @param killer The player who killed them (null if not killed by player)
     */
    public void playerDeath(Player player, Player killer) {
        if (!alivePlayers.contains(player)) return;

        alivePlayers.remove(player);
        spectators.add(player);
        player.setGameMode(GameMode.SPECTATOR);

        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        if (killer != null) {
            broadcast(ChatColor.YELLOW + player.getName() + " was killed by " + killer.getName() + "!");
            int kills = playerKills.getOrDefault(killer.getUniqueId(), 0) + 1;
            playerKills.put(killer.getUniqueId(), kills);

            // Play sound for killer
            killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
        } else {
            broadcast(ChatColor.YELLOW + player.getName() + " died!");
        }

        // Play death sound for everyone
        for (Player p : players) {
            p.playSound(p.getLocation(), Sound.WITHER_HURT, 0.5f, 1.0f);
        }

        // Update scoreboard
        scoreboardManager.updateGameScoreboard(this);
        scoreboardManager.setGameScoreboard(player, this); // Update for spectator

        // Check if game should end
        checkGameEnd();
    }

    /**
     * Checks if the game should end
     */
    private void checkGameEnd() {
        if (alivePlayers.size() <= 1 && state == GameState.PLAYING) {
            Player winner = null;
            if (alivePlayers.size() == 1) {
                winner = alivePlayers.get(0);
            }

            if (winner != null) {
                broadcast(ChatColor.GOLD + "============================================");
                broadcast(ChatColor.YELLOW + winner.getName() + " has won the game!");
                broadcast(ChatColor.YELLOW + "Kills: " + ChatColor.RED + playerKills.get(winner.getUniqueId()));
                broadcast(ChatColor.GOLD + "============================================");

                // Give winner some effect
                winner.playSound(winner.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                winner.playSound(winner.getLocation(), Sound.ENDERDRAGON_DEATH, 1.0f, 1.0f);

                // Launch fireworks or other victory effect
                launchFireworks(winner);
            } else {
                broadcast(ChatColor.GOLD + "============================================");
                broadcast(ChatColor.YELLOW + "Game over! Nobody won!");
                broadcast(ChatColor.GOLD + "============================================");
            }

            // End the game
            prepareEndGame(winner);
        }
    }

    /**
     * Launches fireworks for the winner
     * @param winner The winning player
     */
    private void launchFireworks(Player winner) {
        // Implementation depends on specific Bukkit API version
        // This is a placeholder for the firework launch effect
    }

    /**
     * Prepares to end the game and start the end game countdown
     * @param winner The winning player, or null if no winner
     */
    private void prepareEndGame(Player winner) {
        state = GameState.ENDING;

        // Cancel game timer if it's running
        if (gameTimeTaskId != -1) {
            Bukkit.getScheduler().cancelTask(gameTimeTaskId);
            gameTimeTaskId = -1;
        }

        // Set end game scoreboard for all players
        scoreboardManager.updateEndGameScoreboard(this, winner);

        // Start end game countdown
        new BukkitRunnable() {
            private int timeLeft = endGameCountdown;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    endGame();
                    cancel();
                    return;
                }

                // Update all players' scoreboards every second
                for (Player player : players) {
                    Scoreboard board = player.getScoreboard();
                    if (board != null) {
                        Objective objective = board.getObjective("endgame");
                        if (objective != null) {
                            // This is just a placeholder - in 1.8.8 we'd need to recreate the scoreboard
                            // or use a different approach to update it dynamically
                            scoreboardManager.setEndGameScoreboard(player, Game.this, winner);
                        }
                    }
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Ends the game completely
     */
    public void endGame() {
        // Teleport players to lobby
        for (Player player : players) {
            // Reset player state
            resetPlayer(player);
            scoreboardManager.removeScoreboard(player);

            // Teleport to lobby
            // Replace with your lobby teleport location
            // player.teleport(plugin.getLobbyLocation());

            // Send message
            player.sendMessage(ChatColor.GREEN + "Thank you for playing Arena Battle!");
        }

        // Cancel all tasks
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        if (gameTimeTaskId != -1) {
            Bukkit.getScheduler().cancelTask(gameTimeTaskId);
            gameTimeTaskId = -1;
        }

        // Clear all respawn tasks
        for (BukkitTask task : respawnTasks.values()) {
            task.cancel();
        }
        respawnTasks.clear();

        // Reset game
        players.clear();
        alivePlayers.clear();
        spectators.clear();
        playerKills.clear();
        state = GameState.WAITING;
        countdown = 60;
        isStarting = false;
        gameTime = 0;
    }

    /**
     * Cancels the countdown
     */
    private void cancelCountdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }

        isStarting = false;
        state = GameState.WAITING;
        countdown = 60;
        broadcast(ChatColor.RED + "Not enough players! Countdown cancelled.");
    }

    /**
     * Broadcasts a message to all players in the game
     * @param message The message to broadcast
     */
    public void broadcast(String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    /**
     * Gets the player with the most kills
     * @return The player with the most kills, or null if no players
     */
    public Player getTopKiller() {
        if (players.isEmpty()) return null;

        Player topKiller = null;
        int mostKills = -1;

        for (Player player : players) {
            int kills = playerKills.getOrDefault(player.getUniqueId(), 0);
            if (kills > mostKills) {
                mostKills = kills;
                topKiller = player;
            }
        }

        return topKiller;
    }

    /**
     * Saves the arena to config
     */
    public void saveArena() {
        // Implementation would depend on your config system
        plugin.getConfig().set("arenas." + arenaName + ".spawn", LocationUtil.locationToString(spawnLocation));

        // Save spawn points
        List<String> spawnPointStrings = new ArrayList<>();
        for (Location loc : spawnPoints) {
            spawnPointStrings.add(LocationUtil.locationToString(loc));
        }
        plugin.getConfig().set("arenas." + arenaName + ".spawnpoints", spawnPointStrings);

        plugin.getConfig().set("arenas." + arenaName + ".maxplayers", maxPlayers);
        plugin.getConfig().set("arenas." + arenaName + ".minplayers", minPlayers);
        plugin.saveConfig();
    }

    /**
     * Loads arena data from config
     */
    public void loadArena() {
        // Implementation would depend on your config system
        if (plugin.getConfig().contains("arenas." + arenaName)) {
            // Load spawn points if they exist
            if (plugin.getConfig().contains("arenas." + arenaName + ".spawnpoints")) {
                List<String> spawnPointStrings = plugin.getConfig().getStringList("arenas." + arenaName + ".spawnpoints");
                spawnPoints.clear();

                for (String locString : spawnPointStrings) {
                    Location loc = LocationUtil.stringToLocation(locString);
                    if (loc != null) {
                        spawnPoints.add(loc);
                    }
                }
            }

            // Load max/min players
            if (plugin.getConfig().contains("arenas." + arenaName + ".maxplayers")) {
                maxPlayers = plugin.getConfig().getInt("arenas." + arenaName + ".maxplayers");
            }

            if (plugin.getConfig().contains("arenas." + arenaName + ".minplayers")) {
                minPlayers = plugin.getConfig().getInt("arenas." + arenaName + ".minplayers");
            }
        }
    }

    // Getters and setters

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getAlivePlayers() {
        return alivePlayers;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getCountdown() {
        return countdown;
    }

    public boolean isStarting() {
        return isStarting;
    }

    public String getArenaName() {
        return arenaName;
    }

    public GameState getState() {
        return state;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public int getPlayerKills(Player player) {
        return playerKills.getOrDefault(player.getUniqueId(), 0);
    }

    public int getGameTime() {
        return gameTime;
    }

    /**
     * Enum for game states
     */
    public enum GameState {
        WAITING,
        STARTING,
        PLAYING,
        ENDING
    }
}