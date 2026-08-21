package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Locale;

public class SoundManager {

    private final VoidTeams plugin;

    public SoundManager(VoidTeams plugin) {
        this.plugin = plugin;
    }

    public enum SoundType {
        SUCCESS("minecraft:entity.experience_orb.pickup", 0.9f, 1.35f),
        ERROR("minecraft:entity.villager.no", 0.8f, 0.85f),
        INVITE_SENT("minecraft:entity.arrow.shoot", 0.7f, 1.45f),
        INVITE_RECEIVED("minecraft:block.note_block.chime", 1.0f, 1.25f),
        TEAM_CREATED("minecraft:entity.player.levelup", 0.9f, 1.15f),
        TEAM_JOIN("minecraft:entity.experience_orb.pickup", 0.9f, 1.55f),
        TEAM_LEAVE("minecraft:block.note_block.bass", 0.75f, 0.9f),
        TEAM_RANDOMIZED("minecraft:block.amethyst_block.chime", 1.0f, 1.0f),
        TEAM_ADMIN_CHANGE("minecraft:item.goat_horn.sound.0", 0.8f, 1.0f),
        TEAM_CLEAR("minecraft:entity.ender_dragon.growl", 0.55f, 1.35f),
        TEAM_CHAT("minecraft:block.note_block.hat", 0.35f, 1.8f),
        TEAM_INVENTORY_OPEN("minecraft:block.ender_chest.open", 0.65f, 1.15f),
        TEAM_INVENTORY_TOGGLE("minecraft:block.amethyst_block.chime", 0.9f, 1.1f),
        TEAM_LOCATION("minecraft:block.note_block.bit", 0.35f, 1.65f),
        TEAM_ORES("minecraft:block.note_block.xylophone", 0.35f, 1.4f),
        TEAM_SCENARIO_TOGGLE("minecraft:block.amethyst_block.chime", 0.9f, 1.25f),
        SHARED_HEALTH_DAMAGE("minecraft:block.note_block.didgeridoo", 0.45f, 0.8f),
        CAPTAINS_START("minecraft:item.goat_horn.sound.1", 0.8f, 1.0f),
        CAPTAINS_TURN("minecraft:block.note_block.chime", 0.75f, 1.35f),
        CAPTAINS_PICK("minecraft:entity.experience_orb.pickup", 0.8f, 1.55f),
        CAPTAINS_TICK("minecraft:block.note_block.hat", 0.45f, 1.65f),
        CAPTAINS_END("minecraft:ui.toast.challenge_complete", 0.8f, 1.0f),
        AUCTION_START("minecraft:item.goat_horn.sound.2", 0.8f, 1.0f),
        AUCTION_LOT("minecraft:block.note_block.bell", 0.8f, 1.1f),
        AUCTION_BID("minecraft:block.note_block.pling", 0.7f, 1.45f),
        AUCTION_TICK("minecraft:block.note_block.hat", 0.45f, 1.75f),
        AUCTION_SOLD("minecraft:entity.player.levelup", 0.8f, 1.2f),
        AUCTION_END("minecraft:ui.toast.challenge_complete", 0.8f, 1.0f),
        VOTE_START("minecraft:block.note_block.bell", 0.9f, 1.15f),
        VOTE_CAST("minecraft:block.note_block.pling", 0.7f, 1.55f),
        VOTE_TICK("minecraft:block.note_block.hat", 0.55f, 1.7f),
        VOTE_END("minecraft:ui.toast.challenge_complete", 0.8f, 1.0f);

        private final String key;
        private final float volume;
        private final float pitch;

        SoundType(String key, float volume, float pitch) {
            this.key = key;
            this.volume = volume;
            this.pitch = pitch;
        }
    }

    public void play(Player player, SoundType type) {
        if (player == null || type == null) return;

        String path = "sounds." + type.name().toLowerCase(Locale.ROOT);
        if (!plugin.getConfig().getBoolean(path + ".enabled", true)) return;

        String key = plugin.getConfig().getString(path + ".sound", type.key);
        float volume = (float) plugin.getConfig().getDouble(path + ".volume", type.volume);
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", type.pitch);

        if (key == null || key.isBlank()) return;

        try {
            player.playSound(player.getLocation(), key, volume, pitch);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Sonido invalido en " + path + ": " + key);
        }
    }

    public void broadcast(SoundType type) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            play(player, type);
        }
    }

    public void broadcastToTeam(Team team, SoundType type) {
        if (team == null) return;
        for (String entry : team.getEntries()) {
            Player player = Bukkit.getPlayerExact(entry);
            if (player != null) {
                play(player, type);
            }
        }
    }
}
