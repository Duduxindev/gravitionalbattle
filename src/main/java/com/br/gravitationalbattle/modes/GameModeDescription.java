package com.br.gravitationalbattle.modes;

import org.bukkit.ChatColor;

import com.br.gravitationalbattle.game.GameMode;

/**
 * Classe para descrever os diferentes modos de jogo disponíveis ou planejados
 */
public class GameModeDescription {

    /**
     * Retorna uma descrição completa de um modo de jogo específico
     *
     * @param mode O modo de jogo
     * @return Descrição detalhada do modo
     */
    public static String getFullDescription(GameMode mode) {
        StringBuilder description = new StringBuilder();

        description.append(ChatColor.GOLD).append("=== ").append(ChatColor.YELLOW)
                .append(mode.getDisplayName()).append(ChatColor.GOLD).append(" ===\n");

        switch (mode) {
            case SOLO:
                description.append(ChatColor.WHITE).append("No modo Solo, cada jogador luta por si mesmo em uma arena gravitacional.\n")
                        .append(ChatColor.WHITE).append("Objetivo: Ser o último sobrevivente.\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Força da gravidade varia aleatoriamente\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Itens e powerups aparecem pela arena\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Nenhuma ajuda de outros jogadores\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Multiplicador de recompensas: 1.0x");
                break;

            case DUOS:
                description.append(ChatColor.WHITE).append("No modo Duos, os jogadores formam equipes de 2 pessoas.\n")
                        .append(ChatColor.WHITE).append("Objetivo: Ser a última equipe com jogadores vivos.\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Equipes identificadas por cores\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Membros da equipe podem reviver aliados caídos\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Comunicação em equipe é vital\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Multiplicador de recompensas: 1.2x");
                break;

            case SQUADS:
                description.append(ChatColor.WHITE).append("No modo Esquadrões, os jogadores formam equipes de 4 pessoas.\n")
                        .append(ChatColor.WHITE).append("Objetivo: Ser a última equipe com jogadores vivos.\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Equipes maiores permitem estratégias mais complexas\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Cada jogador pode ter um papel específico\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Coordenar ataques em grupo é essencial\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Multiplicador de recompensas: 1.5x");
                break;

            case TEAM_VS_TEAM:
                description.append(ChatColor.WHITE).append("No modo Equipe contra Equipe, duas grandes equipes se enfrentam.\n")
                        .append(ChatColor.WHITE).append("Objetivo: Eliminar todos os jogadores da equipe adversária.\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Equipes grande vermelho vs. azul\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Respawn limitado para cada jogador\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Arena dividida em território de cada equipe\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Pontuação baseada em abates e objetivos");
                break;

            case CAPTURE_THE_FLAG:
                description.append(ChatColor.WHITE).append("No modo Capturar a Bandeira, equipes competem para roubar a bandeira adversária.\n")
                        .append(ChatColor.WHITE).append("Objetivo: Capturar a bandeira inimiga e levá-la para sua base.\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Cada equipe protege sua própria bandeira\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Jogador com bandeira tem movimentos reduzidos\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Equipe com mais capturas vence\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Sistema de respawn rápido");
                break;

            case DOMINATION:
                description.append(ChatColor.WHITE).append("No modo Dominação, equipes lutam pelo controle de pontos estratégicos.\n")
                        .append(ChatColor.WHITE).append("Objetivo: Controlar a maioria dos pontos por mais tempo.\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("3-5 pontos de controle espalhados pelo mapa\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Pontos geram pontuação ao longo do tempo\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Defesa e ataque estratégicos são necessários\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Partida com tempo limite fixo");
                break;

            case RACE:
                description.append(ChatColor.WHITE).append("No modo Corrida, jogadores competem em um percurso desafiador.\n")
                        .append(ChatColor.WHITE).append("Objetivo: Ser o primeiro a completar todas as etapas do percurso.\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Gravidade alterada em diferentes seções\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Checkpoints para marcar progresso\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Obstáculos e armadilhas no caminho\n")
                        .append(ChatColor.YELLOW).append("• ").append(ChatColor.GRAY).append("Habilidades especiais de movimento disponíveis");
                break;
        }

        // Adicionar observação sobre disponibilidade se não for o modo SOLO, que é o único implementado completamente
        if (mode != GameMode.SOLO) {
            description.append("\n").append(ChatColor.RED).append("Nota: Este modo ainda está em desenvolvimento e será disponibilizado em breve!");
        }

        return description.toString();
    }
}