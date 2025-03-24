package com.br.gravitationalbattle;

import com.br.gravitationalbattle.managers.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.br.gravitationalbattle.commands.ArenaCommand;
import com.br.gravitationalbattle.commands.GravitationalBattleCommand;
import com.br.gravitationalbattle.commands.JoinCommand;
import com.br.gravitationalbattle.commands.LeaveCommand;
import com.br.gravitationalbattle.commands.SetLobbyCommand;
import com.br.gravitationalbattle.commands.SpectateCommand;
import com.br.gravitationalbattle.commands.StatsCommand;
import com.br.gravitationalbattle.listeners.GameListener;
import com.br.gravitationalbattle.listeners.PlayerListener;
import com.br.gravitationalbattle.listeners.NPCListener;
import com.onarandombox.MultiverseCore.MultiverseCore;

public class GravitationalBattle extends JavaPlugin {

    private static GravitationalBattle instance;

    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private MultiverseCore multiverseCore;
    private NPCManager npcManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Setup managers
        configManager = new ConfigManager(this);
        arenaManager = new ArenaManager(this);
        gameManager = new GameManager(this);
        statsManager = new StatsManager(this);
        scoreboardManager = new ScoreboardManager(this);
        npcManager = new NPCManager(this); // Inicializa o NPCManager

        // Hook into Multiverse-Core
        if (getServer().getPluginManager().getPlugin("Multiverse-Core") != null) {
            multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
            getLogger().info("Multiverse-Core encontrado e integrado!");
        } else {
            getLogger().severe("Multiverse-Core não encontrado! O plugin precisa do Multiverse-Core para funcionar!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        getCommand("gravitationalbattle").setExecutor(new GravitationalBattleCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("join").setExecutor(new JoinCommand(this));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("setlobby").setExecutor(new SetLobbyCommand(this));
        getCommand("spectate").setExecutor(new SpectateCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));

        // Register listeners
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new GameListener(this), this);
        pluginManager.registerEvents(new NPCListener(this), this); // Registra o NPCListener

        // Load arenas
        arenaManager.loadArenas();

        getLogger().info("GravitationalBattle habilitado com sucesso!");
    }

    @Override
    public void onDisable() {
        // Save stats
        if (statsManager != null) {
            statsManager.saveStats();
        }

        // Save arenas
        if (arenaManager != null) {
            arenaManager.saveArenas();
        }

        getLogger().info("GravitationalBattle desabilitado!");
    }

    public static GravitationalBattle getInstance() {
        return instance;
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

    public MultiverseCore getMultiverseCore() {
        return multiverseCore;
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }
}