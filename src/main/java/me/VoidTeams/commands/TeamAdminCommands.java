package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import net.kyori.adventure.platform.facet.Facet;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
            ChatUtil.msg(sender, "&8&m--------------------------------");
            ChatUtil.msg(sender, "&f/teamadm color <jugador> &7- Asigna un color al equipo");
            ChatUtil.msg(sender, "&f/teamadm icon <jugador> &7- Asigna un icono al equipo");
            ChatUtil.msg(sender, "&f/teamadm remove <jugador> &7- Remueve a alguien del equipo");
            ChatUtil.msg(sender, "&f/teamadm disband <jugador> &7- Remueve el equipo completo.");
            ChatUtil.msg(sender, "&f/teamadm clear &7- Elimina todos los equipos.");
            ChatUtil.msg(sender, "&f/teamadm force <jugador1> <jugador2> &7- Mover jugador1 a jugador2 .");
            ChatUtil.msg(sender, "&f/teamadm shuffle &7- Realiza aleatoriedad en los equipos");
            ChatUtil.msg(sender, "&f/teamadm shuffleforce &7- Realiza aleatoriedad en los equipos SI O SI");
            ChatUtil.msg(sender, "&f/teamadm block &7- Realiza los bloqueos correspondiente");
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
                if (args.length < 3) {
                    ChatUtil.msg(sender, "&cUso: /teamadmin color <jugador> <color>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                String colorName = args[2];
                if (target != null) {
                    plugin.getTeamManager().setTeamColor(sender, target, colorName);
                } else {
                    ChatUtil.msg(sender, "&cJugador no encontrado.");
                }
            }
            case "icon", "icono" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "&cUso: /teamadmin icono <jugador> <texto>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                String iconText = args[2];
                if (target != null) {
                    plugin.getTeamManager().setTeamIcon(sender, target, iconText);
                } else {
                    ChatUtil.msg(sender, "&cJugador no encontrado.");
                }
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
            case "block" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "&cUso: /teamadmin block <all, chat, teams, none>");
                    return true;
                }
                String target = args[1].toLowerCase();
                switch (target) {
                    case "all" -> {
                        plugin.getTeamManager().setTeamsLocked(true);
                        plugin.getTeamManager().setChatLocked(true);
                        ChatUtil.broadcast("&cTodo el sistema de equipos y chat ha sido bloqueado por la administracion.");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        }
                    }
                    case "chat" -> {
                        plugin.getTeamManager().setChatLocked(true);
                        ChatUtil.broadcast("&cEl chat de equipo ha sido bloqueado.");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        }
                    }
                    case "teams" -> {
                        plugin.getTeamManager().setTeamsLocked(true);
                        ChatUtil.broadcast("&cLa creacion y modificacion de equipos ha sido bloqueada.");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        }
                    }
                    case "none" -> {
                        plugin.getTeamManager().setTeamsLocked(false);
                        plugin.getTeamManager().setChatLocked(false);
                        ChatUtil.broadcast("&aEl sistema de equipos y chat ha sido desbloqueado.");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.0f);
                        }
                    }
                    default -> ChatUtil.msg(sender, "&cOpcion invalida. Usa: all, chat, teams, none.");
                }
            }
            case "reload" -> {
                plugin.getTeamManager().reloadConfigValues();
                ChatUtil.msg(sender, "&aConfiguracion de VoidTeams recargada correctamente.");
            }

            default -> ChatUtil.msg(sender, "&cComando desconocido.");
        }
        return true;
    }
}