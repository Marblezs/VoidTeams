package me.VoidTeams.scenarios;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SharedHealthScenario extends TeamScenario {

    public SharedHealthScenario(VoidTeams plugin) {
        super(plugin,
                "shared_health",
                "Shared Health",
                "El daño de un integrante reduce la misma cantidad de vida al resto del equipo; opcionalmente comparte curación.",
                Material.REDSTONE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!isActive()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (isIgnoredWorld(victim)) return;

        Team team = plugin.getTeamManager().getTeam(victim);
        if (team == null || team.getSize() <= 1) return;

        double healthBefore = victim.getHealth();
        String teamName = team.getName();
        String victimName = victim.getName();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!isActive()) return;

            Team currentTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
            if (currentTeam == null || !currentTeam.hasEntry(victimName)) return;

            double currentHealth = victim.isDead() ? 0.0 : Math.max(0.0, victim.getHealth());
            double lost = Math.max(0.0, healthBefore - currentHealth);
            if (lost <= 0.0001) return;

            for (String entry : currentTeam.getEntries()) {
                if (entry.equalsIgnoreCase(victimName)) continue;
                Player teammate = Bukkit.getPlayerExact(entry);
                if (teammate == null || !teammate.isOnline() || teammate.isDead()) continue;
                if (isIgnoredWorld(teammate)) continue;

                double targetHealth = Math.max(0.0, teammate.getHealth() - lost);
                teammate.setHealth(targetHealth);

                TeamTheme theme = plugin.getTeamManager().getTheme(currentTeam);
                ChatUtil.sendActionBar(teammate,
                        "<" + theme.hexColor() + "><bold>SHARED HEALTH</bold></" + theme.hexColor() + "> " +
                        "<dark_gray>»</dark_gray> <#FF6B6B>-" + oneDecimal(lost / 2.0) + "❤</#FF6B6B> " +
                        "<gray>por " + victimName + "</gray>");
                plugin.getSoundManager().play(teammate, SoundManager.SoundType.SHARED_HEALTH_DAMAGE);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        if (!isActive() || !settingBoolean("share-healing", true)) return;
        if (!(event.getEntity() instanceof Player source)) return;
        if (isIgnoredWorld(source)) return;

        Team team = plugin.getTeamManager().getTeam(source);
        if (team == null || team.getSize() <= 1) return;

        double healthBefore = source.getHealth();
        String teamName = team.getName();
        String sourceName = source.getName();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!isActive() || !settingBoolean("share-healing", true)) return;

            Team currentTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
            if (currentTeam == null || !currentTeam.hasEntry(sourceName)) return;

            double gained = Math.max(0.0, source.getHealth() - healthBefore);
            if (gained <= 0.0001) return;

            for (String entry : currentTeam.getEntries()) {
                if (entry.equalsIgnoreCase(sourceName)) continue;
                Player teammate = Bukkit.getPlayerExact(entry);
                if (teammate == null || !teammate.isOnline() || teammate.isDead()) continue;
                if (isIgnoredWorld(teammate)) continue;

                double maxHealth = teammate.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH) == null
                        ? 20.0
                        : teammate.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
                teammate.setHealth(Math.min(maxHealth, teammate.getHealth() + gained));
            }
        });
    }

    @Override
    public boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length == 0) return false;

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "healing", "heal" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen shared_health healing [on|off]</#22D3EE>");
                    return true;
                }
                boolean enabled;
                if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true")) enabled = true;
                else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")) enabled = false;
                else {
                    ChatUtil.msg(sender, "<#FF5C5C>Usa on u off.</#FF5C5C>");
                    return true;
                }
                plugin.getTeamScenarioManager().set(id(), "share-healing", enabled);
                ChatUtil.broadcast("<#FF6B6B>Shared Health</#FF6B6B> <gray>curación compartida:</gray> " +
                        (enabled ? "<#6BCB77>ON</#6BCB77>" : "<#FF5C5C>OFF</#FF5C5C>"));
                return true;
            }
            case "ignoreworld", "ignoredworld" -> {
                if (args.length < 2 || args[1].equalsIgnoreCase("list")) {
                    List<String> worlds = settingStringList("ignored-worlds");
                    ChatUtil.msg(sender, "<gray>Mundos ignorados:</gray> <white>" +
                            (worlds.isEmpty() ? "Ninguno" : String.join(", ", worlds)) + "</white>");
                    return true;
                }
                if (args.length < 3) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen shared_health ignoreworld [add|remove] [mundo]</#22D3EE>");
                    return true;
                }

                List<String> worlds = new ArrayList<>(settingStringList("ignored-worlds"));
                String world = args[2];
                if (args[1].equalsIgnoreCase("add")) {
                    if (worlds.stream().noneMatch(existing -> existing.equalsIgnoreCase(world))) worlds.add(world);
                } else if (args[1].equalsIgnoreCase("remove")) {
                    worlds.removeIf(existing -> existing.equalsIgnoreCase(world));
                } else {
                    ChatUtil.msg(sender, "<#FF5C5C>Usa add, remove o list.</#FF5C5C>");
                    return true;
                }
                plugin.getTeamScenarioManager().set(id(), "ignored-worlds", worlds);
                ChatUtil.msg(sender, "<#6BCB77>Lista de mundos de Shared Health actualizada.</#6BCB77>");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("healing", "ignoreworld");
        if (args.length == 2 && args[0].equalsIgnoreCase("healing")) return List.of("on", "off");
        if (args.length == 2 && args[0].equalsIgnoreCase("ignoreworld")) return List.of("list", "add", "remove");
        if (args.length == 3 && args[0].equalsIgnoreCase("ignoreworld")) {
            return Bukkit.getWorlds().stream().map(world -> world.getName()).toList();
        }
        return List.of();
    }

    @Override
    public List<String> statusLines() {
        List<String> ignored = settingStringList("ignored-worlds");
        return List.of(
                "<gray>Compartir curación:</gray> " + (settingBoolean("share-healing", true)
                        ? "<#6BCB77>ON</#6BCB77>" : "<#FF5C5C>OFF</#FF5C5C>"),
                "<gray>Mundos ignorados:</gray> <white>" + (ignored.isEmpty() ? "Ninguno" : String.join(", ", ignored)) + "</white>"
        );
    }

    private boolean isIgnoredWorld(Player player) {
        for (String world : settingStringList("ignored-worlds")) {
            if (player.getWorld().getName().equalsIgnoreCase(world)) return true;
        }
        return false;
    }

    private String oneDecimal(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
