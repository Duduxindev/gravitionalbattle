package com.br.gravitationalbattle.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;

public class PlayerListener implements Listener {

    private final GravitationalBattle plugin;

    public PlayerListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Set lobby scoreboard
        plugin.getScoreboardManager().setLobbyScoreboard(player);

        // Teleport to lobby if exists
        if (plugin.getConfigManager().getLobbyLocation() != null) {
            player.teleport(plugin.getConfigManager().getLobbyLocation());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove from game if in one
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game != null) {
            game.removePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // If in a game as spectator, keep them there
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game != null && game.getState() == com.br.gravitationalbattle.game.GameState.INGAME) {
            // Will set spectator mode on next tick
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                game.addSpectator(player);
            });
            return;
        }

        // Otherwise, teleport to lobby
        if (plugin.getConfigManager().getLobbyLocation() != null) {
            event.setRespawnLocation(plugin.getConfigManager().getLobbyLocation());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());

        // If player is in waiting/countdown state, cancel hunger
        if (game != null &&
                (game.getState() == com.br.gravitationalbattle.game.GameState.WAITING ||
                        game.getState() == com.br.gravitationalbattle.game.GameState.COUNTDOWN)) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());

        // If player is in waiting/countdown state, cancel all damage
        if (game != null &&
                (game.getState() == com.br.gravitationalbattle.game.GameState.WAITING ||
                        game.getState() == com.br.gravitationalbattle.game.GameState.COUNTDOWN)) {
            event.setCancelled(true);
        }

        // If player is in lobby, cancel all damage
        if (game == null && player.getWorld().equals(plugin.getConfigManager().getLobbyLocation().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Check if player is teleporting to a different world while in a game
        Game game = plugin.getGameManager().getPlayerGame(player.getUniqueId());
        if (game != null && !event.getTo().getWorld().equals(event.getFrom().getWorld())) {
            // If teleporting to a different world than the game world, remove from game
            if (!event.getTo().getWorld().getUID().equals(game.getArena().getWorldUUID())) {
                // Small delay to ensure teleport completes first
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    game.removePlayer(player);
                }, 1L);
            }
        }
    }
}