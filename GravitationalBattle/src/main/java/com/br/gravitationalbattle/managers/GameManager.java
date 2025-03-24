package com.br.gravitationalbattle.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Arena;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;
import com.br.gravitationalbattle.utils.MessageUtil;
import com.onarandombox.MultiverseCore.MultiverseCore;

/**
 * Gerencia os jogos ativos no plugin
 */
public class GameManager {

    private final GravitationalBattle plugin;
    private final MultiverseCore multiverseCore;

    public GameManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.multiverseCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (this.multiverseCore == null) {
            plugin.getLogger().severe("Multiverse-Core não encontrado! Algumas funcionalidades podem não funcionar corretamente.");
        } else {
            plugin.getLogger().info("Multiverse-Core encontrado e integrado com sucesso!");
        }
    }

    /**
     * Faz um jogador entrar em um jogo
     *
     * @param player Jogador que vai entrar
     * @param arenaName Nome da arena (ou null para escolha aleatória)
     * @return O jogo em que o jogador entrou, ou null se não foi possível entrar
     */
    public Game joinGame(Player player, String arenaName) {
        // Verifica se o jogador já está em um jogo
        Game currentGame = getPlayerGame(player.getUniqueId());
        if (currentGame != null) {
            MessageUtil.sendMessage(player, "&cVocê já está em um jogo. Use /leave para sair.");
            return null;
        }

        // Se arena não foi especificada, tenta encontrar arena disponível
        if (arenaName == null) {
            return joinRandomGame(player);
        }

        // Tenta encontrar a arena especificada
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            MessageUtil.sendMessage(player, "&cArena não encontrada: " + arenaName);
            return null;
        }

        // Verifica se a arena está disponível
        if (arena.getState() == GameState.MAINTENANCE) {
            MessageUtil.sendMessage(player, "&cEsta arena está em manutenção.");
            return null;
        }

        // Verifica se há um jogo ativo nesta arena
        Game game = plugin.getArenaManager().getGame(arenaName);

        // Se não há jogo ativo, cria um novo
        if (game == null) {
            game = plugin.getArenaManager().createGame(arena);
        }

        // Verifica se o jogo está cheio
        if (game.getPlayers().size() >= plugin.getConfigManager().getMaxPlayers() &&
                game.getState() != GameState.INGAME) {
            MessageUtil.sendMessage(player, "&cEste jogo está cheio.");
            return null;
        }

        // Verifica se o jogo já começou
        if (game.getState() == GameState.INGAME) {
            MessageUtil.sendMessage(player, "&cEste jogo já começou. Use /spectate para assistir.");
            return null;
        }

        // Adiciona o jogador ao jogo
        game.addPlayer(player);
        return game;
    }

    /**
     * Faz um jogador entrar em um jogo aleatório disponível
     *
     * @param player Jogador que vai entrar
     * @return O jogo em que o jogador entrou, ou null se não foi possível entrar
     */
    private Game joinRandomGame(Player player) {
        // Lista para arenas com jogos em espera
        List<Arena> availableArenas = new ArrayList<>();

        // Procura arenas com jogos em espera
        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            if (arena.getState() != GameState.MAINTENANCE) {
                Game game = plugin.getArenaManager().getGame(arena.getName());

                if (game != null &&
                        game.getState() != GameState.INGAME &&
                        game.getPlayers().size() < plugin.getConfigManager().getMaxPlayers()) {
                    availableArenas.add(arena);
                }
            }
        }

        // Se encontrou arenas com jogos em espera, escolhe uma aleatoriamente
        if (!availableArenas.isEmpty()) {
            Arena randomArena = availableArenas.get((int) (Math.random() * availableArenas.size()));
            Game game = plugin.getArenaManager().getGame(randomArena.getName());

            // Adiciona o jogador ao jogo
            game.addPlayer(player);
            return game;
        }

        // Caso contrário, procura uma arena disponível para criar um novo jogo
        List<Arena> emptyArenas = new ArrayList<>();

        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            if (arena.getState() != GameState.MAINTENANCE &&
                    plugin.getArenaManager().getGame(arena.getName()) == null &&
                    arena.getSpawnPointCount() >= plugin.getConfigManager().getMinPlayers()) {
                emptyArenas.add(arena);
            }
        }

        // Se encontrou arenas disponíveis, escolhe uma aleatoriamente
        if (!emptyArenas.isEmpty()) {
            Arena randomArena = emptyArenas.get((int) (Math.random() * emptyArenas.size()));
            Game game = plugin.getArenaManager().createGame(randomArena);

            // Adiciona o jogador ao jogo
            game.addPlayer(player);
            return game;
        }

        // Caso não encontre nenhum jogo disponível
        MessageUtil.sendMessage(player, "&cNão há jogos disponíveis no momento.");
        return null;
    }

    /**
     * Faz um jogador sair do jogo atual
     *
     * @param player Jogador que vai sair
     * @return true se o jogador saiu de um jogo
     */
    public boolean leaveGame(Player player) {
        Game game = getPlayerGame(player.getUniqueId());

        if (game == null) {
            MessageUtil.sendMessage(player, "&cVocê não está em nenhum jogo.");
            return false;
        }

        game.removePlayer(player);
        return true;
    }

    /**
     * Obtém o jogo em que um jogador está
     *
     * @param playerUUID UUID do jogador
     * @return O jogo em que o jogador está, ou null se não estiver em nenhum jogo
     */
    public Game getPlayerGame(UUID playerUUID) {
        return plugin.getArenaManager().getPlayerGame(playerUUID);
    }

    /**
     * Verifica se um mundo é um mundo de arena do plugin
     *
     * @param worldName Nome do mundo
     * @return true se for um mundo de arena
     */
    public boolean isArenaWorld(String worldName) {
        for (Arena arena : plugin.getArenaManager().getAllArenas()) {
            org.bukkit.World world = plugin.getServer().getWorld(arena.getWorldUUID());
            if (world != null && world.getName().equals(worldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cria um novo mundo para uma arena usando o Multiverse-Core
     *
     * @param worldName Nome do mundo
     * @param worldType Tipo do mundo (NORMAL, NETHER, THE_END)
     * @param generateStructures Se estruturas serão geradas
     * @param seed Seed do mundo (ou null para aleatório)
     * @return true se o mundo foi criado com sucesso
     */
    public boolean createArenaWorld(String worldName, org.bukkit.WorldType worldType, boolean generateStructures, Long seed) {
        if (multiverseCore == null) {
            plugin.getLogger().severe("Multiverse-Core não encontrado! Não é possível criar novos mundos.");
            return false;
        }

        // Prepara os argumentos para o comando de criação
        List<String> args = new ArrayList<>();
        args.add(worldName);
        args.add("normal"); // Ambiente (normal, nether, end)

        // Tipo do mundo
        String type;
        switch (worldType) {
            case FLAT:
                type = "flat";
                break;
            case AMPLIFIED:
                type = "amplified";
                break;
            case LARGE_BIOMES:
                type = "largebiomes";
                break;
            default:
                type = "normal";
                break;
        }
        args.add(type);

        // Seed (opcional)
        if (seed != null) {
            args.add("-s");
            args.add(seed.toString());
        }

        // Estruturas
        if (!generateStructures) {
            args.add("-g");
            args.add("false");
        }

        // Cria o mundo
        return multiverseCore.getMVWorldManager().addWorld(
                worldName,
                org.bukkit.World.Environment.NORMAL,
                seed != null ? seed.toString() : "",
                worldType,
                generateStructures,
                "GravitationalBattle"
        );
    }

    /**
     * Remove um mundo de arena usando o Multiverse-Core
     *
     * @param worldName Nome do mundo
     * @return true se o mundo foi removido com sucesso
     */
    public boolean removeArenaWorld(String worldName) {
        if (multiverseCore == null) {
            plugin.getLogger().severe("Multiverse-Core não encontrado! Não é possível remover mundos.");
            return false;
        }

        return multiverseCore.getMVWorldManager().deleteWorld(worldName);
    }

    /**
     * Muda a hora do dia em um mundo de arena
     *
     * @param worldName Nome do mundo
     * @param time Hora do dia (0-24000)
     * @return true se a operação foi bem-sucedida
     */
    public boolean setWorldTime(String worldName, long time) {
        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            world.setTime(time);
            return true;
        }
        return false;
    }

    /**
     * Define o clima em um mundo de arena
     *
     * @param worldName Nome do mundo
     * @param storm true para ativar tempestades, false para desativar
     * @return true se a operação foi bem-sucedida
     */
    public boolean setWorldStorm(String worldName, boolean storm) {
        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world != null) {
            world.setStorm(storm);
            return true;
        }
        return false;
    }
}