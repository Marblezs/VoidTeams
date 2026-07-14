package me.VoidTeams.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamsData {
    private final Set<UUID> teamChatToggled = new HashSet<>();

    public boolean isChatToggled(UUID uuid) {
        return teamChatToggled.contains(uuid);
    }

    public void toggleChat(UUID uuid) {
        if (teamChatToggled.contains(uuid)) {
            teamChatToggled.remove(uuid);
        } else {
            teamChatToggled.add(uuid);
        }
    }
}