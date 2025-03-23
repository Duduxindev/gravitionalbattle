package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

public class Game {

    private final GravitationalBattle plugin;
    private final Arena arena;
    private final List<UUID> players;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> spectators;
    private final Map<UUID, Integer> kills;
    private final Map<UUID, ItemStack[]> inventories;
    private final Map<UUID, ItemStack[]> armorContents;
    private final Map<UUID, Integer> expLevels;
    private final Map<UUID, Float> expProgress;
    private final Map<UUID, GameMode> gameModes;

    private GameState state;
    private int countdown;
    private int gameTime;
    private BukkitTask countdownTask;
    private BukkitTask gameTask;

    private int gravityDirection; // 0: normal, 1: up, 2: stronger, 3: weaker
    private int gravityChangeInterval;
    private int timeUntilGravityChange;

    public Game(GravitationalBattle plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.players = new ArrayList<>();
        this.alivePlayers = new HashSet<>();
        this.spectators = new HashSet<>();
        this.kills = new HashMap<>();
        this.inventories = new HashMap<>();
        this.armorContents = new HashMap<>();
        this.expLevels = new HashMap<>();
        this.expProgress = new HashMap<>();
        this.gameModes = new HashMap<>();

        this.state = GameState.WAITING;
        this.countdown = plugin.getConfigManager().getGameStartDelay();
        this.gameTime = plugin.getConfigManager().getGameDuration();

        this.gravityDirection = 0;
        this.gravityChangeInterval = 30; // Change gravity every 30 seconds
        this.timeUntilGravityChange = gravityChangeInterval;
    }

    public void addPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Check if already in the game
        if (players.contains(playerUUID)) {
            return;
        }

        // Add player to the game
        players.add(playerUUID);
        kills.put(playerUUID, 0);

        // Store player's original state
        savePlayerState(player);

        // Teleport to waiting area
        player.teleport(arena.getRandomSpawnPoint().getLocation());

        // Prepare player for the game
        preparePlayer(player);

        // Update player's scoreboard
        plugin.getScoreboardManager().setWaitingScoreboard(player, this);

        // Broadcast join message
        MessageUtil.broadcastMessageToGame(this, "player-joined",
                "player", player.getName(),
                "current", String.valueOf(players.size()),
                "max", String.valueOf(plugin.getConfigManager().getMaxPlayers()));

        // Check if should start countdown
        checkGameStart();
    }

    public void removePlayer(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Check if player is in this game
        if (!players.contains(playerUUID)) {
            return;
        }

        // Remove player from lists
        players.remove(playerUUID);
        alivePlayers.remove(playerUUID);
        spectators.remove(playerUUID);

        // Restore player's state
        restorePlayerState(player);

        // Teleport to lobby
        if (plugin.getConfigManager().getLobbyLocation() != null) {
            player.teleport(plugin.getConfigManager().getLobbyLocation());
        }

        // Reset player's scoreboard
        plugin.getScoreboardManager().setLobbyScoreboard(player);

        // Broadcast leave message
        MessageUtil.broadcastMessageToGame(this, "player-left",
                "player", player.getName(),
                "current", String.valueOf(players.size()),
                "max", String.valueOf(plugin.getConfigManager().getMaxPlayers()));

        // Check if game should continue
        if (state == GameState.WAITING || state == GameState.COUNTDOWN) {
            checkGameStart();
        } else if (state == GameState.INGAME) {
            checkGameEnd();
        }

        // If no players left, remove the game
        if (players.isEmpty() && state != GameState.ENDING) {
            end(null);
        }
    }

    public void addSpectator(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Check if already in the game
        if (!players.contains(playerUUID) && !spectators.contains(playerUUID)) {
            players.add(playerUUID);
            spectators.add(playerUUID);

            // Store player's original state
            savePlayerState(player);

            // Setup spectator
            player.setGameMode(GameMode.SPECTATOR);

            // Teleport to a random alive player or arena spawn if none
            if (!alivePlayers.isEmpty()) {
                Player target = null;
                for (UUID uuid : alivePlayers) {
                    target = Bukkit.getPlayer(uuid);
                    if (target != null && target.isOnline()) {
                        break;
                    }
                }
                if (target != null) {
                    player.teleport(target);
                } else {
                    player.teleport(arena.getRandomSpawnPoint().getLocation());
                }
            } else {
                player.teleport(arena.getRandomSpawnPoint().getLocation());
            }

            // Update scoreboard
            plugin.getScoreboardManager().setGameScoreboard(player, this);

            // Send message
            MessageUtil.sendMessage(player, "spectate-join");
        } else if (players.contains(playerUUID) && !spectators.contains(playerUUID) && !alivePlayers.contains(playerUUID)) {
            // Player died in this game, make them a spectator
            spectators.add(playerUUID);
            player.setGameMode(GameMode.SPECTATOR);

            // Keep them in the same location where they died
        }
    }

    private void savePlayerState(Player player) {
        UUID playerUUID = player.getUniqueId();
        inventories.put(playerUUID, player.getInventory().getContents());
        armorContents.put(playerUUID, player.getInventory().getArmorContents());
        expLevels.put(playerUUID, player.getLevel());
        expProgress.put(playerUUID, player.getExp());
        gameModes.put(playerUUID, player.getGameMode());

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setLevel(0);
        player.setExp(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void restorePlayerState(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (inventories.containsKey(playerUUID)) {
            player.getInventory().setContents(inventories.get(playerUUID));
            inventories.remove(playerUUID);
        } else {
            player.getInventory().clear();
        }

        if (armorContents.containsKey(playerUUID)) {
            player.getInventory().setArmorContents(armorContents.get(playerUUID));
            armorContents.remove(playerUUID);
        } else {
            player.getInventory().setArmorContents(null);
        }

        if (expLevels.containsKey(playerUUID)) {
            player.setLevel(expLevels.get(playerUUID));
            expLevels.remove(playerUUID);
        } else {
            player.setLevel(0);
        }

        if (expProgress.containsKey(playerUUID)) {
            player.setExp(expProgress.get(playerUUID));
            expProgress.remove(playerUUID);
        } else {
            player.setExp(0);
        }

        if (gameModes.containsKey(playerUUID)) {
            player.setGameMode(gameModes.get(playerUUID));
            gameModes.remove(playerUUID);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void preparePlayer(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.ADVENTURE);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private void giveGameItems(Player player) {
        // Give items based on the game's needs
        // This would typically be weapons, armor, food, etc.
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.SURVIVAL);
    }

    private void checkGameStart() {
        int minPlayers = plugin.getConfigManager().getMinPlayers();

        if (players.size() >= minPlayers) {
            if (state == GameState.WAITING) {
                startCountdown();
            }
        } else {
            if (state == GameState.COUNTDOWN) {
                cancelCountdown();
            }
        }
    }

    private void startCountdown() {
        state = GameState.COUNTDOWN;

        // Start countdown task
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    start();
                    cancel();
                    return;
                }

                if (countdown % 5 == 0 || countdown <= 5) {
                    MessageUtil.broadcastMessageToGame(Game.this, "game-starting",
                            "time", String.valueOf(countdown));

                    // Play sound to all players
                    for (UUID uuid : players) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        }
                    }
                }

                // Update scoreboards
                for (UUID uuid : players) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        plugin.getScoreboardManager().updateWaitingScoreboard(player, Game.this);
                    }
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void cancelCountdown() {
        state = GameState.WAITING;
        countdown = plugin.getConfigManager().getGameStartDelay();

        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        MessageUtil.broadcastMessageToGame(this, "countdown-cancelled");

        // Update scoreboards
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getScoreboardManager().updateWaitingScoreboard(player, this);
            }
        }
    }

    private void start() {
        state = GameState.INGAME;

        // Reset countdown for next time
        countdown = plugin.getConfigManager().getGameStartDelay();

        // Move all players to alive set
        alivePlayers.addAll(players);
        alivePlayers.removeAll(spectators);

        // Teleport players to spawn points
        List<SpawnPoint> spawnPoints = arena.getSpawnPoints();
        int spawnIndex = 0;

        for (UUID playerUUID : alivePlayers) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                // Teleport to spawn point
                if (spawnIndex < spawnPoints.size()) {
                    SpawnPoint spawnPoint = spawnPoints.get(spawnIndex);
                    player.teleport(spawnPoint.getLocation());
                    spawnPoint.setInUse(true);
                    spawnIndex++;
                } else {
                    // Fallback to random spawn if we ran out of spawn points
                    player.teleport(arena.getRandomSpawnPoint().getLocation());
                }

                // Give game items
                giveGameItems(player);

                // Update scoreboard
                plugin.getScoreboardManager().setGameScoreboard(player, this);
            }
        }

        // Broadcast start message
        MessageUtil.broadcastMessageToGame(this, "game-started");

        // Play sound to all players
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);

                // Send title
                MessageUtil.sendTitle(player, "&6&lJOGO INICIADO", "&eElimina todos os outros jogadores!");
            }
        }

        // Start game timer
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameTime <= 0) {
                    end(null); // Time's up, no specific winner
                    cancel();
                    return;
                }

                // Update time
                gameTime--;

                // Time for gravity change?
                timeUntilGravityChange--;
                if (timeUntilGravityChange <= 0) {
                    changeGravity();
                    timeUntilGravityChange = gravityChangeInterval;
                }

                // Apply gravity effects
                applyGravityEffects();

                // Update scoreboard every second
                for (UUID uuid : players) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        plugin.getScoreboardManager().updateGameScoreboard(player, Game.this);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void changeGravity() {
        // Randomly select a new gravity direction
        int oldDirection = gravityDirection;

        while (gravityDirection == oldDirection) {
            gravityDirection = new java.util.Random().nextInt(4);
        }

        // Announce the gravity change
        String directionName;
        switch (gravityDirection) {
            case 0:
                directionName = "NORMAL";
                break;
            case 1:
                directionName = "INVERTIDA";
                break;
            case 2:
                directionName = "ALTA";
                break;
            case 3:
                directionName = "BAIXA";
                break;
            default:
                directionName = "NORMAL";
                break;
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                MessageUtil.sendTitle(player, "&b&lGRAVIDADE ALTERADA", "&e" + directionName, 10, 40, 10);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }
    }

    private void applyGravityEffects() {
        for (UUID uuid : alivePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                Vector velocity = player.getVelocity();

                switch (gravityDirection) {
                    case 0: // Normal gravity
                        // Do nothing, let Minecraft handle it
                        break;
                    case 1: // Inverted gravity
                        if (player.isOnGround()) {
                            player.setVelocity(new Vector(velocity.getX(), 0.5, velocity.getZ()));
                        } else {
                            player.setVelocity(new Vector(velocity.getX(),
                                    Math.min(0.3, velocity.getY() + 0.06), velocity.getZ()));
                        }
                        break;
                    case 2: // Stronger gravity
                        if (!player.isOnGround()) {
                            player.setVelocity(new Vector(velocity.getX(),
                                    Math.max(-0.5, velocity.getY() - 0.07), velocity.getZ()));
                        }
                        break;
                    case 3: // Weaker gravity
                        if (!player.isOnGround()) {
                            player.setVelocity(new Vector(velocity.getX(),
                                    Math.max(-0.08, velocity.getY() - 0.01), velocity.getZ()));
                        }
                        break;
                }
            }
        }
    }

    public void playerKilled(Player player, Player killer) {
        UUID playerUUID = player.getUniqueId();

        if (!alivePlayers.contains(playerUUID)) {
            return; // Player not in this game or already dead
        }

        // Remove from alive players
        alivePlayers.remove(playerUUID);

        // Award kill to killer if exists
        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            if (players.contains(killerUUID)) {
                int killCount = kills.getOrDefault(killerUUID, 0) + 1;
                kills.put(killerUUID, killCount);

                // Update stats
                plugin.getStatsManager().incrementKills(killerUUID);

                // Update killer's scoreboard
                plugin.getScoreboardManager().updateGameScoreboard(killer, this);

                // Send kill message
                MessageUtil.broadcastMessageToGame(this, "player-killed",
                        "player", player.getName(),
                        "killer", killer.getName());
            } else {
                // No specific killer (e.g., environment)
                MessageUtil.broadcastMessageToGame(this, "player-died",
                        "player", player.getName());
            }
        } else {
            // No specific killer (e.g., environment)
            MessageUtil.broadcastMessageToGame(this, "player-died",
                    "player", player.getName());
        }

        // Add player as spectator after death
        addSpectator(player);

        // Check if game should end
        checkGameEnd();
    }

    private void checkGameEnd() {
        if (alivePlayers.size() <= 1) {
            Player winner = null;

            if (alivePlayers.size() == 1) {
                UUID winnerUUID = alivePlayers.iterator().next();
                winner = Bukkit.getPlayer(winnerUUID);
            }

            // End the game
            end(winner);
        }
    }

    private void end(Player winner) {
        // Stop any ongoing tasks
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        // Set game state to ending
        state = GameState.ENDING;

        // Reset arena spawn points
        for (SpawnPoint spawnPoint : arena.getSpawnPoints()) {
            spawnPoint.reset();
        }

        // Handle winner and rewards
        if (winner != null) {
            UUID winnerUUID = winner.getUniqueId();

            // Broadcast win message
            MessageUtil.broadcastMessageToGame(this, "game-ended",
                    "winner", winner.getName());

            // Update winner's stats
            plugin.getStatsManager().incrementWins(winnerUUID);

            // Give titles to everyone
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    if (player.getUniqueId().equals(winnerUUID)) {
                        MessageUtil.sendTitle(player, "&6&lVITÓRIA!", "&eVocê venceu o jogo!", 10, 70, 20);
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    } else {
                        MessageUtil.sendTitle(player, "&c&lFIM DE JOGO", "&e" + winner.getName() + " &fvenceu o jogo!", 10, 70, 20);
                        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.0f);
                    }
                }
            }
        } else {
            // No winner (draw or time ran out)
            MessageUtil.broadcastMessageToGame(this, "game-ended-no-winner");

            // Give titles to everyone
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    MessageUtil.sendTitle(player, "&c&lFIM DE JOGO", "&eNão houve vencedor!", 10, 70, 20);
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.0f);
                }
            }
        }

        // Update players' stats for participation
        for (UUID uuid : players) {
            plugin.getStatsManager().incrementGamesPlayed(uuid);
        }

        // Schedule teleport back to lobby
        new BukkitRunnable() {
            @Override
            public void run() {
                // Copy the list to avoid concurrent modification
                List<UUID> playersCopy = new ArrayList<>(players);

                // Teleport all players back to lobby
                for (UUID uuid : playersCopy) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        // Restore player's state and teleport to lobby
                        restorePlayerState(player);

                        if (plugin.getConfigManager().getLobbyLocation() != null) {
                            player.teleport(plugin.getConfigManager().getLobbyLocation());
                        }

                        // Reset scoreboard
                        plugin.getScoreboardManager().setLobbyScoreboard(player);
                    }
                }

                // Clear player lists
                players.clear();
                alivePlayers.clear();
                spectators.clear();

                // Remove the game
                plugin.getArenaManager().removeGame(Game.this);
            }
        }.runTaskLater(plugin, 100L); // 5 seconds delay
    }

    /**
     * Força o início de um jogo independentemente do número de jogadores
     */
    public void forceStart() {
        if (state == GameState.WAITING || state == GameState.COUNTDOWN) {
            // Se já em contagem regressiva, cancela primeiro
            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }

            // Define contagem para 5 segundos
            countdown = 5;

            // Inicia contagem
            state = GameState.COUNTDOWN;

            // Mensagem de início forçado
            MessageUtil.broadcastMessageToGame(this, "&a&lInício de jogo forçado por um administrador!");

            // Inicia contagem forçada
            countdownTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (countdown <= 0) {
                        start();
                        cancel();
                        return;
                    }

                    // Avisar jogadores
                    for (UUID uuid : players) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                        }
                    }

                    MessageUtil.broadcastMessageToGame(Game.this, "game-starting",
                            "time", String.valueOf(countdown));

                    // Update scoreboards
                    for (UUID uuid : players) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            plugin.getScoreboardManager().updateWaitingScoreboard(player, Game.this);
                        }
                    }

                    countdown--;
                }
            }.runTaskTimer(plugin, 20L, 20L);
        }
    }

    /**
     * Força o fim de um jogo em andamento
     */
    public void forceEnd() {
        if (state == GameState.INGAME || state == GameState.COUNTDOWN || state == GameState.WAITING) {
            MessageUtil.broadcastMessageToGame(this, "&c&lJogo encerrado por um administrador!");
            end(null);
        }
    }

    // Getters
    public Arena getArena() {
        return arena;
    }

    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    public Set<UUID> getAlivePlayers() {
        return new HashSet<>(alivePlayers);
    }

    public int getAliveCount() {
        return alivePlayers.size();
    }

    public Set<UUID> getSpectators() {
        return new HashSet<>(spectators);
    }

    public int getKills(UUID playerUUID) {
        return kills.getOrDefault(playerUUID, 0);
    }

    public GameState getState() {
        return state;
    }

    public int getCountdown() {
        return countdown;
    }

    public int getGameTime() {
        return gameTime;
    }

    public int getGravityDirection() {
        return gravityDirection;
    }

    public int getTimeUntilGravityChange() {
        return timeUntilGravityChange;
    }
}