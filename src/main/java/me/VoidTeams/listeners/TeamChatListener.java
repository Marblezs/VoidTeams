package me.VoidTeams.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.VoidTeams.VoidTeams;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

public class TeamChatListener implements Listener {

    private final VoidTeams plugin;

    public TeamChatListener(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getTeamsData().isChatToggled(player.getUniqueId())) {
            return;
        }

        if (plugin.getTeamManager().isChatLocked()) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                ChatUtil.msg(player, "<#FF5C5C>El chat de equipo está bloqueado actualmente.</#FF5C5C>");
                plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            });
            return;
        }

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) {
            event.setCancelled(true);
            plugin.getTeamsData().setChatToggled(player.getUniqueId(), false);
            Bukkit.getScheduler().runTask(plugin, () ->
                    ChatUtil.msg(player, "<#FF5C5C>Tu chat de equipo se desactivó porque ya no perteneces a un equipo.</#FF5C5C>"));
            return;
        }

        event.setCancelled(true);
        Component originalMessage = event.message();

        Bukkit.getScheduler().runTask(plugin, () -> {
            TeamTheme theme = plugin.getTeamManager().getTheme(team);
            TextColor color = TextColor.fromHexString(theme.hexColor());
            if (color == null) color = TextColor.color(0x8B5CF6);

            Component formatted = Component.text("EQUIPO ", TextColor.color(0xAAB2BD))
                    .decorate(TextDecoration.BOLD)
                    .append(Component.text("[" + theme.icon() + "] ", color))
                    .append(Component.text(player.getName(), TextColor.color(0xF5F7FA)))
                    .append(Component.text(" » ", TextColor.color(0x555B66)))
                    .append(originalMessage);

            for (String entry : team.getEntries()) {
                Player member = Bukkit.getPlayerExact(entry);
                if (member != null && member.isOnline()) {
                    member.sendMessage(formatted);
                    if (!member.equals(player)) {
                        plugin.getSoundManager().play(member, SoundManager.SoundType.TEAM_CHAT);
                    }
                }
            }

            if (plugin.getConfig().getBoolean("team-chat-log-console", true)) {
                Bukkit.getConsoleSender().sendMessage(formatted);
            }
        });
    }
}
