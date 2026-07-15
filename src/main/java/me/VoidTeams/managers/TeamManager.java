package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TeamManager {

    private final VoidTeams plugin;
    private final Scoreboard sb;
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    private String teamType;
    private int teamSize;

    public TeamManager(VoidTeams plugin) {
        this.plugin = plugin;
        this.sb = Bukkit.getScoreboardManager().getMainScoreboard();

        this.teamSize = plugin.getConfig().getInt("max-team-size", 2);
        this.teamType = plugin.getConfig().getString("team-type", "Choosen");
    }

    public int getTeamSize() { return teamSize; }
    public String getTeamType() { return teamType; }

    public void setTeamType(CommandSender sender, String type, int size) {
        if (type.equalsIgnoreCase("Choosen") || type.equalsIgnoreCase("Random")) {
            this.teamType = type;
            this.teamSize = size;

            plugin.getConfig().set("team-type", type);
            plugin.getConfig().set("max-team-size", size);
            plugin.saveConfig();

            ChatUtil.msg(sender, "&aModo actualizado: &e" + type + " &ade tamaño &e" + size);
            ChatUtil.broadcast("&aEl administrador ha configurado los equipos como: &e" + type + " &ade tamaño &e" + size);
        } else {
            ChatUtil.msg(sender, "&cEl tipo debe ser 'Choosen' o 'Random'.");
        }
    }

    public void invitePlayer(Player inviter, Player target) {
        if (teamType.equalsIgnoreCase("Random")) {
            ChatUtil.msg(inviter, "&cNo puedes invitar jugadores en el modo Random.");
            return;
        }

        Team myTeam = sb.getEntryTeam(inviter.getName());
        if (myTeam != null && myTeam.getSize() >= teamSize) {
            ChatUtil.msg(inviter, "&cTu equipo ya esta lleno (Max: " + teamSize + ").");
            return;
        }

        pendingInvites.put(target.getUniqueId(), inviter.getUniqueId());
        ChatUtil.msg(inviter, "&aInvitacion enviada a &b" + target.getName());
        ChatUtil.msg(target, "&b" + inviter.getName() + " &ate ha invitado a su equipo. Usa /team accept " + inviter.getName());
    }

    public void acceptInvite(Player player, Player leader) {
        if (teamType.equalsIgnoreCase("Random")) {
            ChatUtil.msg(player, "&cLas invitaciones estan deshabilitadas en modo Random.");
            return;
        }

        if (pendingInvites.containsKey(player.getUniqueId()) && pendingInvites.get(player.getUniqueId()).equals(leader.getUniqueId())) {
            Team lTeam = sb.getEntryTeam(leader.getName());
            if (lTeam == null) {
                lTeam = sb.getTeam("uhc_" + leader.getName());
                if (lTeam == null) {
                    lTeam = sb.registerNewTeam("uhc_" + leader.getName());
                    lTeam.setPrefix(ChatColor.WHITE + "[#1] ");
                }
                lTeam.addEntry(leader.getName());
            }

            if (lTeam.getSize() >= teamSize) {
                ChatUtil.msg(player, "&cEse equipo ya alcanzo el limite de " + teamSize + ".");
                return;
            }

            lTeam.addEntry(player.getName());
            pendingInvites.remove(player.getUniqueId());

            ChatUtil.broadcast("&b" + player.getName() + " &ase ha unido al equipo de &b" + leader.getName());
        } else {
            ChatUtil.msg(player, "&cNo tienes invitaciones de este jugador.");
        }
    }

    public void leaveTeam(Player player) {
        Team t = sb.getEntryTeam(player.getName());
        if (t != null) {
            t.removeEntry(player.getName());
            ChatUtil.msg(player, "&aHas salido de tu equipo.");
            if (t.getSize() == 0) t.unregister();

            if (plugin.getTeamsData().isChatToggled(player.getUniqueId())) {
                plugin.getTeamsData().toggleChat(player.getUniqueId());
                ChatUtil.msg(player, "&eTu chat de equipo se ha desactivado automáticamente.");
            }
        } else {
            ChatUtil.msg(player, "&cNo estas en ningun equipo.");
        }
    }

    public void setRandomColor(CommandSender sender, Player target) {
        Team team = sb.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "&cEse jugador no esta en ningun equipo.");
            return;
        }

        ChatColor[] colores = { ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.GOLD, ChatColor.LIGHT_PURPLE };
        ChatColor randomColor = colores[new Random().nextInt(colores.length)];

        try { team.setColor(randomColor); } catch (NoSuchMethodError ignored) {}

        String oldPrefix = team.getPrefix();
        String currentIcon = "#1";
        if (oldPrefix != null && oldPrefix.contains("[") && oldPrefix.contains("]")) {
            currentIcon = oldPrefix.substring(oldPrefix.indexOf("[") + 1, oldPrefix.indexOf("]"));
        }

        team.setPrefix(randomColor + "[" + currentIcon + "] " + ChatColor.RESET);
        ChatUtil.msg(sender, "&aColor actualizado a: " + randomColor + randomColor.name());
    }

    public void setRandomIcon(CommandSender sender, Player target) {
        Team team = sb.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "&cEse jugador no esta en ningun equipo.");
            return;
        }

        String randomIcon = "#" + (new Random().nextInt(99) + 1);
        String oldPrefix = team.getPrefix();
        String colorString = ChatColor.WHITE.toString();

        if (oldPrefix != null && oldPrefix.length() >= 2 && oldPrefix.startsWith("§")) {
            colorString = oldPrefix.substring(0, 2);
        }

        team.setPrefix(colorString + "[" + randomIcon + "] " + ChatColor.RESET);
        ChatUtil.msg(sender, "&aIcono actualizado a: " + colorString + randomIcon);
    }
    
    public void setTeamColor(CommandSender sender, Player target, String colorName) {
        Team team = sb.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "&cEse jugador no está en un equipo.");
            return;
        }

        try {
            ChatColor newColor = ChatColor.valueOf(colorName.toUpperCase());
            String oldPrefix = team.getPrefix();

            String icon = "[#1]"; // Default
            if (oldPrefix != null && oldPrefix.contains("[") && oldPrefix.contains("]")) {
                icon = oldPrefix.substring(oldPrefix.indexOf("["), oldPrefix.indexOf("]") + 1);
            }

            team.setPrefix(newColor + icon + " ");
            ChatUtil.msg(sender, "&aColor establecido a " + newColor + colorName);
        } catch (IllegalArgumentException e) {
            ChatUtil.msg(sender, "&cColor inválido. Usa nombres estándar (RED, BLUE, GOLD, etc).");
        }
    }

    public void setTeamIcon(CommandSender sender, Player target, String iconText) {
        Team team = sb.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "&cEse jugador no está en un equipo.");
            return;
        }

        String oldPrefix = team.getPrefix();
        String colorCode = "&f";
        if (oldPrefix != null && oldPrefix.length() >= 2) {
            colorCode = oldPrefix.substring(0, 2);
        }

        team.setPrefix(colorCode + "[" + iconText + "] ");
        ChatUtil.msg(sender, "&aIcono establecido a: " + colorCode + "[" + iconText + "]");
    }


    public void forceJoin(CommandSender sender, Player p1, Player p2) {
        Team t2 = sb.getEntryTeam(p2.getName());
        if (t2 == null) {
            t2 = sb.getTeam("uhc_" + p2.getName());
            if (t2 == null) t2 = sb.registerNewTeam("uhc_" + p2.getName());
            t2.addEntry(p2.getName());
        }

        Team t1 = sb.getEntryTeam(p1.getName());
        if (t1 != null) {
            t1.removeEntry(p1.getName());
            if (t1.getSize() == 0) t1.unregister();
        }

        t2.addEntry(p1.getName());
        ChatUtil.msg(p1, "&aUn admin te forzo al equipo de &b" + p2.getName());
        ChatUtil.msg(p2, "&b" + p1.getName() + " &aha sido forzado a tu equipo.");
        ChatUtil.msg(sender, "&aJugador movido con exito.");
    }

    public void removePlayer(CommandSender sender, Player target) {
        Team team = sb.getEntryTeam(target.getName());
        if (team != null) {
            team.removeEntry(target.getName());
            ChatUtil.msg(target, "&cUn administrador te ha expulsado de tu equipo.");
            ChatUtil.msg(sender, "&aHas sacado a &b" + target.getName() + " &ade su equipo.");
            if (team.getSize() == 0) team.unregister();

            if (plugin.getTeamsData().isChatToggled(target.getUniqueId())) {
                plugin.getTeamsData().toggleChat(target.getUniqueId());
            }
        } else {
            ChatUtil.msg(sender, "&cEse jugador no esta en ningun equipo.");
        }
    }

    public void disbandTeam(CommandSender sender, Player target) {
        Team team = sb.getEntryTeam(target.getName());
        if (team != null) {
            for (String entry : team.getEntries()) {
                Player member = Bukkit.getPlayer(entry);
                if (member != null && plugin.getTeamsData().isChatToggled(member.getUniqueId())) {
                    plugin.getTeamsData().toggleChat(member.getUniqueId());
                }
            }
            team.unregister();
            ChatUtil.msg(sender, "&aEquipo disuelto.");
        } else {
            ChatUtil.msg(sender, "&cEse jugador no esta en un equipo.");
        }
    }

    public void clearAllTeamsConsole() {
        for (Team t : sb.getTeams()) {
            if (t.getName().startsWith("uhc_")) {
                t.unregister();
            }
        }
    }

    public void clearAllTeams(CommandSender sender) {
        clearAllTeamsConsole();
        ChatUtil.msg(sender, "&aSe han eliminado todos los equipos.");
    }
}