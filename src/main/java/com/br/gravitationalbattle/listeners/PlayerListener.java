package com.br.gravitationalbattle.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.commands.AbrirLojaCommand;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.utils.MessageUtil;

public class PlayerListener implements Listener {

    private final GravitationalBattle plugin;

    public PlayerListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Set up scoreboard for the player
        plugin.getScoreboardManager().setLobbyScoreboard(player);

        // Teleport to lobby if configured
        if (plugin.getConfigManager().shouldTeleportToLobbyOnJoin()) {
            if (plugin.getConfigManager().getLobbyLocation() != null) {
                player.teleport(plugin.getConfigManager().getLobbyLocation());
            }
        }

        // Dar esmeralda da loja
        plugin.giveShopEmerald(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if player is in a game
        if (plugin.getGameManager().isPlayerInGame(player)) {
            // Remove from game
            plugin.getGameManager().leaveGame(player);
        }

        // Remove scoreboard data
        plugin.getScoreboardManager().removePlayer(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Check if death was in a game
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            // Cancel death message
            event.setDeathMessage(null);

            // Remove drops
            event.getDrops().clear();

            // Mark player as dead in game
            game.playerDied(player);

            // Record kill for killer if any
            Player killer = player.getKiller();
            if (killer != null) {
                game.recordKill(killer);
            }

            // Respawn player immediately (workaround for 1.8.8)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.setHealth(20.0);
                // Any additional respawn handling
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Game game = plugin.getGameManager().getPlayerGame(player);

        // Cancel damage in lobby
        if (game == null) {
            event.setCancelled(true);
            return;
        }

        // Allow damage only in INGAME state
        if (game.getState() != com.br.gravitationalbattle.game.GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        // If player is a spectator, cancel damage
        if (game.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerCombat(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        // Check if both players are in same game
        Game damagerGame = plugin.getGameManager().getPlayerGame(damager);
        Game victimGame = plugin.getGameManager().getPlayerGame(victim);

        if (damagerGame != null && victimGame != null && damagerGame == victimGame) {
            // If team mode, check if same team
            if (damagerGame.getGameMode().isTeamBased()) {
                com.br.gravitationalbattle.game.Team damagerTeam = damagerGame.getPlayerTeam(damager);
                com.br.gravitationalbattle.game.Team victimTeam = damagerGame.getPlayerTeam(victim);

                // Cancel damage if on same team
                if (damagerTeam != null && victimTeam != null && damagerTeam == victimTeam) {
                    event.setCancelled(true);
                    MessageUtil.sendMessage(damager, "&cVocê não pode atacar membros da sua equipe!");
                    return;
                }
            }

            // Let damage pass through if in INGAME state
            if (damagerGame.getState() == com.br.gravitationalbattle.game.GameState.INGAME) {
                return;
            }
        }

        // Cancel all other player combat
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Allow breaking if admin with creative mode
        if (player.hasPermission("gravitationalbattle.admin") && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if player is in a game
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            // Cancel block breaking in game
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Allow placement if admin with creative mode
        if (player.hasPermission("gravitationalbattle.admin") && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if player is in a game
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            // Cancel block placement in game
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Verificar se o item é a esmeralda da loja
        if (item != null && item.getType() == Material.EMERALD &&
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals("§a§lLoja")) {

            event.setCancelled(true);

            // Abrir a loja
            AbrirLojaCommand lojaCommand = new AbrirLojaCommand(plugin);
            lojaCommand.openMainShopMenu(player);
            return;
        }

        // Verificar se o item é a cama de "Voltar ao Lobby"
        if (item != null && item.getType() == Material.RED_BED &&
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals("§c§lVoltar ao Lobby")) {

            event.setCancelled(true);

            // Verificar se o jogador está em um jogo
            if (plugin.getGameManager().isPlayerInGame(player)) {
                // Remover o jogador do jogo
                plugin.getGameManager().leaveGame(player);

                // Mensagem
                MessageUtil.sendMessage(player, "&aVocê saiu do jogo e retornou ao lobby!");
            }
            return;
        }

        // Verificar cliques em camas tanto com botão direito quanto esquerdo
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.RED_BED) {
                // Se estiver em jogo, impedir cliques em camas que não sejam a do hotbar
                Game game = plugin.getGameManager().getPlayerGame(player);
                if (game != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Verificar se é um item de habilidade
        if (item != null && plugin.getAbilityManager() != null) {
            if (plugin.getAbilityManager().processAbilityItemClick(player, item)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}