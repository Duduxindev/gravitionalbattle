package com.br.gravitationalbattle.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.br.gravitationalbattle.GravitationalBattle;

/**
 * Gerencia os níveis e prestígio dos jogadores
 */
public class LevelManager {

    private final GravitationalBattle plugin;
    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private final Map<UUID, Integer> playerXP = new HashMap<>();
    private final Map<UUID, Integer> playerPrestige = new HashMap<>();
    private File levelsFile;
    private FileConfiguration levelsConfig;

    private static final int MAX_LEVEL = 50;
    private static final int MAX_PRESTIGE = 10;

    public LevelManager(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    /**
     * Carrega os dados de níveis de todos os jogadores
     */
    public void loadData() {
        levelsFile = new File(plugin.getDataFolder(), "levels.yml");

        if (!levelsFile.exists()) {
            try {
                levelsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Erro ao criar arquivo levels.yml!");
                e.printStackTrace();
                return;
            }
        }

        levelsConfig = YamlConfiguration.loadConfiguration(levelsFile);

        // Carregar níveis dos jogadores
        if (levelsConfig.contains("players")) {
            for (String uuidString : levelsConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String path = "players." + uuidString + ".";

                    int level = levelsConfig.getInt(path + "level", 1);
                    int xp = levelsConfig.getInt(path + "xp", 0);
                    int prestige = levelsConfig.getInt(path + "prestige", 0);

                    playerLevels.put(uuid, level);
                    playerXP.put(uuid, xp);
                    playerPrestige.put(uuid, prestige);

                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("UUID inválido em levels.yml: " + uuidString);
                }
            }
        }
    }

    /**
     * Salva os dados de níveis de todos os jogadores
     */
    public void saveData() {
        if (levelsConfig == null || levelsFile == null) {
            loadData();
        }

        // Limpar configurações atuais
        levelsConfig.set("players", null);

        // Salvar dados dos jogadores
        for (UUID uuid : playerLevels.keySet()) {
            String path = "players." + uuid.toString() + ".";

            int level = playerLevels.getOrDefault(uuid, 1);
            int xp = playerXP.getOrDefault(uuid, 0);
            int prestige = playerPrestige.getOrDefault(uuid, 0);

            levelsConfig.set(path + "level", level);
            levelsConfig.set(path + "xp", xp);
            levelsConfig.set(path + "prestige", prestige);
        }

        try {
            levelsConfig.save(levelsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar dados em levels.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Obtém o nível de um jogador
     *
     * @param player O jogador
     * @return Nível atual
     */
    public int getPlayerLevel(Player player) {
        if (player == null) return 1;
        return playerLevels.getOrDefault(player.getUniqueId(), 1);
    }

    /**
     * Obtém o XP de um jogador
     *
     * @param player O jogador
     * @return XP atual
     */
    public int getPlayerXP(Player player) {
        if (player == null) return 0;
        return playerXP.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Obtém o prestígio de um jogador
     *
     * @param player O jogador
     * @return Prestígio atual
     */
    public int getPlayerPrestige(Player player) {
        if (player == null) return 0;
        return playerPrestige.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Calcula o XP necessário para o próximo nível
     *
     * @param level Nível atual
     * @return XP necessário
     */
    public int getXPForNextLevel(int level) {
        return 100 + (level * 25);
    }

    /**
     * Adiciona XP a um jogador, atualizando nível se necessário
     *
     * @param player O jogador
     * @param amount Quantidade de XP a adicionar
     * @return true se o jogador subiu de nível
     */
    public boolean addXP(Player player, int amount) {
        if (player == null || amount <= 0) return false;

        UUID uuid = player.getUniqueId();
        int currentXP = playerXP.getOrDefault(uuid, 0);
        int currentLevel = playerLevels.getOrDefault(uuid, 1);
        int currentPrestige = playerPrestige.getOrDefault(uuid, 0);

        // Verificar se está no nível máximo e com prestígio máximo
        if (currentLevel >= MAX_LEVEL && currentPrestige >= MAX_PRESTIGE) {
            // Não pode ganhar mais XP
            return false;
        }

        boolean leveledUp = false;

        // Adicionar XP
        currentXP += amount;
        player.sendMessage("§b§l+ " + amount + " XP");

        // Verificar se subiu de nível
        int xpForNextLevel = getXPForNextLevel(currentLevel);
        while (currentXP >= xpForNextLevel) {
            // Subiu de nível
            currentXP -= xpForNextLevel;
            currentLevel++;
            leveledUp = true;

            // Anunciar para o jogador
            player.sendMessage("§a§l↑ NÍVEL UP! §f" + (currentLevel-1) + " → §a" + currentLevel);

            // Verificar se atingiu o nível máximo
            if (currentLevel >= MAX_LEVEL) {
                currentLevel = MAX_LEVEL;
                break;
            }

            // Atualizar XP para o próximo nível
            xpForNextLevel = getXPForNextLevel(currentLevel);
        }

        // Salvar dados
        playerXP.put(uuid, currentXP);
        playerLevels.put(uuid, currentLevel);

        return leveledUp;
    }

    /**
     * Aumenta o prestígio de um jogador
     *
     * @param player O jogador
     * @return true se o prestígio foi aumentado com sucesso
     */
    public boolean increasePrestige(Player player) {
        if (player == null) return false;

        UUID uuid = player.getUniqueId();
        int currentLevel = playerLevels.getOrDefault(uuid, 1);
        int currentPrestige = playerPrestige.getOrDefault(uuid, 0);

        // Verificar se pode aumentar o prestígio
        if (currentLevel < MAX_LEVEL || currentPrestige >= MAX_PRESTIGE) {
            return false;
        }

        // Aumentar prestígio e resetar nível
        currentPrestige++;
        playerPrestige.put(uuid, currentPrestige);
        playerLevels.put(uuid, 1);
        playerXP.put(uuid, 0);

        // Anunciar para o jogador
        player.sendMessage("§d§l✦ PRESTÍGIO AUMENTADO! §f" + (currentPrestige-1) + " → §d" + currentPrestige);

        // Dar recompensa de prestígio
        int rewardTokens = 1000 + (currentPrestige * 500);
        plugin.getRewardManager().addTokens(player, rewardTokens);
        player.sendMessage("§6§lRecompensa de Prestígio: §e" + rewardTokens + " Moedas");

        return true;
    }

    /**
     * Calcula o XP para uma partida
     *
     * @param kills Número de abates
     * @param winner Se foi o vencedor
     * @param timeMinutes Tempo jogado em minutos
     * @return Quantidade de XP
     */
    public int calculateGameXP(int kills, boolean winner, int timeMinutes) {
        int baseXP = 20; // XP base por jogar
        int killXP = kills * 10; // 10 XP por abate
        int winXP = winner ? 50 : 0; // 50 XP por vitória

        // Bônus por tempo jogado (max 30 XP)
        int timeXP = Math.min(30, timeMinutes * 2);

        return baseXP + killXP + winXP + timeXP;
    }

    /**
     * Verifica se o jogador está habilitado a usar um recurso baseado em nível/prestígio
     *
     * @param player O jogador
     * @param requiredLevel Nível mínimo necessário
     * @param requiredPrestige Prestígio mínimo necessário
     * @return true se o jogador atende aos requisitos
     */
    public boolean hasLevelRequirement(Player player, int requiredLevel, int requiredPrestige) {
        if (player == null) return false;

        int playerLevel = getPlayerLevel(player);
        int playerPrestige = getPlayerPrestige(player);

        if (playerPrestige > requiredPrestige) {
            return true;
        } else if (playerPrestige == requiredPrestige) {
            return playerLevel >= requiredLevel;
        }

        return false;
    }
}