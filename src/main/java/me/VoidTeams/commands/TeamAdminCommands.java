package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
            ChatUtil.msg(sender, "&8&m--------------------------------");
            ChatUtil.msg(sender, "&f/teamadm color <jugador> &7- Asigna un color al equipo");
            ChatUtil.msg(sender, "&f/teamadm icon <jugador> &7- Asigna un icono al equipo");
            ChatUtil.msg(sender, "&f/teamadm remove <jugador> &7- Remueve a alguien del equipo");
            ChatUtil.msg(sender, "&f/teamadm disband <jugador> &7- Remueve el equipo completo.");
            ChatUtil.msg(sender, "&f/teamadm clear &7- Elimina todos los equipos.");
            ChatUtil.msg(sender, "&f/teamadm force <jugador1> <jugador2> &7- Mover jugador1 a jugador2.");
            ChatUtil.msg(sender, "&f/teamadm shuffle &7- Aleatoriedad (Respeta modo actual)");
            ChatUtil.msg(sender, "&f/teamadm shuffleforce &7- Aleatoriedad forzada");
            ChatUtil.msg(sender, "&f/teamadm type <Choosen|Random|Vote> &7- Cambia el modo");
            ChatUtil.msg(sender, "&f/teamadm size <tamaño> &7- Cambia el tamaño maximo");

            ChatUtil.msg(sender, "&f/teamadm vote <type|size> <opc1> <opc2> [opc3..5] &7- Inicia votacion");
            ChatUtil.msg(sender, "&f/teamadm vote stop &7- Finaliza la votacion actual");
            ChatUtil.msg(sender, "&f/teamadm block <all|chat|teams|none> &7- Control de bloqueos");
            ChatUtil.msg(sender, "&8&m--------------------------------");
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
                if (args.length < 2) { ChatUtil.msg(sender, "&cUso: /teamadmin remove <j1>"); return true; }
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
                if (args.length < 3) { ChatUtil.msg(sender, "&cUso: /teamadmin color <jugador> <color>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    plugin.getTeamManager().setTeamColor(sender, target, args[2]);
                } else {
                    ChatUtil.msg(sender, "&cJugador no encontrado.");
                }
            }
            case "icon", "icono" -> {
                if (args.length < 3) { ChatUtil.msg(sender, "&cUso: /teamadmin icono <jugador> <texto>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    plugin.getTeamManager().setTeamIcon(sender, target, args[2]);
                } else {
                    ChatUtil.msg(sender, "&cJugador no encontrado.");
                }
            }
            case "shuffle" -> {
                String currentType = plugin.getTeamManager().getTeamType();
                if (!currentType.equalsIgnoreCase("Random")) {
                    ChatUtil.msg(sender, "&c&lADVERTENCIA: &eEl modo actual es &a" + currentType + "&e. Usa /teamadm shuffleforce");
                    return true;
                }
                plugin.getRandomTeamManager().shuffleForcingTeams(sender);
            }
            case "shuffleforce" -> plugin.getRandomTeamManager().shuffleTeams(sender);

            case "type" -> {
                if (args.length < 2) { ChatUtil.msg(sender, "&cUso: /teamadmin type <Choosen|Random|Vote>"); return true; }
                String tipo = args[1];
                if (!tipo.equalsIgnoreCase("Choosen") && !tipo.equalsIgnoreCase("Random") && !tipo.equalsIgnoreCase("Vote")) {
                    ChatUtil.msg(sender, "&cEl tipo debe ser 'Choosen', 'Random', o 'Vote'.");
                    return true;
                }
                plugin.getTeamManager().setTeamType(sender, tipo);
                ChatUtil.msg(sender, "&aTipo de equipo establecido a: &e" + tipo);
            }

            case "size" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "&cUso: /teamadmin size <set|add|remove> <valor>");
                    return true;
                }

                String action = args[1].toLowerCase();
                try {
                    int value = Integer.parseInt(args[2]);
                    int currentSize = plugin.getTeamManager().getTeamSize();
                    int newSize = currentSize;

                    switch (action) {
                        case "set" -> newSize = value;
                        case "add" -> newSize = currentSize + value;
                        case "remove" -> {
                            newSize = currentSize - value;
                            if (newSize < 1) {
                                newSize = 1; // Evitamos tamaños de equipo menores a 1
                            }
                        }
                        default -> {
                            ChatUtil.msg(sender, "&cAccion invalida. Usa: &eids set, add o remove&c.");
                            return true;
                        }
                    }

                    plugin.getTeamManager().setTeamSize(sender, newSize);
                    ChatUtil.msg(sender, "&aTamaño de equipos actualizado a: &e" + newSize + " &7(Anterior: " + currentSize + ")");
                } catch (NumberFormatException e) {
                    ChatUtil.msg(sender, "&cEl valor ingresado debe ser un numero entero valido.");
                }
            }

            case "vote" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "&cUso: /teamadm vote <type|size|stop> [opciones...]");
                    return true;
                }

                String action = args[1].toLowerCase();

                if (action.equals("stop")) {
                    plugin.getVoteTeamManager().stopVote(sender);
                    return true;
                }

                if (action.equals("type") || action.equals("size")) {
                    if (args.length < 4) {
                        ChatUtil.msg(sender, "&cDebes colocar al menos 2 opciones. Ej: /teamadm vote " + action + " 1 2 3");
                        return true;
                    }

                    List<String> options = new ArrayList<>();
                    for (int i = 2; i < args.length && options.size() < 5; i++) {
                        options.add(args[i]);
                    }

                    plugin.getVoteTeamManager().startVote(sender, action, options);
                } else {
                    ChatUtil.msg(sender, "&cOpcion invalida. Usa: type, size o stop.");
                }
            }

            case "block" -> {
                if (args.length < 2) { ChatUtil.msg(sender, "&cUso: /teamadmin block <all, chat, teams, none>"); return true; }
                String target = args[1].toLowerCase();
                switch (target) {
                    case "all" -> {
                        plugin.getTeamManager().setTeamsLocked(true);
                        plugin.getTeamManager().setChatLocked(true);
                        ChatUtil.broadcast("&cTodo el sistema de equipos y chat ha sido bloqueado.");
                    }
                    case "chat" -> {
                        plugin.getTeamManager().setChatLocked(true);
                        ChatUtil.broadcast("&cEl chat de equipo ha sido bloqueado.");
                    }
                    case "teams" -> {
                        plugin.getTeamManager().setTeamsLocked(true);
                        ChatUtil.broadcast("&cLa creacion de equipos ha sido bloqueada.");
                    }
                    case "none" -> {
                        plugin.getTeamManager().setTeamsLocked(false);
                        plugin.getTeamManager().setChatLocked(false);
                        ChatUtil.broadcast("&aEl sistema de equipos y chat ha sido desbloqueado.");
                    }
                    default -> ChatUtil.msg(sender, "&cOpcion invalida. Usa: all, chat, teams, none.");
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), target.equals("none") ? Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE : Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
            }
            case "reload" -> {
                plugin.getTeamManager().reloadConfigValues();
                ChatUtil.msg(sender, "&aConfiguracion recargada.");
            }

            default -> ChatUtil.msg(sender, "&cComando desconocido.");
        }
        return true;
    }
}