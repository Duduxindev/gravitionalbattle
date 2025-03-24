package com.br.gravitationalbattle.game;

public enum GameState {
    AVAILABLE,    // Arena está disponível para uso
    IN_USE,       // Arena está sendo usada por um jogo
    MAINTENANCE,  // Arena está em manutenção
    WAITING,      // Jogo está esperando por jogadores
    COUNTDOWN,    // Contagem regressiva para início do jogo
    INGAME,       // Jogo em andamento
    ENDING        // Jogo terminando
}