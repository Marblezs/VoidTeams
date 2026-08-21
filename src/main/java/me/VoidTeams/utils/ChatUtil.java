package me.VoidTeams.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class ChatUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private static final String PREFIX =
            "<#8B5CF6><bold>VOID</bold></#8B5CC1>" +
            "<#22D3EE><bold>TEAMS</bold></#22D3EE> " +
            "<dark_gray>»</dark_gray> ";

    private ChatUtil() {
    }

    public static Component component(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return Component.empty();
        }
        return MINI.deserialize(miniMessage);
    }

    public static void msg(Audience recipient, String message) {
        if (recipient == null) return;
        recipient.sendMessage(component(PREFIX + message));
    }

    public static void msgNoPrefix(Audience recipient, String message) {
        if (recipient == null) return;
        recipient.sendMessage(component(message));
    }

    public static void broadcast(String message) {
        Component component = component(PREFIX + message);
        Bukkit.getConsoleSender().sendMessage(component);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }
    }

    public static void broadcastNoPrefix(String message) {
        Component component = component(message);
        Bukkit.getConsoleSender().sendMessage(component);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component);
        }
    }

    public static void sendActionBar(Player player, String message) {
        if (player == null) return;
        player.sendActionBar(component(message));
    }

    public static void sendActionBar(Player player, Component component) {
        if (player == null || component == null) return;
        player.sendActionBar(component);
    }

    public static void sendActionBarToAll(String message) {
        Component component = component(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(component);
        }
    }

    public static void title(Player player,
                             String title,
                             String subtitle,
                             int fadeInTicks,
                             int stayTicks,
                             int fadeOutTicks) {
        if (player == null) return;

        Title.Times times = Title.Times.times(
                Duration.ofMillis(Math.max(0, fadeInTicks) * 50L),
                Duration.ofMillis(Math.max(0, stayTicks) * 50L),
                Duration.ofMillis(Math.max(0, fadeOutTicks) * 50L)
        );

        player.showTitle(Title.title(component(title), component(subtitle), times));
    }

    public static void titleAll(String title,
                                String subtitle,
                                int fadeInTicks,
                                int stayTicks,
                                int fadeOutTicks) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            title(player, title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
        }
    }

    public static String prefixMiniMessage() {
        return PREFIX;
    }
}
