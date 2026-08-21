package me.VoidTeams.scenarios;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeamInventoryScenario extends TeamScenario {

    public TeamInventoryScenario(VoidTeams plugin) {
        super(plugin,
                "team_inventory",
                "Team Inventory",
                "Cada equipo comparte una mochila mediante /ti. Se bloquea temporalmente al entrar en combate.",
                Material.ENDER_CHEST);
    }

    @Override
    protected void onDisableScenario() {
        if (plugin.getTeamInventoryManager() != null) {
            plugin.getTeamInventoryManager().closeAllSharedInventories();
            plugin.getTeamInventoryManager().clearCombatTags();
        }
    }

    @Override
    public boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length == 0) return false;

        switch (args[0].toLowerCase()) {
            case "size" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen team_inventory size [9-54]</#22D3EE>");
                    return true;
                }
                try {
                    int requested = Integer.parseInt(args[1]);
                    int bounded = Math.max(9, Math.min(54, requested));
                    int size = Math.max(9, (bounded / 9) * 9);
                    plugin.getTeamScenarioManager().set(id(), "size", size);
                    plugin.getTeamInventoryManager().rebuildCachedInventories();
                    ChatUtil.broadcast("<#8B5CF6>Team Inventory</#8B5CF6> <gray>ahora tiene</gray> <white>" + size + " slots</white>.");
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>El tamaño debe ser un número.</#FF5C5C>");
                }
                return true;
            }
            case "combatlock", "combat-lock" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen team_inventory combatlock [segundos]</#22D3EE>");
                    return true;
                }
                try {
                    int seconds = Math.max(0, Integer.parseInt(args[1]));
                    plugin.getTeamScenarioManager().set(id(), "combat-lock-seconds", seconds);
                    ChatUtil.broadcast("<#8B5CF6>Team Inventory</#8B5CF6> <gray>combat-lock:</gray> <white>" + seconds + "s</white>.");
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>Los segundos deben ser un número.</#FF5C5C>");
                }
                return true;
            }
            case "see", "view" -> {
                if (!(sender instanceof Player viewer)) {
                    ChatUtil.msg(sender, "<#FF5C5C>La inspección debe hacerse desde el juego.</#FF5C5C>");
                    return true;
                }
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen team_inventory see [jugador]</#22D3EE>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    ChatUtil.msg(sender, "<#FF5C5C>Jugador no encontrado.</#FF5C5C>");
                    return true;
                }
                plugin.getTeamInventoryManager().openReadOnly(viewer, target);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("size", "combatlock", "see");
        if (args.length == 2 && args[0].equalsIgnoreCase("size")) return List.of("9", "18", "27", "36", "45", "54");
        if (args.length == 2 && args[0].equalsIgnoreCase("combatlock")) return List.of("0", "3", "5", "8", "10");
        if (args.length == 2 && args[0].equalsIgnoreCase("see")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }

    @Override
    public List<String> statusLines() {
        return List.of(
                "<gray>Slots:</gray> <white>" + plugin.getTeamInventoryManager().getInventorySize() + "</white>",
                "<gray>Combat lock:</gray> <white>" + plugin.getTeamInventoryManager().getCombatLockSeconds() + "s</white>"
        );
    }
}
