package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommands implements CommandExecutor {

    private final VoidTeams plugin;

    public TeamCommands(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.msg(sender, "Solo jugadores.");
            return true;
        }

        if (!player.hasPermission("Voiduhc.Member")) {
            ChatUtil.msg(sender, "&cSin permisos.");
            return true;
        }

        if (args.length == 0) {
            ChatUtil.msg(sender, "&cUsa: /team <invite|accept|leave|color|icon>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "invite" -> {
                if (args.length < 2) { ChatUtil.msg(sender, "&cUso: /team invite <jugador>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { ChatUtil.msg(sender, "&cJugador no encontrado."); return true; }
                plugin.getTeamManager().invitePlayer(player, target);
            }
            case "accept" -> {
                if (args.length < 2) { ChatUtil.msg(sender, "&cUso: /team accept <jugador>"); return true; }
                Player leader = Bukkit.getPlayer(args[1]);
                if (leader == null) { ChatUtil.msg(sender, "&cJugador no encontrado."); return true; }
                plugin.getTeamManager().acceptInvite(player, leader);
            }
            case "leave" -> plugin.getTeamManager().leaveTeam(player);
            case "color" -> plugin.getTeamManager().setRandomColor(player, player);
            case "icon", "icono" -> plugin.getTeamManager().setRandomIcon(player, player);
            case "chat" -> {
                plugin.getTeamsData().toggleChat(player.getUniqueId());
                boolean enabled = plugin.getTeamsData().isChatToggled(player.getUniqueId());
                ChatUtil.msg(sender, "&bChat de equipo " + (enabled ? "activado" : "desactivado") + ".");
            }
            default -> ChatUtil.msg(sender, "&cComando desconocido.");
        }
        return true;
    }
}