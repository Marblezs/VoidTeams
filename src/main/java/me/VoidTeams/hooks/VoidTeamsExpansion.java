package me.VoidTeams.hooks;

import me.VoidTeams.VoidTeams;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public String getVersion() { return "1.1"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        // %voidteams_team% prefijo del equipo del jugador
        if (params.equalsIgnoreCase("team")) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team != null) {
                return team.getPrefix();
            }
            return "";
        }

        // %voidteams_type%
        if (params.equalsIgnoreCase("type")) {
            return plugin.getTeamManager().getTeamType();
        }

        // %voidteams_size%
        if (params.equalsIgnoreCase("size")) {
            return String.valueOf(plugin.getTeamManager().getTeamSize());
        }

        // %voidteams_teamsize%
        if (params.equalsIgnoreCase("teamsize")) {
            String type = plugin.getTeamManager().getTeamType();
            int size = plugin.getTeamManager().getTeamSize();

            if (size == 1) {
                return "FFA";
            }
            return type + " to " + size;
        }

        // %voidteams_has_team% -> "true" o "false"
        if (params.equalsIgnoreCase("has_team")) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            return String.valueOf(team != null);
        }

        // %voidteams_member_1%, %voidteams_member_2%, etc.
        if (params.startsWith("member_")) {
            try {
                int index = Integer.parseInt(params.replace("member_", "")) - 1;
                Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());

                if (team != null) {
                    List<String> members = new ArrayList<>(team.getEntries());
                    Collections.sort(members);

                    if (index >= 0 && index < members.size()) {
                        String memberName = members.get(index);
                        Player memberPlayer = Bukkit.getPlayer(memberName);
                        Player requester = player.getPlayer();

                        boolean isAlive = false;
                        boolean isConnected = (memberPlayer != null && memberPlayer.isOnline());

                        if (isConnected) {
                            isAlive = memberPlayer.getWorld().getName().equals("world");
                        }

                        // ==========================================
                        // LÓGICA DE DISTANCIA
                        // ==========================================

                        String distanceStr = "";

                        if (requester != null && isConnected) {
                            if (!requester.getName().equals(memberName)) {
                                if (requester.getWorld().equals(memberPlayer.getWorld())) {
                                    int distance = (int) requester.getLocation().distance(memberPlayer.getLocation());
                                    distanceStr = " &8[&e" + distance + "m&8]";
                                } else {
                                    distanceStr = " &8[&cOtro Mundo&8]";
                                }
                            }
                        }

                        String headIcon = "";
                        String colorEstado = isAlive ? "&a" : "&f";
                        if (!isConnected) colorEstado = "&7";

                        return headIcon + "&8> " + colorEstado + memberName + distanceStr;
                    }
                }
            } catch (NumberFormatException ignored) {}
            return "";
        }

        return null;
    }
}