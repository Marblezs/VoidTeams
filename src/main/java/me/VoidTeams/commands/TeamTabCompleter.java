package me.VoidTeams.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("team")) {
            if (args.length == 1) {
                List<String> subCmds = Arrays.asList("invite", "accept", "leave", "color", "icon", "chat");
                for (String sub : subCmds) {
                    if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
                }
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept"))) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) completions.add(p.getName());
                }
            }
        }
        else if (command.getName().equalsIgnoreCase("teamadmin") || command.getName().equalsIgnoreCase("teamadm")) {
            if (args.length == 1) {
                List<String> subCmds = Arrays.asList("force", "clear", "disband", "remove", "type", "shuffle", "shuffleforce", "color", "icon", "block");
                for (String sub : subCmds) {
                    if (sub.startsWith(args[0].toLowerCase())) completions.add(sub);
                }
            } else if (args.length == 2) {
                String sub = args[0].toLowerCase();
                if (sub.equals("type")) {
                    if ("choosen".startsWith(args[1].toLowerCase())) completions.add("Choosen");
                    if ("random".startsWith(args[1].toLowerCase())) completions.add("Random");
                } else if (sub.equals("block")) {
                    List<String> blockOptions = Arrays.asList("all", "chat", "teams", "none");
                    for (String opt : blockOptions) {
                        if (opt.startsWith(args[1].toLowerCase())) completions.add(opt);
                    }
                } else if (Arrays.asList("force", "disband", "remove", "color", "icon").contains(sub)) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) completions.add(p.getName());
                    }
                }
            } else if (args.length == 3) {
                String sub = args[0].toLowerCase();
                if (sub.equals("force")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) completions.add(p.getName());
                    }
                } else if (sub.equals("color")) {
                    List<String> colors = Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "AQUA", "GOLD", "LIGHT_PURPLE", "WHITE", "GRAY", "DARK_GRAY", "DARK_RED", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_PURPLE", "BLACK");
                    for (String c : colors) {
                        if (c.toLowerCase().startsWith(args[2].toLowerCase())) completions.add(c);
                    }
                }
            }
        }
        return completions;
    }
}