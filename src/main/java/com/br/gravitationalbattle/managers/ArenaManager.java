package com.br.gravitationalbattle.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena; // Pacote correto
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil; // Import para MessageUtil

public class ArenaManager {
    private final GravitationalBattle plugin;
    private List<Arena> arenas;

    public ArenaManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.arenas = new ArrayList<>();
    }

    /**
     * Obtém uma lista com os nomes de todas as arenas
     * @return Lista de nomes das arenas
     */
    public List<String> getArenaNames() {
        List<String> names = new ArrayList<>();
        for (Arena arena : getAllArenas()) {
            names.add(arena.getName());
        }
        return names;
    }

    /**
     * Obtém todas as arenas registradas
     * @return Lista de arenas
     */
    public List<Arena> getAllArenas() {
        return arenas;
    }

    /**
     * Obtém uma arena pelo nome
     * @param name Nome da arena
     * @return Arena encontrada ou null se não existir
     */
    public Arena getArena(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }
        return null;
    }

    /**
     * Cria uma nova arena
     * @param name Nome da arena
     * @param displayName Nome de exibição da arena
     * @param player Jogador que está criando a arena
     * @return true se a arena foi criada com sucesso
     */
    public boolean createArena(String name, String displayName, Player player) {
        if (getArena(name) != null) {
            MessageUtil.sendMessage(player, "&cUma arena com este nome já existe!"); // Corrigido
            return false;
        }

        Arena arena = new Arena(name, player.getLocation());
        arena.setDisplayName(displayName); // Definir o nome de exibição
        arenas.add(arena);
        saveArenas(); // Salva as arenas em arquivo
        return true;
    }

    /**
     * Remove uma arena
     * @param name Nome da arena
     * @param player Jogador que está removendo a arena
     * @return true se a arena foi removida com sucesso
     */
    public boolean deleteArena(String name, Player player) {
        Arena arena = getArena(name);
        if (arena == null) {
            MessageUtil.sendMessage(player, "&cArena não encontrada."); // Corrigido
            return false;
        }

        arenas.remove(arena);
        saveArenas(); // Salva as arenas em arquivo
        return true;
    }

    /**
     * Adiciona um ponto de spawn a uma arena
     * @param arenaName Nome da arena
     * @param player Jogador que está adicionando o ponto de spawn
     * @return true se o ponto de spawn foi adicionado com sucesso
     */
    public boolean addSpawnPoint(String arenaName, Player player) {
        Arena arena = getArena(arenaName);
        if (arena == null) {
            MessageUtil.sendMessage(player, "&cArena não encontrada."); // Corrigido
            return false;
        }

        arena.addSpawnPoint(player.getLocation());
        saveArenas(); // Salva as arenas em arquivo
        return true;
    }

    /**
     * Carrega as arenas do arquivo de configuração
     */
    public void loadArenas() {
        // Implementação para carregar arenas de arquivo
        // Esta seria a implementação real
    }

    /**
     * Salva as arenas no arquivo de configuração
     */
    public void saveArenas() {
        // Implementação para salvar arenas em arquivo
        // Esta seria a implementação real
    }

    /**
     * Obtém um jogo associado a uma arena
     * @param arenaName Nome da arena
     * @return Jogo associado à arena ou null se não houver
     */
    public Game getGame(String arenaName) {
        // Esta seria a implementação real que busca ou cria um jogo para a arena
        return null;
    }

}