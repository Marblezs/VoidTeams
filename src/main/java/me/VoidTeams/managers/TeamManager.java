package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

// importaciones importantes imports para importar.
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TeamManager {

    private final VoidTeams plugin;
    private final Scoreboard sb;
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    private String teamType;
    private int teamSize;
    // var de bloqueo
    private boolean teamsLocked = false;
    private boolean chatLocked = false;

    // gett y setts
    public boolean isTeamsLocked() { return teamsLocked; }
    public boolean isChatLocked() { return chatLocked; }
    public void setTeamsLocked(boolean locked) { this.teamsLocked = locked; }
    public void setChatLocked(boolean locked) { this.chatLocked = locked; }


    //scoreboard discreto para radicate
    private final org.bukkit.scoreboard.Objective datapackObj;
    private int nextTeamId = 1;
    private final Map<String, Integer> teamIdMap = new HashMap<>();

    private final ChatColor[] availableColors = {
            ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW,
            ChatColor.AQUA, ChatColor.GOLD, ChatColor.LIGHT_PURPLE
    };
    private final List<String> availableIcons;

    public TeamManager(VoidTeams plugin) {
        this.plugin = plugin;
        this.sb = Bukkit.getScoreboardManager().getMainScoreboard();
        //datapack
        org.bukkit.scoreboard.Objective obj = sb.getObjective("vt_team_id");
        if (obj == null) {
            obj = sb.registerNewObjective("vt_team_id", "dummy", "Team ID");
        }
        this.datapackObj = obj;

        this.teamSize = plugin.getConfig().getInt("max-team-size", 2);
        this.teamType = plugin.getConfig().getString("team-type", "Choosen");

        List<String> iconsConfig = plugin.getConfig().getStringList("team-icons");
        if (iconsConfig.isEmpty()) {
            this.availableIcons = Arrays.asList("asd", "icon", "⚔", "★");
        } else {
            this.availableIcons = iconsConfig;
        }
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

            ChatUtil.msg(sender, "&aModo actualizado: &e" + type + " &ade tamanio &e" + size);
            ChatUtil.broadcast("&aEl administrador ha configurado los equipos como: &e" + type + " &ade tamanio &e" + size);
        } else {
            ChatUtil.msg(sender, "&cEl tipo debe ser 'Choosen' o 'Random'.");
        }
    }

    public void invitePlayer(Player inviter, Player target) {
        if (teamType.equalsIgnoreCase("Random")) {
            ChatUtil.msg(inviter, "&cNo puedes invitar jugadores en el modo Random.");
            return;

        }
        if (teamsLocked) {
            ChatUtil.msg(inviter, "&cLa creación y modificación de equipos está bloqueada.");
            return;
        }

        Team myTeam = sb.getEntryTeam(inviter.getName());
        if (myTeam != null && myTeam.getSize() >= teamSize) {
            ChatUtil.msg(inviter, "&cTu equipo ya esta lleno (Max: " + teamSize + ").");
            return;
        }

        pendingInvites.put(target.getUniqueId(), inviter.getUniqueId());
        ChatUtil.msg(inviter, "&aInvitacion enviada a &b" + target.getName());
        TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&b" + inviter.getName() + " &ate ha invitado a su equipo. "));
        TextComponent click = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&e&l[HAZ CLICK AQUI PARA ACEPTAR]"));

        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept " + inviter.getName()));
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click para unirte al equipo de " + inviter.getName()).create()));

        msg.addExtra(click);
        target.spigot().sendMessage(msg);
    }

    public void acceptInvite(Player player, Player leader) {
        if (teamsLocked) {
            ChatUtil.msg(player, "&cNo puedes unirte a equipos en este momento.");
            return;
        }
        if (teamType.equalsIgnoreCase("Random")) {
            ChatUtil.msg(player, "&cLas invitaciones estan deshabilitadas en modo Random.");
            return;
        }

        if (pendingInvites.containsKey(player.getUniqueId()) && pendingInvites.get(player.getUniqueId()).equals(leader.getUniqueId())) {
            Team lTeam = sb.getEntryTeam(leader.getName());
            if (lTeam == null) {
                lTeam = sb.getTeam("team_" + leader.getName());
                if (lTeam == null) {
                    lTeam = sb.registerNewTeam("team_" + leader.getName());
                    applyRandomTheme(lTeam);
                    lTeam.setAllowFriendlyFire(plugin.getConfig().getBoolean("friendly-fire", false));
                    player.sendTitle(ChatColor.GREEN + "¡Equipo Creado!", ChatColor.YELLOW + "Mucha suerte", 10, 70, 20);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
                lTeam.addEntry(leader.getName());
                updatePlayerDatapackID(leader.getName(), lTeam);
            }

            if (lTeam.getSize() >= teamSize) {
                ChatUtil.msg(player, "&cEse equipo ya alcanzo el limite de " + teamSize + ".");
                return;
            }

            lTeam.addEntry(player.getName());
            updatePlayerDatapackID(player.getName(), lTeam);
            pendingInvites.remove(player.getUniqueId());

            ChatUtil.broadcast("&b" + player.getName() + " &ase ha unido al equipo de &b" + leader.getName());
        } else {
            ChatUtil.msg(player, "&cNo tienes invitaciones de este jugador.");
        }
    }
    public void leaveTeam(Player player) {
        Team t = sb.getEntryTeam(player.getName());
        if (teamsLocked) {
            ChatUtil.msg(player, "&cLa creación y modificación de equipos está bloqueada.");
            return;
        }

        if (t != null) {
            t.removeEntry(player.getName());
            updatePlayerDatapackID(player.getName(), null);
            ChatUtil.msg(player, "&aHas salido de tu equipo.");
            if (t.getSize() == 0) t.unregister();

            if (plugin.getTeamsData().isChatToggled(player.getUniqueId())) {
                plugin.getTeamsData().toggleChat(player.getUniqueId());
                ChatUtil.msg(player, "&eTu chat de equipo se ha desactivado automaticamente.");
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

        ChatColor randomColor = availableColors[new Random().nextInt(availableColors.length)];

        try { team.setColor(randomColor); } catch (NoSuchMethodError ignored) {}

        String oldPrefix = team.getPrefix();
        String currentIcon = "#1";
        if (oldPrefix != null && oldPrefix.contains("[") && oldPrefix.contains("]")) {
            currentIcon = oldPrefix.substring(oldPrefix.indexOf("[") + 1, oldPrefix.indexOf("]"));
        }

        team.setPrefix(randomColor + "[" + currentIcon + "] " + ChatColor.RESET);
        ChatUtil.msg(sender, "&aColor actualizado a: " + randomColor + randomColor.name());
    }

    public void applyRandomTheme(Team team) {
        Random random = new Random();
        ChatColor randomColor = availableColors[random.nextInt(availableColors.length)];
        String prefixIcon;

        boolean useCustomIcons = plugin.getConfig().getBoolean("use-custom-icons", false);

        if (useCustomIcons && !availableIcons.isEmpty()) {
            prefixIcon = availableIcons.get(random.nextInt(availableIcons.size()));
        } else {
            prefixIcon = "#" + (random.nextInt(99) + 1);
        }
        try { team.setColor(randomColor); } catch (NoSuchMethodError ignored) {}
        team.setPrefix(randomColor + "[" + prefixIcon + "] " + ChatColor.RESET);
    }

    public void updatePlayerDatapackID(String playerName, Team team) {
        if (team == null) {
            datapackObj.getScore(playerName).setScore(0);
        } else {
            if (!teamIdMap.containsKey(team.getName())) {
                teamIdMap.put(team.getName(), nextTeamId++);
            }
            datapackObj.getScore(playerName).setScore(teamIdMap.get(team.getName()));
        }
    }

    public void setTeamColor(CommandSender sender, Player target, String colorName) {
        Team team = sb.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "&cEse jugador no esta en un equipo.");
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
            ChatUtil.msg(sender, "&cColor invalido. Usa nombres estandar (RED, BLUE, GOLD, etc).");
        }
    }

    public void setTeamIcon(CommandSender sender, Player target, String iconText) {
        Team team = sb.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "&cEse jugador no esta en un equipo.");
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

    public void reloadConfigValues() {
        plugin.reloadConfig();

        this.teamSize = plugin.getConfig().getInt("max-team-size", 1);
        this.teamType = plugin.getConfig().getString("team-type", "Choosen");

        List<String> iconsConfig = plugin.getConfig().getStringList("team-icons");
        this.availableIcons.clear();

        if (iconsConfig.isEmpty()) {
            this.availableIcons.addAll(Arrays.asList("#1", "#2", "⚔", "★"));
        } else {
            this.availableIcons.addAll(iconsConfig);
        }
    }


    public void forceJoin(CommandSender sender, Player p1, Player p2) {
        Team t2 = sb.getEntryTeam(p2.getName());
        if (t2 == null) {
            t2 = sb.getTeam("team_" + p2.getName());
            if (t2 == null) {
                t2 = sb.registerNewTeam("team_" + p2.getName());
                applyRandomTheme(t2);
                //friendly fire esto coso
                t2.setAllowFriendlyFire(plugin.getConfig().getBoolean("friendly-fire", false));
            }
            t2.addEntry(p2.getName());
            updatePlayerDatapackID(p2.getName(), t2);
        }

        Team t1 = sb.getEntryTeam(p1.getName());
        if (t1 != null) {
            t1.removeEntry(p1.getName());
            updatePlayerDatapackID(p1.getName(), null);
            if (t1.getSize() == 0) t1.unregister();
        }

        t2.addEntry(p1.getName());
        updatePlayerDatapackID(p1.getName(), t2);

        ChatUtil.msg(p1, "&aUn admin te forzo al equipo de &b" + p2.getName());
        ChatUtil.msg(p2, "&b" + p1.getName() + " &aha sido forzado a tu equipo.");
        ChatUtil.msg(sender, "&aJugador movido con exito.");
    }
    public void removePlayer(CommandSender sender, Player target) {
        Team team = sb.getEntryTeam(target.getName());
        if (team != null) {
            team.removeEntry(target.getName());
            updatePlayerDatapackID(target.getName(), null);
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
                updatePlayerDatapackID(entry, null);
            }
            team.unregister();
            ChatUtil.msg(sender, "&aEquipo disuelto.");
        } else {
            ChatUtil.msg(sender, "&cEse jugador no esta en un equipo.");
        }
    }

    public void clearAllTeamsConsole() {
        for (Team t : sb.getTeams()) {
            if (t.getName().startsWith("team_")) {
                t.unregister();
            }
        }
        teamIdMap.clear();
        nextTeamId = 1;
    }

    public void clearAllTeams(CommandSender sender) {
        clearAllTeamsConsole();
        ChatUtil.broadcast("&aSe han eliminado todos los equipos.");
    }
}