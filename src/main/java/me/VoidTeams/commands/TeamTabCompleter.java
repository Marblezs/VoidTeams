package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import me.VoidTeams.scenarios.TeamScenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeamTabCompleter implements TabCompleter {

    private final VoidTeams plugin;

    public TeamTabCompleter(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> candidates = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("team")) {
            if (args.length == 1) {
                candidates.addAll(List.of("invite", "accept", "leave", "chat", "info", "color", "icon"));
                if (plugin.getTeamScenarioManager().isEnabled("captains")) candidates.add("pick");
                if (plugin.getTeamScenarioManager().isEnabled("auction")) candidates.add("bid");
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept"))) {
                onlinePlayers(candidates);
            } else if (args.length == 2 && args[0].equalsIgnoreCase("info") && sender.hasPermission("voidteams.admin")) {
                onlinePlayers(candidates);
            } else if (args.length == 2 && args[0].equalsIgnoreCase("pick")) {
                onlinePlayers(candidates);
            } else if (args.length == 2 && args[0].equalsIgnoreCase("bid")) {
                candidates.addAll(List.of("5", "10", "20", "25", "50", "75", "100"));
            }
        } else if (command.getName().equalsIgnoreCase("teamadmin")) {
            if (args.length == 1) {
                candidates.addAll(List.of("force", "remove", "disband", "clear", "color", "icon", "shuffle", "shuffleforce",
                        "type", "size", "vote", "block", "friendlyfire", "teaminventory", "scen", "config", "info", "reload"));
            } else if (args.length == 2) {
                String sub = args[0].toLowerCase();
                switch (sub) {
                    case "force", "remove", "disband", "color", "icon", "info" -> onlinePlayers(candidates);
                    case "type" -> candidates.addAll(List.of("Choosen", "Random", "Vote"));
                    case "size" -> candidates.addAll(List.of("set", "add", "remove"));
                    case "vote" -> candidates.addAll(List.of("type", "size", "stop"));
                    case "block" -> candidates.addAll(List.of("all", "chat", "teams", "none"));
                    case "friendlyfire", "ff" -> candidates.addAll(List.of("on", "off"));
                    case "teaminventory", "team-inventory", "ti" -> candidates.addAll(List.of("toggle", "on", "off", "see"));
                    case "scen", "scenario", "scenarios" -> {
                        candidates.addAll(List.of("list", "toggle", "on", "off", "info", "reload"));
                        candidates.addAll(plugin.getTeamScenarioManager().ids());
                    }
                    case "config", "configs" -> candidates.addAll(List.of("list", "friendlyfire", "chat", "teams", "size", "type"));
                }
            } else if (args.length == 3) {
                String sub = args[0].toLowerCase();
                if (sub.equals("force")) {
                    onlinePlayers(candidates);
                } else if (sub.equals("color")) {
                    candidates.addAll(plugin.getTeamManager().getRgbSuggestions());
                } else if (sub.equals("size")) {
                    candidates.addAll(List.of("1", "2", "3", "4", "5"));
                } else if (sub.equals("vote") && args[1].equalsIgnoreCase("type")) {
                    candidates.addAll(List.of("Choosen", "Random", "Vote"));
                } else if (sub.equals("vote") && args[1].equalsIgnoreCase("size")) {
                    candidates.addAll(List.of("1", "2", "3", "4"));
                } else if ((sub.equals("teaminventory") || sub.equals("team-inventory") || sub.equals("ti"))
                        && args[1].equalsIgnoreCase("see")) {
                    onlinePlayers(candidates);
                } else if (sub.equals("scen") || sub.equals("scenario") || sub.equals("scenarios")) {
                    String second = args[1].toLowerCase();
                    if (List.of("toggle", "on", "off", "info").contains(second)) {
                        candidates.addAll(plugin.getTeamScenarioManager().ids());
                    } else {
                        TeamScenario scenario = plugin.getTeamScenarioManager().get(second);
                        if (scenario != null) {
                            candidates.addAll(List.of("toggle", "on", "off", "info"));
                            candidates.addAll(scenario.tabComplete(sender, new String[]{args[2]}));
                        }
                    }
                } else if (sub.equals("config") || sub.equals("configs")) {
                    switch (args[1].toLowerCase()) {
                        case "friendlyfire", "chat", "teams" -> candidates.addAll(List.of("on", "off"));
                        case "size" -> candidates.addAll(List.of("1", "2", "3", "4", "5"));
                        case "type" -> candidates.addAll(List.of("Choosen", "Random", "Vote"));
                    }
                }
            } else if (args.length >= 4) {
                String sub = args[0].toLowerCase();
                if (sub.equals("scen") || sub.equals("scenario") || sub.equals("scenarios")) {
                    TeamScenario scenario = plugin.getTeamScenarioManager().get(args[1]);
                    if (scenario != null) {
                        String[] scenarioArgs = Arrays.copyOfRange(args, 2, args.length);
                        candidates.addAll(scenario.tabComplete(sender, scenarioArgs));
                    }
                }
            }
        }

        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], candidates, completions);
        Collections.sort(completions);
        return completions;
    }

    private void onlinePlayers(List<String> output) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            output.add(player.getName());
        }
    }
}
