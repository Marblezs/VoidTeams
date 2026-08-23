package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.scenarios.TeamScenario;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamAdminCommands implements CommandExecutor {

    private final VoidTeams plugin;

    public TeamAdminCommands(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("voidteams.admin")) {
            ChatUtil.msg(sender, "<#FF5C5C>No tienes permisos de host para administrar equipos.</#FF5C5C>");
            if (sender instanceof Player player) {
                plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            }
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                plugin.getTeamAdminGui().open(player);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "gui", "menu" -> {
                if (sender instanceof Player player) plugin.getTeamAdminGui().open(player);
                else ChatUtil.msg(sender, "<#FF5C5C>Este menú solo puede abrirse dentro del juego.</#FF5C5C>");
            }
            case "force" -> force(sender, args);
            case "remove" -> remove(sender, args);
            case "clear" -> plugin.getTeamManager().clearAllTeams(sender);
            case "disband" -> disband(sender, args);
            case "color" -> color(sender, args);
            case "icon", "icono" -> icon(sender, args);
            case "shuffle" -> {
                if (!plugin.getTeamManager().getTeamType().equalsIgnoreCase("Random")) {
                    ChatUtil.msg(sender,
                            "<#FFB347>El modo actual es <white>" + plugin.getTeamManager().getTeamTypeDisplay() + "</white>.</#FFB347> " +
                            "<gray>Usa</gray> <#22D3EE>/teamadm shuffleforce</#22D3EE> <gray>si quieres forzarlo.</gray>");
                    return true;
                }
                plugin.getRandomTeamManager().shuffleForcingTeams(sender);
            }
            case "shuffleforce" -> plugin.getRandomTeamManager().shuffleTeams(sender);
            case "type" -> type(sender, args);
            case "size" -> size(sender, args);
            case "vote" -> vote(sender, args);
            case "block" -> block(sender, args);
            case "friendlyfire", "ff" -> friendlyFire(sender, args);
            case "scen", "scenario", "scenarios" -> scenarios(sender, args);
            case "config", "configs" -> teamConfig(sender, args);
            case "teaminventory", "team-inventory", "ti" -> teamInventory(sender, args);
            case "info" -> info(sender, args);
            case "reload" -> {
                plugin.getTeamManager().reloadConfigValues();
                plugin.getTeamScenarioManager().reload(sender);
            }
            default -> ChatUtil.msg(sender,
                    "<#FF5C5C>Subcomando desconocido.</#FF5C5C> <gray>Usa</gray> <#22D3EE>/teamadm</#22D3EE><gray>.</gray>");
        }
        return true;
    }

    private void force(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm force [jugador] [compañero]</#22D3EE>");
            return;
        }
        Player player = Bukkit.getPlayerExact(args[1]);
        Player teammate = Bukkit.getPlayerExact(args[2]);
        if (player == null || teammate == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Uno de los jugadores no está conectado.</#FF5C5C>");
            return;
        }
        plugin.getTeamManager().forceJoin(sender, player, teammate);
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm remove [jugador]</#22D3EE>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Jugador no encontrado.</#FF5C5C>");
            return;
        }
        plugin.getTeamManager().removePlayer(sender, target);
    }

    private void disband(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm disband [jugador]</#22D3EE>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Jugador no encontrado.</#FF5C5C>");
            return;
        }
        plugin.getTeamManager().disbandTeam(sender, target);
    }

    private void color(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ChatUtil.msg(sender,
                    "<gray>Uso:</gray> <#22D3EE>/teamadm color [jugador] [#RRGGBB|random]</#22D3EE>\n" +
                    "<gray>Ejemplo:</gray> <#FF6EC7>/teamadm color Marblezs #FF6EC7</#FF6EC7>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Jugador no encontrado.</#FF5C5C>");
            return;
        }
        plugin.getTeamManager().setTeamColor(sender, target, args[2]);
    }

    private void icon(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm icon [jugador] [icono]</#22D3EE>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Jugador no encontrado.</#FF5C5C>");
            return;
        }
        String icon = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.getTeamManager().setTeamIcon(sender, target, icon);
    }

    private void type(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm type [Choosen|Random|Vote]</#22D3EE>");
            return;
        }
        plugin.getTeamManager().setTeamType(sender, args[1]);
    }

    private void size(CommandSender sender, String[] args) {
        if (args.length < 3) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm size [set|add|remove] [valor]</#22D3EE>");
            return;
        }

        int value;
        try {
            value = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            ChatUtil.msg(sender, "<#FF5C5C>El valor debe ser un número entero.</#FF5C5C>");
            return;
        }

        if (value < 0) {
            ChatUtil.msg(sender, "<#FF5C5C>El valor no puede ser negativo.</#FF5C5C>");
            return;
        }

        int current = plugin.getTeamManager().getTeamSize();
        switch (args[1].toLowerCase()) {
            case "set" -> plugin.getTeamManager().setTeamSize(sender, value);
            case "add" -> plugin.getTeamManager().setTeamSize(sender, current + value);
            case "remove" -> plugin.getTeamManager().setTeamSize(sender, current - value);
            default -> ChatUtil.msg(sender, "<#FF5C5C>Acción inválida.</#FF5C5C> <gray>Usa set, add o remove.</gray>");
        }
    }

    private void vote(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm vote [type|size|stop] [opciones...]</#22D3EE>");
            return;
        }

        String category = args[1].toLowerCase();
        if (category.equals("stop")) {
            plugin.getVoteTeamManager().stopVote(sender);
            return;
        }
        if (!category.equals("type") && !category.equals("size")) {
            ChatUtil.msg(sender, "<#FF5C5C>Solo puedes votar type o size.</#FF5C5C>");
            return;
        }
        if (args.length < 4) {
            ChatUtil.msg(sender, "<#FF5C5C>Debes colocar al menos 2 opciones.</#FF5C5C>");
            return;
        }

        List<String> options = new ArrayList<>();
        for (int i = 2; i < args.length && options.size() < 5; i++) {
            String option = args[i];

            if (category.equals("type")) {
                if (!option.equalsIgnoreCase("Choosen")
                        && !option.equalsIgnoreCase("Chosen")
                        && !option.equalsIgnoreCase("Random")
                        && !option.equalsIgnoreCase("Vote")) {
                    ChatUtil.msg(sender, "<#FF5C5C>Opción de tipo inválida:</#FF5C5C> <white>" + option + "</white>");
                    return;
                }
                if (option.equalsIgnoreCase("Chosen")) option = "Choosen";
            } else {
                try {
                    int parsed = Integer.parseInt(option);
                    if (parsed < 1) throw new NumberFormatException();
                    option = String.valueOf(parsed);
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>TeamSize inválido:</#FF5C5C> <white>" + option + "</white>");
                    return;
                }
            }

            boolean duplicate = false;
            for (String existing : options) {
                if (existing.equalsIgnoreCase(option)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                options.add(option);
            }
        }

        if (options.size() < 2) {
            ChatUtil.msg(sender, "<#FF5C5C>La votación necesita al menos dos opciones diferentes.</#FF5C5C>");
            return;
        }

        int duration = Math.max(10, plugin.getConfig().getInt("vote-duration-seconds", 30));
        plugin.getVoteTeamManager().startVote(sender, category, options, duration);
    }

    private void block(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm block [all|chat|teams|none]</#22D3EE>");
            return;
        }

        String target = args[1].toLowerCase();
        switch (target) {
            case "all" -> {
                plugin.getTeamManager().setTeamsLocked(true);
                plugin.getTeamManager().setChatLocked(true);
                plugin.getConfig().set("teams-locked", true);
                plugin.getConfig().set("team-chat-locked", true);
                plugin.saveConfig();
                broadcastLock("<#FF5C5C><bold>SISTEMA DE EQUIPOS BLOQUEADO</bold></#FF5C5C>",
                        "<gray>No se pueden modificar equipos ni usar Team Chat.</gray>", false);
            }
            case "chat" -> {
                plugin.getTeamManager().setChatLocked(true);
                plugin.getConfig().set("team-chat-locked", true);
                plugin.saveConfig();
                broadcastLock("<#FFB347><bold>TEAM CHAT BLOQUEADO</bold></#FFB347>",
                        "<gray>Los equipos siguen editables, pero su chat está cerrado.</gray>", false);
            }
            case "teams" -> {
                plugin.getTeamManager().setTeamsLocked(true);
                plugin.getConfig().set("teams-locked", true);
                plugin.saveConfig();
                broadcastLock("<#FFB347><bold>EQUIPOS BLOQUEADOS</bold></#FFB347>",
                        "<gray>No se pueden crear, aceptar o abandonar equipos.</gray>", false);
            }
            case "none" -> {
                if (plugin.getTeamScenarioManager().isFormationRunning()) {
                    ChatUtil.msg(sender, "<#FFB347>No puedes desbloquear los equipos mientras Captains/Auction está formando teams.</#FFB347> <gray>Detén primero el scenario.</gray>");
                    return;
                }
                plugin.getTeamManager().setTeamsLocked(false);
                plugin.getTeamManager().setChatLocked(false);
                plugin.getConfig().set("teams-locked", false);
                plugin.getConfig().set("team-chat-locked", false);
                plugin.saveConfig();
                broadcastLock("<#6BCB77><bold>EQUIPOS DESBLOQUEADOS</bold></#6BCB77>",
                        "<gray>El sistema vuelve a estar disponible.</gray>", true);
            }
            default -> ChatUtil.msg(sender, "<#FF5C5C>Opción inválida.</#FF5C5C> <gray>Usa all, chat, teams o none.</gray>");
        }
    }

    private void friendlyFire(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm friendlyfire [on|off]</#22D3EE>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "on", "true" -> plugin.getTeamManager().setFriendlyFire(sender, true);
            case "off", "false" -> plugin.getTeamManager().setFriendlyFire(sender, false);
            default -> ChatUtil.msg(sender, "<#FF5C5C>Usa on u off.</#FF5C5C>");
        }
    }

    private void scenarios(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
            ChatUtil.msgNoPrefix(sender,
                    "<dark_gray>━━━━━━━━━━</dark_gray> <#8B5CF6><bold>TEAM SCENARIOS</bold></#8B5CF6> <dark_gray>━━━━━━━━━━</dark_gray>");
            for (TeamScenario scenario : plugin.getTeamScenarioManager().getScenarios()) {
                String state = scenario.isActive()
                        ? "<#6BCB77><bold>ON</bold></#6BCB77>"
                        : "<#FF5C5C><bold>OFF</bold></#FF5C5C>";
                ChatUtil.msgNoPrefix(sender,
                        state + " <white>" + scenario.displayName() + "</white> <dark_gray>(" + scenario.id() + ")</dark_gray> " +
                        "<gray>• " + scenario.description() + "</gray>");
            }
            ChatUtil.msgNoPrefix(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>");
            return;
        }

        String first = args[1].toLowerCase();

        if (first.equals("reload")) {
            plugin.getTeamScenarioManager().reload(sender);
            return;
        }

        if (first.equals("toggle") || first.equals("on") || first.equals("off") || first.equals("info")) {
            if (args.length < 3) {
                ChatUtil.msg(sender,
                        "<gray>Uso:</gray> <#22D3EE>/teamadm scen " + first + " [scenario]</#22D3EE>");
                return;
            }

            String scenarioId = args[2];
            switch (first) {
                case "toggle" -> plugin.getTeamScenarioManager().toggle(scenarioId, sender);
                case "on" -> plugin.getTeamScenarioManager().setEnabled(scenarioId, true, sender);
                case "off" -> plugin.getTeamScenarioManager().setEnabled(scenarioId, false, sender);
                case "info" -> sendScenarioInfo(sender, scenarioId);
            }
            return;
        }

        TeamScenario scenario = plugin.getTeamScenarioManager().get(first);
        if (scenario == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Scenario desconocido.</#FF5C5C> <gray>Usa /teamadm scen list.</gray>");
            return;
        }

        if (args.length == 2) {
            sendScenarioInfo(sender, scenario.id());
            return;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "toggle" -> {
                plugin.getTeamScenarioManager().toggle(scenario.id(), sender);
                return;
            }
            case "on" -> {
                plugin.getTeamScenarioManager().setEnabled(scenario.id(), true, sender);
                return;
            }
            case "off" -> {
                plugin.getTeamScenarioManager().setEnabled(scenario.id(), false, sender);
                return;
            }
            case "info" -> {
                sendScenarioInfo(sender, scenario.id());
                return;
            }
        }

        String[] customArgs = Arrays.copyOfRange(args, 2, args.length);
        if (!scenario.handleAdminCommand(sender, customArgs)) {
            ChatUtil.msg(sender,
                    "<#FF5C5C>Acción desconocida para " + scenario.displayName() + ".</#FF5C5C> " +
                    "<gray>Usa /teamadm scen " + scenario.id() + " info.</gray>");
        }
    }

    private void sendScenarioInfo(CommandSender sender, String id) {
        TeamScenario scenario = plugin.getTeamScenarioManager().get(id);
        if (scenario == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Scenario desconocido.</#FF5C5C>");
            return;
        }

        ChatUtil.msgNoPrefix(sender,
                "<dark_gray>━━━━━━━━━━</dark_gray> <#8B5CF6><bold>" + scenario.displayName().toUpperCase() + "</bold></#8B5CF6> <dark_gray>━━━━━━━━━━</dark_gray>");
        ChatUtil.msgNoPrefix(sender,
                "<gray>Estado:</gray> " + (scenario.isActive()
                        ? "<#6BCB77><bold>ACTIVADO</bold></#6BCB77>"
                        : "<#FF5C5C><bold>DESACTIVADO</bold></#FF5C5C>"));
        ChatUtil.msgNoPrefix(sender, "<gray>ID:</gray> <white>" + scenario.id() + "</white>");
        ChatUtil.msgNoPrefix(sender, "<gray>Descripción:</gray> <white>" + scenario.description() + "</white>");
        for (String line : scenario.statusLines()) {
            ChatUtil.msgNoPrefix(sender, line);
        }
        ChatUtil.msgNoPrefix(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>");
    }

    private void teamConfig(CommandSender sender, String[] args) {
        if (args.length < 2 || args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("show")) {
            ChatUtil.msgNoPrefix(sender,
                    "<dark_gray>━━━━━━━━━━</dark_gray> <#22D3EE><bold>TEAM CONFIG</bold></#22D3EE> <dark_gray>━━━━━━━━━━</dark_gray>");
            ChatUtil.msgNoPrefix(sender, "<gray>Resumen:</gray> <white>" + plugin.getTeamScenarioManager().getConfigDisplay() + "</white>");
            ChatUtil.msgNoPrefix(sender, "<gray>Team scenarios:</gray> <white>" + plugin.getTeamScenarioManager().getActiveDisplay() + "</white>");
            ChatUtil.msgNoPrefix(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "friendlyfire", "ff" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm config friendlyfire [on|off]</#22D3EE>");
                    return;
                }
                if (args[2].equalsIgnoreCase("on")) plugin.getTeamManager().setFriendlyFire(sender, true);
                else if (args[2].equalsIgnoreCase("off")) plugin.getTeamManager().setFriendlyFire(sender, false);
                else ChatUtil.msg(sender, "<#FF5C5C>Usa on u off.</#FF5C5C>");
            }
            case "chat" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm config chat [on|off]</#22D3EE>");
                    return;
                }
                boolean enabled = args[2].equalsIgnoreCase("on");
                if (!enabled && !args[2].equalsIgnoreCase("off")) {
                    ChatUtil.msg(sender, "<#FF5C5C>Usa on u off.</#FF5C5C>");
                    return;
                }
                plugin.getTeamManager().setChatLocked(!enabled);
                plugin.getConfig().set("team-chat-locked", !enabled);
                plugin.saveConfig();
                ChatUtil.broadcast(enabled
                        ? "<#6BCB77>Team Chat habilitado por el host.</#6BCB77>"
                        : "<#FFB347>Team Chat deshabilitado por el host.</#FFB347>");
                plugin.getSoundManager().broadcast(SoundManager.SoundType.TEAM_ADMIN_CHANGE);
            }
            case "teams" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm config teams [on|off]</#22D3EE>");
                    return;
                }
                boolean enabled = args[2].equalsIgnoreCase("on");
                if (!enabled && !args[2].equalsIgnoreCase("off")) {
                    ChatUtil.msg(sender, "<#FF5C5C>Usa on u off.</#FF5C5C>");
                    return;
                }
                if (enabled && plugin.getTeamScenarioManager().isFormationRunning()) {
                    ChatUtil.msg(sender, "<#FFB347>No puedes habilitar la edición normal mientras Captains/Auction está formando teams.</#FFB347> <gray>Detén primero el scenario.</gray>");
                    return;
                }
                plugin.getTeamManager().setTeamsLocked(!enabled);
                plugin.getConfig().set("teams-locked", !enabled);
                plugin.saveConfig();
                ChatUtil.broadcast(enabled
                        ? "<#6BCB77>Creación y edición de equipos habilitada.</#6BCB77>"
                        : "<#FFB347>Creación y edición de equipos bloqueada.</#FFB347>");
                plugin.getSoundManager().broadcast(SoundManager.SoundType.TEAM_ADMIN_CHANGE);
            }
            case "size" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm config size [valor]</#22D3EE>");
                    return;
                }
                try {
                    plugin.getTeamManager().setTeamSize(sender, Integer.parseInt(args[2]));
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>El TeamSize debe ser un número.</#FF5C5C>");
                }
            }
            case "type" -> {
                if (args.length < 3) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm config type [Choosen|Random|Vote]</#22D3EE>");
                    return;
                }
                plugin.getTeamManager().setTeamType(sender, args[2]);
            }
            default -> ChatUtil.msg(sender,
                    "<#FF5C5C>Config desconocida.</#FF5C5C> <gray>Usa friendlyfire, chat, teams, size o type.</gray>");
        }
    }

    private void teamInventory(CommandSender sender, String[] args) {
        if (args.length < 2) {
            ChatUtil.msg(sender,
                    "<gray>Uso:</gray> <#22D3EE>/teamadm teaminventory [toggle|on|off|see] [jugador]</#22D3EE>");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "toggle" -> plugin.getTeamScenarioManager().toggle("team_inventory", sender);
            case "on", "true" -> plugin.getTeamScenarioManager().setEnabled("team_inventory", true, sender);
            case "off", "false" -> plugin.getTeamScenarioManager().setEnabled("team_inventory", false, sender);
            case "see", "view" -> {
                if (args.length < 3) {
                    boolean enabled = plugin.getTeamInventoryManager().isEnabled();
                    ChatUtil.msg(sender,
                            "<gray>Team Inventory:</gray> " +
                                    (enabled ? "<#6BCB77><bold>ACTIVADO</bold></#6BCB77>"
                                            : "<#FF5C5C><bold>DESACTIVADO</bold></#FF5C5C>") +
                                    " <dark_gray>•</dark_gray> <gray>Combat lock:</gray> <white>" +
                                    plugin.getTeamInventoryManager().getCombatLockSeconds() + "s</white>");
                    return;
                }
                if (!(sender instanceof Player viewer)) {
                    ChatUtil.msg(sender, "<#FF5C5C>Para inspeccionar un inventario debes ejecutar el comando desde el juego.</#FF5C5C>");
                    return;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    ChatUtil.msg(sender, "<#FF5C5C>Jugador no encontrado o desconectado.</#FF5C5C>");
                    return;
                }
                plugin.getTeamInventoryManager().openReadOnly(viewer, target);
            }
            default -> ChatUtil.msg(sender,
                    "<#FF5C5C>Opción inválida.</#FF5C5C> <gray>Usa toggle, on, off o see.</gray>");
        }
    }

    private void info(CommandSender sender, String[] args) {
        if (!(sender instanceof Player viewer)) {
            ChatUtil.msg(sender, "<#FF5C5C>Desde consola usa un jugador para consultar visualmente el equipo.</#FF5C5C>");
            return;
        }
        if (args.length < 2) {
            plugin.getTeamManager().showTeamInfo(viewer, viewer);
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            ChatUtil.msg(sender, "<#FF5C5C>Jugador no encontrado.</#FF5C5C>");
            return;
        }
        plugin.getTeamManager().showTeamInfo(viewer, target);
    }

    private void broadcastLock(String title, String subtitle, boolean success) {
        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                title + "\n" + subtitle + "\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll(title, subtitle, 5, 35, 10);
        plugin.getSoundManager().broadcast(success ? SoundManager.SoundType.SUCCESS : SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    private void sendHelp(CommandSender sender) {
        ChatUtil.msgNoPrefix(sender, "<dark_gray>━━━━━━━━━━</dark_gray> <#8B5CF6><bold>VOIDTEAMS HOST</bold></#8B5CF6> <dark_gray>━━━━━━━━━━</dark_gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/teamadm gui</#22D3EE> <dark_gray>•</dark_gray> <gray>Abrir configuración visual</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta force [j1] [j2]</#22D3EE> <dark_gray>•</dark_gray> <gray>Mover j1 al equipo de j2</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta remove [jugador]</#22D3EE> <dark_gray>•</dark_gray> <gray>Remover miembro</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta disband [jugador]</#22D3EE> <dark_gray>•</dark_gray> <gray>Disolver equipo</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta clear</#22D3EE> <dark_gray>•</dark_gray> <gray>Eliminar todos los equipos</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta color [jugador] #RRGGBB</#22D3EE> <dark_gray>•</dark_gray> <gray>Color RGB real</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta icon [jugador] [icono]</#22D3EE> <dark_gray>•</dark_gray> <gray>Cambiar icono</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta type [Choosen|Random|Vote]</#22D3EE> <dark_gray>•</dark_gray> <gray>Modo de equipos</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta size [set|add|remove] [n]</#22D3EE> <dark_gray>•</dark_gray> <gray>TeamSize</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta shuffle</#22D3EE> <dark_gray>•</dark_gray> <gray>Rellenar jugadores sin team</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta shuffleforce</#22D3EE> <dark_gray>•</dark_gray> <gray>Rehacer todos los equipos</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta vote <type|size> ...</#22D3EE> <dark_gray>•</dark_gray> <gray>Votación en vivo</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta block [all|chat|teams|none]</#22D3EE> <dark_gray>•</dark_gray> <gray>Bloqueos</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta friendlyfire [on|off]</#22D3EE> <dark_gray>•</dark_gray> <gray>Daño entre aliados</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta teaminventory [toggle|on|off]</#22D3EE> <dark_gray>•</dark_gray> <gray>Inventario compartido por equipo</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta teaminventory see [jugador]</#22D3EE> <dark_gray>•</dark_gray> <gray>Inspección de solo lectura</gray>");
        ChatUtil.msgNoPrefix(sender, "<#8B5CF6>/ta scen list</#8B5CF6> <dark_gray>•</dark_gray> <gray>Ver Team Scenarios</gray>");
        ChatUtil.msgNoPrefix(sender, "<#8B5CF6>/ta scen toggle [scenario]</#8B5CF6> <dark_gray>•</dark_gray> <gray>Alternar scenario</gray>");
        ChatUtil.msgNoPrefix(sender, "<#FFD166>/ta scen captains start</#FFD166> <dark_gray>•</dark_gray> <gray>Iniciar Captains Draft</gray>");
        ChatUtil.msgNoPrefix(sender, "<#FFD166>/ta scen auction start</#FFD166> <dark_gray>•</dark_gray> <gray>Iniciar Auction</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta config</#22D3EE> <dark_gray>•</dark_gray> <gray>Resumen/configuración rápida</gray>");
        ChatUtil.msgNoPrefix(sender, "<#22D3EE>/ta reload</#22D3EE> <dark_gray>•</dark_gray> <gray>Recargar config + scenarios</gray>");
        ChatUtil.msgNoPrefix(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>");
    }
}
