package com.br.gravitationalbattle.game;

/**
 * Estados possíveis para um jogo ou arena
 */
public enum GameState {
    // Estados do jogo
    WAITING,      // Aguardando jogadores suficientes
    STARTING,     // Contagem regressiva para início
    INGAME,       // Jogo em andamento
    ENDING,       // Jogo terminado, aguardando reset

    // Estados da arena
    AVAILABLE,    // Arena disponível para jogos
    MAINTENANCE   // Arena em manutenção
}