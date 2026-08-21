package me.VoidTeams.api.model;

import java.util.Set;

public record TeamInfo(
        String id,
        String color,
        String icon,
        Set<String> members
) {
    public TeamInfo {
        members = Set.copyOf(members);
    }
}
