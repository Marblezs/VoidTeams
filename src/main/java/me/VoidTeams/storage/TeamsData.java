package me.VoidTeams.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeamsData {
    private final Set<UUID> teamChatToggled = new HashSet<>();

    public boolean isChatToggled(UUID uuid) {
        return teamChatToggled.contains(uuid);
    }

    public boolean toggleChat(UUID uuid) {
        boolean enabled = !teamChatToggled.contains(uuid);
        setChatToggled(uuid, enabled);
        return enabled;
    }

    public void setChatToggled(UUID uuid, boolean enabled) {
        if (enabled) {
            teamChatToggled.add(uuid);
        } else {
            teamChatToggled.remove(uuid);
        }
    }

    public void clear() {
        teamChatToggled.clear();
    }
}
