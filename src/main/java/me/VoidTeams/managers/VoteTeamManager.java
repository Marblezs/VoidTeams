package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class VoteTeamManager {

    private final VoidTeams plugin;

    private boolean voteActive = false;
    private String currentVoteCategory = "";
    private List<String> currentOptions = new ArrayList<>();
    private final Map<UUID, Integer> playerVotes = new HashMap<>();

    public VoteTeamManager(VoidTeams plugin) {
        this.plugin = plugin;
    }

    public void startVote(CommandSender sender, String category, List<String> options) {
        if (voteActive) {
            ChatUtil.msg(sender, "&cYa hay una votacion activa. Usa /teamadm vote stop primero.");
            return;
        }

        this.voteActive = true;
        this.currentVoteCategory = category;
        this.currentOptions = new ArrayList<>(options);
        this.playerVotes.clear();

        ChatUtil.broadcastNoPrefix("&8&m--------------------------------");
        ChatUtil.broadcastNoPrefix("&e&lNUEVA VOTACIoN!");
        ChatUtil.broadcastNoPrefix("&7El hoster ha iniciado una votacion de: &a" + category.toUpperCase());
        ChatUtil.broadcastNoPrefix("");

        for (int i = 0; i < currentOptions.size(); i++) {
            ChatUtil.broadcastNoPrefix("&e" + (i + 1) + ". &f" + currentOptions.get(i));
        }

        ChatUtil.broadcastNoPrefix("");
        ChatUtil.broadcastNoPrefix("&7Usa &b/vote <numero> &7para elegir tu preferencia.");
        ChatUtil.broadcastNoPrefix("&8&m--------------------------------");
    }

    public void stopVote(CommandSender sender) {
        if (!voteActive) {
            ChatUtil.msg(sender, "&cNo hay ninguna votacion activa en este momento.");
            return;
        }

        this.voteActive = false;

        int[] voteCounts = new int[currentOptions.size()];
        for (Integer voteIndex : playerVotes.values()) {
            if (voteIndex >= 0 && voteIndex < voteCounts.length) {
                voteCounts[voteIndex]++;
            }
        }
        int winningIndex = 0;
        int maxVotes = -1;

        for (int i = 0; i < voteCounts.length; i++) {
            if (voteCounts[i] > maxVotes) {
                maxVotes = voteCounts[i];
                winningIndex = i;
            }
        }

        String winner = currentOptions.get(winningIndex);

        ChatUtil.broadcastNoPrefix("&8&m--------------------------------");
        ChatUtil.broadcastNoPrefix("&e&lVOTACIoN FINALIZADA!");
        ChatUtil.broadcastNoPrefix("&7Categoria: &a" + currentVoteCategory.toUpperCase());
        ChatUtil.broadcastNoPrefix("&7Resultado ganador: &a&l" + winner + " &8(&e" + maxVotes + " votos&8)");
        ChatUtil.broadcastNoPrefix("&8&m--------------------------------");
        if (currentVoteCategory.equalsIgnoreCase("type")) {
            plugin.getTeamManager().setTeamType(sender, winner);
        } else if (currentVoteCategory.equalsIgnoreCase("size")) {
            try {
                int size = Integer.parseInt(winner);
                plugin.getTeamManager().setTeamSize(sender, size);
            } catch (NumberFormatException e) {
                ChatUtil.broadcast("&cEl tamaño ganador no es un numero. El hoster debera ajustarlo manualmente.");
            }
        }
    }

    public void castVote(Player player, int optionNumber) {
        if (!voteActive) {
            ChatUtil.msg(player, "&cNo hay ninguna votacion activa en este momento.");
            return;
        }

        int index = optionNumber - 1;

        if (index < 0 || index >= currentOptions.size()) {
            ChatUtil.msg(player, "&cOpcion nula. Elige un numero del &e1 al " + currentOptions.size() + "&c.");
            return;
        }

        playerVotes.put(player.getUniqueId(), index);
        ChatUtil.msg(player, "&aHas votado por la opcion: &e" + currentOptions.get(index));
    }

    public boolean isVoteActive() {
        return voteActive;
    }
}