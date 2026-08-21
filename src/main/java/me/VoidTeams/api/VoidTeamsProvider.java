package me.VoidTeams.api;

import java.util.Optional;

public final class VoidTeamsProvider {
    private static volatile VoidTeamsAPI api;

    private VoidTeamsProvider() {
    }

    public static VoidTeamsAPI get() {
        VoidTeamsAPI current = api;
        if (current == null) {
            throw new IllegalStateException("VoidTeams API no está disponible.");
        }
        return current;
    }

    public static Optional<VoidTeamsAPI> getOptional() {
        return Optional.ofNullable(api);
    }

    public static boolean isAvailable() {
        return api != null;
    }

    public static void register(VoidTeamsAPI instance) {
        if (instance == null) {
            throw new IllegalArgumentException("instance");
        }
        api = instance;
    }

    public static void unregister(VoidTeamsAPI instance) {
        if (api == instance) {
            api = null;
        }
    }
}
