package me.VoidTeams.utils;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatUtil {

    private static final String PREFIX = ChatColor.WHITE + "[" + ChatColor.BLUE + "VoidTeams" + ChatColor.WHITE + "] ";

    public static void msg(Audience recipient, String message) {
        if (recipient == null) return;
        recipient.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(PREFIX + message));
    }
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }
}