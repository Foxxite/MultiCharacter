package com.foxxite.multicharacter.inventories;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.Character;
import com.foxxite.multicharacter.misc.Common;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SpawnLocationSelector implements InventoryHolder, Listener {

    private final MultiCharacter plugin;
    private final Player player;
    private final Character character;
    private final FileConfiguration config;
    private final Language language;
    private final Inventory guiInventory;

    private final NamespacedKey namespacedKey;

    public SpawnLocationSelector(final MultiCharacter plugin, final Player player, final Character character) {

        this.plugin = plugin;
        this.player = player;
        this.character = character;

        this.config = plugin.getConfiguration();
        this.language = plugin.getLanguage();

        if (player.isDead())
            player.spigot().respawn();

        this.namespacedKey = new NamespacedKey(plugin, "spawnLocation");

        this.guiInventory = Bukkit.createInventory(this, 54, "Select Spawn Location");

        final ConfigurationSection locations = this.config.getConfigurationSection("spawn-locations");
        final Set<String> locationKeys = locations.getKeys(false);

        for (final String locationKey : locationKeys) {
            this.guiInventory.addItem(this.getSpawnLocationItem(locationKey));
        }
        this.guiInventory.addItem(this.getLastLocationItem(character));

        Bukkit.getPluginManager().registerEvents(this, plugin);

        player.openInventory(this.guiInventory);

    }

    private ItemStack getLastLocationItem(final Character character) {
        final ItemStack lastLocationItem = new ItemStack(Material.WHITE_BED, 1);
        final ItemMeta lastLocationItemMeta = lastLocationItem.getItemMeta();

        lastLocationItemMeta.setDisplayName(Common.colorize("&6Logout Location"));

        final PersistentDataContainer container = lastLocationItemMeta.getPersistentDataContainer();
        container.set(this.namespacedKey, PersistentDataType.STRING, Common.getLocationAsString(character.getLogoutLocation()));

        lastLocationItem.setItemMeta(lastLocationItemMeta);

        return lastLocationItem;
    }

    private ItemStack getSpawnLocationItem(final String locationKey) {

        final String itemDataKey = "spawn-locations." + locationKey + ".";

        final Material material = Material.getMaterial(this.config.getString(itemDataKey + "icon"));
        final String name = this.config.getString(itemDataKey + "name");
        final Location location = new Location(
                Bukkit.getWorld(this.config.getString(itemDataKey + "world")),
                this.config.getDouble(itemDataKey + "x"),
                this.config.getDouble(itemDataKey + "y"),
                this.config.getDouble(itemDataKey + "z"),
                this.config.getInt(itemDataKey + "yaw"),
                this.config.getInt(itemDataKey + "pitch")
        );

        final ItemStack locationItem = new ItemStack(material, 1);
        final ItemMeta locationItemMeta = locationItem.getItemMeta();
        locationItemMeta.setDisplayName(Common.colorize(name));

        final PersistentDataContainer container = locationItemMeta.getPersistentDataContainer();
        container.set(this.namespacedKey, PersistentDataType.STRING, Common.getLocationAsString(location));

        locationItem.setItemMeta(locationItemMeta);

        return locationItem;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(final InventoryClickEvent event) {

        final Player player = (Player) event.getWhoClicked();
        final ItemStack clickedItem = event.getCurrentItem();

        if (event.getClickedInventory() == this.guiInventory) {

            event.setCancelled(true);

            final ItemMeta meta = clickedItem.getItemMeta();

            final PersistentDataContainer container = meta.getPersistentDataContainer();
            final String locationString = container.get(this.namespacedKey, PersistentDataType.STRING);

            final Location spawnLocation = Common.getLocationFromString(locationString);

            if (!player.getLocation().getWorld().equals(spawnLocation.getWorld()))
                player.teleport(spawnLocation);

            this.plugin.getAnimateToLocation().put(this.player.getUniqueId(), spawnLocation);
        }

    }


    @NotNull
    @Override
    public Inventory getInventory() {
        return this.guiInventory;
    }
}
