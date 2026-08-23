package me.VoidTeams.gui;

import me.VoidTeams.VoidTeams;
import me.VoidTeams.managers.SoundManager;
import me.VoidTeams.scenarios.AuctionScenario;
import me.VoidTeams.scenarios.CaptainsScenario;
import me.VoidTeams.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TeamAdminGui implements Listener {

    private final VoidTeams plugin;

    private enum Page {
        MAIN,
        SIZE,
        CAPTAINS,
        AUCTION,
        FORMATION
    }

    private static final class AdminHolder implements InventoryHolder {
        private final Page page;
        private Inventory inventory;

        private AdminHolder(Page page) {
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public TeamAdminGui(VoidTeams plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        openMain(player);
    }

    public void openMain(Player player) {
        AdminHolder holder = new AdminHolder(Page.MAIN);
        Inventory inventory = Bukkit.createInventory(holder, 45, ChatUtil.component("<#8B5CF6>VoidTeams <#22D3EE>Host"));
        holder.inventory = inventory;

        fill(inventory);
        inventory.setItem(4, item(Material.NETHER_STAR,
                "<#F5F7FA>Configuración actual",
                "<gray>Modo:</gray> <#22D3EE>" + plugin.getTeamManager().getTeamSizeDisplay() + "</#22D3EE>",
                "<gray>TeamSize:</gray> <white>" + plugin.getTeamManager().getTeamSize() + "</white>",
                "<gray>Tipo base:</gray> <white>" + plugin.getTeamManager().getTeamTypeDisplay() + "</white>",
                "<gray>Jugadores elegibles:</gray> <#6BCB77>" + plugin.getTeamScenarioManager().getFormationPlayers().size() + "</#6BCB77>"));

        inventory.setItem(10, item(Material.COMPASS,
                "<#38BDF8>Tipo de equipos",
                "<gray>Actual:</gray> <white>" + plugin.getTeamManager().getTeamTypeDisplay() + "</white>",
                "",
                "<#6BCB77>Click</#6BCB77> <gray>para cambiar entre Chosen, Random y Vote.</gray>"));

        inventory.setItem(12, item(Material.OAK_SIGN,
                "<#FFD93D>TeamSize",
                "<gray>Actual:</gray> <white>" + plugin.getTeamManager().getTeamSizeDisplay() + "</white>",
                "",
                "<#6BCB77>Click</#6BCB77> <gray>para elegir FFA, To2, To3, etc.</gray>"));

        boolean friendlyFire = plugin.getConfig().getBoolean("friendly-fire", false);
        inventory.setItem(14, toggleItem(Material.IRON_SWORD,
                "<#FF7F50>Friendly Fire", friendlyFire,
                "<gray>Daño entre compañeros.</gray>"));

        boolean chatEnabled = !plugin.getTeamManager().isChatLocked();
        inventory.setItem(16, toggleItem(Material.ECHO_SHARD,
                "<#C4B5FD>Team Chat", chatEnabled,
                "<gray>Permite usar el chat privado de equipo.</gray>"));

        boolean teamsEnabled = !plugin.getTeamManager().isTeamsLocked();
        inventory.setItem(20, toggleItem(Material.REDSTONE_TORCH,
                "<#2DE2E6>Edición de equipos", teamsEnabled,
                "<gray>Permite crear, aceptar y abandonar equipos.</gray>"));

        boolean captainsEnabled = plugin.getTeamScenarioManager().isEnabled("captains");
        inventory.setItem(22, scenarioItem(Material.NETHERITE_SWORD,
                "<#FFD166>Captains", captainsEnabled,
                "<gray>Draft en serpiente dirigido por capitanes.</gray>",
                "<#6BCB77>Click</#6BCB77> <gray>para configurar.</gray>"));

        boolean auctionEnabled = plugin.getTeamScenarioManager().isEnabled("auction");
        inventory.setItem(24, scenarioItem(Material.GOLD_INGOT,
                "<#FFBF00>Auction", auctionEnabled,
                "<gray>Capitanes pujan por jugadores usando créditos.</gray>",
                "<#6BCB77>Click</#6BCB77> <gray>para configurar.</gray>"));

        inventory.setItem(28, scenarioItem(Material.ENDER_CHEST,
                "<#9B5DE5>Team Inventory",
                plugin.getTeamScenarioManager().isEnabled("team_inventory"),
                "<gray>Inventario compartido por equipo.</gray>",
                "<#6BCB77>Click</#6BCB77> <gray>para alternar.</gray>"));

        inventory.setItem(30, scenarioItem(Material.RED_DYE,
                "<#FB7185>Shared Health",
                plugin.getTeamScenarioManager().isEnabled("shared_health"),
                "<gray>Comparte daño y, opcionalmente, curación.</gray>",
                "<#6BCB77>Click</#6BCB77> <gray>para alternar.</gray>"));

        inventory.setItem(32, item(Material.PLAYER_HEAD,
                "<#AAB2BD>Participación en formaciones",
                "<gray>Configura quién entra en Captains/Auction.</gray>",
                "<gray>Hosters:</gray> " + (plugin.getTeamScenarioManager().areHostersIncluded() ? "<#6BCB77>Incluidos" : "<#FFB347>Excluidos"),
                "<gray>Spectators:</gray> " + (plugin.getTeamScenarioManager().areSpectatorsExcluded() ? "<#FFB347>Excluidos" : "<#6BCB77>Incluidos"),
                "",
                "<#6BCB77>Click</#6BCB77> <gray>para configurar.</gray>"));

        inventory.setItem(40, item(Material.BARRIER, "<#FF5C5C>Cerrar", "<gray>Cierra este menú.</gray>"));
        player.openInventory(inventory);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void openSize(Player player) {
        AdminHolder holder = new AdminHolder(Page.SIZE);
        Inventory inventory = Bukkit.createInventory(holder, 45, ChatUtil.component("<#FFD93D>TeamSize <gray>•</gray> <white>VoidTeams"));
        holder.inventory = inventory;
        fill(inventory);

        inventory.setItem(4, item(Material.NAME_TAG,
                "<#F5F7FA>TeamSize actual",
                "<#22D3EE>" + plugin.getTeamManager().getTeamSizeDisplay() + "</#22D3EE>",
                "<gray>Valor:</gray> <white>" + plugin.getTeamManager().getTeamSize() + "</white>"));

        int[] sizes = {1, 2, 3, 4, 5, 6, 8};
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            String display = size == 1 ? "FFA" : "To" + size;
            inventory.setItem(slots[i], item(size == 1 ? Material.BARRIER : Material.PLAYER_HEAD,
                    "<#22D3EE>" + display,
                    plugin.getTeamManager().getTeamSize() == size
                            ? "<#6BCB77>Seleccionado actualmente</#6BCB77>"
                            : "<gray>Click para usar TeamSize " + size + ".</gray>"));
        }

        inventory.setItem(28, item(Material.RED_DYE,
                "<#FF5C5C>-1",
                "<gray>Reduce el TeamSize en uno.</gray>"));
        inventory.setItem(34, item(Material.LIME_DYE,
                "<#6BCB77>+1",
                "<gray>Aumenta el TeamSize en uno.</gray>"));
        inventory.setItem(40, item(Material.ARROW, "<#AAB2BD>Volver", "<gray>Regresa al menú principal.</gray>"));

        player.openInventory(inventory);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void openCaptains(Player player) {
        AdminHolder holder = new AdminHolder(Page.CAPTAINS);
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatUtil.component("<#FFD166>Captains <gray>•</gray> <white>Configuración"));
        holder.inventory = inventory;
        fill(inventory);

        CaptainsScenario captains = plugin.getTeamScenarioManager().get("captains", CaptainsScenario.class);
        boolean active = captains != null && captains.isActive();
        int pickTime = plugin.getTeamScenarioManager().getInt("captains", "pick-time-seconds", 30);

        inventory.setItem(4, item(Material.BOOK,
                "<#FFD166>¿Cómo funciona Captains?",
                "<gray>1.</gray> <white>Se eligen capitanes automáticamente.</white>",
                "<gray>2.</gray> <white>Cada capitán usa /team pick &lt;jugador&gt;.</white>",
                "<gray>3.</gray> <white>El orden usa snake draft.</white>",
                "<gray>4.</gray> <white>Si se acaba el tiempo, se hace auto-pick.</white>",
                "",
                "<gray>TeamSize:</gray> <#22D3EE>" + plugin.getTeamManager().getTeamSizeDisplay() + "</#22D3EE>",
                "<gray>Elegibles:</gray> <#6BCB77>" + plugin.getTeamScenarioManager().getFormationPlayers().size() + "</#6BCB77>"));

        inventory.setItem(11, scenarioItem(Material.LEVER,
                "<#FFD166>Scenario Captains", active,
                "<gray>Activa o desactiva Captains.</gray>",
                "<#6BCB77>Click</#6BCB77> <gray>para alternar.</gray>"));

        inventory.setItem(13, item(Material.CLOCK,
                "<#38BDF8>Tiempo por elección",
                "<gray>Actual:</gray> <white>" + pickTime + "s</white>",
                "",
                "<#6BCB77>Click izquierdo</#6BCB77> <gray>+5s</gray>",
                "<#FF7F50>Click derecho</#FF7F50> <gray>-5s</gray>",
                "<dark_gray>Shift = 15s</dark_gray>"));

        boolean running = captains != null && captains.isDraftRunning();
        inventory.setItem(15, item(running ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                running ? "<#FF5C5C>Detener draft" : "<#6BCB77>Iniciar Captains",
                running
                        ? "<gray>Detiene el draft conservando los equipos ya formados.</gray>"
                        : "<gray>Activa Captains si hace falta e inicia el draft.</gray>"));

        inventory.setItem(31, item(Material.ARROW, "<#AAB2BD>Volver", "<gray>Regresa al menú principal.</gray>"));
        player.openInventory(inventory);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void openAuction(Player player) {
        AdminHolder holder = new AdminHolder(Page.AUCTION);
        Inventory inventory = Bukkit.createInventory(holder, 45, ChatUtil.component("<#FFBF00>Auction <gray>•</gray> <white>Configuración"));
        holder.inventory = inventory;
        fill(inventory);

        AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
        boolean active = auction != null && auction.isActive();

        inventory.setItem(4, item(Material.WRITABLE_BOOK,
                "<#FFBF00>¿Cómo funciona Auction?",
                "<gray>1.</gray> <white>Se eligen capitanes automáticamente.</white>",
                "<gray>2.</gray> <white>Cada capitán recibe créditos.</white>",
                "<gray>3.</gray> <white>Se subasta un jugador por turno.</white>",
                "<gray>4.</gray> <white>Los capitanes usan /team bid &lt;cantidad&gt;.</white>",
                "<gray>5.</gray> <white>La mayor puja gana al jugador.</white>",
                "",
                "<gray>TeamSize:</gray> <#22D3EE>" + plugin.getTeamManager().getTeamSizeDisplay() + "</#22D3EE>",
                "<gray>Elegibles:</gray> <#6BCB77>" + plugin.getTeamScenarioManager().getFormationPlayers().size() + "</#6BCB77>"));

        inventory.setItem(10, scenarioItem(Material.LEVER,
                "<#FFBF00>Scenario Auction", active,
                "<gray>Activa o desactiva Auction.</gray>",
                "<#6BCB77>Click</#6BCB77> <gray>para alternar.</gray>"));

        inventory.setItem(12, settingItem(Material.GOLD_INGOT, "<#FFD166>Créditos iniciales",
                plugin.getTeamScenarioManager().getInt("auction", "starting-credits", 100), "", 25));
        inventory.setItem(14, settingItem(Material.CLOCK, "<#38BDF8>Tiempo por jugador",
                plugin.getTeamScenarioManager().getInt("auction", "bid-time-seconds", 15), "s", 5));
        inventory.setItem(16, settingItem(Material.GOLD_NUGGET, "<#F5B942>Puja inicial mínima",
                plugin.getTeamScenarioManager().getInt("auction", "minimum-opening-bid", 1), "", 1));
        inventory.setItem(28, settingItem(Material.EMERALD, "<#6BCB77>Incremento mínimo",
                plugin.getTeamScenarioManager().getInt("auction", "minimum-increment", 5), "", 1));
        inventory.setItem(30, settingItem(Material.REPEATER, "<#C4B5FD>Anti-snipe",
                plugin.getTeamScenarioManager().getInt("auction", "anti-snipe-seconds", 5), "s", 1));

        boolean running = auction != null && auction.isAuctionRunning();
        inventory.setItem(32, item(running ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                running ? "<#FF5C5C>Detener Auction" : "<#6BCB77>Iniciar Auction",
                running
                        ? "<gray>Detiene la subasta conservando los equipos ya formados.</gray>"
                        : "<gray>Activa Auction si hace falta e inicia la subasta.</gray>"));

        inventory.setItem(40, item(Material.ARROW, "<#AAB2BD>Volver", "<gray>Regresa al menú principal.</gray>"));
        player.openInventory(inventory);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    public void openFormation(Player player) {
        AdminHolder holder = new AdminHolder(Page.FORMATION);
        Inventory inventory = Bukkit.createInventory(holder, 36, ChatUtil.component("<#AAB2BD>Participación <gray>•</gray> <white>Formaciones"));
        holder.inventory = inventory;
        fill(inventory);

        String permission = plugin.getTeamScenarioManager().getFormationHosterPermission();
        inventory.setItem(4, item(Material.BOOK,
                "<#F5F7FA>¿Quién participa?",
                "<gray>Este apartado solo afecta Captains y Auction.</gray>",
                "<gray>No cambia permisos administrativos.</gray>",
                "",
                "<gray>Permiso de rol hoster:</gray>",
                "<#22D3EE>" + permission + "</#22D3EE>",
                "",
                "<gray>Elegibles ahora:</gray> <#6BCB77>" + plugin.getTeamScenarioManager().getFormationPlayers().size() + "</#6BCB77>"));

        inventory.setItem(11, toggleItem(Material.PLAYER_HEAD,
                "<#FFD166>Incluir hosters",
                plugin.getTeamScenarioManager().areHostersIncluded(),
                "<gray>Los jugadores con</gray> <#22D3EE>" + permission + "</#22D3EE>",
                "<gray>participan o quedan fuera de la formación.</gray>"));

        inventory.setItem(15, toggleItem(Material.ENDER_EYE,
                "<#C4B5FD>Excluir spectators",
                plugin.getTeamScenarioManager().areSpectatorsExcluded(),
                "<gray>Evita que jugadores en Spectator entren</gray>",
                "<gray>en Captains o Auction.</gray>"));

        inventory.setItem(31, item(Material.ARROW, "<#AAB2BD>Volver", "<gray>Regresa al menú principal.</gray>"));
        player.openInventory(inventory);
        plugin.getSoundManager().play(player, SoundManager.SoundType.TEAM_ADMIN_CHANGE);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AdminHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!player.hasPermission("voidteams.admin")) {
            player.closeInventory();
            return;
        }
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) return;

        switch (holder.page) {
            case MAIN -> handleMain(player, event.getSlot());
            case SIZE -> handleSize(player, event.getSlot());
            case CAPTAINS -> handleCaptains(player, event);
            case AUCTION -> handleAuction(player, event);
            case FORMATION -> handleFormation(player, event.getSlot());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof AdminHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMain(Player player, int slot) {
        switch (slot) {
            case 10 -> {
                String current = plugin.getTeamManager().getTeamType();
                String next = current.equalsIgnoreCase("Choosen") ? "Random"
                        : current.equalsIgnoreCase("Random") ? "Vote" : "Choosen";
                plugin.getTeamManager().setTeamType(player, next);
                openMain(player);
            }
            case 12 -> openSize(player);
            case 14 -> {
                plugin.getTeamManager().setFriendlyFire(player, !plugin.getConfig().getBoolean("friendly-fire", false));
                openMain(player);
            }
            case 16 -> {
                boolean enabled = plugin.getTeamManager().isChatLocked();
                plugin.getTeamManager().setChatLocked(!enabled);
                plugin.getConfig().set("team-chat-locked", !enabled);
                plugin.saveConfig();
                success(player);
                openMain(player);
            }
            case 20 -> {
                boolean enabled = plugin.getTeamManager().isTeamsLocked();
                if (enabled && plugin.getTeamScenarioManager().isFormationRunning()) {
                    ChatUtil.msg(player, "<#FFB347>No puedes habilitar la edición normal mientras Captains/Auction está en progreso.</#FFB347>");
                    error(player);
                    return;
                }
                plugin.getTeamManager().setTeamsLocked(!enabled);
                plugin.getConfig().set("teams-locked", !enabled);
                plugin.saveConfig();
                success(player);
                openMain(player);
            }
            case 22 -> openCaptains(player);
            case 24 -> openAuction(player);
            case 28 -> {
                plugin.getTeamScenarioManager().toggle("team_inventory", player);
                openMain(player);
            }
            case 30 -> {
                plugin.getTeamScenarioManager().toggle("shared_health", player);
                openMain(player);
            }
            case 32 -> openFormation(player);
            case 40 -> player.closeInventory();
        }
    }

    private void handleFormation(Player player, int slot) {
        switch (slot) {
            case 11 -> {
                boolean include = !plugin.getTeamScenarioManager().areHostersIncluded();
                plugin.getConfig().set("formation.include-hosters", include);
                plugin.saveConfig();
                ChatUtil.msg(player, include
                        ? "<#6BCB77>Los hosters ahora participan en Captains/Auction.</#6BCB77>"
                        : "<#FFB347>Los hosters quedan fuera de Captains/Auction.</#FFB347>");
                success(player);
                openFormation(player);
            }
            case 15 -> {
                boolean exclude = !plugin.getTeamScenarioManager().areSpectatorsExcluded();
                plugin.getConfig().set("formation.exclude-spectators", exclude);
                plugin.saveConfig();
                ChatUtil.msg(player, exclude
                        ? "<#6BCB77>Los spectators quedan fuera de las formaciones.</#6BCB77>"
                        : "<#FFB347>Los spectators pueden participar en las formaciones.</#FFB347>");
                success(player);
                openFormation(player);
            }
            case 31 -> openMain(player);
        }
    }

    private void handleSize(Player player, int slot) {
        int size = switch (slot) {
            case 10 -> 1;
            case 11 -> 2;
            case 12 -> 3;
            case 13 -> 4;
            case 14 -> 5;
            case 15 -> 6;
            case 16 -> 8;
            case 28 -> Math.max(1, plugin.getTeamManager().getTeamSize() - 1);
            case 34 -> plugin.getTeamManager().getTeamSize() + 1;
            default -> -1;
        };

        if (slot == 40) {
            openMain(player);
            return;
        }
        if (size > 0) {
            plugin.getTeamManager().setTeamSize(player, size);
            openSize(player);
        }
    }

    private void handleCaptains(Player player, InventoryClickEvent event) {
        CaptainsScenario captains = plugin.getTeamScenarioManager().get("captains", CaptainsScenario.class);
        switch (event.getSlot()) {
            case 11 -> {
                plugin.getTeamScenarioManager().toggle("captains", player);
                openCaptains(player);
            }
            case 13 -> {
                int step = event.isShiftClick() ? 15 : 5;
                int current = plugin.getTeamScenarioManager().getInt("captains", "pick-time-seconds", 30);
                int updated = event.isRightClick() ? Math.max(5, current - step) : Math.min(180, current + step);
                plugin.getTeamScenarioManager().set("captains", "pick-time-seconds", updated);
                success(player);
                openCaptains(player);
            }
            case 15 -> {
                if (captains == null) return;
                if (captains.isDraftRunning()) {
                    captains.stop(player);
                } else {
                    if (!captains.isActive()) plugin.getTeamScenarioManager().setEnabled("captains", true, player);
                    captains.start(player);
                }
                openCaptains(player);
            }
            case 31 -> openMain(player);
        }
    }

    private void handleAuction(Player player, InventoryClickEvent event) {
        AuctionScenario auction = plugin.getTeamScenarioManager().get("auction", AuctionScenario.class);
        switch (event.getSlot()) {
            case 10 -> {
                plugin.getTeamScenarioManager().toggle("auction", player);
                openAuction(player);
            }
            case 12 -> adjust("auction", "starting-credits", event, 25, 1, 10000, player);
            case 14 -> adjust("auction", "bid-time-seconds", event, 5, 5, 120, player);
            case 16 -> adjust("auction", "minimum-opening-bid", event, 1, 1, 10000, player);
            case 28 -> adjust("auction", "minimum-increment", event, 1, 1, 10000, player);
            case 30 -> adjust("auction", "anti-snipe-seconds", event, 1, 0, 30, player);
            case 32 -> {
                if (auction == null) return;
                if (auction.isAuctionRunning()) {
                    auction.stop(player);
                } else {
                    if (!auction.isActive()) plugin.getTeamScenarioManager().setEnabled("auction", true, player);
                    auction.start(player);
                }
                openAuction(player);
            }
            case 40 -> openMain(player);
        }
    }

    private void adjust(String scenario, String path, InventoryClickEvent event, int step, int min, int max, Player player) {
        int realStep = event.isShiftClick() ? step * 5 : step;
        int current = plugin.getTeamScenarioManager().getInt(scenario, path, min);
        int updated = event.isRightClick() ? current - realStep : current + realStep;
        updated = Math.max(min, Math.min(max, updated));
        plugin.getTeamScenarioManager().set(scenario, path, updated);
        success(player);
        openAuction(player);
    }

    private ItemStack settingItem(Material material, String name, int value, String suffix, int step) {
        return item(material, name,
                "<gray>Actual:</gray> <white>" + value + suffix + "</white>",
                "",
                "<#6BCB77>Click izquierdo</#6BCB77> <gray>+" + step + suffix + "</gray>",
                "<#FF7F50>Click derecho</#FF7F50> <gray>-" + step + suffix + "</gray>",
                "<dark_gray>Shift = x5</dark_gray>");
    }

    private ItemStack toggleItem(Material material, String name, boolean enabled, String... description) {
        List<String> lore = new ArrayList<>(List.of(description));
        lore.add("");
        lore.add(enabled ? "<#6BCB77>● ACTIVADO</#6BCB77>" : "<#FF5C5C>● DESACTIVADO</#FF5C5C>");
        lore.add("<gray>Click para alternar.</gray>");
        return item(material, name, lore.toArray(String[]::new));
    }

    private ItemStack scenarioItem(Material material, String name, boolean enabled, String... description) {
        List<String> lore = new ArrayList<>(List.of(description));
        lore.add("");
        lore.add(enabled ? "<#6BCB77>● ACTIVO</#6BCB77>" : "<#AAB2BD>○ INACTIVO</#AAB2BD>");
        return item(material, name, lore.toArray(String[]::new));
    }

    private ItemStack item(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.displayName(ChatUtil.component(name));
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) lore.add(ChatUtil.component(line));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fill(Inventory inventory) {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
    }

    private void success(Player player) {
        plugin.getSoundManager().play(player, SoundManager.SoundType.SUCCESS);
    }

    private void error(Player player) {
        plugin.getSoundManager().play(player, SoundManager.SoundType.ERROR);
    }
}
