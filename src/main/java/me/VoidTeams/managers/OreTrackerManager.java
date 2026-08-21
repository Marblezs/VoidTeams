package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class OreTrackerManager implements Listener {

    private final VoidTeams plugin;
    private final Map<UUID, EnumMap<OreType, Integer>> mined = new HashMap<>();
    private final Set<BlockKey> placedOres = new HashSet<>();
    private final File dataFile;
    private final YamlConfiguration data;

    public OreTrackerManager(VoidTeams plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "team-ores.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (OreType.from(event.getBlockPlaced().getType()) == null) return;
        placedOres.add(BlockKey.of(event.getBlockPlaced().getWorld(),
                event.getBlockPlaced().getX(), event.getBlockPlaced().getY(), event.getBlockPlaced().getZ()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        OreType type = OreType.from(event.getBlock().getType());
        if (type == null) return;

        BlockKey key = BlockKey.of(event.getBlock().getWorld(),
                event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());

        if (placedOres.remove(key)) return;

        mined.computeIfAbsent(event.getPlayer().getUniqueId(), ignored -> new EnumMap<>(OreType.class))
                .merge(type, 1, Integer::sum);
    }

    public void sendReport(Player player) {
        if (plugin.getTeamManager().isChatLocked()) {
            ChatUtil.msg(player, "<#FF5C5C>El chat de equipo está bloqueado por el host.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            ChatUtil.msg(player, "<#FF5C5C>Debes estar en un equipo para usar /mores.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        EnumMap<OreType, Integer> values = mined.getOrDefault(player.getUniqueId(), new EnumMap<>(OreType.class));
        int total = values.values().stream().mapToInt(Integer::intValue).sum();
        int others = count(values, OreType.COAL) + count(values, OreType.COPPER)
                + count(values, OreType.QUARTZ) + count(values, OreType.ANCIENT_DEBRIS);
        TeamTheme theme = plugin.getTeamManager().getTheme(team);

        String message = "<" + theme.hexColor() + "><bold>[" + theme.icon() + "] ORES</bold></" + theme.hexColor() + "> " +
                "<white>" + player.getName() + "</white> <dark_gray>»</dark_gray> " +
                "<#7DD3FC>Diamante " + count(values, OreType.DIAMOND) + "</#7DD3FC> <dark_gray>•</dark_gray> " +
                "<#FFD166>Oro " + count(values, OreType.GOLD) + "</#FFD166> <dark_gray>•</dark_gray> " +
                "<#D1D5DB>Hierro " + count(values, OreType.IRON) + "</#D1D5DB> <dark_gray>•</dark_gray> " +
                "<#38BDF8>Lapislázuli " + count(values, OreType.LAPIS) + "</#38BDF8> <dark_gray>•</dark_gray> " +
                "<#EF4444>Redstone " + count(values, OreType.REDSTONE) + "</#EF4444> <dark_gray>•</dark_gray> " +
                "<#34D399>Esmeralda " + count(values, OreType.EMERALD) + "</#34D399> <dark_gray>•</dark_gray> " +
                "<gray>Otros " + others + "</gray> " +
                "<gray>(Total: <white>" + total + "</white>)</gray>";

        plugin.getTeamManager().sendToTeam(team, message);
        plugin.getSoundManager().broadcastToTeam(team, SoundManager.SoundType.TEAM_ORES);
    }

    public int getTotal(UUID uuid) {
        EnumMap<OreType, Integer> values = mined.get(uuid);
        return values == null ? 0 : values.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void saveAll() {
        data.set("players", null);
        for (Map.Entry<UUID, EnumMap<OreType, Integer>> entry : mined.entrySet()) {
            String base = "players." + entry.getKey();
            for (Map.Entry<OreType, Integer> ore : entry.getValue().entrySet()) {
                data.set(base + "." + ore.getKey().name().toLowerCase(Locale.ROOT), ore.getValue());
            }
        }
        saveFile();
    }

    public void clearAll() {
        mined.clear();
        placedOres.clear();
        data.set("players", null);
        saveFile();
    }

    private int count(EnumMap<OreType, Integer> values, OreType type) {
        return values.getOrDefault(type, 0);
    }

    private void load() {
        ConfigurationSection players = data.getConfigurationSection("players");
        if (players == null) return;

        for (String uuidText : players.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidText);
                ConfigurationSection section = players.getConfigurationSection(uuidText);
                if (section == null) continue;

                EnumMap<OreType, Integer> stats = new EnumMap<>(OreType.class);
                for (OreType type : OreType.values()) {
                    int value = section.getInt(type.name().toLowerCase(Locale.ROOT), 0);
                    if (value > 0) stats.put(type, value);
                }
                if (!stats.isEmpty()) mined.put(uuid, stats);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("UUID inválido en team-ores.yml: " + uuidText);
            }
        }
    }

    private void saveFile() {
        try {
            data.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("No se pudo guardar team-ores.yml: " + exception.getMessage());
        }
    }

    private enum OreType {
        DIAMOND,
        GOLD,
        IRON,
        LAPIS,
        REDSTONE,
        EMERALD,
        COAL,
        COPPER,
        QUARTZ,
        ANCIENT_DEBRIS;

        static OreType from(Material material) {
            return switch (material) {
                case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> DIAMOND;
                case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> GOLD;
                case IRON_ORE, DEEPSLATE_IRON_ORE -> IRON;
                case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> LAPIS;
                case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> REDSTONE;
                case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> EMERALD;
                case COAL_ORE, DEEPSLATE_COAL_ORE -> COAL;
                case COPPER_ORE, DEEPSLATE_COPPER_ORE -> COPPER;
                case NETHER_QUARTZ_ORE -> QUARTZ;
                case ANCIENT_DEBRIS -> ANCIENT_DEBRIS;
                default -> null;
            };
        }
    }

    private record BlockKey(UUID world, int x, int y, int z) {
        static BlockKey of(World world, int x, int y, int z) {
            return new BlockKey(world.getUID(), x, y, z);
        }
    }
}
