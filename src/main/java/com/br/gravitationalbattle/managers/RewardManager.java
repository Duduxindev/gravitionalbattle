package com.br.gravitationalbattle.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;

/**
 * Gerencia recompensas e moedas do jogo
 */
public class RewardManager {

    private final GravitationalBattle plugin;
    private Map<UUID, Integer> playerTokens;
    private Map<UUID, List<String>> playerItems;
    private FileConfiguration tokensConfig;
    private FileConfiguration itemsConfig;
    private File tokensFile;
    private File itemsFile;

    // Configurações de recompensas
    private int baseWinReward = 100;
    private int participationReward = 20;
    private int killReward = 15;
    private int secondPlaceReward = 70;
    private int thirdPlaceReward = 50;

    // Multiplicadores para diferentes modos de jogo
    private double soloMultiplier = 1.0;
    private double duosMultiplier = 1.2;
    private double squadsMultiplier = 1.5;

    public RewardManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.playerTokens = new HashMap<>();
        this.playerItems = new HashMap<>();

        loadConfig();
    }

    /**
     * Carrega as configurações de recompensas e dados salvos
     */
    public void loadConfig() {
        // Carregar configurações de recompensas do config.yml
        FileConfiguration config = plugin.getConfig();

        baseWinReward = config.getInt("rewards.win", 100);
        participationReward = config.getInt("rewards.participation", 20);
        killReward = config.getInt("rewards.kill", 15);
        secondPlaceReward = config.getInt("rewards.second-place", 70);
        thirdPlaceReward = config.getInt("rewards.third-place", 50);

        soloMultiplier = config.getDouble("rewards.multipliers.solo", 1.0);
        duosMultiplier = config.getDouble("rewards.multipliers.duos", 1.2);
        squadsMultiplier = config.getDouble("rewards.multipliers.squads", 1.5);

        // Carregar dados salvos de tokens e itens comprados
        setupFiles();
        loadTokens();
        loadItems();
    }

    /**
     * Configura os arquivos de dados
     */
    private void setupFiles() {
        // Tokens
        tokensFile = new File(plugin.getDataFolder(), "tokens.yml");
        if (!tokensFile.exists()) {
            try {
                tokensFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo tokens.yml!");
                e.printStackTrace();
            }
        }
        tokensConfig = YamlConfiguration.loadConfiguration(tokensFile);

        // Items
        itemsFile = new File(plugin.getDataFolder(), "purchased-items.yml");
        if (!itemsFile.exists()) {
            try {
                itemsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar o arquivo purchased-items.yml!");
                e.printStackTrace();
            }
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    /**
     * Carrega os tokens dos jogadores do arquivo
     */
    private void loadTokens() {
        playerTokens.clear();

        for (String uuidStr : tokensConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                int tokens = tokensConfig.getInt(uuidStr);
                playerTokens.put(uuid, tokens);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID inválido no arquivo tokens.yml: " + uuidStr);
            }
        }
    }

    /**
     * Carrega os itens comprados pelos jogadores do arquivo
     */
    private void loadItems() {
        playerItems.clear();

        for (String uuidStr : itemsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> items = itemsConfig.getStringList(uuidStr);
                playerItems.put(uuid, items);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID inválido no arquivo purchased-items.yml: " + uuidStr);
            }
        }
    }

    /**
     * Salva os tokens dos jogadores no arquivo
     */
    public void saveTokens() {
        for (Map.Entry<UUID, Integer> entry : playerTokens.entrySet()) {
            tokensConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            tokensConfig.save(tokensFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar tokens.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Salva os itens comprados pelos jogadores no arquivo
     */
    public void saveItems() {
        for (Map.Entry<UUID, List<String>> entry : playerItems.entrySet()) {
            itemsConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar purchased-items.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Adiciona tokens a um jogador
     *
     * @param player O jogador
     * @param amount Quantidade de tokens a adicionar
     */
    public void addTokens(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentTokens = getPlayerTokens(player);
        playerTokens.put(uuid, currentTokens + amount);

        // Salvar automaticamente
        saveTokens();
    }

    /**
     * Remove tokens de um jogador
     *
     * @param player O jogador
     * @param amount Quantidade de tokens a remover
     * @return true se tiver tokens suficientes e a operação for bem sucedida
     */
    public boolean removeTokens(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int currentTokens = getPlayerTokens(player);

        if (currentTokens < amount) {
            return false; // Tokens insuficientes
        }

        playerTokens.put(uuid, currentTokens - amount);

        // Salvar automaticamente
        saveTokens();
        return true;
    }

    /**
     * Obtém a quantidade de tokens de um jogador
     *
     * @param player O jogador
     * @return Quantidade de tokens
     */
    public int getPlayerTokens(Player player) {
        return playerTokens.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Verifica se um jogador possui um item específico
     *
     * @param player O jogador
     * @param itemId ID do item
     * @return true se o jogador possuir o item
     */
    public boolean hasItem(Player player, String itemId) {
        UUID uuid = player.getUniqueId();
        List<String> items = playerItems.getOrDefault(uuid, null);

        if (items == null) {
            return false;
        }

        return items.contains(itemId);
    }

    /**
     * Adiciona um item comprado à coleção de um jogador
     *
     * @param player O jogador
     * @param itemId ID do item
     */
    public void addPurchasedItem(Player player, String itemId) {
        UUID uuid = player.getUniqueId();
        List<String> items = playerItems.getOrDefault(uuid, new ArrayList<>());

        if (!items.contains(itemId)) {
            items.add(itemId);
        }

        playerItems.put(uuid, items);
        saveItems();
    }

    /**
     * Calcula a recompensa de um jogador baseado na performance
     *
     * @param kills Quantidade de abates
     * @param isWinner Se é o vencedor
     * @param gameTime Tempo de jogo em segundos
     * @return Quantidade de tokens a receber
     */
    public int calculateGameReward(int kills, boolean isWinner, int gameTime) {
        int reward = participationReward; // Recompensa base por participação

        // Adicionar recompensa por abates
        reward += kills * killReward;

        // Adicionar recompensa por vitória
        if (isWinner) {
            reward += baseWinReward;
        }

        // Bônus por tempo jogado (máx 50% extra)
        int minutesPlayed = gameTime / 60;
        double timeMultiplier = Math.min(1.5, 1.0 + (0.01 * minutesPlayed));
        reward = (int)(reward * timeMultiplier);

        // Aplicar multiplicador do modo de jogo
        // Isso seria implementado baseado no modo atual

        return reward;
    }

    /**
     * Obtém o multiplicador de recompensas para um modo de jogo específico
     *
     * @param gameMode O modo de jogo
     * @return O multiplicador de recompensas
     */
    public double getModeMultiplier(com.br.gravitationalbattle.game.GameMode gameMode) {
        switch (gameMode) {
            case SOLO:
                return soloMultiplier;
            case DUOS:
                return duosMultiplier;
            case SQUADS:
                return squadsMultiplier;
            default:
                return 1.0;
        }
    }

    /**
     * Define a recompensa para vitória
     *
     * @param amount A quantidade de tokens
     */
    public void setBaseWinReward(int amount) {
        baseWinReward = amount;
    }

    /**
     * Define a recompensa por abate
     *
     * @param amount A quantidade de tokens
     */
    public void setKillReward(int amount) {
        killReward = amount;
    }

    /**
     * Define a recompensa por participação
     *
     * @param amount A quantidade de tokens
     */
    public void setParticipationReward(int amount) {
        participationReward = amount;
    }
}