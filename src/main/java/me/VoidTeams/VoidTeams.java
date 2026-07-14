package me.VoidTeams;

import me.VoidTeams.commands.TeamAdminCommands;
import me.VoidTeams.commands.TeamCommands;
import me.VoidTeams.commands.TeamTabCompleter;
import me.VoidTeams.hooks.VoidTeamsExpansion;
import me.VoidTeams.managers.RandomTeamManager;
import me.VoidTeams.managers.TeamManager;
import me.VoidTeams.listeners.TeamChatListener;
import me.VoidTeams.storage.TeamsData;
import org.bukkit.plugin.java.JavaPlugin;

public class VoidTeams extends JavaPlugin {

    private TeamManager teamManager;
    private RandomTeamManager randomTeamManager;
    private TeamsData teamsData;
    private final VoidTeams plugin;

    public VoidTeams(VoidTeams plugin) {
        this.plugin = plugin;
    }

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
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VoidTeamsExpansion(this).register();
            getLogger().info("PlaceholderAPI detectado, integrando placeholders.");
        }
    }
    @Override
    public void onDisable() {
        if (getConfig().getBoolean("clear-teams-on-stop", false)) {
            getTeamManager().clearAllTeamsConsole();
            getLogger().info("Se han limpiado todos los equipos debido a la configuración de apagado.");
        }
        saveConfig();
        getLogger().info("VoidTeams se ha desactivado correctamente.");
    }

    public TeamManager getTeamManager() { return teamManager; }
    public RandomTeamManager getRandomTeamManager() { return randomTeamManager; }
    public TeamsData getTeamsData() { return teamsData; }

}