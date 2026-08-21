package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand implements CommandExecutor {

    private final VoidTeams plugin;

    public VoteCommand(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.msg(sender, "<#FF5C5C>Solo los jugadores pueden votar.</#FF5C5C>");
            return true;
        }

        if (args.length < 1) {
            ChatUtil.msg(player, "<gray>Uso:</gray> <#22D3EE>/vote [número]</#22D3EE>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return true;
        }

        try {
            int optionNumber = Integer.parseInt(args[0]);
            plugin.getVoteTeamManager().castVote(player, optionNumber);
        } catch (NumberFormatException ex) {
            ChatUtil.msg(player, "<#FF5C5C>Debes ingresar un número.</#FF5C5C> <gray>Ejemplo:</gray> <#22D3EE>/vote 2</#22D3EE>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
        }

        return true;
    }
}
