package me.VoidTeams.hooks;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.scenarios.AuctionScenario;
import me.VoidTeams.scenarios.CaptainsScenario;
import me.VoidTeams.scenarios.TeamScenario;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoidTeamsExpansion extends PlaceholderExpansion {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final VoidTeams plugin;

    public VoidTeamsExpansion(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "voidteams";
    }

    @Override
    public String getAuthor() {
        return "MarcyWu";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String key = params == null ? "" : params.toLowerCase();

        switch (key) {
            case "type" -> { return plugin.getTeamManager().getTeamTypeDisplay(); }
            case "size" -> { return String.valueOf(plugin.getTeamManager().getTeamSize()); }
            case "teamsize" -> {
                return plugin.getTeamManager().getTeamSize() == 1
                        ? "FFA"
                        : plugin.getTeamManager().getTeamTypeDisplay() + " to " + plugin.getTeamManager().getTeamSize();
            }
            case "teaminventory", "team_inventory" -> {
                return String.valueOf(plugin.getTeamInventoryManager().isEnabled());
            }
            case "teaminventory_state", "team_inventory_state" -> {
                return plugin.getTeamInventoryManager().isEnabled() ? "Activado" : "Desactivado";
            }
            case "teaminventory_display", "team_inventory_display" -> {
                return plugin.getTeamInventoryManager().isEnabled() ? "Team Inventory" : "";
            }
            case "scenarios", "team_scenarios" -> {
                return plugin.getTeamScenarioManager().getActiveDisplay();
            }
            case "scenarios_compact", "team_scenarios_compact" -> {
                return plugin.getTeamScenarioManager().getActiveDisplayCompact();
            }
            case "scenarios_count", "team_scenarios_count" -> {
                return String.valueOf(plugin.getTeamScenarioManager().getActiveScenarios().size());
            }
            case "configs", "team_configs" -> {
                return plugin.getTeamScenarioManager().getConfigDisplay();
            }
            case "config_friendlyfire" -> {
                return plugin.getConfig().getBoolean("friendly-fire", false) ? "On" : "Off";
            }
            case "config_chat" -> {
                return plugin.getTeamManager().isChatLocked() ? "Off" : "On";
            }
            case "config_teams" -> {
                return plugin.getTeamManager().isTeamsLocked() ? "Locked" : "Open";
            }
            case "captains_active" -> {
                CaptainsScenario captains = plugin.getTeamScenarioManager().get("captains", CaptainsScenario.class);
                return String.valueOf(captains != null && captains.isDraftRunning());
            }
            case "captains_turn" -> {
                CaptainsScenario captains = plugin.getTeamScenarioManager().get("captains", CaptainsScenario.class);
                return captains == null ? "Nadie" : captains.getCurrentCaptainName();
            }
            case "captains_time" -> {
                CaptainsScenario captains = plugin.getTeamScenarioManager().get("captains", CaptainsScenario.class);
                return String.valueOf(captains == null ? 0 : captains.getSecondsLeft());
            }
            case "captains_remaining" -> {
                CaptainsScenario captains = plugin.getTeamScenarioManager().get("captains", CaptainsScenario.class);
                return String.valueOf(captains == null ? 0 : captains.getRemainingPlayers());
            }
            case "auction_active" -> {
                AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
                return String.valueOf(auction != null && auction.isAuctionRunning());
            }
            case "auction_player" -> {
                AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
                return auction == null ? "Nadie" : auction.getCurrentPlayerName();
            }
            case "auction_bidder" -> {
                AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
                return auction == null ? "Nadie" : auction.getCurrentBidderName();
            }
            case "auction_bid" -> {
                AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
                return String.valueOf(auction == null ? 0 : auction.getCurrentBid());
            }
            case "auction_time" -> {
                AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
                return String.valueOf(auction == null ? 0 : auction.getSecondsLeft());
            }
            case "vote_active" -> { return String.valueOf(plugin.getVoteTeamManager().isVoteActive()); }
            case "vote_time" -> { return String.valueOf(plugin.getVoteTeamManager().getTimeLeft()); }
            case "vote_leader" -> { return plugin.getVoteTeamManager().getCurrentLeader(); }
        }

        if (key.startsWith("scenario_")) {
            String scenarioKey = key.substring("scenario_".length());

            String suffix = "";
            for (String candidate : List.of("_display", "_description", "_icon", "_state")) {
                if (scenarioKey.endsWith(candidate)) {
                    suffix = candidate;
                    scenarioKey = scenarioKey.substring(0, scenarioKey.length() - candidate.length());
                    break;
                }
            }

            TeamScenario scenario = plugin.getTeamScenarioManager().get(scenarioKey);
            if (scenario == null) return "";

            return switch (suffix) {
                case "_display" -> scenario.isActive() ? scenario.displayName() : "";
                case "_description" -> scenario.description();
                case "_icon" -> scenario.icon().name();
                case "_state" -> scenario.isActive() ? "Activado" : "Desactivado";
                default -> String.valueOf(scenario.isActive());
            };
        }

        if (player == null || player.getName() == null) return "";
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());

        return switch (key) {
            case "team" -> team == null ? "" : plugin.getTeamManager().getTeamPrefixLegacy(team);
            case "team_color", "color" -> team == null ? "" : plugin.getTeamManager().getTeamHex(team);
            case "team_icon", "icon" -> team == null ? "" : plugin.getTeamManager().getTeamIcon(team);
            case "team_count", "count" -> team == null ? "0" : String.valueOf(team.getSize());
            case "has_team" -> String.valueOf(team != null);
            case "chat" -> String.valueOf(plugin.getTeamsData().isChatToggled(player.getUniqueId()));
            case "teaminventory_combat", "team_inventory_combat" -> {
                Player online = player.getPlayer();
                yield online == null ? "false" : String.valueOf(plugin.getTeamInventoryManager().isCombatLocked(online));
            }
            case "mores_total" -> String.valueOf(plugin.getOreTrackerManager().getTotal(player.getUniqueId()));
            case "auction_credits" -> {
                AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
                Player online = player.getPlayer();
                yield auction == null || online == null ? "0" : String.valueOf(auction.getCredits(online));
            }
            default -> {
                if (key.startsWith("member_")) {
                    yield memberPlaceholder(player, team, params);
                }
                yield null;
            }
        };
    }

    private String memberPlaceholder(OfflinePlayer requesterOffline, Team team, String params) {
        if (team == null) return "";

        int index;
        try {
            index = Integer.parseInt(params.substring("member_".length())) - 1;
        } catch (NumberFormatException ex) {
            return "";
        }

        List<String> members = new ArrayList<>(team.getEntries());
        Collections.sort(members);
        if (index < 0 || index >= members.size()) return "";

        String memberName = members.get(index);
        Player member = Bukkit.getPlayerExact(memberName);
        Player requester = requesterOffline.getPlayer();
        boolean online = member != null && member.isOnline();

        String distance = "";
        if (requester != null && online && !requester.getName().equals(memberName)) {
            if (requester.getWorld().equals(member.getWorld())) {
                int blocks = (int) Math.round(requester.getLocation().distance(member.getLocation()));
                distance = legacy(Component.text(" [" + blocks + "m]", TextColor.color(0xFFD93D)));
            } else {
                distance = legacy(Component.text(" [Otro mundo]", TextColor.color(0xFF5C5C)));
            }
        }

        TeamTheme theme = plugin.getTeamManager().getTheme(team);
        TextColor stateColor = TextColor.color(online ? 0x6BCB77 : 0xAAB2BD);
        String name = legacy(Component.text(memberName, stateColor));
        String icon = legacy(Component.text("[" + theme.icon() + "] ", TextColor.fromHexString(theme.hexColor())));

        return "%headsanywhere_head_" + memberName + "%" + icon + name + distance;
    }

    private String legacy(Component component) {
        return LEGACY.serialize(component);
    }
}
