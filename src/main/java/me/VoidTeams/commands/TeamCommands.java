package me.VoidTeams.commands;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.scenarios.AuctionScenario;
import me.VoidTeams.scenarios.CaptainsScenario;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class TeamCommands implements CommandExecutor {

    private final VoidTeams plugin;

    public TeamCommands(VoidTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            ChatUtil.msg(sender, "<#FF5C5C>Este comando solo puede usarlo un jugador.</#FF5C5C>");
            return true;
        }
        if (!player.hasPermission("voidteams.member")) {
            ChatUtil.msg(player, "<#FF5C5C>No tienes permiso para usar VoidTeams.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "invite" -> {
                if (args.length < 2) {
                    ChatUtil.msg(player, "<gray>Uso:</gray> <#22D3EE>/team invite [jugador]</#22D3EE>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    ChatUtil.msg(player, "<#FF5C5C>Jugador no encontrado o desconectado.</#FF5C5C>");
                    return true;
                }
                plugin.getTeamManager().invitePlayer(player, target);
            }
            case "accept" -> {
                if (args.length < 2) {
                    ChatUtil.msg(player, "<gray>Uso:</gray> <#22D3EE>/team accept [jugador]</#22D3EE>");
                    return true;
                }
                Player leader = Bukkit.getPlayerExact(args[1]);
                if (leader == null) {
                    ChatUtil.msg(player, "<#FF5C5C>El jugador que te invitó ya no está conectado.</#FF5C5C>");
                    return true;
                }
                plugin.getTeamManager().acceptInvite(player, leader);
            }
            case "leave" -> plugin.getTeamManager().leaveTeam(player);
            case "color" -> plugin.getTeamManager().setRandomColor(player, player);
            case "icon", "icono" -> plugin.getTeamManager().setRandomIcon(player, player);
            case "chat", "tc" -> toggleTeamChat(player);
            case "pick" -> {
                if (args.length < 2) {
                    ChatUtil.msg(player, "<gray>Uso:</gray> <#22D3EE>/team pick [jugador]</#22D3EE>");
                    return true;
                }
                CaptainsScenario captains = plugin.getTeamScenarioManager().get("captains", CaptainsScenario.class);
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    ChatUtil.msg(player, "<#FF5C5C>Jugador no encontrado o desconectado.</#FF5C5C>");
                    return true;
                }
                if (captains == null) {
                    ChatUtil.msg(player, "<#FF5C5C>Captains no está disponible.</#FF5C5C>");
                    return true;
                }
                captains.pick(player, target);
            }
            case "bid" -> {
                if (args.length < 2) {
                    ChatUtil.msg(player, "<gray>Uso:</gray> <#22D3EE>/team bid [créditos]</#22D3EE>");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    ChatUtil.msg(player, "<#FF5C5C>La puja debe ser un número entero.</#FF5C5C>");
                    return true;
                }
                AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
                if (auction == null) {
                    ChatUtil.msg(player, "<#FF5C5C>Auction no está disponible.</#FF5C5C>");
                    return true;
                }
                auction.bid(player, amount);
            }
            case "info" -> {
                Player target = player;
                if (args.length >= 2 && player.hasPermission("voidteams.admin")) {
                    Player found = Bukkit.getPlayerExact(args[1]);
                    if (found != null) target = found;
                }
                plugin.getTeamManager().showTeamInfo(player, target);
            }
            default -> {
                ChatUtil.msg(player, "<#FF5C5C>Subcomando desconocido.</#FF5C5C> <gray>Usa</gray> <#22D3EE>/team</#22D3EE><gray>.</gray>");
                plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            }
        }
        return true;
    }

    private void toggleTeamChat(Player player) {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team == null) {
            ChatUtil.msg(player, "<#FF5C5C>Debes estar en un equipo para usar el chat de equipo.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }
        if (plugin.getTeamManager().isChatLocked()) {
            ChatUtil.msg(player, "<#FF5C5C>El chat de equipo está bloqueado por el host.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        boolean enabled = plugin.getTeamsData().toggleChat(player.getUniqueId());
        ChatUtil.msg(player,
                enabled
                        ? "<#6BCB77>Chat de equipo activado.</#6BCB77> <gray>Ahora tus mensajes solo los verá tu equipo.</gray>"
                        : "<#FFB347>Chat de equipo desactivado.</#FFB347> <gray>Volviste al chat global.</gray>");
        ChatUtil.sendActionBar(player,
                enabled
                        ? "<#6BCB77>● TEAM CHAT</#6BCB77> <dark_gray>»</dark_gray> <white>ACTIVO</white>"
                        : "<#FFB347>○ TEAM CHAT</#FFB347> <dark_gray>»</dark_gray> <white>GLOBAL</white>");
        plugin.getSoundManager().play(player, SoundManager.SoundType.SUCCESS);
    }

    private void sendHelp(Player player) {
        ChatUtil.msgNoPrefix(player, "<dark_gray>━━━━━━━━━━━━</dark_gray> <#8B5CF6><bold>VOID</bold></#8B5CF6><#22D3EE><bold>TEAMS</bold></#22D3EE> <dark_gray>━━━━━━━━━━━━</dark_gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/team invite [jugador]</#22D3EE> <dark_gray>•</dark_gray> <gray>Invitar jugador</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/team accept [jugador]</#22D3EE> <dark_gray>•</dark_gray> <gray>Aceptar invitación</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/team leave</#22D3EE> <dark_gray>•</dark_gray> <gray>Salir del equipo</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/team chat</#22D3EE> <dark_gray>•</dark_gray> <gray>Alternar chat privado</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/team info</#22D3EE> <dark_gray>•</dark_gray> <gray>Ver miembros y estilo</gray>");
        if (plugin.getTeamScenarioManager().isEnabled("captains")) {
            ChatUtil.msgNoPrefix(player, "<#FFD166>/team pick [jugador]</#FFD166> <dark_gray>•</dark_gray> <gray>Elegir jugador en Captains</gray>");
        }
        if (plugin.getTeamScenarioManager().isEnabled("auction")) {
            ChatUtil.msgNoPrefix(player, "<#FFD166>/team bid [créditos]</#FFD166> <dark_gray>•</dark_gray> <gray>Pujar durante Auction</gray>");
        }
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/team color</#22D3EE> <dark_gray>•</dark_gray> <gray>Nuevo color RGB aleatorio</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/team icon</#22D3EE> <dark_gray>•</dark_gray> <gray>Nuevo icono aleatorio</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/ti</#22D3EE> <dark_gray>•</dark_gray> <gray>Abrir inventario compartido del equipo</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/tl</#22D3EE> <dark_gray>•</dark_gray> <gray>Compartir tus coordenadas</gray>");
        ChatUtil.msgNoPrefix(player, "<#22D3EE>/mores</#22D3EE> <dark_gray>•</dark_gray> <gray>Compartir minerales picados</gray>");
        ChatUtil.msgNoPrefix(player, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>");
    }
}
