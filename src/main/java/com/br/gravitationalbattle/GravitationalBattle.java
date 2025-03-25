package com.br.gravitationalbattle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.br.gravitationalbattle.commands.AbrirLojaCommand;
import com.br.gravitationalbattle.commands.ArenaCommand;
import com.br.gravitationalbattle.commands.GravitationalBattleCommand;
import com.br.gravitationalbattle.commands.JoinCommand;
import com.br.gravitationalbattle.commands.LeaveCommand;
import com.br.gravitationalbattle.commands.ModeratorCommands;
import com.br.gravitationalbattle.commands.PlayerCommands;
import com.br.gravitationalbattle.commands.SetLobbyCommand;
import com.br.gravitationalbattle.commands.SpectateCommand;
import com.br.gravitationalbattle.commands.StatsCommand;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.listeners.GameListener;
import com.br.gravitationalbattle.listeners.MenuListener;
import com.br.gravitationalbattle.listeners.PlayerListener;
import com.br.gravitationalbattle.managers.AbilityManager;
import com.br.gravitationalbattle.managers.ArenaManager;
import com.br.gravitationalbattle.managers.ConfigManager;
import com.br.gravitationalbattle.managers.GameManager;
import com.br.gravitationalbattle.managers.LevelManager;
import com.br.gravitationalbattle.managers.NPCManager;
import com.br.gravitationalbattle.managers.RewardManager;
import com.br.gravitationalbattle.managers.ScoreboardManager;
import com.br.gravitationalbattle.managers.StatsManager;
import com.br.gravitationalbattle.managers.TabManager;
import com.br.gravitationalbattle.managers.TeamManager;

import java.util.Arrays;

public class GravitationalBattle extends JavaPlugin {

    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private TabManager tabManager;
    private RewardManager rewardManager;
    private LevelManager levelManager;
    private AbilityManager abilityManager;
    private TeamManager teamManager;
    private PlayerCommands playerCommands;
    private ModeratorCommands moderatorCommands;
    private NPCManager npcManager;

    @Override
    public void onEnable() {
        // Initialize managers
        configManager = new ConfigManager(this);
        arenaManager = new ArenaManager(this);
        statsManager = new StatsManager(this);
        scoreboardManager = new ScoreboardManager(this);
        rewardManager = new RewardManager(this);
        levelManager = new LevelManager(this);
        abilityManager = new AbilityManager(this);
        teamManager = new TeamManager(this);
        gameManager = new GameManager(this);
        playerCommands = new PlayerCommands(this);
        moderatorCommands = new ModeratorCommands(this);
        npcManager = new NPCManager(this);

        // Load configuration
        configManager.loadConfig();

        // Load data
        rewardManager.loadData();
        levelManager.loadData();

        // Load arenas
        arenaManager.loadArenas();

        // Initialize tab manager after configs loaded
        tabManager = new TabManager(this);

        // Register commands - correção para garantir o registro adequado
        registerCommands();

        // Register event listeners
        registerListeners();

        // Inicializar scoreboards para todos os jogadores online
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                scoreboardManager.setLobbyScoreboard(player);
            }
            getLogger().info("Scoreboards atualizados para todos os jogadores online.");
        }, 40L); // Esperar 2 segundos após o carregamento do plugin

        // Dar esmeralda da loja para todos os jogadores
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                giveShopEmerald(player);
            }
            getLogger().info("Item da loja distribuído para todos os jogadores online.");
        }, 60L); // Esperar 3 segundos após o carregamento do plugin

        getLogger().info("Batalha Gravitacional foi ativada com sucesso!");
    }

    /**
     * Registra todos os comandos do plugin
     */
    private void registerCommands() {
        try {
            getCommand("gravitationalbattle").setExecutor(new GravitationalBattleCommand(this));
            getCommand("arena").setExecutor(new ArenaCommand(this));
            getCommand("setlobby").setExecutor(new SetLobbyCommand(this));
            getCommand("join").setExecutor(new JoinCommand(this));
            getCommand("leave").setExecutor(new LeaveCommand(this));
            getCommand("spectate").setExecutor(new SpectateCommand(this));
            getCommand("stats").setExecutor(new StatsCommand(this));
            getCommand("abrirloja").setExecutor(new AbrirLojaCommand(this));

            // Configurar tab completers
            getCommand("gravitationalbattle").setTabCompleter(new GravitationalBattleCommand(this));
            getCommand("join").setTabCompleter((org.bukkit.command.TabCompleter) getCommand("join").getExecutor());
            getCommand("arena").setTabCompleter((org.bukkit.command.TabCompleter) getCommand("arena").getExecutor());
            getCommand("spectate").setTabCompleter((org.bukkit.command.TabCompleter) getCommand("spectate").getExecutor());

            getLogger().info("Todos os comandos foram registrados com sucesso!");
        } catch (Exception e) {
            getLogger().severe("Erro ao registrar comandos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registra todos os listeners do plugin
     */
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new GameListener(this), this);
        pm.registerEvents(new MenuListener(this), this);
        getLogger().info("Todos os listeners foram registrados com sucesso!");
    }

    /**
     * Dá a esmeralda da loja para um jogador
     *
     * @param player O jogador
     */
    public void giveShopEmerald(Player player) {
        if (player == null || !player.isOnline()) return;

        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta meta = emerald.getItemMeta();
        meta.setDisplayName("§a§lLoja");
        meta.setLore(Arrays.asList(
                "§7Clique com botão direito para",
                "§7abrir a loja do jogo!"
        ));
        emerald.setItemMeta(meta);

        // Verificar se o jogador já tem o item
        boolean hasItem = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD &&
                    item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                    item.getItemMeta().getDisplayName().equals("§a§lLoja")) {
                hasItem = true;
                break;
            }
        }

        // Dar o item se não tiver
        if (!hasItem) {
            player.getInventory().setItem(4, emerald); // Slot do meio da hotbar
        }
    }

    @Override
    public void onDisable() {
        // Save all data
        statsManager.saveStats();
        rewardManager.saveData();
        levelManager.saveData();

        // End all active games
        for (Game game : arenaManager.getActiveGames()) {
            game.forceEnd();
        }

        getLogger().info("Batalha Gravitacional foi desativada!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public PlayerCommands getPlayerCommands() {
        return playerCommands;
    }

    public ModeratorCommands getModeratorCommands() {
        return moderatorCommands;
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }
}