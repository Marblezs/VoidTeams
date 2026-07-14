package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamAdminCommands implements CommandExecutor {

    private final VoidTeams plugin;

    public TeamAdminCommands(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("VoidUHC.admin")) {
            ChatUtil.msg(sender, "&cSin permisos de admin.");
            return true;
        }

        if (args.length == 0) {
            ChatUtil.msg(sender, "&cUso: /teamadmin <force|clear|disband|color|icon|remove|type>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "force" -> {
                if (args.length < 3) { ChatUtil.msg(sender, "&cUso: /teamadmin force <j1> <j2>"); return true; }
                Player p1 = Bukkit.getPlayer(args[1]);
                Player p2 = Bukkit.getPlayer(args[2]);
                if (p1 == null || p2 == null) { ChatUtil.msg(sender, "&cJugadores offline."); return true; }
                plugin.getTeamManager().forceJoin(sender, p1, p2);
            }
            case "remove" -> {
                if (args.length <2) { ChatUtil.msg(sender, "&cUso: /teamadmin remove <j1>"); return true; }
                    Player p1 = Bukkit.getPlayer(args[1]);
                    if (p1 == null) { ChatUtil.msg(sender, "&cJugadores offline."); return true; }
                    plugin.getTeamManager().removePlayer(sender, p1);
            }
            case "clear" -> plugin.getTeamManager().clearAllTeams(sender);
            case "disband" -> {
                if (args.length < 2) { ChatUtil.msg(sender,"&cUso: /teamadmin disband <jugador>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { ChatUtil.msg(sender, "&cJugador offline."); return true; }
                plugin.getTeamManager().disbandTeam(sender, target);
            }
            case "color" -> {
                if (args.length < 2) return true;
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) plugin.getTeamManager().setRandomColor(sender, target);
            }
            case "icon", "icono" -> {
                if (args.length < 2) return true;
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) plugin.getTeamManager().setRandomIcon(sender, target);
            }
            case "shuffle" -> {
                String currentType = plugin.getTeamManager().getTeamType();

                if (!currentType.equalsIgnoreCase("Random")) {
                    ChatUtil.msg(sender, "&c&lADVERTENCIA: &eEl modo actual es &a" + currentType + " Usa /team shufflefroce si eso buscas");
                    return true;
                }
                plugin.getRandomTeamManager().shuffleForcingTeams(sender);
            }
            case "shuffleforce" -> plugin.getRandomTeamManager().shuffleTeams(sender);

            case "type" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "&cUso: /teamadmin type <Choosen|Random> <tamaño>");
                    return true;
                }

                String tipo = args[1];
                if (!tipo.equalsIgnoreCase("Choosen") && !tipo.equalsIgnoreCase("Random")) {
                    ChatUtil.msg(sender, "&cEl tipo debe ser 'Choosen' o 'Random'.");
                    return true;
                }

                try {
                    int size = Integer.parseInt(args[2]);
                    plugin.getTeamManager().setTeamType(sender, tipo, size);
                } catch (NumberFormatException e) {
                    ChatUtil.msg(sender, "&cEl tamaño debe ser un numero valido.");
                }
            }

            default -> ChatUtil.msg(sender, "&cComando desconocido.");
        }
        return true;
    }
}