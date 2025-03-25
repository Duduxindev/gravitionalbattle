package com.br.gravitationalbattle.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.Team;

/**
 * Gerencia as equipes do jogo
 */
public class TeamManager {

    private final GravitationalBattle plugin;
    private Map<UUID, Team> playerTeams; // UUID do jogador -> Equipe
    private Map<String, Map<String, Team>> gameTeams; // ID do jogo -> ID da equipe -> Equipe

    // Equipes padrão
    private static final Team[] DEFAULT_TEAMS = {
            new Team("red", "Vermelho", ChatColor.RED, Color.RED),
            new Team("blue", "Azul", ChatColor.BLUE, Color.BLUE),
            new Team("green", "Verde", ChatColor.GREEN, Color.GREEN),
            new Team("yellow", "Amarelo", ChatColor.YELLOW, Color.YELLOW),
            new Team("aqua", "Ciano", ChatColor.AQUA, Color.AQUA),
            new Team("purple", "Roxo", ChatColor.LIGHT_PURPLE, Color.PURPLE),
            new Team("gray", "Cinza", ChatColor.GRAY, Color.GRAY),
            new Team("white", "Branco", ChatColor.WHITE, Color.WHITE)
    };

    public TeamManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.playerTeams = new HashMap<>();
        this.gameTeams = new HashMap<>();
    }

    /**
     * Cria equipes para um jogo
     *
     * @param game O jogo
     * @param teamCount Número de equipes
     */
    public void createTeamsForGame(Game game, int teamCount) {
        if (game == null || teamCount <= 0) return;

        String gameId = game.getArena().getName();
        Map<String, Team> teams = new HashMap<>();

        // Limitar ao número máximo de equipes predefinidas
        teamCount = Math.min(teamCount, DEFAULT_TEAMS.length);

        // Criar cópias das equipes padrão
        for (int i = 0; i < teamCount; i++) {
            Team defaultTeam = DEFAULT_TEAMS[i];
            Team gameTeam = new Team(defaultTeam.getId(), defaultTeam.getName(),
                    defaultTeam.getChatColor(), defaultTeam.getArmorColor());

            teams.put(gameTeam.getId(), gameTeam);
        }

        // Registrar equipes para este jogo
        gameTeams.put(gameId, teams);
    }

    /**
     * Remove equipes de um jogo
     *
     * @param game O jogo
     */
    public void removeTeamsForGame(Game game) {
        if (game == null) return;

        String gameId = game.getArena().getName();

        // Limpar associações jogador-equipe para este jogo
        for (UUID playerId : new ArrayList<>(playerTeams.keySet())) {
            Team team = playerTeams.get(playerId);

            // Verificar se a equipe pertence a este jogo
            Map<String, Team> teams = gameTeams.get(gameId);
            if (teams != null && teams.containsValue(team)) {
                playerTeams.remove(playerId);
            }
        }

        // Remover equipes do jogo
        gameTeams.remove(gameId);
    }

    /**
     * Adiciona um jogador a uma equipe
     *
     * @param game O jogo
     * @param player O jogador
     * @param teamId ID da equipe (opcional, se null, escolhe automaticamente)
     */
    public void addPlayerToTeam(Game game, Player player, String teamId) {
        if (game == null || player == null) return;

        String gameId = game.getArena().getName();
        Map<String, Team> teams = gameTeams.get(gameId);

        if (teams == null || teams.isEmpty()) {
            // Criar equipes se não existirem
            createTeamsForGame(game, 2);
            teams = gameTeams.get(gameId);
        }

        // Remover jogador de qualquer equipe atual no mesmo jogo
        removePlayerFromTeam(game, player);

        Team targetTeam = null;

        if (teamId != null && teams.containsKey(teamId)) {
            // Equipe específica solicitada
            targetTeam = teams.get(teamId);
        } else {
            // Escolher equipe com menos jogadores
            int minPlayers = Integer.MAX_VALUE;

            for (Team team : teams.values()) {
                int count = team.getMemberCount();
                if (count < minPlayers) {
                    minPlayers = count;
                    targetTeam = team;
                }
            }
        }

        if (targetTeam != null) {
            targetTeam.addMember(player);
            playerTeams.put(player.getUniqueId(), targetTeam);

            // Notificar jogador
            player.sendMessage("§aVocê foi adicionado à equipe " + targetTeam.getColoredName() + "§a!");
        }
    }

    /**
     * Remove um jogador de sua equipe atual
     *
     * @param game O jogo
     * @param player O jogador
     */
    public void removePlayerFromTeam(Game game, Player player) {
        if (game == null || player == null) return;

        UUID playerId = player.getUniqueId();
        Team team = playerTeams.get(playerId);

        if (team != null) {
            team.removeMember(player);
            playerTeams.remove(playerId);
        }
    }

    /**
     * Obtém a equipe de um jogador
     *
     * @param player O jogador
     * @return Equipe do jogador ou null
     */
    public Team getPlayerTeam(Player player) {
        if (player == null) return null;
        return playerTeams.get(player.getUniqueId());
    }

    /**
     * Obtém todas as equipes de um jogo
     *
     * @param game O jogo
     * @return Lista de equipes
     */
    public List<Team> getTeamsForGame(Game game) {
        if (game == null) return new ArrayList<>();

        String gameId = game.getArena().getName();
        Map<String, Team> teams = gameTeams.get(gameId);

        if (teams == null) return new ArrayList<>();
        return new ArrayList<>(teams.values());
    }

    /**
     * Obtém uma equipe específica de um jogo
     *
     * @param game O jogo
     * @param teamId ID da equipe
     * @return Equipe ou null
     */
    public Team getTeam(Game game, String teamId) {
        if (game == null || teamId == null) return null;

        String gameId = game.getArena().getName();
        Map<String, Team> teams = gameTeams.get(gameId);

        if (teams == null) return null;
        return teams.get(teamId);
    }

    /**
     * Distribui jogadores automaticamente em equipes
     *
     * @param game O jogo
     * @param teamCount Número de equipes
     */
    public void balanceTeams(Game game, int teamCount) {
        if (game == null || teamCount <= 0) return;

        // Obter todos os jogadores
        List<Player> players = new ArrayList<>();
        for (UUID playerId : game.getPlayers()) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }

        // Criar equipes
        createTeamsForGame(game, teamCount);

        // Distribuir jogadores
        int playerIndex = 0;
        String gameId = game.getArena().getName();
        Map<String, Team> teams = gameTeams.get(gameId);

        if (teams == null || teams.isEmpty()) return;

        List<Team> teamList = new ArrayList<>(teams.values());

        for (Player player : players) {
            Team team = teamList.get(playerIndex % teamList.size());
            playerIndex++;

            team.addMember(player);
            playerTeams.put(player.getUniqueId(), team);
        }

        // Equipar jogadores com cores da equipe
        for (Team team : teamList) {
            team.equipTeamMembers();
        }
    }
}