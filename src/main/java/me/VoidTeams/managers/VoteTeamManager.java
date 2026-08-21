package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class VoteTeamManager {

    private final VoidTeams plugin;
    private final Random random = new Random();

    private boolean voteActive;
    private String currentVoteCategory = "";
    private List<String> currentOptions = new ArrayList<>();
    private final Map<UUID, Integer> playerVotes = new HashMap<>();

    private int timeLeft;
    private BukkitTask voteTask;

    public VoteTeamManager(VoidTeams plugin) {
        this.plugin = plugin;
    }

    public void startVote(CommandSender sender, String category, List<String> options, int durationSeconds) {
        if (voteActive) {
            ChatUtil.msg(sender, "<#FF5C5C>Ya hay una votación activa.</#FF5C5C> <gray>Usa</gray> <#22D3EE>/teamadm vote stop</#22D3EE><gray>.</gray>");
            return;
        }

        if (options == null || options.size() < 2) {
            ChatUtil.msg(sender, "<#FF5C5C>La votación necesita al menos dos opciones.</#FF5C5C>");
            return;
        }

        voteActive = true;
        currentVoteCategory = category.toLowerCase();
        currentOptions = new ArrayList<>(options);
        playerVotes.clear();
        timeLeft = Math.max(5, durationSeconds);

        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < currentOptions.size(); i++) {
            lines.append("<#FFD93D><bold>").append(i + 1).append(".</bold></#FFD93D> <white>")
                    .append(currentOptions.get(i)).append("</white>\n");
        }

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#8B5CF6><bold>NUEVA VOTACIÓN</bold></#8B5CF6> <dark_gray>•</dark_gray> <#22D3EE>" + categoryDisplay() + "</#22D3EE>\n" +
                lines +
                "<gray>Vota con</gray> <#22D3EE><bold>/vote [número]</bold></#22D3EE> <dark_gray>•</dark_gray> <gray>Tiempo:</gray> <white>" + timeLeft + "s</white>\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#8B5CF6><bold>VOTACIÓN</bold></#8B5CF6>",
                "<gray>Usa</gray> <#22D3EE>/vote [número]</#22D3EE>", 5, 35, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.VOTE_START);

        startVoteTask();
    }

    private void startVoteTask() {
        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!voteActive) {
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    stopVote(Bukkit.getConsoleSender());
                    cancel();
                    return;
                }

                String leader = calculateCurrentLeader();
                ChatUtil.sendActionBarToAll(
                        "<#8B5CF6><bold>VOTO</bold></#8B5CF6> <dark_gray>»</dark_gray> " +
                        "<gray>" + categoryDisplay() + "</gray> <dark_gray>•</dark_gray> " +
                        "<white>Ganando:</white> <#22D3EE><bold>" + leader + "</bold></#22D3EE> " +
                        "<dark_gray>•</dark_gray> <#FFD93D>" + timeLeft + "s</#FFD93D>"
                );

                if (shouldTickSound(timeLeft)) {
                    plugin.getSoundManager().broadcast(SoundManager.SoundType.VOTE_TICK);
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopVote(CommandSender sender) {
        if (!voteActive) {
            ChatUtil.msg(sender, "<gray>No hay ninguna votación activa.</gray>");
            return;
        }

        voteActive = false;
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }

        Result result = calculateResult();
        if (result.winner() == null) {
            ChatUtil.broadcastNoPrefix(
                    "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                    "<#FFB347><bold>VOTACIÓN FINALIZADA</bold></#FFB347>\n" +
                    "<gray>No hubo votos. La configuración no fue modificada.</gray>\n" +
                    "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
            );
            plugin.getSoundManager().broadcast(SoundManager.SoundType.ERROR);
            resetVoteData();
            return;
        }

        String tieLine = result.tie()
                ? "\n<#FFB347>Empate:</#FFB347> <gray>el resultado se resolvió aleatoriamente entre las opciones empatadas.</gray>"
                : "";

        ChatUtil.broadcastNoPrefix(
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>\n" +
                "<#6BCB77><bold>VOTACIÓN FINALIZADA</bold></#6BCB77>\n" +
                "<gray>Categoría:</gray> <#22D3EE>" + categoryDisplay() + "</#22D3EE>\n" +
                "<gray>Ganador:</gray> <#FFD93D><bold>" + result.winner() + "</bold></#FFD93D> <dark_gray>(" + result.votes() + " votos)</dark_gray>" +
                tieLine + "\n" +
                "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"
        );
        ChatUtil.titleAll("<#6BCB77><bold>VOTACIÓN TERMINADA</bold></#6BCB77>",
                "<gray>Ganó:</gray> <#FFD93D>" + result.winner() + "</#FFD93D>", 5, 45, 10);
        plugin.getSoundManager().broadcast(SoundManager.SoundType.VOTE_END);

        if (currentVoteCategory.equalsIgnoreCase("type")) {
            plugin.getTeamManager().setTeamType(sender, result.winner());
        } else if (currentVoteCategory.equalsIgnoreCase("size")) {
            try {
                plugin.getTeamManager().setTeamSize(sender, Integer.parseInt(result.winner()));
            } catch (NumberFormatException ex) {
                ChatUtil.broadcast("<#FF5C5C>El TeamSize ganador no era un número válido.</#FF5C5C>");
            }
        }

        resetVoteData();
    }

    public void castVote(Player player, int optionNumber) {
        if (!voteActive) {
            ChatUtil.msg(player, "<#FF5C5C>No hay ninguna votación activa.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        int index = optionNumber - 1;
        if (index < 0 || index >= currentOptions.size()) {
            ChatUtil.msg(player,
                    "<#FF5C5C>Opción inválida.</#FF5C5C> <gray>Elige un número del 1 al " + currentOptions.size() + ".</gray>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        boolean changedVote = playerVotes.containsKey(player.getUniqueId());
        playerVotes.put(player.getUniqueId(), index);
        String option = currentOptions.get(index);

        ChatUtil.msg(player,
                (changedVote ? "<#FFB347>Voto actualizado:</#FFB347> " : "<#6BCB77>Voto registrado:</#6BCB77> ") +
                        "<white><bold>" + option + "</bold></white>");
        ChatUtil.sendActionBar(player,
                "<#6BCB77>✓</#6BCB77> <white>Tu voto:</white> <#22D3EE><bold>" + option + "</bold></#22D3EE>");
        plugin.getSoundManager().play(player, SoundManager.SoundType.VOTE_CAST);
    }

    public boolean isVoteActive() {
        return voteActive;
    }

    public int getTimeLeft() {
        return Math.max(0, timeLeft);
    }

    public String getCurrentLeader() {
        return voteActive ? calculateCurrentLeader() : "Ninguno";
    }

    public void shutdown() {
        voteActive = false;
        if (voteTask != null) {
            voteTask.cancel();
            voteTask = null;
        }
        resetVoteData();
    }

    private String calculateCurrentLeader() {
        if (playerVotes.isEmpty()) return "Sin votos";
        Result result = calculateResult();
        if (result.tie()) return "Empate";
        return result.winner() == null ? "Sin votos" : result.winner();
    }

    private Result calculateResult() {
        if (currentOptions.isEmpty() || playerVotes.isEmpty()) {
            return new Result(null, 0, false);
        }

        int[] counts = new int[currentOptions.size()];
        for (int index : playerVotes.values()) {
            if (index >= 0 && index < counts.length) counts[index]++;
        }

        int max = 0;
        List<Integer> winners = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > max) {
                max = counts[i];
                winners.clear();
                winners.add(i);
            } else if (counts[i] == max && counts[i] > 0) {
                winners.add(i);
            }
        }

        if (max <= 0 || winners.isEmpty()) return new Result(null, 0, false);
        boolean tie = winners.size() > 1;
        int winnerIndex = winners.get(random.nextInt(winners.size()));
        return new Result(currentOptions.get(winnerIndex), max, tie);
    }

    private String categoryDisplay() {
        return currentVoteCategory.equalsIgnoreCase("size") ? "TEAM SIZE" : "TEAM TYPE";
    }

    private boolean shouldTickSound(int seconds) {
        return switch (seconds) {
            case 15, 10, 5, 4, 3, 2, 1 -> true;
            default -> false;
        };
    }

    private void resetVoteData() {
        currentVoteCategory = "";
        currentOptions = new ArrayList<>();
        playerVotes.clear();
        timeLeft = 0;
    }

    private record Result(String winner, int votes, boolean tie) {
    }
}
