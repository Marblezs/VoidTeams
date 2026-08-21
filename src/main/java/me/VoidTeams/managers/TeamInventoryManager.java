package me.VoidTeams.managers;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.models.TeamTheme;
import me.VoidTeams.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamInventoryManager implements Listener {

    private final VoidTeams plugin;
    private final Map<String, Inventory> inventories = new HashMap<>();
    private final Map<UUID, Long> combatUntil = new HashMap<>();

    private final File dataFile;
    private YamlConfiguration data;

    public TeamInventoryManager(VoidTeams plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "team-inventories.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean isEnabled() {
        if (plugin.getTeamScenarioManager() != null) {
            return plugin.getTeamScenarioManager().isEnabled("team_inventory");
        }
        return plugin.getConfig().getBoolean("team-inventory.enabled", false);
    }

    public int getCombatLockSeconds() {
        int configured = plugin.getTeamScenarioManager() == null
                ? plugin.getConfig().getInt("team-inventory.combat-lock-seconds", 5)
                : plugin.getTeamScenarioManager().getInt("team_inventory", "combat-lock-seconds", 5);
        return Math.max(0, configured);
    }

    public int getInventorySize() {
        int configured = plugin.getTeamScenarioManager() == null
                ? plugin.getConfig().getInt("team-inventory.size", 27)
                : plugin.getTeamScenarioManager().getInt("team_inventory", "size", 27);
        int bounded = Math.max(9, Math.min(54, configured));
        return Math.max(9, (bounded / 9) * 9);
    }

    public void open(Player player) {
        if (!isEnabled()) {
            ChatUtil.msg(player, "<#FF5C5C>Team Inventory está desactivado por el host.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            ChatUtil.msg(player, "<#FF5C5C>Debes pertenecer a un equipo para usar /ti.</#FF5C5C>");
            plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
            return;
        }

        if (isCombatLocked(player)) {
            return;
        }

        Inventory inventory = inventories.computeIfAbsent(team.getName(), ignored -> createSharedInventory(team));
        player.openInventory(inventory);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_INVENTORY_OPEN);
    }

    public boolean openReadOnly(Player viewer, Player target) {
        Team team = plugin.getTeamManager().getTeam(target);
        if (team == null) {
            ChatUtil.msg(viewer, "<#FF5C5C>Ese jugador no pertenece a ningún equipo.</#FF5C5C>");
            plugin.getSoundManager().play(viewer, SoundManager.SoundType.ERROR);
            return false;
        }

        Inventory source = inventories.computeIfAbsent(team.getName(), ignored -> createSharedInventory(team));
        TeamInventoryHolder holder = new TeamInventoryHolder(team.getName(), true);
        TeamTheme theme = plugin.getTeamManager().getTheme(team);
        TextColor color = TextColor.fromHexString(theme.hexColor());
        if (color == null) color = TextColor.color(0x8B5CF6);

        Inventory copy = Bukkit.createInventory(
                holder,
                source.getSize(),
                Component.text("Team Inventory • " + target.getName(), color)
        );
        holder.setInventory(copy);
        copy.setContents(cloneContents(source.getContents(), source.getSize()));

        viewer.openInventory(copy);
        ChatUtil.sendActionBar(viewer,
                "<" + theme.hexColor() + ">" + theme.icon() + "</" + theme.hexColor() + "> <white>Vista de solo lectura del Team Inventory</white>");
        plugin.getSoundManager().play(viewer, SoundManager.SoundType.TEAM_INVENTORY_OPEN);
        return true;
    }

    public void setEnabled(boolean enabled) {
        if (plugin.getTeamScenarioManager() != null) {
            plugin.getTeamScenarioManager().setEnabled("team_inventory", enabled, Bukkit.getConsoleSender());
            return;
        }
        plugin.getConfig().set("team-inventory.enabled", enabled);
        plugin.saveConfig();
        if (!enabled) closeAllSharedInventories();
    }

    public boolean toggle() {
        if (plugin.getTeamScenarioManager() != null) {
            boolean next = !plugin.getTeamScenarioManager().isEnabled("team_inventory");
            plugin.getTeamScenarioManager().setEnabled("team_inventory", next, Bukkit.getConsoleSender());
            return next;
        }
        boolean enabled = !isEnabled();
        setEnabled(enabled);
        return enabled;
    }

    public boolean isCombatLocked(Player player) {
        Long until = combatUntil.get(player.getUniqueId());
        if (until == null) return false;
        if (until <= System.currentTimeMillis()) {
            combatUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public void clearCombatTags() {
        combatUntil.clear();
    }

    public long getCombatMillisRemaining(Player player) {
        Long until = combatUntil.get(player.getUniqueId());
        if (until == null) return 0L;
        long remaining = until - System.currentTimeMillis();
        if (remaining <= 0L) {
            combatUntil.remove(player.getUniqueId());
            return 0L;
        }
        return remaining;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) return;

        long until = System.currentTimeMillis() + (getCombatLockSeconds() * 1000L);
        combatUntil.put(victim.getUniqueId(), until);
        combatUntil.put(attacker.getUniqueId(), until);

        closeIfSharedInventory(victim);
        closeIfSharedInventory(attacker);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TeamInventoryHolder holder)) return;
        if (holder.readOnly()) return;
        saveTeam(holder.teamName(), event.getInventory());
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof TeamInventoryHolder holder)) return;
        if (holder.readOnly()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof TeamInventoryHolder holder)) return;
        if (!holder.readOnly()) return;

        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            event.setCancelled(true);
        }
    }

    public void deleteTeamInventory(String teamName) {
        if (teamName == null || teamName.isBlank()) return;
        inventories.remove(teamName);
        data.set("teams." + teamName, null);
        saveFile();
    }

    public void clearAll() {
        inventories.clear();
        data.set("teams", null);
        saveFile();
    }

    public void saveAll() {
        for (Map.Entry<String, Inventory> entry : inventories.entrySet()) {
            writeInventory(entry.getKey(), entry.getValue());
        }
        saveFile();
    }

    public void rebuildCachedInventories() {
        saveAll();
        closeAllSharedInventories();
        inventories.clear();
    }

    private Inventory createSharedInventory(Team team) {
        int size = getInventorySize();
        TeamInventoryHolder holder = new TeamInventoryHolder(team.getName(), false);
        TeamTheme theme = plugin.getTeamManager().getTheme(team);
        TextColor color = TextColor.fromHexString(theme.hexColor());
        if (color == null) color = TextColor.color(0x8B5CF6);

        Inventory inventory = Bukkit.createInventory(
                holder,
                size,
                Component.text("Team Inventory [" + theme.icon() + "]", color)
        );
        holder.setInventory(inventory);
        inventory.setContents(loadContents(team.getName(), size));
        return inventory;
    }

    private ItemStack[] loadContents(String teamName, int size) {
        ItemStack[] contents = new ItemStack[size];
        List<?> stored = data.getList("teams." + teamName + ".contents");
        if (stored == null) return contents;

        for (int i = 0; i < Math.min(size, stored.size()); i++) {
            Object value = stored.get(i);
            if (value instanceof ItemStack item) {
                contents[i] = item.clone();
            }
        }
        return contents;
    }

    private void saveTeam(String teamName, Inventory inventory) {
        writeInventory(teamName, inventory);
        saveFile();
    }

    private void writeInventory(String teamName, Inventory inventory) {
        List<ItemStack> serialized = new ArrayList<>(inventory.getSize());
        for (ItemStack item : inventory.getContents()) {
            serialized.add(item == null ? null : item.clone());
        }
        data.set("teams." + teamName + ".contents", serialized);
    }

    private void saveFile() {
        try {
            data.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("No se pudo guardar team-inventories.yml: " + exception.getMessage());
        }
    }

    private ItemStack[] cloneContents(ItemStack[] source, int size) {
        ItemStack[] clone = new ItemStack[size];
        for (int i = 0; i < Math.min(source.length, size); i++) {
            clone[i] = source[i] == null ? null : source[i].clone();
        }
        return clone;
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) return player;
        }
        return null;
    }

    private void closeIfSharedInventory(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof TeamInventoryHolder holder
                && !holder.readOnly()) {
            player.closeInventory();
        }
    }

    public void closeAllSharedInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof TeamInventoryHolder holder
                    && !holder.readOnly()) {
                player.closeInventory();
            }
        }
    }

    public static final class TeamInventoryHolder implements InventoryHolder {
        private final String teamName;
        private final boolean readOnly;
        private Inventory inventory;

        public TeamInventoryHolder(String teamName, boolean readOnly) {
            this.teamName = teamName;
            this.readOnly = readOnly;
        }

        public String teamName() {
            return teamName;
        }

        public boolean readOnly() {
            return readOnly;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
