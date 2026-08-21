package me.VoidTeams.api.internal;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.api.VoidTeamsAPI;
import me.VoidTeams.api.model.TeamInfo;
import me.VoidTeams.api.model.TeamScenarioInfo;
import me.VoidTeams.scenarios.TeamScenario;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class VoidTeamsAPIImpl implements VoidTeamsAPI {
    private final VoidTeams plugin;

    public VoidTeamsAPIImpl(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public Collection<TeamScenarioInfo> getScenarios() {
        return plugin.getTeamScenarioManager().getScenarios().stream()
                .map(this::toScenarioInfo)
                .toList();
    }

    @Override
    public Optional<TeamScenarioInfo> getScenario(String id) {
        TeamScenario scenario = plugin.getTeamScenarioManager().get(id);
        return scenario == null ? Optional.empty() : Optional.of(toScenarioInfo(scenario));
    }

    @Override
    public List<String> getActiveScenarioIds() {
        return plugin.getTeamScenarioManager().getActiveScenarios().stream()
                .map(TeamScenario::id)
                .toList();
    }

    @Override
    public boolean isScenarioEnabled(String id) {
        return plugin.getTeamScenarioManager().isEnabled(id);
    }

    @Override
    public boolean setScenarioEnabled(String id, boolean enabled) {
        ensurePrimaryThread();
        return plugin.getTeamScenarioManager().setEnabled(id, enabled);
    }

    @Override
    public boolean toggleScenario(String id) {
        ensurePrimaryThread();
        return plugin.getTeamScenarioManager().toggle(id);
    }

    @Override
    public int getTeamSize() {
        return plugin.getTeamManager().getTeamSize();
    }

    @Override
    public String getTeamType() {
        return plugin.getTeamManager().getTeamTypeDisplay();
    }

    @Override
    public boolean isFriendlyFireEnabled() {
        return plugin.getConfig().getBoolean("friendly-fire", false);
    }

    @Override
    public boolean areTeamsLocked() {
        return plugin.getTeamManager().isTeamsLocked();
    }

    @Override
    public boolean isTeamChatLocked() {
        return plugin.getTeamManager().isChatLocked();
    }

    @Override
    public Optional<TeamInfo> getTeam(Player player) {
        if (player == null) return Optional.empty();
        return toTeamInfo(plugin.getTeamManager().getTeam(player));
    }

    @Override
    public Optional<TeamInfo> getTeam(String playerName) {
        if (playerName == null || playerName.isBlank()) return Optional.empty();
        return toTeamInfo(plugin.getTeamManager().getTeam(playerName));
    }

    @Override
    public Collection<TeamInfo> getTeams() {
        List<TeamInfo> teams = new ArrayList<>();
        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            if (!team.getName().startsWith("vt_") && !team.getName().startsWith("team_")) continue;
            toTeamInfo(team).ifPresent(teams::add);
        }
        return List.copyOf(teams);
    }

    @Override
    public List<String> getAvailableColors() {
        return plugin.getTeamManager().getAvailableColors();
    }

    @Override
    public Map<String, String> getColorAliases() {
        return plugin.getTeamManager().getColorAliases();
    }

    @Override
    public List<String> getAvailableIcons() {
        return plugin.getTeamManager().getAvailableIcons();
    }

    private void ensurePrimaryThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Las modificaciones de VoidTeams API deben ejecutarse en el hilo principal de Bukkit.");
        }
    }

    private TeamScenarioInfo toScenarioInfo(TeamScenario scenario) {
        return new TeamScenarioInfo(
                scenario.id(),
                scenario.displayName(),
                scenario.description(),
                scenario.icon(),
                scenario.isActive(),
                scenario.conflictsWith()
        );
    }

    private Optional<TeamInfo> toTeamInfo(Team team) {
        if (team == null) return Optional.empty();
        return Optional.of(new TeamInfo(
                team.getName(),
                plugin.getTeamManager().getTeamHex(team),
                plugin.getTeamManager().getTeamIcon(team),
                team.getEntries()
        ));
    }
}
