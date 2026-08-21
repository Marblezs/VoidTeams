package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomTeamManager {

    private final VoidTeams plugin;

    public RandomTeamManager(VoidTeams plugin) {
        this.plugin = plugin;
    }

    public void shuffleTeams(CommandSender sender) {
        int teamSize = plugin.getTeamManager().getTeamSize();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (players.isEmpty()) {
            ChatUtil.msg(sender, "<#FF5C5C>No hay jugadores online para repartir.</#FF5C5C>");
            return;
        }

        plugin.getTeamManager().clearAllTeamsConsole();
        Collections.shuffle(players);

        List<Team> createdTeams = new ArrayList<>();
        Team currentTeam = null;
        int currentCount = 0;

        for (Player player : players) {
            if (currentTeam == null || currentCount >= teamSize) {
                currentTeam = plugin.getTeamManager().createTeam();
                createdTeams.add(currentTeam);
                currentCount = 0;
            }

            currentTeam.addEntry(player.getName());
            plugin.getTeamManager().updatePlayerDatapackID(player.getName(), currentTeam);
            currentCount++;
        }

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#8B5CF6><bold>EQUIPOS ALEATORIOS</bold></#8B5CF6>\n" +
                "<gray>Jugadores:</gray> <white>" + players.size() + "</white>  " +
                "<dark_gray>•</dark_gray>  <gray>TeamSize:</gray> <#22D3EE>" + teamSize + "</#22D3EE>  " +
                "<dark_gray>•</dark_gray>  <gray>Equipos:</gray> <#FFD93D>" + createdTeams.size() + "</#FFD93D>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );

        for (Player player : players) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
            if (team != null) {
                plugin.getTeamManager().notifyTeamAssignment(player, team, true);
            }
        }

        ChatUtil.msg(sender, "<#6BCB77>Shuffle completado correctamente.</#6BCB77>");
    }

    public void shuffleForcingTeams(CommandSender sender) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        int teamSize = plugin.getTeamManager().getTeamSize();

        List<Player> unteamedPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (scoreboard.getEntryTeam(player.getName()) == null) {
                unteamedPlayers.add(player);
            }
        }

        if (unteamedPlayers.isEmpty()) {
            ChatUtil.msg(sender, "<gray>No hay jugadores sin equipo para repartir.</gray>");
            return;
        }

        Collections.shuffle(unteamedPlayers);
        int createdTeams = 0;

        for (Player player : unteamedPlayers) {
            Team destination = findTeamWithSpace(scoreboard, teamSize);
            if (destination == null) {
                destination = plugin.getTeamManager().createTeam();
                createdTeams++;
            }

            destination.addEntry(player.getName());
            plugin.getTeamManager().updatePlayerDatapackID(player.getName(), destination);
            plugin.getTeamManager().notifyTeamAssignment(player, destination, true);
        }

        ChatUtil.broadcastNoPrefix(
                "<#8B5CF6><bold>RANDOM FILL</bold></#8B5CF6> <dark_gray>»</dark_gray> " +
                "<white>" + unteamedPlayers.size() + "</white> <gray>jugadores sin equipo fueron repartidos.</gray> " +
                "<dark_gray>(Nuevos equipos: " + createdTeams + ")</dark_gray>"
        );
        ChatUtil.msg(sender, "<#6BCB77>Asignación aleatoria completada.</#6BCB77>");
    }

    private Team findTeamWithSpace(Scoreboard scoreboard, int teamSize) {
        List<Team> candidates = new ArrayList<>();
        for (Team team : scoreboard.getTeams()) {
            String name = team.getName();
            if ((name.startsWith("vt_") || name.startsWith("team_")) && team.getSize() < teamSize) {
                candidates.add(team);
            }
        }

        if (candidates.isEmpty()) return null;
        Collections.shuffle(candidates);
        return candidates.getFirst();
    }
}
