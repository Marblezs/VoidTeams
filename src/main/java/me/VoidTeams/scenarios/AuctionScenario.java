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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class AuctionScenario extends TeamScenario {

    private final List<UUID> captains = new ArrayList<>();
    private final List<UUID> playerPool = new ArrayList<>();
    private final Map<UUID, String> captainTeams = new HashMap<>();
    private final Map<UUID, Integer> credits = new HashMap<>();

    private boolean auctionRunning;
    private UUID currentPlayer;
    private UUID currentBidder;
    private int currentBid;
    private int secondsLeft;
    private BukkitTask auctionTask;
    private boolean previousTeamsLocked;

    public AuctionScenario(VoidTeams plugin) {
        super(plugin,
                "auction",
                "Auction",
                "Capitanes reciben créditos y pujan por jugadores hasta completar sus equipos.",
                Material.GOLD_INGOT);
    }

    @Override
    public Set<String> conflictsWith() {
        return Set.of("captains");
    }

    public boolean isAuctionRunning() {
        return auctionRunning;
    }

    public String getCurrentPlayerName() {
        Player player = currentPlayer == null ? null : Bukkit.getPlayer(currentPlayer);
        return player == null ? "Nadie" : player.getName();
    }

    public String getCurrentBidderName() {
        Player player = currentBidder == null ? null : Bukkit.getPlayer(currentBidder);
        return player == null ? "Nadie" : player.getName();
    }

    public int getCurrentBid() {
        return currentBid;
    }

    public int getSecondsLeft() {
        return Math.max(0, secondsLeft);
    }

    public int getCredits(Player player) {
        return player == null ? 0 : credits.getOrDefault(player.getUniqueId(), 0);
    }

    public void start(CommandSender sender) {
        if (!isActive()) {
            ChatUtil.msg(sender, "<#FF5C5C>Auction debe estar activado antes de iniciar la subasta.</#FF5C5C>");
            return;
        }
        if (auctionRunning) {
            ChatUtil.msg(sender, "<#FFB347>Ya hay una subasta en progreso.</#FFB347>");
            return;
        }

        int teamSize = plugin.getTeamManager().getTeamSize();
        List<Player> players = new ArrayList<>(plugin.getTeamScenarioManager().getFormationPlayers());

        if (teamSize <= 1) {
            ChatUtil.msg(sender, "<#FF5C5C>Auction requiere TeamSize mayor a 1.</#FF5C5C>");
            return;
        }
        if (players.size() < 4) {
            ChatUtil.msg(sender, "<#FF5C5C>Auction necesita al menos 4 jugadores elegibles.</#FF5C5C>");
            return;
        }

        int captainCount = (int) Math.ceil(players.size() / (double) teamSize);
        if (captainCount < 2 || captainCount >= players.size()) {
            ChatUtil.msg(sender, "<#FF5C5C>El TeamSize actual no permite formar al menos dos equipos útiles para Auction.</#FF5C5C>");
            return;
        }

        java.util.Collections.shuffle(players);
        previousTeamsLocked = plugin.getTeamManager().isTeamsLocked();
        plugin.getTeamManager().setTeamsLocked(true);
        plugin.getTeamManager().clearAllTeamsConsole();

        captains.clear();
        playerPool.clear();
        captainTeams.clear();
        credits.clear();
        currentPlayer = null;
        currentBidder = null;
        currentBid = 0;

        int startingCredits = Math.max(1, settingInt("starting-credits", 100));

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i < captainCount) {
                Team team = plugin.getTeamManager().createTeam();
                team.addEntry(player.getName());
                plugin.getTeamManager().updatePlayerDatapackID(player.getName(), team);
                captains.add(player.getUniqueId());
                captainTeams.put(player.getUniqueId(), team.getName());
                credits.put(player.getUniqueId(), startingCredits);
            } else {
                playerPool.add(player.getUniqueId());
            }
        }

        java.util.Collections.shuffle(playerPool);
        auctionRunning = true;

        String captainNames = captains.stream()
                .map(Bukkit::getPlayer)
                .filter(java.util.Objects::nonNull)
                .map(player -> player.getName() + " (" + startingCredits + ")")
                .toList()
                .toString();

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#FFD166><bold>TEAM AUCTION</bold></#FFD166>\n" +
                "<gray>Capitanes:</gray> <white>" + captainNames.substring(1, captainNames.length() - 1) + "</white>\n" +
                "<gray>Usa:</gray> <#22D3EE>/team bid &lt;créditos&gt;</#22D3EE> <dark_gray>•</dark_gray> <gray>Jugadores:</gray> <white>" + playerPool.size() + "</white>\n" +
                "<gray>Puja:</gray> <white>" + settingInt("minimum-opening-bid", 1) + " inicial / +" + settingInt("minimum-increment", 5) + "</white> <dark_gray>•</dark_gray> <gray>Tiempo:</gray> <white>" + settingInt("bid-time-seconds", 15) + "s</white>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#FFD166><bold>AUCTION</bold></#FFD166>",
                "<gray>La subasta de equipos está comenzando.</gray>", 5, 45, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.AUCTION_START);

        nextLot();
    }

    public void stop(CommandSender sender) {
        if (!auctionRunning) {
            ChatUtil.msg(sender, "<gray>No hay una subasta activa.</gray>");
            return;
        }
        stopInternal(false);
        ChatUtil.broadcast("<#FFB347>El host detuvo Auction.</#FFB347> <gray>Los equipos ya formados se conservan.</gray>");
    }

    public void bid(Player bidder, int amount) {
        if (!isActive() || !auctionRunning || currentPlayer == null) {
            ChatUtil.msg(bidder, "<#FF5C5C>No hay una subasta activa.</#FF5C5C>");
            return;
        }
        if (!captains.contains(bidder.getUniqueId())) {
            ChatUtil.msg(bidder, "<#FF5C5C>Solo los capitanes pueden pujar.</#FF5C5C>");
            plugin.getSoundManager().play(bidder, SoundManager.SoundType.ERROR);
            return;
        }

        Team team = teamForCaptain(bidder.getUniqueId());
        if (team == null || team.getSize() >= plugin.getTeamManager().getTeamSize()) {
            ChatUtil.msg(bidder, "<#FF5C5C>Tu equipo ya está completo.</#FF5C5C>");
            return;
        }

        int balance = credits.getOrDefault(bidder.getUniqueId(), 0);
        int minimum = currentBid == 0
                ? Math.max(1, settingInt("minimum-opening-bid", 1))
                : currentBid + Math.max(1, settingInt("minimum-increment", 5));

        if (amount < minimum) {
            ChatUtil.msg(bidder, "<#FF5C5C>La puja mínima es</#FF5C5C> <white>" + minimum + "</white>.");
            return;
        }
        if (amount > balance) {
            ChatUtil.msg(bidder, "<#FF5C5C>No tienes suficientes créditos.</#FF5C5C> <gray>Saldo:</gray> <white>" + balance + "</white>");
            return;
        }

        currentBid = amount;
        currentBidder = bidder.getUniqueId();
        int antiSnipe = Math.max(0, settingInt("anti-snipe-seconds", 5));
        if (secondsLeft < antiSnipe) secondsLeft = antiSnipe;

        Player lot = Bukkit.getPlayer(currentPlayer);
        ChatUtil.broadcastNoPrefix(
                "<#FFD166><bold>AUCTION</bold></#FFD166> <dark_gray>»</dark_gray> " +
                "<white>" + bidder.getName() + "</white> <gray>puja</gray> <#6BCB77><bold>" + amount + "</bold></#6BCB77> " +
                "<gray>por</gray> <#22D3EE>" + (lot == null ? "Jugador" : lot.getName()) + "</#22D3EE>"
        );
        plugin.getSoundManager().broadcast(SoundManager.SoundType.AUCTION_BID);
    }

    private void nextLot() {
        cancelAuctionTask();

        while (!playerPool.isEmpty()) {
            UUID next = playerPool.removeFirst();
            Player player = Bukkit.getPlayer(next);
            if (player != null && player.isOnline()) {
                currentPlayer = next;
                break;
            }
        }

        if (currentPlayer == null) {
            finishAuction();
            return;
        }

        currentBidder = null;
        currentBid = 0;
        secondsLeft = Math.max(5, settingInt("bid-time-seconds", 15));

        Player lot = Bukkit.getPlayer(currentPlayer);
        String lotName = lot == null ? "Jugador" : lot.getName();
        ChatUtil.titleAll("<#FFD166><bold>EN SUBASTA</bold></#FFD166>",
                "<#22D3EE>" + lotName + "</#22D3EE>", 3, 28, 7);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.AUCTION_LOT);

        auctionTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!auctionRunning || currentPlayer == null) {
                cancelAuctionTask();
                return;
            }

            Player current = Bukkit.getPlayer(currentPlayer);
            if (current == null || !current.isOnline()) {
                currentPlayer = null;
                nextLot();
                return;
            }

            String bidderName = getCurrentBidderName();
            ChatUtil.sendActionBarToAll(
                    "<#FFD166><bold>AUCTION</bold></#FFD166> <dark_gray>»</dark_gray> " +
                    "<#22D3EE>" + current.getName() + "</#22D3EE> <dark_gray>•</dark_gray> " +
                    "<gray>Puja:</gray> <#6BCB77>" + currentBid + "</#6BCB77> <gray>(" + bidderName + ")</gray> <dark_gray>•</dark_gray> " +
                    "<white>" + secondsLeft + "s</white>"
            );

            if (secondsLeft <= 0) {
                finishLot();
                return;
            }
            if (secondsLeft == 10 || secondsLeft <= 5) {
                plugin.getSoundManager().broadcast(SoundManager.SoundType.AUCTION_TICK);
            }
            secondsLeft--;
        }, 0L, 20L);
    }

    private void finishLot() {
        cancelAuctionTask();
        if (currentPlayer == null) {
            nextLot();
            return;
        }

        Player lot = Bukkit.getPlayer(currentPlayer);
        UUID winnerId = currentBidder;

        Player winningCaptain = winnerId == null ? null : Bukkit.getPlayer(winnerId);
        if (winnerId == null
                || winningCaptain == null
                || !winningCaptain.isOnline()
                || teamForCaptain(winnerId) == null
                || teamForCaptain(winnerId).getSize() >= plugin.getTeamManager().getTeamSize()) {
            winnerId = chooseFallbackCaptain();
        }

        if (winnerId == null) {
            finishAuction();
            return;
        }

        Team team = teamForCaptain(winnerId);
        Player captain = Bukkit.getPlayer(winnerId);

        if (lot != null && team != null) {
            team.addEntry(lot.getName());
            plugin.getTeamManager().updatePlayerDatapackID(lot.getName(), team);
            plugin.getTeamManager().notifyTeamAssignment(lot, team, false);
        }

        if (currentBidder != null && currentBidder.equals(winnerId)) {
            credits.computeIfPresent(winnerId, (uuid, balance) -> Math.max(0, balance - currentBid));
        }

        if (team != null) {
            TeamTheme theme = plugin.getTeamManager().getTheme(team);
            ChatUtil.broadcastNoPrefix(
                    "<" + theme.hexColor() + "><bold>[" + theme.icon() + "] VENDIDO</bold></" + theme.hexColor() + "> " +
                    "<#22D3EE>" + (lot == null ? "Jugador" : lot.getName()) + "</#22D3EE> <dark_gray>→</dark_gray> " +
                    "<white>" + (captain == null ? "Equipo" : captain.getName()) + "</white> " +
                    "<dark_gray>(</dark_gray><#FFD166>" + (currentBidder == null ? 0 : currentBid) + "</#FFD166><dark_gray>)</dark_gray>"
            );
        }
        plugin.getSoundManager().broadcast(SoundManager.SoundType.AUCTION_SOLD);

        currentPlayer = null;
        currentBidder = null;
        currentBid = 0;
        nextLot();
    }

    private UUID chooseFallbackCaptain() {
        List<UUID> available = captains.stream()
                .filter(uuid -> {
                    Team team = teamForCaptain(uuid);
                    Player captain = Bukkit.getPlayer(uuid);
                    return captain != null && captain.isOnline()
                            && team != null
                            && team.getSize() < plugin.getTeamManager().getTeamSize();
                })
                .toList();

        if (available.isEmpty()) return null;

        int smallestTeam = available.stream()
                .mapToInt(uuid -> teamForCaptain(uuid).getSize())
                .min()
                .orElse(Integer.MAX_VALUE);

        List<UUID> smallest = available.stream()
                .filter(uuid -> teamForCaptain(uuid).getSize() == smallestTeam)
                .toList();

        int bestCredits = smallest.stream()
                .mapToInt(uuid -> credits.getOrDefault(uuid, 0))
                .max()
                .orElse(0);

        List<UUID> finalists = smallest.stream()
                .filter(uuid -> credits.getOrDefault(uuid, 0) == bestCredits)
                .toList();

        return finalists.get(ThreadLocalRandom.current().nextInt(finalists.size()));
    }

    private Team teamForCaptain(UUID captainId) {
        String name = captainTeams.get(captainId);
        return name == null ? null : Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name);
    }

    private void finishAuction() {
        cancelAuctionTask();
        auctionRunning = false;
        plugin.getTeamManager().setTeamsLocked(previousTeamsLocked);
        currentPlayer = null;
        currentBidder = null;
        currentBid = 0;
        secondsLeft = 0;

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#6BCB77><bold>AUCTION COMPLETADO</bold></#6BCB77>\n" +
                "<gray>La formación de equipos por subasta terminó.</gray>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#6BCB77><bold>EQUIPOS LISTOS</bold></#6BCB77>",
                "<gray>Auction ha finalizado.</gray>", 5, 45, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.AUCTION_END);
    }

    private void cancelAuctionTask() {
        if (auctionTask != null) {
            auctionTask.cancel();
            auctionTask = null;
        }
    }

    private void stopInternal(boolean pluginDisable) {
        cancelAuctionTask();
        auctionRunning = false;
        currentPlayer = null;
        currentBidder = null;
        currentBid = 0;
        secondsLeft = 0;
        playerPool.clear();
        if (!pluginDisable) {
            plugin.getTeamManager().setTeamsLocked(previousTeamsLocked);
            captains.clear();
            captainTeams.clear();
            credits.clear();
        }
    }

    @Override
    protected void onDisableScenario() {
        if (auctionRunning) stopInternal(false);
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
                        "<gray>Auction:</gray> " + (auctionRunning ? "<#6BCB77>EN PROGRESO</#6BCB77>" : "<#AAB2BD>En espera</#AAB2BD>") +
                        " <dark_gray>•</dark_gray> <gray>Jugador:</gray> <white>" + getCurrentPlayerName() + "</white>" +
                        " <dark_gray>•</dark_gray> <gray>Puja:</gray> <white>" + currentBid + "</white>" +
                        " <dark_gray>•</dark_gray> <gray>Tiempo:</gray> <white>" + secondsLeft + "s</white>");
                return true;
            }
            case "credits" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen auction credits [cantidad]</#22D3EE>");
                    return true;
                }
                try {
                    int value = Math.max(1, Integer.parseInt(args[1]));
                    plugin.getTeamScenarioManager().set(id(), "starting-credits", value);
                    ChatUtil.msg(sender, "<#6BCB77>Créditos iniciales:</#6BCB77> <white>" + value + "</white>");
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>Debes indicar un número.</#FF5C5C>");
                }
                return true;
            }
            case "bidtime", "bid-time" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen auction bidtime [segundos]</#22D3EE>");
                    return true;
                }
                try {
                    int value = Math.max(5, Integer.parseInt(args[1]));
                    plugin.getTeamScenarioManager().set(id(), "bid-time-seconds", value);
                    ChatUtil.msg(sender, "<#6BCB77>Tiempo por jugador:</#6BCB77> <white>" + value + "s</white>");
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>Debes indicar un número.</#FF5C5C>");
                }
                return true;
            }
            case "increment" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen auction increment [cantidad]</#22D3EE>");
                    return true;
                }
                try {
                    int value = Math.max(1, Integer.parseInt(args[1]));
                    plugin.getTeamScenarioManager().set(id(), "minimum-increment", value);
                    ChatUtil.msg(sender, "<#6BCB77>Incremento mínimo:</#6BCB77> <white>" + value + "</white>");
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>Debes indicar un número.</#FF5C5C>");
                }
                return true;
            }
            case "opening", "openingbid", "opening-bid" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen auction opening [cantidad]</#22D3EE>");
                    return true;
                }
                try {
                    int value = Math.max(1, Integer.parseInt(args[1]));
                    plugin.getTeamScenarioManager().set(id(), "minimum-opening-bid", value);
                    ChatUtil.msg(sender, "<#6BCB77>Puja inicial mínima:</#6BCB77> <white>" + value + "</white>");
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(sender, "<#FF5C5C>Debes indicar un número.</#FF5C5C>");
                }
                return true;
            }
            case "antisnipe", "anti-snipe" -> {
                if (args.length < 2) {
                    ChatUtil.msg(sender, "<gray>Uso:</gray> <#22D3EE>/teamadm scen auction antisnipe [segundos]</#22D3EE>");
                    return true;
                }
                try {
                    int value = Math.max(0, Integer.parseInt(args[1]));
                    plugin.getTeamScenarioManager().set(id(), "anti-snipe-seconds", value);
                    ChatUtil.msg(sender, "<#6BCB77>Anti-snipe:</#6BCB77> <white>" + value + "s</white>");
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
        if (args.length == 1) return List.of("start", "stop", "status", "credits", "bidtime", "opening", "increment", "antisnipe");
        if (args.length == 2 && args[0].equalsIgnoreCase("credits")) return List.of("50", "75", "100", "150", "200");
        if (args.length == 2 && args[0].equalsIgnoreCase("bidtime")) return List.of("10", "15", "20", "30");
        if (args.length == 2 && args[0].equalsIgnoreCase("opening")) return List.of("1", "5", "10", "20");
        if (args.length == 2 && args[0].equalsIgnoreCase("increment")) return List.of("1", "5", "10", "20");
        if (args.length == 2 && args[0].equalsIgnoreCase("antisnipe")) return List.of("0", "3", "5", "10");
        return List.of();
    }

    @Override
    public List<String> statusLines() {
        return List.of(
                "<gray>Subasta:</gray> " + (auctionRunning ? "<#6BCB77>En progreso</#6BCB77>" : "<#AAB2BD>En espera</#AAB2BD>"),
                "<gray>Créditos iniciales:</gray> <white>" + settingInt("starting-credits", 100) + "</white>",
                "<gray>Tiempo por lote:</gray> <white>" + settingInt("bid-time-seconds", 15) + "s</white>",
                "<gray>Puja inicial:</gray> <white>" + settingInt("minimum-opening-bid", 1) + "</white>",
                "<gray>Incremento mínimo:</gray> <white>" + settingInt("minimum-increment", 5) + "</white>",
                "<gray>Anti-snipe:</gray> <white>" + settingInt("anti-snipe-seconds", 5) + "s</white>"
        );
    }
}
