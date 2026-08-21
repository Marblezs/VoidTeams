package me.VoidTeams.scenarios;

import me.VoidTeams.VoidTeams;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class TeamScenario implements Listener {

    protected final VoidTeams plugin;

    private final String id;
    private final String displayName;
    private final String description;
    private final Material icon;
    private boolean active;

    protected TeamScenario(VoidTeams plugin,
                           String id,
                           String displayName,
                           String description,
                           Material icon) {
        this.plugin = plugin;
        this.id = normalize(id);
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public final String id() {
        return id;
    }

    public final String displayName() {
        return displayName;
    }

    public final String description() {
        return description;
    }

    public final Material icon() {
        return icon;
    }

    public final boolean isActive() {
        return active;
    }

    public final void setActiveInternal(boolean active) {
        if (this.active == active) return;
        this.active = active;
        if (active) {
            onEnableScenario();
        } else {
            onDisableScenario();
        }
    }

    public Set<String> conflictsWith() {
        return Set.of();
    }

    protected void onEnableScenario() {
    }

    protected void onDisableScenario() {
    }

    public void shutdown() {
        onDisableScenario();
    }

    public boolean handleAdminCommand(CommandSender sender, String[] args) {
        return false;
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    public List<String> statusLines() {
        return List.of();
    }

    protected final int settingInt(String path, int def) {
        return plugin.getTeamScenarioManager().getInt(id, path, def);
    }

    protected final boolean settingBoolean(String path, boolean def) {
        return plugin.getTeamScenarioManager().getBoolean(id, path, def);
    }

    protected final String settingString(String path, String def) {
        return plugin.getTeamScenarioManager().getString(id, path, def);
    }

    protected final List<String> settingStringList(String path) {
        return plugin.getTeamScenarioManager().getStringList(id, path);
    }

    private static String normalize(String input) {
        if (input == null) return "";
        return input.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
