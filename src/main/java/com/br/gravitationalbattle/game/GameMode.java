package com.br.gravitationalbattle.game;

/**
 * Representa os diferentes modos de jogo disponíveis
 */
public enum GameMode {

    /**
     * Cada jogador por si
     */
    SOLO(false, "Solo"),

    /**
     * Times de 2 jogadores
     */
    DUOS(true, "Duos"),

    /**
     * Times de 4 jogadores
     */
    SQUADS(true, "Esquadrões"),

    /**
     * Apenas duas equipes
     */
    TEAM_VS_TEAM(true, "Equipe contra Equipe"),

    /**
     * Capturar a bandeira
     */
    CAPTURE_THE_FLAG(true, "Capturar a Bandeira"),

    /**
     * Modo com controle de ponto específico
     */
    DOMINATION(true, "Dominação"),

    /**
     * Corrida para completar objetivos
     */
    RACE(false, "Corrida");

    private final boolean teamBased;
    private final String displayName;

    GameMode(boolean teamBased, String displayName) {
        this.teamBased = teamBased;
        this.displayName = displayName;
    }

    /**
     * Verifica se o modo de jogo é baseado em equipes
     *
     * @return true se for baseado em equipes
     */
    public boolean isTeamBased() {
        return teamBased;
    }

    /**
     * Obtém o nome de exibição do modo de jogo
     *
     * @return Nome de exibição
     */
    public String getDisplayName() {
        return displayName;
    }
}