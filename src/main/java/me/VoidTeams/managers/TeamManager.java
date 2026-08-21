package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class TeamManager {

    private static final String TEAM_PREFIX = "vt_";
    private static final LegacyComponentSerializer LEGACY_RGB = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private static final Map<String, String> RGB_ALIASES = new LinkedHashMap<>();

    static {
        RGB_ALIASES.put("red", "#FF5C5C");
        RGB_ALIASES.put("crimson", "#DC143C");
        RGB_ALIASES.put("coral", "#FF7F50");
        RGB_ALIASES.put("salmon", "#FA8072");
        RGB_ALIASES.put("orange", "#FF8C42");
        RGB_ALIASES.put("amber", "#FFBF00");
        RGB_ALIASES.put("gold", "#FFB347");
        RGB_ALIASES.put("yellow", "#FFD93D");
        RGB_ALIASES.put("lime", "#A7F432");
        RGB_ALIASES.put("green", "#6BCB77");
        RGB_ALIASES.put("emerald", "#2ECC71");
        RGB_ALIASES.put("mint", "#3EB489");
        RGB_ALIASES.put("teal", "#14B8A6");
        RGB_ALIASES.put("aqua", "#2DE2E6");
        RGB_ALIASES.put("cyan", "#22D3EE");
        RGB_ALIASES.put("sky", "#38BDF8");
        RGB_ALIASES.put("blue", "#4D96FF");
        RGB_ALIASES.put("royal", "#4169E1");
        RGB_ALIASES.put("navy", "#1E3A8A");
        RGB_ALIASES.put("indigo", "#6366F1");
        RGB_ALIASES.put("purple", "#9B5DE5");
        RGB_ALIASES.put("violet", "#8B5CF6");
        RGB_ALIASES.put("lavender", "#C4B5FD");
        RGB_ALIASES.put("magenta", "#F15BB5");
        RGB_ALIASES.put("pink", "#FF6EC7");
        RGB_ALIASES.put("rose", "#FB7185");
        RGB_ALIASES.put("brown", "#A16207");
        RGB_ALIASES.put("gray", "#AAB2BD");
        RGB_ALIASES.put("silver", "#CBD5E1");
        RGB_ALIASES.put("white", "#F5F7FA");
        RGB_ALIASES.put("black", "#303036");
    }

    private final VoidTeams plugin;
    private final Scoreboard scoreboard;
    private final Random random = new Random();

    private final Map<UUID, PendingInvite> pendingInvites = new HashMap<>();
    private final Map<String, TeamTheme> teamThemes = new HashMap<>();
    private final Map<String, Integer> teamIdMap = new HashMap<>();

    private final List<String> availableColors = new ArrayList<>();
    private final List<String> availableIcons = new ArrayList<>();

    private final Objective datapackObj;
    private int nextTeamId = 1;

    private String teamType;
    private int teamSize;
    private boolean teamsLocked;
    private boolean chatLocked;

    private record PendingInvite(UUID inviter, long expiresAtMillis) {
        boolean expired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }

    public TeamManager(VoidTeams plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Objective objective = scoreboard.getObjective("vt_team_id");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("vt_team_id", "dummy", "VoidTeams ID");
        }
        this.datapackObj = objective;

        loadConfigValues(false);
        recoverExistingTeams();
    }

    public int getTeamSize() {
        return teamSize;
    }

    public String getTeamType() {
        return teamType;
    }

    public String getTeamTypeDisplay() {
        return teamType.equalsIgnoreCase("Choosen") ? "Chosen" : teamType;
    }

    public boolean isTeamsLocked() {
        return teamsLocked;
    }

    public boolean isChatLocked() {
        return chatLocked;
    }

    public void setTeamsLocked(boolean locked) {
        this.teamsLocked = locked;
    }

    public void setChatLocked(boolean locked) {
        this.chatLocked = locked;
    }

    public List<String> getRgbSuggestions() {
        List<String> suggestions = new ArrayList<>(RGB_ALIASES.keySet());
        for (String color : availableColors) {
            if (!suggestions.contains(color)) {
                suggestions.add(color);
            }
        }
        suggestions.add("random");
        return suggestions;
    }

    public Map<String, String> getColorAliases() {
        return Map.copyOf(RGB_ALIASES);
    }

    public List<String> getAvailableColors() {
        return List.copyOf(availableColors);
    }

    public List<String> getAvailableIcons() {
        return List.copyOf(availableIcons);
    }

    public void setTeamType(CommandSender sender, String type) {
        String normalized = normalizeTeamType(type);
        if (normalized == null) {
            ChatUtil.msg(sender, "<#FF5C5C>El tipo debe ser <white>Choosen</white>, <white>Random</white> o <white>Vote</white>.");
            return;
        }

        if (teamType.equalsIgnoreCase(normalized)) {
            ChatUtil.msg(sender, "<gray>El modo de equipos ya es <#22D3EE>" + displayType(normalized) + "</#22D3EE>.");
            return;
        }

        String previous = teamType;
        teamType = normalized;
        plugin.getConfig().set("team-type", normalized);
        plugin.saveConfig();

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#8B5CF6><bold>CONFIGURACIÓN DE EQUIPOS</bold></#8B5CF6>\n" +
                "<gray>Modo:</gray> <#AAB2BD>" + displayType(previous) + "</#AAB2BD> <dark_gray>→</dark_gray> <#22D3EE><bold>" + displayType(normalized) + "</bold></#22D3EE>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#8B5CF6><bold>EQUIPOS</bold></#8B5CF6>",
                "<gray>Modo:</gray> <#22D3EE>" + displayType(normalized) + "</#22D3EE>", 5, 35, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void setTeamSize(CommandSender sender, int size) {
        int newSize = Math.max(1, size);
        int previous = teamSize;

        if (previous == newSize) {
            ChatUtil.msg(sender, "<gray>El TeamSize ya está en <#22D3EE>" + newSize + "</#22D3EE>.");
            return;
        }

        teamSize = newSize;
        plugin.getConfig().set("max-team-size", newSize);
        plugin.saveConfig();

        String display = newSize == 1 ? "FFA" : "To" + newSize;
        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#22D3EE><bold>TEAM SIZE ACTUALIZADO</bold></#22D3EE>\n" +
                "<gray>Tamaño máximo:</gray> <white>" + previous + "</white> <dark_gray>→</dark_gray> <#FFD93D><bold>" + newSize + "</bold></#FFD93D> <dark_gray>(" + display + ")</dark_gray>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.sendActionBarToAll("<#22D3EE><bold>TEAMS</bold></#22D3EE> <dark_gray>»</dark_gray> <white>TeamSize:</white> <#FFD93D>" + display + "</#FFD93D>");
        plugin.getSoundManager().broadcast(SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void setFriendlyFire(CommandSender sender, boolean enabled) {
        plugin.getConfig().set("friendly-fire", enabled);
        plugin.saveConfig();

        for (Team team : getManagedTeams()) {
            team.setAllowFriendlyFire(enabled);
        }

        ChatUtil.broadcast(
                enabled
                        ? "<#FFB347>Friendly Fire <white>activado</white> por el host.</#FFB347>"
                        : "<#6BCB77>Friendly Fire <white>desactivado</white> por el host.</#6BCB77>"
        );
        plugin.getSoundManager().broadcast(SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void invitePlayer(Player inviter, Player target) {
        if (teamSize <= 1) {
            error(inviter, "El TeamSize actual es FFA. No se pueden formar equipos.");
            return;
        }
        if (teamType.equalsIgnoreCase("Random")) {
            error(inviter, "Las invitaciones están deshabilitadas en modo Random.");
            return;
        }
        if (teamsLocked) {
            error(inviter, "La creación y modificación de equipos está bloqueada.");
            return;
        }
        if (inviter.equals(target)) {
            error(inviter, "No puedes invitarte a tu propio equipo.");
            return;
        }

        Team inviterTeam = scoreboard.getEntryTeam(inviter.getName());
        Team targetTeam = scoreboard.getEntryTeam(target.getName());

        if (inviterTeam != null && inviterTeam.getEntries().contains(target.getName())) {
            error(inviter, target.getName() + " ya está en tu equipo.");
            return;
        }
        if (targetTeam != null) {
            error(inviter, target.getName() + " ya pertenece a un equipo.");
            return;
        }
        if (inviterTeam != null && inviterTeam.getSize() >= teamSize) {
            error(inviter, "Tu equipo ya está lleno (máximo " + teamSize + ").");
            return;
        }

        int expireSeconds = Math.max(10, plugin.getConfig().getInt("invite-expire-seconds", 60));
        pendingInvites.put(target.getUniqueId(),
                new PendingInvite(inviter.getUniqueId(), System.currentTimeMillis() + expireSeconds * 1000L));

        ChatUtil.msg(inviter,
                "<#6BCB77>Invitación enviada a <white><bold>" + target.getName() + "</bold></white>.</#6BCB77>");
        ChatUtil.sendActionBar(inviter,
                "<#6BCB77>✓</#6BCB77> <white>Invitación enviada a</white> <#22D3EE>" + target.getName() + "</#22D3EE>");
        plugin.getSoundManager().play(inviter, SoundManager.SoundType.INVITE_SENT);

        Component invite = ChatUtil.component(
                        "<#8B5CF6><bold>INVITACIÓN DE EQUIPO</bold></#8B5CF6>\n" +
                        "<white>" + inviter.getName() + "</white> <gray>quiere formar equipo contigo.</gray>\n")
                .append(Component.text("[ACEPTAR INVITACIÓN]", TextColor.fromHexString("#6BCB77"))
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/team accept " + inviter.getName()))
                        .hoverEvent(HoverEvent.showText(ChatUtil.component(
                                "<#6BCB77>Click para unirte al equipo de <white>" + inviter.getName() + "</white>."))));

        target.sendMessage(invite);
        ChatUtil.title(target,
                "<#8B5CF6><bold>INVITACIÓN</bold></#8B5CF6>",
                "<white>" + inviter.getName() + "</white> <gray>te invitó a su equipo</gray>",
                5, 45, 10);
        plugin.getSoundManager().play(target, SoundManager.SoundType.INVITE_RECEIVED);
    }

    public void acceptInvite(Player player, Player leader) {
        if (teamSize <= 1) {
            error(player, "El TeamSize actual es FFA. No se pueden formar equipos.");
            return;
        }
        if (teamsLocked) {
            error(player, "No puedes unirte a equipos en este momento.");
            return;
        }
        if (teamType.equalsIgnoreCase("Random")) {
            error(player, "Las invitaciones están deshabilitadas en modo Random.");
            return;
        }
        if (scoreboard.getEntryTeam(player.getName()) != null) {
            error(player, "Ya perteneces a un equipo. Sal primero con /team leave.");
            return;
        }

        PendingInvite invite = pendingInvites.get(player.getUniqueId());
        if (invite == null || !invite.inviter().equals(leader.getUniqueId())) {
            error(player, "No tienes una invitación activa de " + leader.getName() + ".");
            return;
        }
        if (invite.expired()) {
            pendingInvites.remove(player.getUniqueId());
            error(player, "La invitación de " + leader.getName() + " ya expiró.");
            return;
        }

        Team leaderTeam = scoreboard.getEntryTeam(leader.getName());
        boolean created = false;
        if (leaderTeam == null) {
            leaderTeam = createTeam();
            leaderTeam.addEntry(leader.getName());
            updatePlayerDatapackID(leader.getName(), leaderTeam);
            created = true;
        }

        if (leaderTeam.getSize() >= teamSize) {
            pendingInvites.remove(player.getUniqueId());
            error(player, "Ese equipo ya alcanzó el límite de " + teamSize + " jugadores.");
            return;
        }

        leaderTeam.addEntry(player.getName());
        updatePlayerDatapackID(player.getName(), leaderTeam);
        pendingInvites.remove(player.getUniqueId());

        TeamTheme theme = getTheme(leaderTeam);
        String hex = theme.hexColor();

        ChatUtil.title(player,
                "<" + hex + "><bold>EQUIPO CONFIRMADO</bold></" + hex + ">",
                "<gray>Ahora juegas junto a</gray> <white>" + leader.getName() + "</white>",
                5, 45, 10);
        ChatUtil.sendActionBar(player,
                "<" + hex + ">" + theme.icon() + "</" + hex + "> <white>Te uniste al equipo de</white> <#22D3EE>" + leader.getName() + "</#22D3EE>");

        plugin.getSoundManager().play(player, created ? SoundManager.SoundType.TEAM_CREATED : SoundManager.SoundType.TEAM_JOIN);
        plugin.getSoundManager().play(leader, SoundManager.SoundType.TEAM_JOIN);

        if (plugin.getConfig().getBoolean("broadcast-team-joins", true)) {
            ChatUtil.broadcastNoPrefix(
                    "<" + hex + ">" + theme.icon() + "</" + hex + "> " +
                    "<white>" + player.getName() + "</white> <gray>se unió al equipo de</gray> <white>" + leader.getName() + "</white><dark_gray>.</dark_gray>"
            );
        } else {
            notifyTeam(leaderTeam,
                    "<" + hex + ">" + theme.icon() + "</" + hex + "> <white>" + player.getName() + "</white> <gray>se unió al equipo.</gray>");
        }
    }

    public void leaveTeam(Player player) {
        if (teamsLocked) {
            error(player, "La creación y modificación de equipos está bloqueada.");
            return;
        }

        Team team = scoreboard.getEntryTeam(player.getName());
        if (team == null) {
            error(player, "No estás en ningún equipo.");
            return;
        }

        TeamTheme theme = getTheme(team);
        team.removeEntry(player.getName());
        updatePlayerDatapackID(player.getName(), null);

        if (plugin.getTeamsData().isChatToggled(player.getUniqueId())) {
            plugin.getTeamsData().setChatToggled(player.getUniqueId(), false);
        }

        ChatUtil.title(player,
                "<#FFB347><bold>SALISTE DEL EQUIPO</bold></#FFB347>",
                "<gray>Ya no compartes chat ni marcador con ese equipo.</gray>", 5, 35, 10);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_LEAVE);

        if (team.getSize() == 0) {
            if (plugin.getTeamInventoryManager() != null) {
                plugin.getTeamInventoryManager().deleteTeamInventory(team.getName());
            }
            removeTheme(team);
            team.unregister();
        } else {
            notifyTeam(team,
                    "<" + theme.hexColor() + ">" + theme.icon() + "</" + theme.hexColor() + "> <white>" + player.getName() + "</white> <gray>abandonó el equipo.</gray>");
        }
    }

    public void setRandomColor(CommandSender sender, Player target) {
        Team team = requireTeam(sender, target);
        if (team == null) return;

        if (sender instanceof Player && !sender.hasPermission("voidteams.admin")
                && !plugin.getConfig().getBoolean("players-can-randomize-color", true)) {
            error((Player) sender, "El cambio de color está deshabilitado.");
            return;
        }

        TeamTheme current = getTheme(team);
        String hex = chooseRandomColor(current.hexColor());
        applyTheme(team, new TeamTheme(hex, current.icon()));

        ChatUtil.msg(sender,
                "<#6BCB77>Color del equipo actualizado:</#6BCB77> <" + hex + "><bold>" + hex.toUpperCase(Locale.ROOT) + "</bold></" + hex + ">");
        notifyTeamActionBar(team, "<" + hex + ">●</" + hex + "> <white>Nuevo color del equipo:</white> <" + hex + ">" + hex.toUpperCase(Locale.ROOT) + "</" + hex + ">");
        plugin.getSoundManager().broadcastToTeam(team, SoundManager.SoundType.SUCCESS);
    }

    public void setRandomIcon(CommandSender sender, Player target) {
        Team team = requireTeam(sender, target);
        if (team == null) return;

        if (sender instanceof Player && !sender.hasPermission("voidteams.admin")
                && !plugin.getConfig().getBoolean("players-can-randomize-icon", true)) {
            error((Player) sender, "El cambio de icono está deshabilitado.");
            return;
        }

        TeamTheme current = getTheme(team);
        String icon = chooseRandomIcon(current.icon());
        applyTheme(team, new TeamTheme(current.hexColor(), icon));

        ChatUtil.msg(sender,
                "<#6BCB77>Icono del equipo actualizado:</#6BCB77> <" + current.hexColor() + "><bold>[" + icon + "]</bold></" + current.hexColor() + ">");
        plugin.getSoundManager().broadcastToTeam(team, SoundManager.SoundType.SUCCESS);
    }

    public void setTeamColor(CommandSender sender, Player target, String input) {
        Team team = requireTeam(sender, target);
        if (team == null) return;

        String hex;
        if (input.equalsIgnoreCase("random")) {
            hex = chooseRandomColor(getTheme(team).hexColor());
        } else {
            hex = resolveRgb(input);
        }

        if (hex == null) {
            ChatUtil.msg(sender,
                    "<#FF5C5C>Color inválido.</#FF5C5C> <gray>Usa un RGB como</gray> <#22D3EE>#22D3EE</#22D3EE> <gray>o</gray> <white>random</white><gray>.</gray>");
            return;
        }

        TeamTheme current = getTheme(team);
        applyTheme(team, new TeamTheme(hex, current.icon()));

        ChatUtil.broadcastNoPrefix(
                "<#8B5CF6><bold>TEAM STYLE</bold></#8B5CF6> <dark_gray>»</dark_gray> " +
                "<gray>El equipo de</gray> <white>" + target.getName() + "</white> <gray>ahora usa</gray> " +
                "<" + hex + "><bold>" + hex.toUpperCase(Locale.ROOT) + "</bold></" + hex + ">"
        );
        plugin.getSoundManager().broadcastToTeam(team, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void setTeamIcon(CommandSender sender, Player target, String iconText) {
        Team team = requireTeam(sender, target);
        if (team == null) return;

        String icon = sanitizeIcon(iconText);
        if (icon == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Icono inválido.</#FF5C5C> <gray>Usa entre 1 y 6 caracteres y evita < y >.</gray>");
            return;
        }

        TeamTheme current = getTheme(team);
        applyTheme(team, new TeamTheme(current.hexColor(), icon));

        ChatUtil.broadcastNoPrefix(
                "<#8B5CF6><bold>TEAM STYLE</bold></#8B5CF6> <dark_gray>»</dark_gray> " +
                "<gray>Nuevo icono para el equipo de</gray> <white>" + target.getName() + "</white><gray>:</gray> " +
                "<" + current.hexColor() + "><bold>[" + icon + "]</bold></" + current.hexColor() + ">"
        );
        plugin.getSoundManager().broadcastToTeam(team, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void applyRandomTheme(Team team) {
        String hex = chooseRandomColor(null);
        String icon = chooseRandomIcon(null);
        applyTheme(team, new TeamTheme(hex, icon));
    }

    public void applyTheme(Team team, TeamTheme theme) {
        TextColor color = TextColor.fromHexString(theme.hexColor());
        if (color == null) color = TextColor.color(0x8B5CF6);

        Component prefix = Component.text("[" + theme.icon() + "] ", color);
        team.prefix(prefix);
        team.displayName(Component.text("VoidTeam [" + theme.icon() + "]", color));
        teamThemes.put(team.getName(), new TeamTheme(color.asHexString().toUpperCase(Locale.ROOT), theme.icon()));
    }

    public TeamTheme getTheme(Team team) {
        TeamTheme theme = teamThemes.get(team.getName());
        if (theme != null) return theme;

        theme = recoverTheme(team);
        teamThemes.put(team.getName(), theme);
        return theme;
    }

    public String getTeamPrefixLegacy(Team team) {
        if (team == null) return "";
        return LEGACY_RGB.serialize(team.prefix());
    }

    public String getTeamHex(Team team) {
        return team == null ? "" : getTheme(team).hexColor();
    }

    public String getTeamIcon(Team team) {
        return team == null ? "" : getTheme(team).icon();
    }

    public Team getTeam(Player player) {
        return player == null ? null : scoreboard.getEntryTeam(player.getName());
    }

    public Team getTeam(String playerName) {
        return playerName == null ? null : scoreboard.getEntryTeam(playerName);
    }

    public void sendToTeam(Team team, String miniMessage) {
        if (team == null || miniMessage == null) return;
        notifyTeam(team, miniMessage);
    }

    public void showTeamInfo(Player viewer, Player target) {
        Team team = scoreboard.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(viewer, "<gray>" + target.getName() + "</gray> <#FF5C5C>no pertenece a ningún equipo.</#FF5C5C>");
            return;
        }

        TeamTheme theme = getTheme(team);
        ChatUtil.msgNoPrefix(viewer, "<dark_gray>━━━━━━━━━━━━━━</dark_gray> <#8B5CF6><bold>EQUIPO</bold></#8B5CF6> <dark_gray>━━━━━━━━━━━━━━</dark_gray>");
        ChatUtil.msgNoPrefix(viewer,
                "<gray>Identidad:</gray> <" + theme.hexColor() + "><bold>[" + theme.icon() + "]</bold> " + theme.hexColor() + "</" + theme.hexColor() + ">");
        ChatUtil.msgNoPrefix(viewer,
                "<gray>Jugadores:</gray> <white>" + team.getSize() + "</white><dark_gray>/</dark_gray><#22D3EE>" + teamSize + "</#22D3EE>");
        boolean teamInventory = plugin.getTeamInventoryManager() != null && plugin.getTeamInventoryManager().isEnabled();
        ChatUtil.msgNoPrefix(viewer,
                "<gray>Team Inventory:</gray> " + (teamInventory
                        ? "<#6BCB77>Activado</#6BCB77> <dark_gray>(/ti)</dark_gray>"
                        : "<#FF5C5C>Desactivado</#FF5C5C>"));
        if (plugin.getTeamScenarioManager() != null) {
            ChatUtil.msgNoPrefix(viewer,
                    "<gray>Team Scenarios:</gray> <white>" + plugin.getTeamScenarioManager().getActiveDisplay() + "</white>");
        }

        for (String entry : team.getEntries()) {
            Player member = Bukkit.getPlayerExact(entry);
            String state = member != null && member.isOnline() ? "<#6BCB77>●</#6BCB77>" : "<#AAB2BD>●</#AAB2BD>";
            ChatUtil.msgNoPrefix(viewer, "  " + state + " <white>" + entry + "</white>");
        }
        ChatUtil.msgNoPrefix(viewer, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>");
    }

    public Team createTeam() {
        String name;
        do {
            name = TEAM_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (scoreboard.getTeam(name) != null);

        Team team = scoreboard.registerNewTeam(name);
        team.setAllowFriendlyFire(plugin.getConfig().getBoolean("friendly-fire", false));
        applyRandomTheme(team);
        return team;
    }

    public void notifyTeamAssignment(Player player, Team team, boolean randomized) {
        TeamTheme theme = getTheme(team);
        List<String> teammates = new ArrayList<>(team.getEntries());
        teammates.remove(player.getName());

        String subtitle = teammates.isEmpty()
                ? "<gray>Equipo individual</gray>"
                : "<gray>Con:</gray> <white>" + String.join(", ", teammates) + "</white>";

        ChatUtil.title(player,
                "<" + theme.hexColor() + "><bold>[" + theme.icon() + "] TU EQUIPO</bold></" + theme.hexColor() + ">",
                subtitle,
                5, 55, 12);
        ChatUtil.sendActionBar(player,
                "<" + theme.hexColor() + ">●</" + theme.hexColor() + "> <white>" +
                        (randomized ? "Equipo aleatorio asignado" : "Equipo asignado") + "</white>");
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_RANDOMIZED);
    }

    public void updatePlayerDatapackID(String playerName, Team team) {
        if (team == null) {
            datapackObj.getScore(playerName).setScore(0);
            return;
        }

        int id = teamIdMap.computeIfAbsent(team.getName(), ignored -> nextTeamId++);
        datapackObj.getScore(playerName).setScore(id);
    }

    public void reloadConfigValues() {
        plugin.reloadConfig();
        loadConfigValues(true);
        for (Team team : getManagedTeams()) {
            team.setAllowFriendlyFire(plugin.getConfig().getBoolean("friendly-fire", false));
        }
    }

    public void forceJoin(CommandSender sender, Player player, Player targetTeamPlayer) {
        if (player.equals(targetTeamPlayer)) {
            ChatUtil.msg(sender, "<#FF5C5C>No puedes forzar un jugador a sí mismo.</#FF5C5C>");
            return;
        }

        Team destination = scoreboard.getEntryTeam(targetTeamPlayer.getName());
        if (destination == null) {
            destination = createTeam();
            destination.addEntry(targetTeamPlayer.getName());
            updatePlayerDatapackID(targetTeamPlayer.getName(), destination);
        }

        if (destination.getSize() >= teamSize && !destination.getEntries().contains(player.getName())) {
            ChatUtil.msg(sender, "<#FF5C5C>El equipo destino ya está lleno.</#FF5C5C>");
            return;
        }

        Team current = scoreboard.getEntryTeam(player.getName());
        if (current != null && current != destination) {
            current.removeEntry(player.getName());
            if (current.getSize() == 0) {
                if (plugin.getTeamInventoryManager() != null) {
                    plugin.getTeamInventoryManager().deleteTeamInventory(current.getName());
                }
                removeTheme(current);
                current.unregister();
            }
        }

        destination.addEntry(player.getName());
        updatePlayerDatapackID(player.getName(), destination);
        pendingInvites.remove(player.getUniqueId());

        TeamTheme theme = getTheme(destination);
        ChatUtil.title(player,
                "<" + theme.hexColor() + "><bold>EQUIPO ASIGNADO</bold></" + theme.hexColor() + ">",
                "<gray>El host te movió al equipo de</gray> <white>" + targetTeamPlayer.getName() + "</white>",
                5, 45, 10);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
        ChatUtil.msg(sender, "<#6BCB77>" + player.getName() + " fue movido correctamente.</#6BCB77>");
    }

    public void removePlayer(CommandSender sender, Player target) {
        Team team = scoreboard.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Ese jugador no está en ningún equipo.</#FF5C5C>");
            return;
        }

        team.removeEntry(target.getName());
        updatePlayerDatapackID(target.getName(), null);
        pendingInvites.remove(target.getUniqueId());
        plugin.getTeamsData().setChatToggled(target.getUniqueId(), false);

        ChatUtil.title(target,
                "<#FF5C5C><bold>REMOVIDO DEL EQUIPO</bold></#FF5C5C>",
                "<gray>Un administrador te retiró de tu equipo.</gray>", 5, 45, 10);
        plugin.getSoundManager().play(target, SoundManager.SoundType.TEAM_LEAVE);
        ChatUtil.msg(sender, "<#6BCB77>" + target.getName() + " fue removido de su equipo.</#6BCB77>");

        if (team.getSize() == 0) {
            if (plugin.getTeamInventoryManager() != null) {
                plugin.getTeamInventoryManager().deleteTeamInventory(team.getName());
            }
            removeTheme(team);
            team.unregister();
        }
    }

    public void disbandTeam(CommandSender sender, Player target) {
        Team team = scoreboard.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Ese jugador no está en un equipo.</#FF5C5C>");
            return;
        }

        List<String> members = new ArrayList<>(team.getEntries());
        for (String entry : members) {
            Player member = Bukkit.getPlayerExact(entry);
            updatePlayerDatapackID(entry, null);
            if (member != null) {
                plugin.getTeamsData().setChatToggled(member.getUniqueId(), false);
                ChatUtil.title(member,
                        "<#FF5C5C><bold>EQUIPO DISUELTO</bold></#FF5C5C>",
                        "<gray>El host disolvió tu equipo.</gray>", 5, 40, 10);
                plugin.getSoundManager().play(member, SoundManager.SoundType.TEAM_LEAVE);
            }
        }

        if (plugin.getTeamInventoryManager() != null) {
            plugin.getTeamInventoryManager().deleteTeamInventory(team.getName());
        }
        removeTheme(team);
        team.unregister();
        ChatUtil.broadcast("<#FFB347>El host disolvió el equipo de <white>" + target.getName() + "</white>.</#FFB347>");
    }

    public void clearAllTeamsConsole() {
        for (Team team : new ArrayList<>(scoreboard.getTeams())) {
            if (isManagedTeam(team)) {
                for (String entry : new ArrayList<>(team.getEntries())) {
                    updatePlayerDatapackID(entry, null);
                }
                removeTheme(team);
                team.unregister();
            }
        }
        teamIdMap.clear();
        nextTeamId = 1;
        pendingInvites.clear();
        plugin.getTeamsData().clear();
        if (plugin.getTeamInventoryManager() != null) plugin.getTeamInventoryManager().clearAll();
    }

    public void clearAllTeams(CommandSender sender) {
        int amount = getManagedTeams().size();
        clearAllTeamsConsole();

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#FF5C5C><bold>EQUIPOS REINICIADOS</bold></#FF5C5C>\n" +
                "<gray>El host eliminó</gray> <white>" + amount + "</white> <gray>equipos activos.</gray>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#FF5C5C><bold>EQUIPOS LIMPIADOS</bold></#FF5C5C>",
                "<gray>La configuración de equipos fue reiniciada.</gray>", 5, 35, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.TEAM_CLEAR);
    }

    private void loadConfigValues(boolean reload) {
        teamSize = Math.max(1, plugin.getConfig().getInt("max-team-size", 2));
        String configuredType = plugin.getConfig().getString("team-type", "Choosen");
        String normalized = normalizeTeamType(configuredType);
        teamType = normalized == null ? "Choosen" : normalized;
        teamsLocked = plugin.getConfig().getBoolean("teams-locked", false);
        chatLocked = plugin.getConfig().getBoolean("team-chat-locked", false);

        availableColors.clear();
        for (String configured : plugin.getConfig().getStringList("team-colors")) {
            String resolved = resolveRgb(configured);
            if (resolved != null && !availableColors.contains(resolved)) {
                availableColors.add(resolved);
            }
        }
        if (availableColors.isEmpty()) {
            availableColors.addAll(RGB_ALIASES.values().stream().distinct().toList());
        }

        availableIcons.clear();
        availableIcons.addAll(plugin.getConfig().getStringList("team-icons"));
        if (availableIcons.isEmpty()) {
            availableIcons.addAll(List.of("⚔", "✦", "◆", "☄", "♛", "❖", "☯", "★"));
        }

        if (reload) {
            pendingInvites.clear();
        }
    }

    private void recoverExistingTeams() {
        for (Team team : scoreboard.getTeams()) {
            if (!isManagedTeam(team)) continue;

            TeamTheme theme = recoverTheme(team);
            teamThemes.put(team.getName(), theme);
            team.setAllowFriendlyFire(plugin.getConfig().getBoolean("friendly-fire", false));

            teamIdMap.put(team.getName(), nextTeamId++);
            for (String entry : team.getEntries()) {
                datapackObj.getScore(entry).setScore(teamIdMap.get(team.getName()));
            }
        }
    }

    private TeamTheme recoverTheme(Team team) {
        Component prefix = team.prefix();
        String plain = PlainTextComponentSerializer.plainText().serialize(prefix).trim();
        String icon = "★";
        if (plain.startsWith("[") && plain.contains("]")) {
            icon = plain.substring(1, plain.indexOf(']'));
        }

        TextColor color = prefix.color();
        String hex = color == null ? availableColors.get(0) : color.asHexString().toUpperCase(Locale.ROOT);
        return new TeamTheme(hex, icon);
    }

    private String normalizeTeamType(String input) {
        if (input == null) return null;
        if (input.equalsIgnoreCase("Choosen") || input.equalsIgnoreCase("Chosen")) return "Choosen";
        if (input.equalsIgnoreCase("Random")) return "Random";
        if (input.equalsIgnoreCase("Vote")) return "Vote";
        return null;
    }

    private String displayType(String type) {
        return type != null && type.equalsIgnoreCase("Choosen") ? "Chosen" : String.valueOf(type);
    }

    private String resolveRgb(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        String alias = RGB_ALIASES.get(trimmed.toLowerCase(Locale.ROOT));
        if (alias != null) return alias;
        if (!trimmed.startsWith("#") && trimmed.matches("[0-9A-Fa-f]{6}")) {
            trimmed = "#" + trimmed;
        }
        if (!HEX_PATTERN.matcher(trimmed).matches()) return null;
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String chooseRandomColor(String excluded) {
        if (availableColors.size() == 1) return availableColors.get(0);
        String choice;
        do {
            choice = availableColors.get(random.nextInt(availableColors.size()));
        } while (excluded != null && choice.equalsIgnoreCase(excluded));
        return choice;
    }

    private String chooseRandomIcon(String excluded) {
        if (!plugin.getConfig().getBoolean("use-custom-icons", true)) {
            return "#" + (random.nextInt(99) + 1);
        }
        if (availableIcons.size() == 1) return availableIcons.get(0);
        String choice;
        do {
            choice = availableIcons.get(random.nextInt(availableIcons.size()));
        } while (excluded != null && choice.equals(excluded));
        return choice;
    }

    private String sanitizeIcon(String input) {
        if (input == null) return null;
        String cleaned = input.trim();
        int length = cleaned.codePointCount(0, cleaned.length());
        if (length < 1 || length > 6 || cleaned.contains("<") || cleaned.contains(">")) return null;
        return cleaned;
    }

    private Team requireTeam(CommandSender sender, Player target) {
        Team team = scoreboard.getEntryTeam(target.getName());
        if (team == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Ese jugador no está en ningún equipo.</#FF5C5C>");
        }
        return team;
    }

    private boolean isManagedTeam(Team team) {
        String name = team.getName();
        return name.startsWith(TEAM_PREFIX) || name.startsWith("team_");
    }

    private List<Team> getManagedTeams() {
        List<Team> teams = new ArrayList<>();
        for (Team team : scoreboard.getTeams()) {
            if (isManagedTeam(team)) teams.add(team);
        }
        return teams;
    }

    private void removeTheme(Team team) {
        if (team == null) return;
        teamThemes.remove(team.getName());
        teamIdMap.remove(team.getName());
    }

    private void error(Player player, String message) {
        ChatUtil.msg(player, "<#FF5C5C>" + message + "</#FF5C5C>");
        ChatUtil.sendActionBar(player, "<#FF5C5C>✕</#FF5C5C> <white>" + message + "</white>");
        plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
    }

    private void notifyTeam(Team team, String miniMessage) {
        for (String entry : team.getEntries()) {
            Player member = Bukkit.getPlayerExact(entry);
            if (member != null) ChatUtil.msgNoPrefix(member, miniMessage);
        }
    }

    private void notifyTeamActionBar(Team team, String miniMessage) {
        for (String entry : team.getEntries()) {
            Player member = Bukkit.getPlayerExact(entry);
            if (member != null) ChatUtil.sendActionBar(member, miniMessage);
        }
    }
}
