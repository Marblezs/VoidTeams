package me.VoidTeams.api;

import me.VoidTeams.api.model.TeamInfo;
import me.VoidTeams.api.model.TeamScenarioInfo;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VoidTeamsAPI {
    String getVersion();

    Collection<TeamScenarioInfo> getScenarios();

    Optional<TeamScenarioInfo> getScenario(String id);

    List<String> getActiveScenarioIds();

    boolean isScenarioEnabled(String id);

    boolean setScenarioEnabled(String id, boolean enabled);

    boolean toggleScenario(String id);

    int getTeamSize();

    String getTeamType();

    String getEffectiveTeamType();

    String getTeamSizeDisplay();

    boolean isFriendlyFireEnabled();

    boolean areTeamsLocked();

    boolean isTeamChatLocked();

    Optional<TeamInfo> getTeam(Player player);

    Optional<TeamInfo> getTeam(String playerName);

    Collection<TeamInfo> getTeams();

    List<String> getAvailableColors();

    Map<String, String> getColorAliases();

    List<String> getAvailableIcons();
}
