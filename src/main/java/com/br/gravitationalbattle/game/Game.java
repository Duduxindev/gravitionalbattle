package com.br.gravitationalbattle.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena; // IMPORTAÇÃO CORRIGIDA
import com.br.gravitationalbattle.utils.MessageUtil;

// Resto do código continua igual...

public class Game {
    private final GravitationalBattle plugin;
    private final Arena arena;
    private GameState state;
    private final List<Player> players;
    private final List<Player> spectators;
    private final Map<UUID, ItemStack[]> playerInventories;
    private final Map<UUID, ItemStack[]> playerArmor;
    private final Map<UUID, GameMode> playerGameModes;
    private final Map<UUID, Float> playerExp;
    private final Map<UUID, Integer> playerLevels;
    private final Map<UUID, Location> playerLocations;

    private BukkitTask countdownTask;
    private int countdown;
    private int gameTime;
    private BukkitTask gameTimeTask;

    /**
     * Construtor do jogo
     * @param plugin Instância do plugin
     * @param arena Arena onde o jogo ocorrerá
     */
    public Game(GravitationalBattle plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.state = GameState.WAITING;
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.playerInventories = new HashMap<>();
        this.playerArmor = new HashMap<>();
        this.playerGameModes = new HashMap<>();
        this.playerExp = new HashMap<>();
        this.playerLevels = new HashMap<>();
        this.playerLocations = new HashMap<>();
    }

    /**
     * Verifica se o jogador está neste jogo
     * @param player Jogador a verificar
     * @return true se o jogador está no jogo
     */
    public boolean isPlayerInGame(Player player) {
        return players.contains(player);
    }

    /**
     * Verifica se o jogador está espectando este jogo
     * @param player Jogador a verificar
     * @return true se o jogador está espectando
     */
    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    /**
     * Adiciona um jogador ao jogo
     * @param player Jogador a adicionar
     * @return true se o jogador foi adicionado com sucesso
     */
    public boolean addPlayer(Player player) {
        // Verifica se o jogo já começou ou está cheio
        if (state == GameState.INGAME || state == GameState.ENDING) {
            MessageUtil.sendMessage(player, "&cEste jogo já está em andamento.");
            return false;
        }

        if (players.size() >= arena.getMaxPlayers()) {
            MessageUtil.sendMessage(player, "&cEsta arena está cheia.");
            return false;
        }

        // Salva o estado do jogador
        savePlayerState(player);

        // Adiciona o jogador à lista
        players.add(player);

        // Prepara o jogador para o jogo
        preparePlayer(player);

        // Teleporta para o lobby da arena
        player.teleport(arena.getLobbyLocation());

        // Notifica todos os jogadores
        broadcastMessage("&a" + player.getName() + " &7entrou no jogo! &e(" + players.size() + "/" + arena.getMaxPlayers() + ")");

        // Se atingiu o mínimo de jogadores, inicia contagem
        if (players.size() >= arena.getMinPlayers() && state == GameState.WAITING) {
            startCountdown();
        }

        return true;
    }

    /**
     * Remove um jogador do jogo
     * @param player Jogador a remover
     */
    public void removePlayer(Player player) {
        if (players.contains(player)) {
            players.remove(player);

            // Restaura o estado do jogador
            restorePlayerState(player);

            // Notifica os jogadores
            if (state == GameState.WAITING || state == GameState.STARTING) {
                broadcastMessage("&c" + player.getName() + " &7saiu do jogo! &e(" + players.size() + "/" + arena.getMaxPlayers() + ")");

                // Cancela a contagem se não houver jogadores suficientes
                if (players.size() < arena.getMinPlayers() && state == GameState.STARTING) {
                    cancelCountdown();
                }
            } else if (state == GameState.INGAME) {
                broadcastMessage("&c" + player.getName() + " &7saiu do jogo!");

                // Verifica se resta apenas um jogador
                checkWinner();
            }
        }
    }

    /**
     * Adiciona um espectador ao jogo
     * @param player Jogador a adicionar como espectador
     * @return true se o jogador foi adicionado como espectador com sucesso
     */
    public boolean addSpectator(Player player) {
        if (state != GameState.INGAME) {
            MessageUtil.sendMessage(player, "&cNão há jogo em andamento nesta arena para assistir.");
            return false;
        }

        // Salva o estado do jogador
        savePlayerState(player);

        // Adiciona à lista de espectadores
        spectators.add(player);

        // Configura o jogador como espectador
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(arena.getLobbyLocation());

        MessageUtil.sendMessage(player, "&aVocê agora está assistindo ao jogo na arena &e" + arena.getDisplayName() + "&a.");

        return true;
    }

    /**
     * Remove um espectador do jogo
     * @param player Jogador a remover da espectação
     */
    public void removeSpectator(Player player) {
        if (spectators.contains(player)) {
            spectators.remove(player);
            restorePlayerState(player);
            MessageUtil.sendMessage(player, "&aVocê parou de assistir ao jogo.");
        }
    }

    /**
     * Força o início do jogo mesmo sem atingir o número mínimo de jogadores
     */
    public void forceStart() {
        // Verifica se o jogo pode ser iniciado à força
        if (state != GameState.WAITING && state != GameState.STARTING) {
            return;
        }

        // Se não houver jogadores suficientes, não inicia
        if (players.isEmpty()) {
            return;
        }

        // Cancela qualquer contagem regressiva atual
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        // Altera o estado para INGAME
        state = GameState.INGAME;

        // Inicia o jogo (teleporta jogadores, distribui kits, etc.)
        startGame();

        // Notifica os jogadores
        broadcastMessage("&aO jogo foi iniciado à força por um administrador!");
    }

    /**
     * Força o fim do jogo atual
     */
    public void forceEnd() {
        if (state != GameState.INGAME) {
            return;
        }

        // Altera o estado
        state = GameState.ENDING;

        // Notifica os jogadores
        broadcastMessage("&cO jogo foi encerrado à força por um administrador!");

        // Finaliza o jogo (teleporta jogadores de volta, limpa inventários, etc.)
        endGame(null); // Null significa que não há vencedor
    }

    /**
     * Inicia a contagem regressiva para o início do jogo
     */
    private void startCountdown() {
        state = GameState.STARTING;
        countdown = arena.getCountdownTime();

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                countdown--;

                if (countdown <= 0) {
                    // Inicia o jogo quando a contagem chegar a zero
                    cancel();
                    countdownTask = null;
                    startGame();
                } else if (countdown <= 5 || countdown % 10 == 0) {
                    // Anuncia o tempo restante em intervalos regulares
                    broadcastMessage("&aO jogo começará em &e" + countdown + " &asegundos!");

                    // Toca um som para todos os jogadores
                    for (Player player : players) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Executa a cada segundo
    }

    /**
     * Cancela a contagem regressiva e retorna ao estado de espera
     */
    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        state = GameState.WAITING;
        broadcastMessage("&cContagem cancelada! &7Não há jogadores suficientes.");
    }

    /**
     * Inicia o jogo
     */
    private void startGame() {
        state = GameState.INGAME;

        // Teleporta cada jogador para um spawn point
        List<Location> spawns = new ArrayList<>(arena.getSpawnPoints());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i < spawns.size()) {
                player.teleport(spawns.get(i));
            } else {
                player.teleport(arena.getRandomSpawnPoint());
            }

            // Configurações iniciais do jogador no jogo
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SURVIVAL);

            // Fornece equipamento inicial (implementar conforme necessário)
            // giveKit(player);

            // Efeito de início
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        }

        // Mensagem de início
        broadcastMessage("&a&lO JOGO COMEÇOU!");
        broadcastMessage("&eSobreviva utilizando a gravidade a seu favor!");

        // Inicia o contador de tempo de jogo
        gameTime = 0;
        gameTimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                gameTime++;

                // Efeitos periódicos durante o jogo (implementar conforme necessário)
                // applyGameEffects();

                // Define um limite de tempo para o jogo (se necessário)
                if (gameTime >= 600) { // 10 minutos
                    endGame(null); // Empate por tempo
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Finaliza o jogo
     * @param winner Jogador vencedor (null se não houver vencedor)
     */
    private void endGame(Player winner) {
        // Cancela tarefas
        if (gameTimeTask != null) {
            gameTimeTask.cancel();
            gameTimeTask = null;
        }

        // Altera o estado
        state = GameState.ENDING;

        // Anuncia o vencedor
        if (winner != null) {
            broadcastMessage("&a&lJOGO FINALIZADO!");
            broadcastMessage("&e&l" + winner.getName() + " &a&lvenceu o jogo!");

            // Atualiza estatísticas do vencedor
            // plugin.getStatsManager().addWin(winner);
        } else {
            broadcastMessage("&a&lJOGO FINALIZADO!");
            broadcastMessage("&e&lNão houve vencedor.");
        }

        // Aguarda alguns segundos e depois restaura os jogadores
        new BukkitRunnable() {
            @Override
            public void run() {
                // Restaura todos os jogadores
                for (Player player : new ArrayList<>(players)) {
                    removePlayer(player);
                }

                // Restaura espectadores
                for (Player spectator : new ArrayList<>(spectators)) {
                    removeSpectator(spectator);
                }

                // Reseta o jogo para o estado inicial
                state = GameState.WAITING;
            }
        }.runTaskLater(plugin, 100L); // 5 segundos
    }

    /**
     * Verifica se há um vencedor
     */
    private void checkWinner() {
        if (state != GameState.INGAME) {
            return;
        }

        // Se restar apenas um jogador, ele é o vencedor
        if (players.size() == 1) {
            endGame(players.get(0));
        }
        // Se não houver jogadores, finaliza sem vencedor
        else if (players.isEmpty()) {
            endGame(null);
        }
    }

    /**
     * Salva o estado atual do jogador
     * @param player Jogador para salvar o estado
     */
    private void savePlayerState(Player player) {
        playerInventories.put(player.getUniqueId(), player.getInventory().getContents());
        playerArmor.put(player.getUniqueId(), player.getInventory().getArmorContents());
        playerGameModes.put(player.getUniqueId(), player.getGameMode());
        playerExp.put(player.getUniqueId(), player.getExp());
        playerLevels.put(player.getUniqueId(), player.getLevel());
        playerLocations.put(player.getUniqueId(), player.getLocation());

        // Limpa o inventário e estados
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);

        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Restaura o estado salvo do jogador
     * @param player Jogador para restaurar o estado
     */
    private void restorePlayerState(Player player) {
        UUID uuid = player.getUniqueId();

        if (playerInventories.containsKey(uuid)) {
            player.getInventory().setContents(playerInventories.get(uuid));
            playerInventories.remove(uuid);
        }

        if (playerArmor.containsKey(uuid)) {
            player.getInventory().setArmorContents(playerArmor.get(uuid));
            playerArmor.remove(uuid);
        }

        if (playerGameModes.containsKey(uuid)) {
            player.setGameMode(playerGameModes.get(uuid));
            playerGameModes.remove(uuid);
        }

        if (playerExp.containsKey(uuid)) {
            player.setExp(playerExp.get(uuid));
            playerExp.remove(uuid);
        }

        if (playerLevels.containsKey(uuid)) {
            player.setLevel(playerLevels.get(uuid));
            playerLevels.remove(uuid);
        }

        if (playerLocations.containsKey(uuid)) {
            player.teleport(playerLocations.get(uuid));
            playerLocations.remove(uuid);
        } else {
            // Teleporta para o lobby principal se a localização original não estiver disponível
            Location mainLobby = plugin.getConfigManager().getLobbyLocation();
            if (mainLobby != null) {
                player.teleport(mainLobby);
            }
        }
    }

    /**
     * Prepara o jogador para o jogo (lobby)
     * @param player Jogador a preparar
     */
    private void preparePlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);

        // Aqui você pode adicionar itens específicos do lobby
        // como seletores de kit, etc.
    }

    /**
     * Envia mensagem para todos os jogadores e espectadores no jogo
     * @param message Mensagem a enviar
     */
    private void broadcastMessage(String message) {
        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                "&8[&6GravitationalBattle&8] &r" + message);

        for (Player player : players) {
            player.sendMessage(formattedMessage);
        }

        for (Player spectator : spectators) {
            spectator.sendMessage(formattedMessage);
        }
    }

    /**
     * @return A arena associada a este jogo
     */
    public Arena getArena() {
        return arena;
    }

    /**
     * @return O estado atual do jogo
     */
    public GameState getState() {
        return state;
    }

    /**
     * @return Lista de jogadores no jogo
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * @return Número de jogadores no jogo
     */
    public int getPlayerCount() {
        return players.size();
    }
    
}