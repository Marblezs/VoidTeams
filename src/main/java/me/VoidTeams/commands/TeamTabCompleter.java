package me.VoidTeams.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeamTabCompleter implements TabCompleter {

    private static final List<String> TEAM_SUB_COMMANDS = Arrays.asList(
            "invite", "accept", "leave", "color", "icon", "chat"
    );

    private static final List<String> TEAMADMIN_SUB_COMMANDS = Arrays.asList(
            "force", "clear", "disband", "remove", "type", "size", "shuffle", "shuffleforce", "color", "icon", "block", "startvote"
    );

    private static final List<String> TEAM_TYPES = Arrays.asList(
            "Choosen", "Random", "Vote", "Auctions", "Moles"
    );

    private static final List<String> BLOCK_OPTIONS = Arrays.asList(
            "all", "chat", "teams", "none"
    );

    private static final List<String> COLORS = Arrays.asList(
            "RED", "BLUE", "GREEN", "YELLOW", "AQUA", "GOLD", "LIGHT_PURPLE", "WHITE",
            "GRAY", "DARK_GRAY", "DARK_RED", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_PURPLE", "BLACK"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> candidates = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("team")) {
            if (args.length == 1) {
                candidates = TEAM_SUB_COMMANDS;
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept"))) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    candidates.add(p.getName());
                }
            }
        }
        else if (command.getName().equalsIgnoreCase("teamadmin") || command.getName().equalsIgnoreCase("teamadm")) {
            if (args.length == 1) {
                candidates = TEAMADMIN_SUB_COMMANDS;
            } else if (args.length == 2) {
                String sub = args[0].toLowerCase();

                if (sub.equals("type")) {
                    candidates = TEAM_TYPES;
                } else if (sub.equals("block")) {
                    candidates = BLOCK_OPTIONS;
                } else if (sub.equals("size")) {
                    candidates = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
                } else if (Arrays.asList("force", "disband", "remove", "color", "icon").contains(sub)) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        candidates.add(p.getName());
                    }
                }
            } else if (args.length == 3) {
                String sub = args[0].toLowerCase();

                if (sub.equals("force")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        candidates.add(p.getName());
                    }
                } else if (sub.equals("color")) {
                    candidates = COLORS;
                }
            }
        }
        StringUtil.copyPartialMatches(args[args.length - 1], candidates, completions);
        Collections.sort(completions);

        return completions;
    }
}