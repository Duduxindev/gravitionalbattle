package com.br.gravitationalbattle.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;
import com.br.gravitationalbattle.game.GameState;

public class GameListener implements Listener {

    private final GravitationalBattle plugin;

    public GameListener(GravitationalBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            // Sempre verificar se o player não é nulo
            if (event.getPlayer() == null) {
                return;
            }

            Player player = event.getPlayer();

            // Verificar se o jogador está em um jogo
            Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());
            if (game == null) {
                return; // O jogador não está em um jogo
            }

            // Se o jogo não estiver em andamento, cancela interações com blocos
            if (game.getState() != GameState.INGAME) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                        event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Verificação de segurança para o bloco e material
            if (event.getClickedBlock() != null) {
                Block block = event.getClickedBlock();

                // Impedir interações com determinados blocos durante o jogo
                if (block.getType() == Material.CHEST ||
                        block.getType() == Material.TRAPPED_CHEST ||
                        block.getType() == Material.ENDER_CHEST ||
                        block.getType() == Material.FURNACE ||
                        block.getType() == Material.LEGACY_BURNING_FURNACE ||
                        block.getType() == Material.BREWING_STAND ||
                        block.getType() == Material.ANVIL ||
                        block.getType() == Material.ENCHANTING_TABLE ||
                        block.getType() == Material.LEGACY_WORKBENCH) {

                    // Permitir apenas se o jogo estiver em andamento
                    if (game.getState() != GameState.INGAME) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // Impedir interações com portas, botões, etc. se não estiver em jogo
                if (game.getState() != GameState.INGAME) {
                    if (block.getType().name().contains("DOOR") ||
                            block.getType().name().contains("BUTTON") ||
                            block.getType().name().contains("LEVER") ||
                            block.getType().name().contains("PLATE")) {

                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // Log da exceção para facilitar depuração
            plugin.getLogger().severe("Erro no evento PlayerInteractEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game != null) {
            // Impede quebrar blocos fora do modo de jogo
            if (game.getState() != GameState.INGAME) {
                event.setCancelled(true);
                return;
            }

            // Você pode adicionar lógica específica para permitir quebrar apenas certos blocos
            // durante o jogo se necessário
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game != null) {
            // Impede colocar blocos fora do modo de jogo
            if (game.getState() != GameState.INGAME) {
                event.setCancelled(true);
                return;
            }

            // Você pode adicionar lógica específica para permitir colocar apenas certos blocos
            // durante o jogo se necessário
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game != null) {
            // Remove mensagem padrão de morte
            event.setDeathMessage(null);

            // Remove drops do jogador
            event.getDrops().clear();

            // Remove experiência
            event.setDroppedExp(0);

            // Processa a morte do jogador no jogo
            Player killer = player.getKiller();
            game.playerKilled(player, killer);

            // Incrementa a estatística de morte do jogador
            plugin.getStatsManager().incrementDeaths(player.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game != null) {
            // Impede dano fora do modo de jogo
            if (game.getState() != GameState.INGAME) {
                event.setCancelled(true);
                return;
            }

            // Lógica específica para tipos de dano durante o jogo
            // Por exemplo, você pode permitir apenas certos tipos de dano
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game == null) {
            return;
        }

        // Impede dano por entidade fora do modo de jogo
        if (game.getState() != GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        // Verifica se o dano foi causado por outro jogador ou projétil
        Player damager = null;

        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource source = projectile.getShooter();

            if (source instanceof Player) {
                damager = (Player) source;
            }
        }

        // Se o causador do dano for um jogador, verifica se está no mesmo jogo
        if (damager != null) {
            Game damagerGame = plugin.getArenaManager().getPlayerGame(damager.getUniqueId());

            if (damagerGame != game) {
                event.setCancelled(true);
                return;
            }

            // Verifica se o jogador é um espectador
            if (game.getSpectators().contains(damager.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game != null) {
            // Configura se jogadores podem ou não dropar itens durante o jogo
            // Neste exemplo, impede sempre o drop de itens
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game != null) {
            // Impede mudanças no nível de fome fora do modo de jogo
            if (game.getState() != GameState.INGAME) {
                event.setCancelled(true);
                event.setFoodLevel(20); // Mantém a fome no máximo
                return;
            }

            // Você pode configurar regras específicas para fome durante o jogo aqui
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.getArenaManager().getPlayerGame(player.getUniqueId());

        if (game != null) {
            // Remove o jogador do jogo quando ele desconecta
            game.removePlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeatherChange(WeatherChangeEvent event) {
        // Impede mudanças climáticas em mundos de arena
        if (plugin.getGameManager().isArenaWorld(event.getWorld().getName())) {
            if (event.toWeatherState()) { // Se estiver mudando para chuvoso
                event.setCancelled(true);
            }
        }
    }
}