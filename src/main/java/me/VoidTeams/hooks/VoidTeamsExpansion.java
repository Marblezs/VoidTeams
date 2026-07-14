package me.VoidTeams.hooks;

import me.VoidTeams.VoidTeams;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
    public String getAuthor() { return "TuNombre"; }

    @Override
    public String getVersion() { return "1.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        // %voidteams_team%
        if (params.equalsIgnoreCase("team")) {
            Team team = plugin.getServer().getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            return (team != null) ? team.getName().replace("uhc_", "") : "Ninguno";
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