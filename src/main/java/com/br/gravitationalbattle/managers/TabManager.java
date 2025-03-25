package com.br.gravitationalbattle.managers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.br.gravitationalbattle.GravitationalBattle;
import com.br.gravitationalbattle.game.Game;

/**
 * Gerenciador de Tab para mostrar header e footer customizados
 */
public class TabManager {

    private final GravitationalBattle plugin;
    private String header;
    private String footer;
    private int updateInterval = 20; // Ticks (1 segundo)

    public TabManager(GravitationalBattle plugin) {
        this.plugin = plugin;
        setupDefaults();
        startUpdateTask();
    }

    private void setupDefaults() {
        String serverName = "&5&lPURPLE&d&lMC";
        String serverIP = "&dpurplemc.net";

        // Header padrão
        header = "\n" +
                "&5&l⚔ " + serverName + " &5&l⚔\n" +
                "&dBatalha Gravitacional\n" +
                "&7Use /gb help para ajuda\n";

        // Footer padrão
        footer = "\n" +
                "&7Jogadores Online: &d" + Bukkit.getOnlinePlayers().size() + "&7/&d" + Bukkit.getMaxPlayers() + "\n" +
                "&7" + serverIP + "\n";
    }

    /**
     * Inicia a tarefa de atualização do TAB
     */
    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayers();
            }
        }.runTaskTimer(plugin, 20L, updateInterval);
    }

    /**
     * Atualiza o TAB para todos os jogadores online
     */
    public void updateAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabForPlayer(player);
        }
    }

    /**
     * Atualiza o TAB para um jogador específico
     *
     * @param player O jogador
     */
    public void updateTabForPlayer(Player player) {
        String customHeader = parseVariables(header, player);
        String customFooter = parseVariables(footer, player);

        sendHeaderFooter(player, customHeader, customFooter);
    }

    /**
     * Substitui variáveis no texto
     *
     * @param text O texto com variáveis
     * @param player O jogador
     * @return Texto com variáveis substituídas
     */
    private String parseVariables(String text, Player player) {
        text = text.replace("{player}", player.getName());
        text = text.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));

        // Variáveis de jogo
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            text = text.replace("{game}", game.getArena().getDisplayName());
            text = text.replace("{players}", game.getPlayerCount() + "/" + game.getArena().getMaxPlayers());
            text = text.replace("{kills}", String.valueOf(game.getPlayerKills(player)));
            text = text.replace("{remaining}", String.valueOf(game.getAliveCount()));
            text = text.replace("{time}", formatTime(game.getGameTime()));
            text = text.replace("{state}", translateGameState(game.getState().toString()));
        } else {
            text = text.replace("{game}", "Nenhum");
            text = text.replace("{players}", "0/0");
            text = text.replace("{kills}", "0");
            text = text.replace("{remaining}", "0");
            text = text.replace("{time}", "00:00");
            text = text.replace("{state}", "Nenhum");
        }

        // Estatísticas do jogador
        text = text.replace("{total_kills}", String.valueOf(plugin.getStatsManager().getPlayerKills(player)));
        text = text.replace("{total_deaths}", String.valueOf(plugin.getStatsManager().getPlayerDeaths(player)));
        text = text.replace("{total_wins}", String.valueOf(plugin.getStatsManager().getPlayerWins(player)));

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Traduz estado do jogo para português
     *
     * @param state Estado em inglês
     * @return Estado traduzido
     */
    private String translateGameState(String state) {
        switch (state) {
            case "WAITING": return "Aguardando";
            case "STARTING": return "Iniciando";
            case "COUNTDOWN": return "Contagem";
            case "INGAME": return "Em Jogo";
            case "ENDING": return "Finalizando";
            default: return state;
        }
    }

    /**
     * Formata tempo em segundos para MM:SS
     *
     * @param seconds Tempo em segundos
     * @return Tempo formatado
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * Define o header personalizado
     *
     * @param header Novo header
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Define o footer personalizado
     *
     * @param footer Novo footer
     */
    public void setFooter(String footer) {
        this.footer = footer;
    }

    /**
     * Define o intervalo de atualização
     *
     * @param ticks Intervalo em ticks
     */
    public void setUpdateInterval(int ticks) {
        this.updateInterval = ticks;
    }

    /**
     * Envia header e footer para o jogador usando reflexão (1.8.8)
     *
     * @param player O jogador
     * @param header Header a enviar
     * @param footer Footer a enviar
     */
    private void sendHeaderFooter(Player player, String header, String footer) {
        try {
            // Reflexão para compatibilidade com 1.8.8
            Object tabHeader = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + header + "\"}");

            Object tabFooter = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + footer + "\"}");

            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor();
            Object packet = titleConstructor.newInstance();

            Field aField = packet.getClass().getDeclaredField("a");
            aField.setAccessible(true);
            aField.set(packet, tabHeader);

            Field bField = packet.getClass().getDeclaredField("b");
            bField.setAccessible(true);
            bField.set(packet, tabFooter);

            sendPacket(player, packet);

        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao enviar header/footer do TAB: " + e.getMessage());
        }
    }

    /**
     * Obtém uma classe NMS usando reflexão
     *
     * @param className Nome da classe NMS
     * @return A classe NMS
     */
    private Class<?> getNMSClass(String className) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + className);
    }

    /**
     * Envia um pacote para o jogador usando reflexão
     *
     * @param player O jogador
     * @param packet O pacote a enviar
     */
    private void sendPacket(Player player, Object packet) throws Exception {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
        Method sendPacket = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
        sendPacket.invoke(playerConnection, packet);
    }
}