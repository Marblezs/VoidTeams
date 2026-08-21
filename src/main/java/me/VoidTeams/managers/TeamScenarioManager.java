package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.api.events.TeamScenarioStateChangeEvent;
import me.VoidTeams.api.model.TeamScenarioInfo;
import me.VoidTeams.scenarios.AuctionScenario;
import me.VoidTeams.scenarios.CaptainsScenario;
import me.VoidTeams.scenarios.SharedHealthScenario;
import me.VoidTeams.scenarios.TeamInventoryScenario;
import me.VoidTeams.scenarios.TeamScenario;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeamScenarioManager {

    private final VoidTeams plugin;
    private final Map<String, TeamScenario> scenarios = new LinkedHashMap<>();
    private final File file;
    private YamlConfiguration data;

    public TeamScenarioManager(VoidTeams plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "team-scenarios.yml");

        boolean firstCreation = !file.exists();
        if (firstCreation) {
            plugin.saveResource("team-scenarios.yml", false);
        }

        this.data = YamlConfiguration.loadConfiguration(file);
        if (firstCreation && plugin.getConfig().isConfigurationSection("team-inventory")) {
            data.set("scenarios.team_inventory.enabled",
                    plugin.getConfig().getBoolean("team-inventory.enabled", false));
            data.set("scenarios.team_inventory.settings.size",
                    plugin.getConfig().getInt("team-inventory.size", 27));
            data.set("scenarios.team_inventory.settings.combat-lock-seconds",
                    plugin.getConfig().getInt("team-inventory.combat-lock-seconds", 5));
            save();
            plugin.getLogger().info("Team Inventory migrado de config.yml a team-scenarios.yml.");
        }

        registerDefaults();
        registerListeners();
        loadStates(false);
    }

    private void registerDefaults() {
        register(new TeamInventoryScenario(plugin));
        register(new SharedHealthScenario(plugin));
        register(new CaptainsScenario(plugin));
        register(new AuctionScenario(plugin));
    }

    private void register(TeamScenario scenario) {
        scenarios.put(normalize(scenario.id()), scenario);
    }

    private void registerListeners() {
        for (TeamScenario scenario : scenarios.values()) {
            Bukkit.getPluginManager().registerEvents(scenario, plugin);
        }
    }

    private void loadStates(boolean announceChanges) {
        boolean changed = false;

        for (TeamScenario scenario : scenarios.values()) {
            boolean enabled = data.getBoolean("scenarios." + scenario.id() + ".enabled", false);

            if (enabled) {
                for (String conflictId : scenario.conflictsWith()) {
                    TeamScenario conflict = get(conflictId);
                    if (conflict != null && conflict.isActive()) {
                        conflict.setActiveInternal(false);
                        data.set("scenarios." + conflict.id() + ".enabled", false);
                        changed = true;
                        if (announceChanges) announceState(conflict, false, null);
                    }
                }
            }

            if (scenario.isActive() == enabled) continue;

            scenario.setActiveInternal(enabled);
            if (announceChanges) {
                announceState(scenario, enabled, null);
            }
        }

        if (changed) save();
    }

    public void reload(CommandSender sender) {
        this.data = YamlConfiguration.loadConfiguration(file);
        loadStates(false);

        CaptainsScenario captains = get("captains", CaptainsScenario.class);
        AuctionScenario auction = get("auction", AuctionScenario.class);
        if ((captains != null && captains.isDraftRunning())
                || (auction != null && auction.isAuctionRunning())) {
            plugin.getTeamManager().setTeamsLocked(true);
        }

        ChatUtil.msg(sender, "<#6BCB77>Team scenarios recargados desde team-scenarios.yml.</#6BCB77>");
        if (sender instanceof Player player) {
            plugin.getSoundManager().play(player, SoundManager.SoundType.SUCCESS);
        }
    }

    public TeamScenario get(String id) {
        return id == null ? null : scenarios.get(normalize(id));
    }

    public <T extends TeamScenario> T get(String id, Class<T> type) {
        TeamScenario scenario = get(id);
        if (scenario == null || !type.isInstance(scenario)) return null;
        return type.cast(scenario);
    }

    public Collection<TeamScenario> getScenarios() {
        return List.copyOf(scenarios.values());
    }

    public List<TeamScenario> getActiveScenarios() {
        return scenarios.values().stream().filter(TeamScenario::isActive).toList();
    }

    public boolean isEnabled(String id) {
        TeamScenario scenario = get(id);
        return scenario != null && scenario.isActive();
    }

    public boolean setEnabled(String id, boolean enabled) {
        return setEnabled(id, enabled, null);
    }

    public boolean setEnabled(String id, boolean enabled, CommandSender sender) {
        TeamScenario scenario = get(id);
        if (scenario == null) {
            if (sender != null) {
                ChatUtil.msg(sender, "<#FF5C5C>Scenario de equipos desconocido:</#FF5C5C> <white>" + id + "</white>");
            }
            return false;
        }

        if (scenario.isActive() == enabled) {
            if (sender != null) {
                ChatUtil.msg(sender, "<gray>" + scenario.displayName() + " ya está " +
                        (enabled ? "<#6BCB77>activado</#6BCB77>" : "<#FF5C5C>desactivado</#FF5C5C>") + ".</gray>");
            }
            return true;
        }

        if (enabled) {
            for (String conflictId : scenario.conflictsWith()) {
                TeamScenario conflict = get(conflictId);
                if (conflict != null && conflict.isActive()) {
                    boolean previousConflictState = conflict.isActive();
                    conflict.setActiveInternal(false);
                    data.set("scenarios." + conflict.id() + ".enabled", false);
                    announceState(conflict, false, sender);
                    fireStateChange(conflict, previousConflictState);
                }
            }
        }

        boolean previousState = scenario.isActive();
        scenario.setActiveInternal(enabled);
        data.set("scenarios." + scenario.id() + ".enabled", enabled);
        save();
        announceState(scenario, enabled, sender);
        fireStateChange(scenario, previousState);
        return true;
    }

    public boolean toggle(String id) {
        return toggle(id, null);
    }

    public boolean toggle(String id, CommandSender sender) {
        TeamScenario scenario = get(id);
        if (scenario == null) {
            if (sender != null) {
                ChatUtil.msg(sender, "<#FF5C5C>Scenario de equipos desconocido:</#FF5C5C> <white>" + id + "</white>");
            }
            return false;
        }
        return setEnabled(id, !scenario.isActive(), sender);
    }

    private void announceState(TeamScenario scenario, boolean enabled, CommandSender sender) {
        String stateColor = enabled ? "#6BCB77" : "#FF5C5C";
        String state = enabled ? "ACTIVADO" : "DESACTIVADO";
        String subtitle = enabled
                ? "<gray>Ahora forma parte de las reglas de equipos.</gray>"
                : "<gray>Dejó de formar parte de las reglas de equipos.</gray>";

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#8B5CF6><bold>TEAM SCENARIO</bold></#8B5CF6> <dark_gray>»</dark_gray> <white>" + scenario.displayName() + "</white>\n" +
                "<gray>Estado:</gray> <" + stateColor + "><bold>" + state + "</bold></" + stateColor + ">\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );

        ChatUtil.titleAll(
                "<#8B5CF6><bold>TEAM SCENARIO</bold></#8B5CF6>",
                "<white>" + scenario.displayName() + "</white> <dark_gray>•</dark_gray> <" + stateColor + ">" + state + "</" + stateColor + ">",
                5, 35, 10
        );
        ChatUtil.sendActionBarToAll("<#8B5CF6><bold>SCENARIOS</bold></#8B5CF6> <dark_gray>»</dark_gray> " + subtitle);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.TEAM_SCENARIO_TOGGLE);
    }

    private void fireStateChange(TeamScenario scenario, boolean previousState) {
        TeamScenarioInfo info = new TeamScenarioInfo(
                scenario.id(),
                scenario.displayName(),
                scenario.description(),
                scenario.icon(),
                scenario.isActive(),
                scenario.conflictsWith()
        );
        Bukkit.getPluginManager().callEvent(new TeamScenarioStateChangeEvent(info, previousState));
    }

    public String getActiveDisplay() {
        List<String> names = getActiveScenarios().stream().map(TeamScenario::displayName).toList();
        return names.isEmpty() ? "Ninguno" : String.join(", ", names);
    }

    public String getActiveDisplayCompact() {
        List<String> names = getActiveScenarios().stream().map(TeamScenario::displayName).toList();
        return names.isEmpty() ? "Ninguno" : String.join(" • ", names);
    }

    public boolean isFormationRunning() {
        CaptainsScenario captains = get("captains", CaptainsScenario.class);
        AuctionScenario auction = get("auction", AuctionScenario.class);
        return (captains != null && captains.isDraftRunning())
                || (auction != null && auction.isAuctionRunning());
    }

    public String getConfigDisplay() {
        String ff = plugin.getConfig().getBoolean("friendly-fire", false) ? "FF On" : "FF Off";
        String chat = plugin.getTeamManager().isChatLocked() ? "Chat Off" : "Chat On";
        String teams = plugin.getTeamManager().isTeamsLocked() ? "Teams Locked" : "Teams Open";
        String size = plugin.getTeamManager().getTeamSize() == 1
                ? "FFA"
                : "To" + plugin.getTeamManager().getTeamSize();
        return size + " • " + plugin.getTeamManager().getTeamTypeDisplay() + " • " + ff + " • " + chat + " • " + teams;
    }

    public int getInt(String scenarioId, String path, int def) {
        return data.getInt(path(scenarioId, path), def);
    }

    public boolean getBoolean(String scenarioId, String path, boolean def) {
        return data.getBoolean(path(scenarioId, path), def);
    }

    public String getString(String scenarioId, String path, String def) {
        return data.getString(path(scenarioId, path), def);
    }

    public List<String> getStringList(String scenarioId, String path) {
        return data.getStringList(path(scenarioId, path));
    }

    public void set(String scenarioId, String path, Object value) {
        data.set(path(scenarioId, path), value);
        save();
    }

    private String path(String scenarioId, String path) {
        return "scenarios." + normalize(scenarioId) + ".settings." + path;
    }

    public List<String> ids() {
        return new ArrayList<>(scenarios.keySet());
    }

    public void shutdown() {
        for (TeamScenario scenario : scenarios.values()) {
            scenario.shutdown();
        }
        save();
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("No se pudo guardar team-scenarios.yml: " + exception.getMessage());
        }
    }

    private String normalize(String value) {
        if (value == null) return "";
        String normalized = value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "teaminventory", "ti" -> "team_inventory";
            case "sharedhealth", "shared_hp", "sharedhp" -> "shared_health";
            case "captain" -> "captains";
            default -> normalized;
        };
    }
}
