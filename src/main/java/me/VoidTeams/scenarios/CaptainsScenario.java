package me.VoidTeams.scenarios;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CaptainsScenario extends TeamScenario {

    private final List<UUID> captains = new ArrayList<>();
    private final List<UUID> pool = new ArrayList<>();
    private final List<UUID> turnQueue = new ArrayList<>();
    private final Map<UUID, String> captainTeams = new HashMap<>();

    private boolean draftRunning;
    private int turnIndex;
    private int secondsLeft;
    private BukkitTask turnTask;
    private boolean previousTeamsLocked;

    public CaptainsScenario(VoidTeams plugin) {
        super(plugin,
                "captains",
                "Captains",
                "Se eligen capitanes al azar y estos forman los equipos mediante un draft en serpiente.",
                Material.NETHERITE_SWORD);
    }

    @Override
    public Set<String> conflictsWith() {
        return Set.of("auction");
    }

    public boolean isDraftRunning() {
        return draftRunning;
    }

    public String getCurrentCaptainName() {
        UUID uuid = currentCaptain();
        if (uuid == null) return "Nadie";
        Player player = Bukkit.getPlayer(uuid);
        return player == null ? "Desconectado" : player.getName();
    }

    public int getSecondsLeft() {
        return Math.max(0, secondsLeft);
    }

    public int getRemainingPlayers() {
        return pool.size();
    }

    public void start(CommandSender sender) {
        if (!isActive()) {
            ChatUtil.msg(sender, "<#FF5C5C>Captains debe estar activado antes de iniciar el draft.</#FF5C5C>");
            return;
        }
        if (draftRunning) {
            ChatUtil.msg(sender, "<#FFB347>Ya hay un draft de Captains en progreso.</#FFB347>");
            return;
        }

        int teamSize = plugin.getTeamManager().getTeamSize();
        List<Player> players = new ArrayList<>(plugin.getTeamScenarioManager().getFormationPlayers());

        if (teamSize <= 1) {
            ChatUtil.msg(sender, "<#FF5C5C>Captains requiere TeamSize mayor a 1.</#FF5C5C>");
            return;
        }
        if (players.size() < 4) {
            ChatUtil.msg(sender, "<#FF5C5C>Captains necesita al menos 4 jugadores elegibles.</#FF5C5C>");
            return;
        }

        int captainCount = (int) Math.ceil(players.size() / (double) teamSize);
        if (captainCount < 2) {
            ChatUtil.msg(sender, "<#FF5C5C>El TeamSize actual solo formaría un equipo; Captains necesita al menos dos.</#FF5C5C>");
            return;
        }
        captainCount = Math.min(captainCount, players.size());

        Collections.shuffle(players);
        previousTeamsLocked = plugin.getTeamManager().isTeamsLocked();
        plugin.getTeamManager().setTeamsLocked(true);
        plugin.getTeamManager().clearAllTeamsConsole();

        captains.clear();
        pool.clear();
        turnQueue.clear();
        captainTeams.clear();
        turnIndex = 0;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i < captainCount) {
                Team team = plugin.getTeamManager().createTeam();
                team.addEntry(player.getName());
                plugin.getTeamManager().updatePlayerDatapackID(player.getName(), team);
                captains.add(player.getUniqueId());
                captainTeams.put(player.getUniqueId(), team.getName());
            } else {
                pool.add(player.getUniqueId());
            }
        }

        buildSnakeQueue(pool.size());
        draftRunning = true;

        String captainNames = captains.stream()
                .map(Bukkit::getPlayer)
                .filter(java.util.Objects::nonNull)
                .map(Player::getName)
                .toList()
                .toString();

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#FFD166><bold>CAPTAINS DRAFT</bold></#FFD166>\n" +
                "<gray>Capitanes:</gray> <white>" + captainNames.substring(1, captainNames.length() - 1) + "</white>\n" +
                "<gray>Jugadores por elegir:</gray> <#22D3EE>" + pool.size() + "</#22D3EE> <dark_gray>•</dark_gray> <gray>TeamSize:</gray> <white>" + teamSize + "</white>\n" +
                "<gray>Turno:</gray> <white>" + settingInt("pick-time-seconds", 30) + "s</white> <dark_gray>•</dark_gray> <gray>Elegir:</gray> <#22D3EE>/team pick &lt;jugador&gt;</#22D3EE>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#FFD166><bold>CAPTAINS</bold></#FFD166>",
                "<gray>El draft está comenzando.</gray>", 5, 45, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.CAPTAINS_START);

        if (pool.isEmpty()) {
            finishDraft();
            return;
        }
        beginTurn();
    }

    public void stop(CommandSender sender) {
        if (!draftRunning) {
            ChatUtil.msg(sender, "<gray>No hay un draft de Captains activo.</gray>");
            return;
        }
        stopInternal(false);
        ChatUtil.broadcast("<#FFB347>El host detuvo el draft de Captains.</#FFB347> <gray>Los equipos ya formados se conservan.</gray>");
    }

    public void pick(Player captain, Player target) {
        if (!isActive() || !draftRunning) {
            ChatUtil.msg(captain, "<#FF5C5C>No hay un draft de Captains activo.</#FF5C5C>");
            return;
        }

        UUID current = currentCaptain();
        if (current == null || !current.equals(captain.getUniqueId())) {
            ChatUtil.msg(captain, "<#FF5C5C>No es tu turno de elegir.</#FF5C5C>");
            plugin.getSoundManager().play(captain, SoundManager.SoundType.ERROR);
            return;
        }

        if (!pool.contains(target.getUniqueId())) {
            ChatUtil.msg(captain, "<#FF5C5C>Ese jugador no está disponible en el draft.</#FF5C5C>");
            plugin.getSoundManager().play(captain, SoundManager.SoundType.ERROR);
            return;
        }

        performPick(current, target.getUniqueId(), false);
    }

    private void performPick(UUID captainId, UUID targetId, boolean automatic) {
        if (!draftRunning) return;

        Player captain = Bukkit.getPlayer(captainId);
        Player target = Bukkit.getPlayer(targetId);
        String teamName = captainTeams.get(captainId);
        Team team = teamName == null ? null : Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);

        if (team == null || target == null || !target.isOnline() || !pool.remove(targetId)) {
            advanceTurn();
            return;
        }

        team.addEntry(target.getName());
        plugin.getTeamManager().updatePlayerDatapackID(target.getName(), team);
        plugin.getTeamManager().notifyTeamAssignment(target, team, false);

        TeamTheme theme = plugin.getTeamManager().getTheme(team);
        String captainName = captain == null ? "Capitán" : captain.getName();

        ChatUtil.broadcastNoPrefix(
                "<" + theme.hexColor() + "><bold>[" + theme.icon() + "] CAPTAINS</bold></" + theme.hexColor() + "> " +
                "<white>" + captainName + "</white> <dark_gray>→</dark_gray> <#FFD166>" + target.getName() + "</#FFD166>" +
                (automatic ? " <dark_gray>(auto)</dark_gray>" : "")
        );
        plugin.getSoundManager().broadcast(SoundManager.SoundType.CAPTAINS_PICK);
        advanceTurn();
    }

    private void advanceTurn() {
        cancelTurnTask();
        turnIndex++;

        if (pool.isEmpty() || turnIndex >= turnQueue.size()) {
            finishDraft();
            return;
        }
        beginTurn();
    }

    private void beginTurn() {
        if (!draftRunning) return;

        UUID captainId = currentCaptain();
        if (captainId == null) {
            finishDraft();
            return;
        }

        Player captain = Bukkit.getPlayer(captainId);
        if (captain == null || !captain.isOnline()) {
            autoPick(captainId);
            return;
        }

        String teamName = captainTeams.get(captainId);
        Team team = teamName == null ? null : Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
        if (team == null || team.getSize() >= plugin.getTeamManager().getTeamSize()) {
            advanceTurn();
            return;
        }

        secondsLeft = Math.max(5, settingInt("pick-time-seconds", 30));
        TeamTheme theme = plugin.getTeamManager().getTheme(team);

        ChatUtil.title(captain,
                "<" + theme.hexColor() + "><bold>TU TURNO</bold></" + theme.hexColor() + ">",
                "<gray>Usa</gray> <white>/team pick &lt;jugador&gt;</white>", 5, 40, 10);
        plugin.getSoundManager().play(captain, SoundManager.SoundType.CAPTAINS_TURN);

        turnTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!draftRunning) {
                cancelTurnTask();
                return;
            }

            Player currentPlayer = Bukkit.getPlayer(captainId);
            if (currentPlayer == null || !currentPlayer.isOnline()) {
                autoPick(captainId);
                return;
            }

            ChatUtil.sendActionBarToAll(
                    "<#FFD166><bold>CAPTAINS</bold></#FFD166> <dark_gray>»</dark_gray> " +
                    "<white>Turno de " + currentPlayer.getName() + "</white> <dark_gray>•</dark_gray> " +
                    "<#22D3EE>" + secondsLeft + "s</#22D3EE> <dark_gray>•</dark_gray> " +
                    "<gray>Restantes:</gray> <white>" + pool.size() + "</white>"
            );

            if (secondsLeft <= 0) {
                autoPick(captainId);
                return;
            }
            if (secondsLeft == 10 || secondsLeft <= 5) {
                plugin.getSoundManager().play(currentPlayer, SoundManager.SoundType.CAPTAINS_TICK);
            }
            secondsLeft--;
        }, 0L, 20L);
    }

    private void autoPick(UUID captainId) {
        cancelTurnTask();
        if (pool.isEmpty()) {
            finishDraft();
            return;
        }

        List<UUID> available = pool.stream()
                .filter(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    return player != null && player.isOnline();
                })
                .toList();

        if (available.isEmpty()) {
            finishDraft();
            return;
        }

        UUID target = available.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(available.size()));
        performPick(captainId, target, true);
    }

    private void finishDraft() {
        cancelTurnTask();
        draftRunning = false;
        secondsLeft = 0;
        plugin.getTeamManager().setTeamsLocked(previousTeamsLocked);

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#6BCB77><bold>CAPTAINS COMPLETADO</bold></#6BCB77>\n" +
                "<gray>Todos los jugadores disponibles fueron asignados.</gray>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#6BCB77><bold>EQUIPOS LISTOS</bold></#6BCB77>",
                "<gray>El draft de Captains terminó.</gray>", 5, 45, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.CAPTAINS_END);
    }

    private void buildSnakeQueue(int picksNeeded) {
        boolean forward = true;
        int remaining = picksNeeded;

        while (remaining > 0) {
            List<UUID> round = new ArrayList<>(captains);
            if (!forward) Collections.reverse(round);

            for (UUID captain : round) {
                if (remaining <= 0) break;
                turnQueue.add(captain);
                remaining--;
            }
            forward = !forward;
        }
    }

    private UUID currentCaptain() {
        if (!draftRunning || turnIndex < 0 || turnIndex >= turnQueue.size()) return null;
        return turnQueue.get(turnIndex);
    }

    private void cancelTurnTask() {
        if (turnTask != null) {
            turnTask.cancel();
            turnTask = null;
        }
    }

    private void stopInternal(boolean pluginDisable) {
        cancelTurnTask();
        draftRunning = false;
        secondsLeft = 0;
        pool.clear();
        turnQueue.clear();
        turnIndex = 0;
        if (!pluginDisable) {
            plugin.getTeamManager().setTeamsLocked(previousTeamsLocked);
            captains.clear();
            captainTeams.clear();
        }
    }

    @Override
    protected void onDisableScenario() {
        if (draftRunning) stopInternal(false);
    }

    @Override
    public void shutdown() {
        stopInternal(true);
    }

    @Override
    public boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length == 0) return false;
        switch (args[0].toLowerCase()) {
            case "start" -> {
                start(sender);
                return true;
            }
            case "stop" -> {
                stop(sender);
                return true;
            }
            case "status" -> {
                ChatUtil.msg(sender,
                        "<gray>Captains:</gray> " + (draftRunning ? "<#6BCB77>EN PROGRESO</#6BCB77>" : "<#AAB2BD>En espera</#AAB2BD>") +
                        " <dark_gray>•</dark_gray> <gray>Turno:</gray> <white>" + getCurrentCaptainName() + "</white>" +
                        " <dark_gray>•</dark_gray> <gray>Restantes:</gray> <white>" + pool.size() + "</white>");
                return true;
            }
            case "picktime", "pick-time" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen captains picktime [segundos]</#22D3EE>");
                    return true;
                }
                try {
                    int seconds = Math.max(5, Integer.parseInt(args[1]));
                    plugin.getTeamScenarioManager().set(id(), "pick-time-seconds", seconds);
                    ChatUtil.msg(sender, "<#6BCB77>Tiempo por elección:</#6BCB77> <white>" + seconds + "s</white>");
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>Debes indicar un número.</#FF5C5C>");
                }
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return List.of("start", "stop", "status", "picktime");
        if (args.length == 2 && args[0].equalsIgnoreCase("picktime")) return List.of("15", "20", "30", "45", "60");
        return List.of();
    }

    @Override
    public List<String> statusLines() {
        return List.of(
                "<gray>Draft:</gray> " + (draftRunning ? "<#6BCB77>En progreso</#6BCB77>" : "<#AAB2BD>En espera</#AAB2BD>"),
                "<gray>Tiempo por pick:</gray> <white>" + settingInt("pick-time-seconds", 30) + "s</white>",
                "<gray>Jugador actual:</gray> <white>" + getCurrentCaptainName() + "</white>"
        );
    }
}
