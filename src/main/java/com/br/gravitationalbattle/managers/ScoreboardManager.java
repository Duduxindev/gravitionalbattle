package com.br.gravitationalbattle.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final GravitationalBattle plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;

    public ScoreboardManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new HashMap<>();
    }

    /**
     * Atualiza o scoreboard de um jogador com base em seu jogo atual
     *
     * @param player O jogador
     */
    public void updateScoreboard(Player player) {
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null) {
            // Jogador está em um jogo, mostre o scoreboard de jogo
            updateGameScoreboard(player, game);
        } else {
            // Jogador está no lobby, mostre o scoreboard de lobby
            updateLobbyScoreboard(player);
        }
    }

    /**
     * Atualiza o scoreboard de lobby para um jogador
     *
     * @param player O jogador
     */
    public void updateLobbyScoreboard(Player player) {
        Scoreboard scoreboard = getScoreboard(player);
        Objective objective = getObjective(scoreboard, "lobby");

        // Limpar scores anteriores
        clearScores(objective);

        // Definir título
        objective.setDisplayName(ChatColor.GOLD + "Batalha Gravitacional");

        // Use uma abordagem diferente para 1.8.8

        setScore(objective, ChatColor.YELLOW + "Nível: " + ChatColor.WHITE + plugin.getLevelManager().getPlayerLevel(player), 13);
        setScore(objective, ChatColor.YELLOW + "Prestígio: " + ChatColor.WHITE + plugin.getLevelManager().getPlayerPrestige(player), 12);
        setScore(objective, "", 11);
        setScore(objective, ChatColor.YELLOW + "Moedas: " + ChatColor.WHITE + plugin.getRewardManager().getPlayerTokens(player), 10);
        setScore(objective, ChatColor.YELLOW + "Abates: " + ChatColor.WHITE + plugin.getStatsManager().getPlayerKills(player), 9);
        setScore(objective, ChatColor.YELLOW + "Vitórias: " + ChatColor.WHITE + plugin.getStatsManager().getPlayerWins(player), 8);
        setScore(objective, "", 7);
        setScore(objective, ChatColor.WHITE + "Jogadores em Partida: " + ChatColor.GREEN + plugin.getGameManager().getTotalPlayersInGames(), 6);
        setScore(objective, "", 5);
        setScore(objective, ChatColor.YELLOW + "purplemc.net", 4);

        // Aplicar scoreboard
        player.setScoreboard(scoreboard);
    }

    /**
     * Atualiza o scoreboard de jogo para um jogador
     *
     * @param player O jogador
     * @param game O jogo
     */
    public void updateGameScoreboard(Player player, Game game) {
        Scoreboard scoreboard = getScoreboard(player);
        Objective objective = getObjective(scoreboard, "game");

        // Limpar scores anteriores
        clearScores(objective);

        // Definir título
        objective.setDisplayName(ChatColor.GOLD + "Batalha Gravitacional");

        // Definir scores baseados no estado do jogo
        switch (game.getState()) {
            case WAITING:
                setScore(objective, ChatColor.GRAY + "Aguardando jogadores...", 15);
                setScore(objective, "", 14);
                setScore(objective, ChatColor.WHITE + "Modo: " + ChatColor.GREEN + game.getGameMode().getDisplayName(), 13);
                setScore(objective, ChatColor.WHITE + "Jogadores: " + ChatColor.GREEN + game.getPlayerCount() + "/" + game.getArena().getMaxPlayers(), 12);
                setScore(objective, ChatColor.WHITE + "Necessário: " + ChatColor.GREEN + game.getArena().getMinPlayers(), 11);
                setScore(objective, "", 10);
                setScore(objective, ChatColor.YELLOW + "Use a cama para sair", 9);
                break;

            case STARTING:
            case COUNTDOWN:
                setScore(objective, ChatColor.GRAY + "Iniciando em " + ChatColor.YELLOW + game.getCountdown() + ChatColor.GRAY + " segundos", 15);
                setScore(objective, "", 14);
                setScore(objective, ChatColor.WHITE + "Modo: " + ChatColor.GREEN + game.getGameMode().getDisplayName(), 13);
                setScore(objective, ChatColor.WHITE + "Jogadores: " + ChatColor.GREEN + game.getPlayerCount() + "/" + game.getArena().getMaxPlayers(), 12);
                setScore(objective, "", 11);
                setScore(objective, ChatColor.YELLOW + "Use a cama para sair", 10);
                break;

            case INGAME:
                setScore(objective, ChatColor.WHITE + "Jogadores Restantes: " + ChatColor.GREEN + game.getAliveCount(), 15);
                setScore(objective, "", 14);
                setScore(objective, ChatColor.WHITE + "Modo: " + ChatColor.GREEN + game.getGameMode().getDisplayName(), 13);
                setScore(objective, ChatColor.WHITE + "Abates: " + ChatColor.GREEN + game.getPlayerKills(player), 12);

                // Mostrar equipe se estiver em modo de equipes
                if (game.getGameMode().isTeamBased()) {
                    com.br.gravitationalbattle.game.Team team = game.getPlayerTeam(player);
                    if (team != null) {
                        setScore(objective, ChatColor.WHITE + "Equipe: " + team.getColoredName(), 11);
                    }
                }

                // Mostrar habilidade ativa e recarga
                String ability = plugin.getAbilityManager().getActiveAbility(player);
                if (ability != null) {
                    int cooldown = plugin.getAbilityManager().getCooldown(player);
                    if (cooldown > 0) {
                        setScore(objective, ChatColor.WHITE + "Habilidade: " + ChatColor.RED + cooldown + "s", 10);
                    } else {
                        setScore(objective, ChatColor.WHITE + "Habilidade: " + ChatColor.GREEN + "Pronta", 10);
                    }
                }

                setScore(objective, ChatColor.WHITE + "Tempo de Jogo: " + ChatColor.GREEN + formatTime(game.getGameTime()), 9);
                break;

            case ENDING:
                setScore(objective, ChatColor.GRAY + "Jogo terminando...", 15);

                // Mostrar ganhos da partida
                setScore(objective, "", 14);
                setScore(objective, ChatColor.WHITE + "Moedas ganhas: " + ChatColor.GOLD + game.getPlayerReward(player), 13);
                setScore(objective, ChatColor.WHITE + "XP ganho: " + ChatColor.AQUA + game.getPlayerXP(player), 12);
                break;

            default:
                break;
        }

        // Rodapé comum
        setScore(objective, "", 5);
        setScore(objective, ChatColor.YELLOW + "purplemc.net", 4);

        // Aplicar scoreboard
        player.setScoreboard(scoreboard);
    }

    /**
     * Formata o tempo em segundos para MM:SS
     *
     * @param seconds Tempo em segundos
     * @return Tempo formatado
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * Obtém um scoreboard para um jogador, criando se necessário
     */
    private Scoreboard getScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        if (!playerScoreboards.containsKey(uuid)) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            playerScoreboards.put(uuid, scoreboard);
            return scoreboard;
        }

        return playerScoreboards.get(uuid);
    }

    /**
     * Obtém um objetivo para um scoreboard, criando se necessário
     */
    private Objective getObjective(Scoreboard scoreboard, String name) {
        Objective objective = scoreboard.getObjective(name);

        if (objective == null) {
            // No 1.8.8 o construtor é diferente
            objective = scoreboard.registerNewObjective(name, "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        return objective;
    }

    /**
     * Limpa todos os scores de um objetivo
     */
    private void clearScores(Objective objective) {
        Scoreboard scoreboard = objective.getScoreboard();

        // No 1.8.8, precisamos lidar com isso de maneira diferente
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
    }

    /**
     * Define um score no objetivo
     */
    private void setScore(Objective objective, String text, int value) {
        Score score = objective.getScore(text);
        score.setScore(value);
    }

    /**
     * Define o scoreboard de lobby para um jogador
     *
     * @param player O jogador
     */
    public void setLobbyScoreboard(Player player) {
        // Remover qualquer scoreboard anterior
        if (playerScoreboards.containsKey(player.getUniqueId())) {
            playerScoreboards.remove(player.getUniqueId());
        }

        // Criar uma nova scoreboard limpa
        updateLobbyScoreboard(player);
    }

    /**
     * Remove os dados de scoreboard de um jogador
     *
     * @param player O jogador
     */
    public void removePlayer(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Atualiza os scoreboards de todos os jogadores
     */
    public void updateAllScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }

    /**
     * Atualiza todos os scoreboards em um jogo específico
     *
     * @param game O jogo
     */
    public void updateGameScoreboards(Game game) {
        if (game == null) return;

        for (UUID playerId : game.getPlayers()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                updateGameScoreboard(player, game);
            }
        }
    }

    /**
     * Configura o objetivo de saúde para mostrar saúde dos jogadores acima da cabeça
     *
     * @param player O jogador
     * @param scoreboard O scoreboard
     */
    public void setupHealthObjective(Player player, Scoreboard scoreboard) {
        // Remover objetivo existente se necessário
        Objective healthObj = scoreboard.getObjective("health");
        if (healthObj != null) {
            healthObj.unregister();
        }

        // Criar novo objetivo de saúde
        healthObj = scoreboard.registerNewObjective("health", "health");
        healthObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        healthObj.setDisplayName("❤");

        // Aplicar ao jogador
        player.setScoreboard(scoreboard);
    }

    /**
     * Limpa todos os dados armazenados
     * (útil para recarregar o plugin)
     */
    public void clearAll() {
        // Restaurar scoreboard padrão para todos os jogadores
        for (UUID uuid : playerScoreboards.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }

        // Limpar mapa de scoreboards
        playerScoreboards.clear();
    }
}