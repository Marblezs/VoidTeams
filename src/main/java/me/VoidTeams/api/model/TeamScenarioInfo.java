package me.VoidTeams.api.model;

import org.bukkit.Material;

import java.util.Set;

public record TeamScenarioInfo(
        String id,
        String displayName,
        String description,
        Material icon,
        boolean enabled,
        Set<String> conflicts
) {
    public TeamScenarioInfo {
        conflicts = Set.copyOf(conflicts);
    }
}
