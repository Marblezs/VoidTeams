package me.VoidTeams.utils;

import net.kyori.adventure.audience.Audience;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class ChatUtil {

    private static final String PREFIX = ChatColor.WHITE + "[" + ChatColor.AQUA + "VoidTeams" + ChatColor.WHITE + "] ";

    public static void msg(Audience recipient, String message) {
        if (recipient == null) return;
        recipient.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(PREFIX + message));
    }
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
    public static void broadcastNoPrefix(String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    public static void msgNoPrefix(Audience recipient, String message) {
        if (recipient == null) return;
        recipient.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }
    public static void sendActionBar(Player player, String message) {
        String coloredMessage = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(coloredMessage)
        );
    }
}