package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
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

        plugin.getTeamManager().clearAllTeamsConsole();
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);

        int teamIndex = 1;
        Team currentTeam = null;
        int currentCount = 0;

        for (Player p : players) {
            if (currentTeam == null || currentCount >= teamSize) {
                currentTeam = sb.registerNewTeam("team_rand_" + teamIndex);
                plugin.getTeamManager().applyRandomTheme(currentTeam);
                teamIndex++;
                currentCount = 0;
            }
            currentTeam.addEntry(p.getName());
            plugin.getTeamManager().updatePlayerDatapackID(p.getName(), currentTeam);
            currentCount++;

            ChatUtil.msg(sender,"&aHas sido asignado aleatoriamente a un equipo.");
        }

        ChatUtil.msg(sender, "&eTodos los jugadores han sido aleatorizados en equipos de " + teamSize + ".");
        ChatUtil.broadcast("&a¡Los equipos han sido aleatorizados!");
    }

    public void shuffleForcingTeams(CommandSender sender) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        int teamSize = plugin.getTeamManager().getTeamSize();

        List<Player> unteamedPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (sb.getEntryTeam(p.getName()) == null) {
                unteamedPlayers.add(p);
            }
        }

        if (unteamedPlayers.isEmpty()) {
            ChatUtil.msg(sender, "&cNo hay jugadores sin equipo para aleatorizar.");
            return;
        }

        Collections.shuffle(unteamedPlayers);

        for (Player p : unteamedPlayers) {
            boolean added = false;

            for (Team t : sb.getTeams()) {
                // Actualizado a "team_"
                if (t.getName().startsWith("team_") && t.getSize() < teamSize) {
                    t.addEntry(p.getName());
                    plugin.getTeamManager().updatePlayerDatapackID(p.getName(), t);
                    ChatUtil.msg(p, "&aHas sido asignado a un equipo por un administrador.");
                    added = true;
                    break;
                }
            }

            if (!added) {
                // Actualizado a "team_"
                Team newTeam = sb.registerNewTeam("team_" + (System.currentTimeMillis() % 10000));
                plugin.getTeamManager().applyRandomTheme(newTeam);
                newTeam.addEntry(p.getName());
                plugin.getTeamManager().updatePlayerDatapackID(p.getName(), newTeam);
                ChatUtil.msg(p, "&aSe te ha asignado a un nuevo equipo.");
            }
        }

        ChatUtil.msg(sender, "&aSe han aleatorizado y asignado " + unteamedPlayers.size() + " jugadores a equipos.");
        ChatUtil.broadcast("&aLos jugadores sin equipo han sido asignados.");
    }
}