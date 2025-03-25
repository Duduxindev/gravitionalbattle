package com.br.gravitationalbattle.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.br.gravitationalbattle.GravitationalBattle;

/**
 * Gerencia as habilidades especiais dos jogadores
 */
public class AbilityManager {

    private final GravitationalBattle plugin;
    private final Map<UUID, Long> playerCooldowns;
    private final Map<UUID, String> playerAbilities;
    private final Map<String, Integer> abilityCooldowns;

    public AbilityManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        this.playerCooldowns = new HashMap<>();
        this.playerAbilities = new HashMap<>();
        this.abilityCooldowns = new HashMap<>();
        setupDefaultCooldowns();
    }

    /**
     * Configura os tempos de recarga padrão para habilidades
     */
    private void setupDefaultCooldowns() {
        abilityCooldowns.put("doublejump", 5);    // 5 segundos
        abilityCooldowns.put("shield", 20);       // 20 segundos
        abilityCooldowns.put("teleport", 15);     // 15 segundos
        abilityCooldowns.put("fireball", 10);     // 10 segundos
    }

    /**
     * Define a habilidade ativa de um jogador
     *
     * @param player O jogador
     * @param abilityId ID da habilidade
     * @return true se a habilidade foi definida com sucesso
     */
    public boolean setActiveAbility(Player player, String abilityId) {
        if (player == null) return false;

        // Verificar se o jogador possui a habilidade
        if (!plugin.getRewardManager().hasItem(player, "ability." + abilityId)) {
            player.sendMessage("§cVocê não possui esta habilidade!");
            return false;
        }

        // Definir habilidade
        playerAbilities.put(player.getUniqueId(), abilityId);
        player.sendMessage("§aHabilidade §e" + getAbilityName(abilityId) + " §aativada!");

        // Dar item de habilidade para o jogador
        giveAbilityItem(player);

        return true;
    }

    /**
     * Obtém a habilidade ativa de um jogador
     *
     * @param player O jogador
     * @return ID da habilidade ativa ou null
     */
    public String getActiveAbility(Player player) {
        if (player == null) return null;
        return playerAbilities.getOrDefault(player.getUniqueId(), null);
    }

    /**
     * Obtém o tempo de recarga restante de um jogador em segundos
     *
     * @param player O jogador
     * @return Tempo de recarga em segundos ou 0 se pronta
     */
    public int getCooldown(Player player) {
        if (player == null) return 0;

        UUID uuid = player.getUniqueId();
        long cooldownUntil = playerCooldowns.getOrDefault(uuid, 0L);
        long now = System.currentTimeMillis();

        if (cooldownUntil > now) {
            return (int)((cooldownUntil - now) / 1000);
        }

        return 0;
    }

    /**
     * Verifica se a habilidade de um jogador está pronta
     *
     * @param player O jogador
     * @return true se a habilidade estiver pronta
     */
    public boolean isAbilityReady(Player player) {
        return getCooldown(player) == 0;
    }

    /**
     * Usa a habilidade ativa do jogador
     *
     * @param player O jogador
     * @return true se a habilidade foi usada com sucesso
     */
    public boolean useAbility(Player player) {
        if (player == null) return false;

        // Verificar se o jogador tem habilidade ativa
        String abilityId = getActiveAbility(player);
        if (abilityId == null) {
            player.sendMessage("§cVocê não tem nenhuma habilidade ativa!");
            return false;
        }

        // Verificar se está em recarga
        if (!isAbilityReady(player)) {
            player.sendMessage("§cSua habilidade está em recarga! Aguarde " + getCooldown(player) + " segundos.");
            return false;
        }

        // Usar habilidade específica
        boolean success = false;

        switch (abilityId) {
            case "doublejump":
                success = useDoubleJump(player);
                break;

            case "shield":
                success = useShield(player);
                break;

            case "teleport":
                success = useTeleport(player);
                break;

            case "fireball":
                success = useFireball(player);
                break;

            default:
                player.sendMessage("§cHabilidade inválida ou não implementada!");
                return false;
        }

        if (success) {
            // Aplicar recarga
            applyCooldown(player, abilityId);
        }

        return success;
    }

    /**
     * Implementação da habilidade de Salto Duplo
     *
     * @param player O jogador
     * @return true se usado com sucesso
     */
    private boolean useDoubleJump(Player player) {
        // Verificar se o jogador está no ar
        if (player.isOnGround()) {
            player.sendMessage("§cVocê precisa estar no ar para usar o Salto Duplo!");
            return false;
        }

        // Aplicar impulso
        Vector velocity = player.getLocation().getDirection().multiply(0.5);
        velocity.setY(0.8);
        player.setVelocity(velocity);

        // Efeitos
        player.sendMessage("§a§lSALTO DUPLO!");

        return true;
    }

    /**
     * Implementação da habilidade de Escudo
     *
     * @param player O jogador
     * @return true se usado com sucesso
     */
    private boolean useShield(Player player) {
        // Aplicar resistência ao dano
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1, false, true));

        // Efeitos
        player.sendMessage("§b§lESCUDO ATIVADO!");

        return true;
    }

    /**
     * Implementação da habilidade de Teleporte
     *
     * @param player O jogador
     * @return true se usado com sucesso
     */
    private boolean useTeleport(Player player) {
        // Teleportar na direção que está olhando
        Vector direction = player.getLocation().getDirection().multiply(8.0);
        player.teleport(player.getLocation().add(direction));

        // Efeitos
        player.sendMessage("§5§lTELEPORTE!");

        return true;
    }

    /**
     * Implementação da habilidade de Bola de Fogo
     *
     * @param player O jogador
     * @return true se usado com sucesso
     */
    private boolean useFireball(Player player) {
        // Lançar bola de fogo
        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setYield(2.0f); // Tamanho da explosão
        fireball.setIsIncendiary(false); // Não causa incêndio

        // Marcar entidade para identificação posterior
        fireball.setCustomName(player.getName());

        // Efeitos
        player.sendMessage("§c§lBOLA DE FOGO LANÇADA!");

        return true;
    }

    /**
     * Aplica recarga para uma habilidade
     *
     * @param player O jogador
     * @param abilityId ID da habilidade
     */
    private void applyCooldown(Player player, String abilityId) {
        int cooldownSeconds = abilityCooldowns.getOrDefault(abilityId, 10);
        long cooldownUntil = System.currentTimeMillis() + (cooldownSeconds * 1000);

        playerCooldowns.put(player.getUniqueId(), cooldownUntil);

        // Atualizar o jogador sobre o status da habilidade
        player.sendMessage("§eHabilidade em recarga por §c" + cooldownSeconds + " §esegundos.");

        // Agendar notificação quando a habilidade estiver pronta
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.sendMessage("§a§lSua habilidade está pronta para uso!");
                }
            }
        }.runTaskLater(plugin, cooldownSeconds * 20L);
    }

    /**
     * Dá ao jogador o item para usar a habilidade
     *
     * @param player O jogador
     */
    public void giveAbilityItem(Player player) {
        if (player == null) return;

        String abilityId = getActiveAbility(player);
        if (abilityId == null) return;

        ItemStack item;

        // Criar item baseado na habilidade
        switch (abilityId) {
            case "doublejump":
                item = new ItemStack(Material.FEATHER);
                break;

            case "shield":
                item = new ItemStack(Material.IRON_INGOT);
                break;

            case "teleport":
                item = new ItemStack(Material.ENDER_PEARL);
                break;

            case "fireball":
                item = new ItemStack(Material.FIRE_CHARGE);
                break;

            default:
                item = new ItemStack(Material.NETHER_STAR);
                break;
        }

        // Configurar meta do item
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d§lHabilidade: §e" + getAbilityName(abilityId));
        item.setItemMeta(meta);

        // Adicionar ao inventário do jogador
        player.getInventory().setItem(4, item); // Slot do meio da hotbar
    }

    /**
     * Obtém o nome de exibição de uma habilidade
     *
     * @param abilityId ID da habilidade
     * @return Nome formatado
     */
    public String getAbilityName(String abilityId) {
        switch (abilityId) {
            case "doublejump":
                return "Salto Duplo";
            case "shield":
                return "Escudo Protetor";
            case "teleport":
                return "Teleporte Rápido";
            case "fireball":
                return "Bola de Fogo";
            default:
                return abilityId;
        }
    }

    /**
     * Processa o clique em um item de habilidade
     *
     * @param player O jogador
     * @param item O item clicado
     * @return true se foi um item de habilidade
     */
    public boolean processAbilityItemClick(Player player, ItemStack item) {
        if (player == null || item == null || !item.hasItemMeta() ||
                !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();

        if (displayName.startsWith("§d§lHabilidade:")) {
            // É um item de habilidade, usar
            return useAbility(player);
        }

        return false;
    }
}