package me.VoidTeams;

import me.VoidTeams.commands.TeamAdminCommands;
import me.VoidTeams.commands.TeamCommands;
import me.VoidTeams.commands.TeamTabCompleter;
import me.VoidTeams.managers.RandomTeamManager;
import me.VoidTeams.managers.TeamManager;
import me.VoidTeams.listeners.TeamChatListener;
import me.VoidTeams.storage.TeamsData;
import org.bukkit.plugin.java.JavaPlugin;

public class VoidTeams extends JavaPlugin {

    private TeamManager teamManager;
    private RandomTeamManager randomTeamManager;
    private TeamsData teamsData;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.teamsData = new TeamsData();
        this.teamManager = new TeamManager(this);
        this.randomTeamManager = new RandomTeamManager(this);

        getCommand("team").setExecutor(new TeamCommands(this));
        getCommand("team").setTabCompleter(new TeamTabCompleter());

        getCommand("teamadmin").setExecutor(new TeamAdminCommands(this));
        getCommand("teamadmin").setTabCompleter(new TeamTabCompleter());

        getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);

        getLogger().info("VoidTeams ha sido activado correctamente!");
    }

    public TeamManager getTeamManager() { return teamManager; }
    public RandomTeamManager getRandomTeamManager() { return randomTeamManager; }
    public TeamsData getTeamsData() { return teamsData; }
}