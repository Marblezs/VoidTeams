package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
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
        if (!(sender instanceof Player)) {
            ChatUtil.msg(sender, "&cSolo jugadores :p");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            ChatUtil.msg(player, "&cUso correcto: /vote <numero>");
            return true;
        }
        try {
            int optionNumber = Integer.parseInt(args[0]);
            plugin.getVoteTeamManager().castVote(player, optionNumber);
            
        } catch (NumberFormatException e) {
            ChatUtil.msg(player, "&cPor favor ingresa un numero. Ejemplo: /vote 2");
        }

        return true;
    }
}