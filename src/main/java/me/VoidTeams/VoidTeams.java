package me.VoidTeams;

import me.VoidTeams.api.VoidTeamsAPI;
import me.VoidTeams.api.VoidTeamsProvider;
import me.VoidTeams.api.internal.VoidTeamsAPIImpl;
import me.VoidTeams.commands.TeamAdminCommands;
import me.VoidTeams.commands.TeamCommands;
import me.VoidTeams.commands.TeamTabCompleter;
import me.VoidTeams.commands.VoteCommand;
import me.VoidTeams.commands.TeamUtilityCommands;
import me.VoidTeams.hooks.VoidTeamsExpansion;
import me.VoidTeams.gui.TeamAdminGui;
import me.VoidTeams.listeners.TeamChatListener;
import me.VoidTeams.managers.RandomTeamManager;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.managers.TeamManager;
import me.VoidTeams.managers.TeamInventoryManager;
import me.VoidTeams.managers.TeamScenarioManager;
import me.VoidTeams.managers.OreTrackerManager;
import me.VoidTeams.managers.VoteTeamManager;
import me.VoidTeams.storage.TeamsData;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class VoidTeams extends JavaPlugin {

    private TeamManager teamManager;
    private RandomTeamManager randomTeamManager;
    private TeamsData teamsData;
    private VoteTeamManager voteTeamManager;
    private SoundManager soundManager;
    private TeamInventoryManager teamInventoryManager;
    private OreTrackerManager oreTrackerManager;
    private TeamScenarioManager teamScenarioManager;
    private VoidTeamsAPI api;
    private TeamAdminGui teamAdminGui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        teamsData = new TeamsData();
        soundManager = new SoundManager(this);
        teamManager = new TeamManager(this);
        randomTeamManager = new RandomTeamManager(this);
        voteTeamManager = new VoteTeamManager(this);
        teamInventoryManager = new TeamInventoryManager(this);
        oreTrackerManager = new OreTrackerManager(this);
        teamScenarioManager = new TeamScenarioManager(this);
        teamAdminGui = new TeamAdminGui(this);
        api = new VoidTeamsAPIImpl(this);
        getServer().getServicesManager().register(VoidTeamsAPI.class, api, this, ServicePriority.Normal);
        VoidTeamsProvider.register(api);

        registerCommands();
        getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);
        getServer().getPluginManager().registerEvents(teamInventoryManager, this);
        getServer().getPluginManager().registerEvents(oreTrackerManager, this);
        getServer().getPluginManager().registerEvents(teamAdminGui, this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new VoidTeamsExpansion(this).register();
            getLogger().info("PlaceholderAPI detectado: expansion voidteams registrada.");
        }

        getLogger().info("=================================================");
        getLogger().info(" VoidTeams " + getPluginMeta().getVersion() + " habilitado");
        getLogger().info(" RGB Adventure + Team system listo para UHC");
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        if (api != null) {
            getServer().getServicesManager().unregister(VoidTeamsAPI.class, api);
            VoidTeamsProvider.unregister(api);
        }
        if (voteTeamManager != null) {
            voteTeamManager.shutdown();
        }
        if (teamScenarioManager != null) {
            teamScenarioManager.shutdown();
        }

        if (getConfig().getBoolean("clear-teams-on-stop", false) && teamManager != null) {
            teamManager.clearAllTeamsConsole();
            if (oreTrackerManager != null) oreTrackerManager.clearAll();
            getLogger().info("Equipos y estadísticas temporales eliminados por clear-teams-on-stop.");
        } else {
            if (teamInventoryManager != null) teamInventoryManager.saveAll();
            if (oreTrackerManager != null) oreTrackerManager.saveAll();
        }

        saveConfig();
        getLogger().info("VoidTeams deshabilitado correctamente.");
    }

    private void registerCommands() {
        TeamTabCompleter tabCompleter = new TeamTabCompleter(this);

        PluginCommand team = requireCommand("team");
        team.setExecutor(new TeamCommands(this));
        team.setTabCompleter(tabCompleter);

        PluginCommand teamAdmin = requireCommand("teamadmin");
        teamAdmin.setExecutor(new TeamAdminCommands(this));
        teamAdmin.setTabCompleter(tabCompleter);

        PluginCommand vote = requireCommand("vote");
        vote.setExecutor(new VoteCommand(this));

        TeamUtilityCommands utilities = new TeamUtilityCommands(this);
        requireCommand("ti").setExecutor(utilities);
        requireCommand("tl").setExecutor(utilities);
        requireCommand("mores").setExecutor(utilities);
    }

    private PluginCommand requireCommand(String name) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            throw new IllegalStateException("El comando /" + name + " no existe en plugin.yml");
        }
        return command;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public RandomTeamManager getRandomTeamManager() {
        return randomTeamManager;
    }

    public TeamsData getTeamsData() {
        return teamsData;
    }

    public VoteTeamManager getVoteTeamManager() {
        return voteTeamManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public TeamInventoryManager getTeamInventoryManager() {
        return teamInventoryManager;
    }

    public OreTrackerManager getOreTrackerManager() {
        return oreTrackerManager;
    }

    public TeamScenarioManager getTeamScenarioManager() {
        return teamScenarioManager;
    }

    public VoidTeamsAPI getAPI() {
        return api;
    }

    public TeamAdminGui getTeamAdminGui() {
        return teamAdminGui;
    }
}
