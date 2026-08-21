package me.VoidTeams.api.events;

import me.VoidTeams.api.model.TeamScenarioInfo;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class TeamScenarioStateChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final TeamScenarioInfo scenario;
    private final boolean previousState;

    public TeamScenarioStateChangeEvent(TeamScenarioInfo scenario, boolean previousState) {
        this.scenario = scenario;
        this.previousState = previousState;
    }

    public TeamScenarioInfo getScenario() {
        return scenario;
    }

    public String getScenarioId() {
        return scenario.id();
    }

    public boolean wasEnabled() {
        return previousState;
    }

    public boolean isEnabled() {
        return scenario.enabled();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
