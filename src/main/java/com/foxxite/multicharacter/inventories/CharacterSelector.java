package com.foxxite.multicharacter.inventories;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.configs.Language;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.UUID;

public class CharacterSelector implements InventoryHolder, Listener {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final Language language;
    private final Player player;
    private final Inventory selectorGui;
    private final ItemStack[] currPlayerInventory;
    private boolean canClose = false;
    private Location playerLoginLocation;

    public CharacterSelector(final MultiCharacter plugin, final Player player) {

        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.language = plugin.getLanguage();
        this.player = player;

        this.currPlayerInventory = player.getInventory().getContents();
        player.getInventory().clear();

        this.selectorGui = Bukkit.createInventory(this, 9, "Character Selection");

        this.openGuiForPlayer();

        this.populateGUI();
    }

    private void openGuiForPlayer() {
        this.playerLoginLocation = this.player.getLocation();

        final double x = this.config.getDouble("menu-location.x");
        final double y = this.config.getDouble("menu-location.y");
        final double z = this.config.getDouble("menu-location.z");
        final float pitch = 90;
        final float yaw = 0;
        final String world = this.config.getString("menu-location.world");

        final Location menuLocation = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

        this.player.teleport(menuLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        this.player.setFlying(true);
        this.player.setGameMode(GameMode.SPECTATOR);

        this.player.openInventory(this.getInventory());
    }

    private ItemStack getCharacterSelector(final UUID skin, final Character character) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(skin));
        skull.setItemMeta(meta);

        final String characterName = "";
        meta.setDisplayName(characterName);

        final HashMap<String, String> placeholdersLore = new HashMap<>();
        placeholdersLore.put("{birthday}", "");
        placeholdersLore.put("{nationality}", "");
        meta.setLore(this.language.getMultiLineMessageCustom("character-selection.character.lore", placeholdersLore));

        return skull;
    }

    private void populateGUI() {

        final ItemStack characterSkull = new ItemStack(Material.PLAYER_HEAD, 1);
        this.selectorGui.setItem(1, characterSkull);
        this.selectorGui.setItem(4, characterSkull);
        this.selectorGui.setItem(7, characterSkull);

        final ItemStack staffMode = new ItemStack(Material.CREEPER_HEAD, 1);
        this.selectorGui.setItem(8, staffMode);
    }

    void teleportToSpawnLocation(final Location spawnLocation) {
        this.canClose = true;
        this.player.closeInventory();
        this.player.setGameMode(GameMode.CREATIVE);
        this.player.teleport(spawnLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        this.player.getInventory().setContents(this.currPlayerInventory);
        this.player.setFlying(false);
    }

    @Override
    public Inventory getInventory() {
        return this.selectorGui;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClose(final InventoryCloseEvent event) {
        final Inventory inventory = event.getInventory();
        final Player player = (Player) event.getPlayer();

        if (inventory == this.selectorGui && !this.canClose) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                player.openInventory(this.selectorGui);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();

        if (inventory == this.selectorGui) {
            event.setCancelled(true);

            if (event.getSlot() == 8) {
                this.teleportToSpawnLocation(this.playerLoginLocation);
            }
        }
    }
}
