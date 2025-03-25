package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.Chunk;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.utils.MessageUtil;

public class Game {

    private final GravitationalBattle plugin;
    private final Arena arena;
    private GameState state;
    private List<UUID> players;
    private List<UUID> spectators;
    private Map<UUID, Boolean> playerAliveStatus;
    private Map<UUID, Integer> playerKills;
    private Map<UUID, Integer> playerRewards;
    private Map<UUID, Integer> playerXP;
    private Map<UUID, Location> playerStartLocations;
    private com.br.gravitationalbattle.game.GameMode gameMode;
    private BukkitTask gameTask;
    private BukkitTask countdownTask;
    private int countdown;
    private int gameTime;
    private Scoreboard healthScoreboard;

    public Game(GravitationalBattle plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.state = GameState.WAITING;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.playerAliveStatus = new HashMap<>();
        this.playerKills = new HashMap<>();
        this.playerRewards = new HashMap<>();
        this.playerXP = new HashMap<>();
        this.playerStartLocations = new HashMap<>();
        this.gameMode = com.br.gravitationalbattle.game.GameMode.SOLO; // Modo padrão
        this.countdown = plugin.getConfigManager().getStartCountdown();
        this.gameTime = 0;
        this.healthScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    /**
     * Obtém o modo de jogo atual
     *
     * @return O modo de jogo
     */
    public com.br.gravitationalbattle.game.GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Define o modo de jogo
     *
     * @param gameMode Novo modo de jogo
     */
    public void setGameMode(com.br.gravitationalbattle.game.GameMode gameMode) {
        this.gameMode = gameMode;

        // Se for baseado em equipes, inicializar equipes
        if (gameMode.isTeamBased()) {
            plugin.getTeamManager().createTeamsForGame(this, 2); // 2 equipes por padrão
        }
    }

    /**
     * Obtém a equipe de um jogador
     *
     * @param player O jogador
     * @return A equipe do jogador ou null se não estiver em uma equipe
     */
    public com.br.gravitationalbattle.game.Team getPlayerTeam(Player player) {
        if (player == null || !gameMode.isTeamBased()) {
            return null;
        }

        return plugin.getTeamManager().getPlayerTeam(player);
    }

    /**
     * Obtém a recompensa em moedas de um jogador nesta partida
     *
     * @param player O jogador
     * @return Quantidade de moedas ganhas
     */
    public int getPlayerReward(Player player) {
        if (player == null) return 0;

        // Se não tiver sido calculada ainda, calcular agora
        if (!playerRewards.containsKey(player.getUniqueId())) {
            int kills = getPlayerKills(player);
            boolean isWinner = isWinner(player);
            int reward = plugin.getRewardManager().calculateGameReward(kills, isWinner, gameTime);
            playerRewards.put(player.getUniqueId(), reward);
        }

        return playerRewards.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Obtém o XP ganho por um jogador nesta partida
     *
     * @param player O jogador
     * @return Quantidade de XP ganho
     */
    public int getPlayerXP(Player player) {
        if (player == null) return 0;

        // Se não tiver sido calculado ainda, calcular agora
        if (!playerXP.containsKey(player.getUniqueId())) {
            int kills = getPlayerKills(player);
            boolean isWinner = isWinner(player);
            int timeMinutes = gameTime / 60;
            int xp = plugin.getLevelManager().calculateGameXP(kills, isWinner, timeMinutes);
            playerXP.put(player.getUniqueId(), xp);
        }

        return playerXP.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Verifica se um jogador é o vencedor da partida
     *
     * @param player O jogador
     * @return true se for o vencedor
     */
    private boolean isWinner(Player player) {
        if (player == null) return false;

        // Se o jogador estiver vivo e for o único, é vencedor
        if (state == GameState.ENDING &&
                playerAliveStatus.getOrDefault(player.getUniqueId(), false) &&
                getAliveCount() == 1) {
            return true;
        }

        // Em modo de equipe, verificar se a equipe dele ganhou
        if (gameMode.isTeamBased()) {
            com.br.gravitationalbattle.game.Team team = getPlayerTeam(player);
            if (team != null) {
                // Lógica para verificar se a equipe ganhou
                // ...
            }
        }

        return false;
    }

    /**
     * Checks if a player is a spectator in this game
     *
     * @param player The player to check
     * @return true if player is a spectator, false otherwise
     */
    public boolean isSpectator(Player player) {
        if (player == null) return false;
        return spectators.contains(player.getUniqueId());
    }

    /**
     * Gets the current game time in seconds
     *
     * @return Game time in seconds
     */
    public int getGameTime() {
        return gameTime;
    }

    /**
     * Gets the current countdown value
     *
     * @return Current countdown value in seconds
     */
    public int getCountdown() {
        return countdown;
    }

    /**
     * Gets player kills in this game
     *
     * @param player The player
     * @return Number of kills
     */
    public int getPlayerKills(Player player) {
        return playerKills.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Records a player killing another player
     *
     * @param killer The killer
     */
    public void recordKill(Player killer) {
        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            int currentKills = playerKills.getOrDefault(killerUUID, 0);
            playerKills.put(killerUUID, currentKills + 1);

            // Update killer's scoreboard
            plugin.getScoreboardManager().updateGameScoreboard(killer, this);
        }
    }

    /**
     * Gets player assists in this game
     *
     * @param player The player
     * @return Number of assists
     */
    public int getPlayerAssists(Player player) {
        // This is a placeholder - we're not implementing assists but the method
        // needs to exist to satisfy the ScoreboardManager
        return 0;
    }

    /**
     * Gets all online players in this game
     *
     * @return List of online players
     */
    public List<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();

        // Add regular players
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                onlinePlayers.add(player);
            }
        }

        // Add spectators
        for (UUID spectatorId : spectators) {
            Player spectator = Bukkit.getPlayer(spectatorId);
            if (spectator != null && spectator.isOnline()) {
                onlinePlayers.add(spectator);
            }
        }

        return onlinePlayers;
    }

    /**
     * Adds a spectator to the game
     *
     * @param player Player to add as spectator
     * @return true if successfully added, false otherwise
     */
    public boolean addSpectator(Player player) {
        // Check if player is already in the game or spectating
        if (players.contains(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "&cYou are already playing in this game!");
            return false;
        }

        if (spectators.contains(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "&cYou are already spectating this game!");
            return false;
        }

        // Add player to spectators
        spectators.add(player.getUniqueId());

        // Prepare player for spectating
        prepareSpectator(player);

        // Send message
        MessageUtil.sendMessage(player, "&7You are now spectating the game in &e" + arena.getDisplayName() + "&7.");

        return true;
    }

    /**
     * Adds a player to the game
     *
     * @param player Player to add
     * @return true if successfully added, false otherwise
     */
    public boolean addPlayer(Player player) {
        // Check if game is full
        if (players.size() >= arena.getMaxPlayers()) {
            MessageUtil.sendMessage(player, "&cThis game is full!");
            return false;
        }

        // Check if player is already in the game
        if (players.contains(player.getUniqueId())) {
            MessageUtil.sendMessage(player, "&cYou are already in this game!");
            return false;
        }

        // Check if game is joinable
        if (state != GameState.WAITING && state != GameState.STARTING) {
            MessageUtil.sendMessage(player, "&cThis game is already in progress!");
            return false;
        }

        // Add player to the game
        players.add(player.getUniqueId());
        playerAliveStatus.put(player.getUniqueId(), true);
        playerKills.put(player.getUniqueId(), 0);

        // Teleport player to arena
        teleportPlayerToArena(player);

        // Store player's starting location
        playerStartLocations.put(player.getUniqueId(), player.getLocation().clone());

        // Prepare player (clear inventory, set gamemode, etc.)
        preparePlayer(player);

        // Add player to team if in team mode
        if (gameMode.isTeamBased()) {
            plugin.getTeamManager().addPlayerToTeam(this, player, null);
        }

        // Send join message
        broadcastMessage("&e" + player.getName() + " &7has joined the game! &8(" +
                getPlayerCount() + "/" + arena.getMaxPlayers() + ")");

        // Check if game should start
        checkGameStartConditions();

        return true;
    }

    /**
     * Marks a player as dead in the game
     *
     * @param player The player
     */
    public void playerDied(Player player) {
        UUID playerId = player.getUniqueId();

        if (players.contains(playerId)) {
            playerAliveStatus.put(playerId, false);

            // Remove battle items and effects
            removeBattleItemsAndEffects(player);

            // Broadcast death message
            broadcastMessage("&c" + player.getName() + " &7has died! &8(" + getAliveCount() + " players remaining)");

            // Make player a spectator
            makeSpectator(player);

            // Check if game should end
            checkGameEndConditions();
        }
    }

    /**
     * Makes a player a spectator
     *
     * @param player The player
     */
    public void makeSpectator(Player player) {
        UUID playerId = player.getUniqueId();

        if (players.contains(playerId) && !spectators.contains(playerId)) {
            spectators.add(playerId);

            // Set up spectator mode
            prepareSpectator(player);

            MessageUtil.sendMessage(player, "&7You are now spectating the game.");
        }
    }

    /**
     * Removes a player from the game
     *
     * @param player Player to remove
     * @return true if player was in the game and removed
     */
    public boolean removePlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            return false;
        }

        // Remove battle items and effects
        removeBattleItemsAndEffects(player);

        // Remove player from lists
        players.remove(player.getUniqueId());
        playerAliveStatus.remove(player.getUniqueId());
        playerKills.remove(player.getUniqueId());
        playerStartLocations.remove(player.getUniqueId());

        // Remove player from team
        if (gameMode.isTeamBased()) {
            plugin.getTeamManager().removePlayerFromTeam(this, player);
        }

        // Remove from spectators if needed
        if (spectators.contains(player.getUniqueId())) {
            spectators.remove(player.getUniqueId());
        }

        // Remove from health display
        removePlayerFromHealthDisplay(player);

        // Send leave message
        broadcastMessage("&e" + player.getName() + " &7has left the game! &8(" +
                getPlayerCount() + "/" + arena.getMaxPlayers() + ")");

        // Reset player (teleport to lobby, restore inventory, etc.)
        resetPlayer(player);

        // Ensure player is properly unregistered
        plugin.getArenaManager().unregisterPlayer(player);

        // Check if game should end due to lack of players
        checkGameEndConditions();

        return true;
    }

    /**
     * Gets the current number of players in the game
     *
     * @return Number of players
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Gets the number of players that are still alive in the game
     *
     * @return The count of alive players
     */
    public int getAliveCount() {
        int count = 0;

        for (UUID playerId : players) {
            // If the player has an alive status of true or doesn't have an entry
            // (which means they're still alive by default)
            Boolean status = playerAliveStatus.get(playerId);
            if (status == null || status) {
                count++;
            }
        }

        return count;
    }

    /**
     * Gets the current game state
     *
     * @return Current game state
     */
    public GameState getState() {
        return state;
    }

    /**
     * Sets the game state
     *
     * @param state New state
     */
    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * Gets the arena for this game
     *
     * @return The arena
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * Checks if a player is in the game
     *
     * @param player Player to check
     * @return true if player is in game
     */
    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    /**
     * Gets all players in the game
     *
     * @return List of player UUIDs
     */
    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Forces the game to start immediately
     */
    public void forceStart() {
        if (state == GameState.WAITING || state == GameState.STARTING) {
            if (countdownTask != null) {
                countdownTask.cancel();
            }
            countdown = 1; // Set countdown to 1 to start quickly
            startCountdown();
        }
    }

    /**
     * Forces the game to end immediately
     */
    public void forceEnd() {
        endGame();
    }

    // Helper methods

    private void teleportPlayerToArena(Player player) {
        try {
            // Garantir que o jogador esteja no servidor antes de teleportar
            if (!player.isOnline()) {
                plugin.getLogger().warning("Tentativa de teleportar jogador offline: " + player.getName());
                return;
            }

            if (arena.getSpawnPointCount() > 0) {
                // Choose a random spawn point
                int index = (int)(Math.random() * arena.getSpawnPointCount());
                Location spawnLocation = arena.getSpawnPoints().get(index);

                // Verifica se a localização é válida
                if (spawnLocation == null || spawnLocation.getWorld() == null) {
                    plugin.getLogger().warning("Localização de spawn inválida para arena: " + arena.getName());

                    // Tentar recuperar usando o mundo da arena
                    World arenaWorld = Bukkit.getWorld(arena.getWorldUUID());
                    if (arenaWorld != null) {
                        teleportSafely(player, arenaWorld.getSpawnLocation());
                    } else {
                        // Fallback para o spawn do mundo padrão
                        teleportSafely(player, Bukkit.getWorlds().get(0).getSpawnLocation());
                    }
                    return;
                }

                // Verificar se o mundo existe
                World targetWorld = spawnLocation.getWorld();
                if (targetWorld == null) {
                    plugin.getLogger().warning("Mundo não encontrado para arena: " + arena.getName());
                    teleportSafely(player, Bukkit.getWorlds().get(0).getSpawnLocation());
                    return;
                }

                // Carregar chunk antes do teleporte
                Chunk chunk = targetWorld.getChunkAt(spawnLocation);
                if (!chunk.isLoaded()) {
                    chunk.load(true);
                }

                // Teleportar com segurança
                teleportSafely(player, spawnLocation);
            } else {
                // Sem pontos de spawn, teleportar para spawn do mundo
                World world = Bukkit.getWorld(arena.getWorldUUID());
                if (world != null) {
                    // Carregar chunk antes do teleporte
                    Location worldSpawn = world.getSpawnLocation();
                    Chunk chunk = world.getChunkAt(worldSpawn);
                    if (!chunk.isLoaded()) {
                        chunk.load(true);
                    }

                    teleportSafely(player, worldSpawn);
                } else {
                    plugin.getLogger().warning("Mundo não encontrado para arena: " + arena.getName());
                    // Fallback para o spawn do mundo padrão
                    teleportSafely(player, Bukkit.getWorlds().get(0).getSpawnLocation());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao teleportar jogador para arena: " + e.getMessage());
            e.printStackTrace();

            // Fallback para o spawn do mundo padrão
            teleportSafely(player, Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }

    /**
     * Teleporta um jogador com segurança usando tarefas Bukkit para evitar problemas no thread principal
     *
     * @param player O jogador a ser teleportado
     * @param location Localização para onde teleportar
     */
    private void teleportSafely(final Player player, final Location location) {
        // Verificar se o jogador e a localização são válidos
        if (player == null || !player.isOnline() || location == null || location.getWorld() == null) {
            plugin.getLogger().warning("Tentativa de teleporte com player ou localização inválidos");
            return;
        }

        // Executar em uma tarefa síncrona para garantir thread-safety
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    // Verificar novamente se o jogador ainda está online
                    if (!player.isOnline()) return;

                    // Tentar integração direta com Multiverse se disponível
                    if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
                        // Ir para o mundo primeiro e depois para a localização exata
                        Location safeLocation = location.clone();
                        World targetWorld = safeLocation.getWorld();

                        // Registrar a tentativa no log
                        plugin.getLogger().info("Tentando teleportar " + player.getName() +
                                " para mundo " + targetWorld.getName() + " via métodos seguros");

                        // Primeiro teleportar para o spawn do mundo
                        player.teleport(targetWorld.getSpawnLocation());

                        // Depois para a localização específica
                        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) {
                                    player.teleport(safeLocation);
                                    plugin.getLogger().info("Teleporte em duas etapas concluído para " + player.getName());
                                }
                            }
                        }, 10L); // Meio segundo depois
                    } else {
                        // Sem Multiverse, teleportar diretamente
                        boolean success = player.teleport(location);
                        plugin.getLogger().info("Teleporte direto para " + player.getName() +
                                " resultado: " + (success ? "sucesso" : "falha"));
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Erro durante teleporte seguro: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void preparePlayer(Player player) {
        // Clear inventory, set gamemode, etc.
        player.getInventory().clear();
        player.setGameMode(org.bukkit.GameMode.ADVENTURE); // Using ADVENTURE to prevent block breaking
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
    }

    /**
     * Give battle items and effects to a player
     */
    private void giveBattleItemsAndEffects(Player player) {
        PlayerInventory inv = player.getInventory();

        // Em modo de equipe, dar armadura da cor da equipe
        if (gameMode.isTeamBased()) {
            com.br.gravitationalbattle.game.Team team = getPlayerTeam(player);
            if (team != null) {
                inv.setHelmet(team.createArmorItem(Material.LEATHER_HELMET));
                inv.setChestplate(team.createArmorItem(Material.LEATHER_CHESTPLATE));
                inv.setLeggings(team.createArmorItem(Material.LEATHER_LEGGINGS));
                inv.setBoots(team.createArmorItem(Material.LEATHER_BOOTS));
            } else {
                // Dar armadura de diamante padrão se não tiver time (não deveria acontecer)
                inv.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                inv.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            }
        } else {
            // Dar armadura de diamante padrão
            inv.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            inv.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            inv.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            inv.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        }

        // Dar espada de ferro
        inv.setItem(0, new ItemStack(Material.IRON_SWORD));

        // Dar arco
        inv.setItem(1, new ItemStack(Material.BOW));

        // Dar maçãs douradas
        inv.setItem(2, new ItemStack(Material.GOLDEN_APPLE, 16));

        // Dar flechas
        inv.setItem(9, new ItemStack(Material.ARROW, 64));

        // Dar item de habilidade se tiver uma configurada
        if (plugin.getAbilityManager() != null) {
            String activeAbility = plugin.getAbilityManager().getActiveAbility(player);
            if (activeAbility != null) {
                plugin.getAbilityManager().giveAbilityItem(player);
            }
        }

        // Setup health display
        setupPlayerHealthDisplay(player);
    }

    /**
     * Remove battle items and effects from a player
     */
    private void removeBattleItemsAndEffects(Player player) {
        // Clear inventory
        player.getInventory().clear();

        // Remove all armor
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Remove from health display
        removePlayerFromHealthDisplay(player);
    }

    /**
     * Setup player health display above their head
     */
    private void setupPlayerHealthDisplay(Player player) {
        // Create team for player if it doesn't exist
        Team team = healthScoreboard.getTeam(player.getName());
        if (team == null) {
            team = healthScoreboard.registerNewTeam(player.getName());
        }

        // Add player to team
        team.addPlayer(player);

        // Set name prefix/suffix to show health
        updatePlayerHealthDisplay(player);

        // Apply scoreboard to all players
        for (UUID playerId : players) {
            Player gamePlayer = Bukkit.getPlayer(playerId);
            if (gamePlayer != null) {
                gamePlayer.setScoreboard(healthScoreboard);
            }
        }
    }

    /**
     * Update player health display
     */
    private void updatePlayerHealthDisplay(Player player) {
        Team team = healthScoreboard.getTeam(player.getName());
        if (team != null) {
            int health = (int) Math.ceil(player.getHealth());
            String healthDisplay = " &c❤ " + health;
            team.setSuffix(MessageUtil.colorize(healthDisplay));
        }
    }

    /**
     * Remove player from health display
     */
    private void removePlayerFromHealthDisplay(Player player) {
        Team team = healthScoreboard.getTeam(player.getName());
        if (team != null) {
            team.unregister();
        }
    }

    private void prepareSpectator(Player player) {
        // Em 1.8.8, não há modo espectador, então usamos o modo aventura e habilitamos voo
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();

        // Encontrar um jogador para teleportar
        Player targetPlayer = null;
        for (UUID playerId : players) {
            Player target = Bukkit.getPlayer(playerId);
            Boolean isAlive = playerAliveStatus.get(playerId);

            if (target != null && (isAlive == null || isAlive)) {
                targetPlayer = target;
                break;
            }
        }

        // Teleportar para o jogador ou para o spawn do mundo
        if (targetPlayer != null) {
            player.teleport(targetPlayer.getLocation());
        } else {
            World world = Bukkit.getWorld(arena.getWorldUUID());
            if (world != null) {
                player.teleport(world.getSpawnLocation());
            }
        }
    }

    private void resetPlayer(Player player) {
        // Reset game-specific settings
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);

        // Remove armor
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Set proper gamemode
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);

        // Update scoreboard to lobby
        plugin.getScoreboardManager().setLobbyScoreboard(player);

        // Teleport to lobby
        teleportToLobby(player);

        // Dar a esmeralda da loja de volta para o jogador
        plugin.giveShopEmerald(player);
    }

    private void checkGameStartConditions() {
        // Logic to start the game when enough players have joined
        if (state == GameState.WAITING && getPlayerCount() >= arena.getMinPlayers()) {
            startCountdown();
        }
    }

    private void checkGameEndConditions() {
        // Check if game should end due to lack of players
        if ((state == GameState.INGAME && getAliveCount() <= 1) ||
                getPlayerCount() == 0) {
            endGame();
        }
    }

    private void startCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }

        state = GameState.STARTING;

        // Configurar área de espera com camas para retornar ao lobby
        setupWaitingArea();

        // Start countdown task
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdown <= 0) {
                    cancel();
                    startGame();
                    return;
                }

                // Update all players' scoreboards
                for (UUID playerId : players) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null) {
                        plugin.getScoreboardManager().updateGameScoreboard(player, Game.this);
                    }
                }

                // Broadcast countdown messages at specific intervals
                if (countdown <= 5 || countdown == 10 || countdown == 30) {
                    broadcastMessage("&eGravitational Battle starting in &c" + countdown + " &eseconds!");
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    /**
     * Configura a área de espera do jogo
     */
    private void setupWaitingArea() {
        // Adicionar uma cama para cada jogador no lobby
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                // Dar cama para voltar ao lobby
                ItemStack bedItem = new ItemStack(Material.STICK);
                ItemMeta meta = bedItem.getItemMeta();
                meta.setDisplayName("§c§lVoltar ao Lobby");
                meta.setLore(Arrays.asList(
                        "§7Clique para sair do jogo",
                        "§7e voltar ao lobby principal"
                ));
                bedItem.setItemMeta(meta);
                player.getInventory().setItem(8, bedItem); // Último slot da hotbar
            }
        }
    }

    private void startGame() {
        state = GameState.INGAME;

        // Give items to players without teleporting them
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                // Update scoreboard
                plugin.getScoreboardManager().updateGameScoreboard(player, this);

                // Give battle items and effects
                giveBattleItemsAndEffects(player);

                // Set gamemode to allow movement
                player.setGameMode(org.bukkit.GameMode.ADVENTURE);
            }
        }

        broadcastMessage("&a&lThe Gravitational Battle has started! Good luck!");

        // Start game timer
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                gameTime++;

                // Check for game end conditions
                checkGameEndConditions();

                // Update scoreboards every 5 seconds
                if (gameTime % 5 == 0) {
                    for (UUID playerId : players) {
                        Player player = Bukkit.getPlayer(playerId);
                        if (player != null) {
                            plugin.getScoreboardManager().updateGameScoreboard(player, Game.this);
                        }
                    }
                }

                // Update health displays every second
                for (UUID playerId : players) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && playerAliveStatus.getOrDefault(playerId, false)) {
                        updatePlayerHealthDisplay(player);
                    }
                }

                // Force end after max time
                if (gameTime >= plugin.getConfigManager().getGameTime()) {
                    broadcastMessage("&c&lGame time limit reached! Ending the game...");
                    endGame();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    private void endGame() {
        // Cancel tasks
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        // Set state to ending
        state = GameState.ENDING;

        // Find winner if any
        Player winner = null;
        for (UUID playerId : players) {
            Boolean isAlive = playerAliveStatus.get(playerId);
            if (isAlive != null && isAlive) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    winner = player;
                    break;
                }
            }
        }

        // Announce winner if there is one
        if (winner != null && getState() == GameState.ENDING) {
            broadcastMessage("&a&l" + winner.getName() + " &e&lhas won the Gravitational Battle!");

            // Update stats
            plugin.getStatsManager().addWin(winner);
            plugin.getStatsManager().saveStats();
        } else {
            broadcastMessage("&c&lThe Gravitational Battle has ended with no winner!");
        }

        // Primeiro: preparar as informações de recompensa para cada jogador
        final Map<UUID, Integer> finalRewards = new HashMap<>();
        final Map<UUID, Integer> finalXP = new HashMap<>();

        // Calculate rewards for all players and store for later
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                int kills = getPlayerKills(player);
                boolean isWinner = (winner != null && player.getUniqueId().equals(winner.getUniqueId()));

                // Calcular moedas
                int tokens = plugin.getRewardManager().calculateGameReward(kills, isWinner, gameTime);
                finalRewards.put(playerId, tokens);
                playerRewards.put(playerId, tokens);

                // Calcular XP
                int timeMinutes = gameTime / 60;
                int xp = plugin.getLevelManager().calculateGameXP(kills, isWinner, timeMinutes);
                finalXP.put(playerId, xp);
                playerXP.put(playerId, xp);

                // Mostrar na tela
                MessageUtil.sendMessage(player, "&a&l=== Recompensas da Partida ===");
                MessageUtil.sendMessage(player, "&6Moedas: &e+" + tokens);
                MessageUtil.sendMessage(player, "&bXP: &a+" + xp);

                // Atualizar scoreboard
                plugin.getScoreboardManager().updateGameScoreboard(player, this);

                // Remove battle items and effects
                removeBattleItemsAndEffects(player);
            }
        }

        // Segundo: salvar a arena e o mundo antes de prosseguir
        final World arenaWorld = Bukkit.getWorld(arena.getWorldUUID());
        final Location arenaLobbyLocation = arena.getLobbyLocation();
        final Location globalLobbyLocation = plugin.getConfigManager().getLobbyLocation();

        // Terceiro: definir a tarefa para retornar os jogadores
        new BukkitRunnable() {
            @Override
            public void run() {
                // Unregister game first to prevent re-entry problems
                plugin.getArenaManager().unregisterGame(Game.this);

                // Clean up teams if needed
                if (gameMode.isTeamBased()) {
                    plugin.getTeamManager().removeTeamsForGame(Game.this);
                }

                // Copy the list to avoid concurrent modification
                List<UUID> toRemove = new ArrayList<>(players);
                for (UUID playerId : toRemove) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        // Update stats
                        plugin.getStatsManager().addGamePlayed(player);

                        // Add tokens from saved reward
                        int reward = finalRewards.getOrDefault(playerId, 0);
                        if (reward > 0) {
                            plugin.getRewardManager().addTokens(player, reward);
                        }

                        // Add XP from saved reward
                        int xp = finalXP.getOrDefault(playerId, 0);
                        if (xp > 0) {
                            plugin.getLevelManager().addXP(player, xp);
                        }

                        // Properly clear player status
                        players.remove(playerId);
                        playerAliveStatus.remove(playerId);

                        // Unregister player from arena manager before teleporting
                        plugin.getArenaManager().unregisterPlayer(player);

                        // Reset player (include teleport)
                        resetPlayerWithLobby(player, arenaWorld, arenaLobbyLocation, globalLobbyLocation);
                    }
                }

                // Clear players list
                players.clear();

                // Remove spectators too
                List<UUID> spectatorsToRemove = new ArrayList<>(spectators);
                for (UUID spectatorId : spectatorsToRemove) {
                    Player spectator = Bukkit.getPlayer(spectatorId);
                    if (spectator != null && spectator.isOnline()) {
                        // Limpar status de espectador
                        spectator.setAllowFlight(false);
                        spectator.setFlying(false);
                        spectator.setGameMode(org.bukkit.GameMode.SURVIVAL);
                        spectator.getInventory().clear();

                        // Atualizar scoreboard
                        plugin.getScoreboardManager().setLobbyScoreboard(spectator);

                        // Reset com teleporte para o lobby
                        resetPlayerWithLobby(spectator, arenaWorld, arenaLobbyLocation, globalLobbyLocation);

                        MessageUtil.sendMessage(spectator, "&aVocê foi retornado ao lobby.");

                        // Dar a esmeralda da loja de volta para o espectador
                        plugin.giveShopEmerald(spectator);
                    }
                }
                spectators.clear();
            }
        }.runTaskLater(plugin, 100L); // 5 seconds delay
    }

    /**
     * Reset a player with proper lobby teleportation
     *
     * @param player The player to reset
     * @param arenaWorld The arena world
     * @param arenaLobby The arena-specific lobby location
     * @param globalLobby The global lobby location
     */
    private void resetPlayerWithLobby(Player player, World arenaWorld, Location arenaLobby, Location globalLobby) {
        // Reset game-specific settings
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);

        // Remove armor
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Set proper gamemode
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);

        // Update scoreboard to lobby
        plugin.getScoreboardManager().setLobbyScoreboard(player);

        // Teleport to lobby with priority:
        // 1. Arena-specific lobby if available (staying in the same world)
        // 2. Global lobby if available
        // 3. Default world spawn as last resort
        if (arenaLobby != null && arenaWorld != null && arenaLobby.getWorld().equals(arenaWorld)) {
            // Log the teleport attempt
            plugin.getLogger().info("Teleportando jogador " + player.getName() +
                    " para o lobby da arena: " + arenaLobby.getWorld().getName() +
                    " X:" + arenaLobby.getX() + " Y:" + arenaLobby.getY() + " Z:" + arenaLobby.getZ());

            // Teleport to arena-specific lobby (keeps player in same world)
            teleportSafely(player, arenaLobby);
        } else if (globalLobby != null) {
            // Log the teleport attempt
            plugin.getLogger().info("Teleportando jogador " + player.getName() +
                    " para o lobby global: " + globalLobby.getWorld().getName() +
                    " X:" + globalLobby.getX() + " Y:" + globalLobby.getY() + " Z:" + globalLobby.getZ());

            // Teleport to global lobby
            teleportSafely(player, globalLobby);
        } else {
            // Log the teleport attempt
            plugin.getLogger().warning("Nenhum lobby configurado, teleportando jogador " + player.getName() +
                    " para o spawn do mundo padrão.");

            // Fallback to main world spawn
            teleportSafely(player, Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        // Dar a esmeralda da loja de volta para o jogador
        plugin.giveShopEmerald(player);
    }

    /**
     * Helper method to teleport player to lobby
     * Fixed version that ensures players stay in the arena world
     */
    private void teleportToLobby(Player player) {
        // Obter o mundo da arena atual
        World arenaWorld = Bukkit.getWorld(arena.getWorldUUID());
        Location arenaLobby = arena.getLobbyLocation();
        Location globalLobby = plugin.getConfigManager().getLobbyLocation();

        // Teleportar com a lógica correta de prioridade
        resetPlayerWithLobby(player, arenaWorld, arenaLobby, globalLobby);
    }

    /**
     * Broadcasts a message to all players in the game
     */
    public void broadcastMessage(String message) {
        if (message == null) {
            plugin.getLogger().warning("Tentativa de transmitir mensagem nula no jogo: " + arena.getName());
            return;
        }

        // Registrar mensagem para depuração
        plugin.getLogger().info("Transmitindo para o jogo " + arena.getName() + ": " + message);

        // Enviar para todos os jogadores
        for (UUID playerId : players) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null && p.isOnline()) {
                p.sendMessage(MessageUtil.colorize(message));
            }
        }

        // Enviar para todos os espectadores
        for (UUID spectatorId : spectators) {
            Player p = Bukkit.getPlayer(spectatorId);
            if (p != null && p.isOnline()) {
                p.sendMessage(MessageUtil.colorize(message));
            }
        }
    }
}