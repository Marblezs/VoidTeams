package me.VoidTeams.listeners;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scoreboard.Team;

public class TeamChatListener implements Listener {

    private final VoidTeams plugin;

    public TeamChatListener(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("allow-team-chat", true)) return;

        Player player = event.getPlayer();

        if (plugin.getTeamsData().isChatToggled(player.getUniqueId())) {
            Team team = plugin.getServer().getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());

            if (team != null) {
                event.setCancelled(true);
                String teamFormat = ChatColor.AQUA + "[Equipo] " + ChatColor.WHITE + player.getName() + ": " + event.getMessage();

                for (String entry : team.getEntries()) {
                    Player teamMember = plugin.getServer().getPlayer(entry);
                    if (teamMember != null && teamMember.isOnline()) {
                        teamMember.sendMessage(teamFormat);
                    }
                }
            } else {
                plugin.getTeamsData().toggleChat(player.getUniqueId());
                ChatUtil.msg(player, "&cTu chat de equipo se desactivo porque no estas en ningun equipo.");
            }
        }
    }
}