package me.VoidTeams.hooks;

import me.VoidTeams.VoidTeams;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

public class VoidTeamsExpansion extends PlaceholderExpansion {

    private final VoidTeams plugin;

    public VoidTeamsExpansion(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() { return "voidteams"; }

    @Override
    public String getAuthor() { return "MarcyWu"; }

    @Override
    public String getVersion() { return "1.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        // %voidteams_team%
        if (params.equalsIgnoreCase("team")) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team != null) {
                return team.getPrefix();
            }
            return "&7";
        }

        // %voidteams_type%
        if (params.equalsIgnoreCase("type")) {
            return plugin.getTeamManager().getTeamType();
        }

        // %voidteams_size%
        if (params.equalsIgnoreCase("size")) {
            return String.valueOf(plugin.getTeamManager().getTeamSize());
        }

        return null;
    }
}