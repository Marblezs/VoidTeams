package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class TeamUtilityCommands implements CommandExecutor {

    private final VoidTeams plugin;

    public TeamUtilityCommands(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.msg(sender, "<#FF5C5C>Este comando solo puede usarlo un jugador.</#FF5C5C>");
            return true;
        }

        if (!player.hasPermission("voidteams.member")) {
            ChatUtil.msg(player, "<#FF5C5C>No tienes permiso para usar VoidTeams.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return true;
        }

        return switch (command.getName().toLowerCase()) {
            case "ti" -> {
                plugin.getTeamInventoryManager().open(player);
                yield true;
            }
            case "tl" -> {
                sendTeamLocation(player);
                yield true;
            }
            case "mores" -> {
                plugin.getOreTrackerManager().sendReport(player);
                yield true;
            }
            default -> false;
        };
    }

    private void sendTeamLocation(Player player) {
        if (plugin.getTeamManager().isChatLocked()) {
            ChatUtil.msg(player, "<#FF5C5C>El chat de equipo está bloqueado por el host.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            ChatUtil.msg(player, "<#FF5C5C>Debes estar en un equipo para usar /tl.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        TeamTheme theme = plugin.getTeamManager().getTheme(team);
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        String world = prettyWorld(player.getWorld().getName());

        String message = "<" + theme.hexColor() + "><bold>[" + theme.icon() + "] TL</bold></" + theme.hexColor() + "> " +
                "<white>" + player.getName() + "</white> <dark_gray>»</dark_gray> " +
                "<gray>X:</gray> <#22D3EE>" + x + "</#22D3EE> " +
                "<gray>Y:</gray> <#22D3EE>" + y + "</#22D3EE> " +
                "<gray>Z:</gray> <#22D3EE>" + z + "</#22D3EE> " +
                "<dark_gray>•</dark_gray> <gray>" + world + "</gray>";

        plugin.getTeamManager().sendToTeam(team, message);
        plugin.getSoundManager().broadcastToTeam(team, SoundManager.SoundType.TEAM_LOCATION);
    }

    private String prettyWorld(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("nether")) return "Nether";
        if (lower.contains("end")) return "End";
        return "Overworld";
    }
}
