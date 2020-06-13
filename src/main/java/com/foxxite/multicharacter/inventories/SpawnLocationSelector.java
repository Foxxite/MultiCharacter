package com.foxxite.multicharacter.inventories;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.config.Language;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
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
    private boolean canClose = false;

    public SpawnLocationSelector(MultiCharacter plugin, Player player, Character character) {

        this.plugin = plugin;
        this.player = player;
        this.character = character;

        config = plugin.getConfiguration();
        language = plugin.getLanguage();

        if (player.isDead()) {
            player.spigot().respawn();
        }

        namespacedKey = new NamespacedKey(plugin, "spawnLocation");

        guiInventory = Bukkit.createInventory(this, 54, "Select Spawn Location");

        ConfigurationSection locations = config.getConfigurationSection("spawn-locations");
        Set<String> locationKeys = locations.getKeys(false);

        for (String locationKey : locationKeys) {
            guiInventory.addItem(getSpawnLocationItem(locationKey));
        }
        guiInventory.addItem(getLastLocationItem(character));

        Bukkit.getPluginManager().registerEvents(this, plugin);

        player.openInventory(guiInventory);

    }

    private ItemStack getLastLocationItem(Character character) {
        ItemStack lastLocationItem = new ItemStack(Material.WHITE_BED, 1);
        ItemMeta lastLocationItemMeta = lastLocationItem.getItemMeta();

        lastLocationItemMeta.setDisplayName(Common.colorize("&6Logout Location"));

        PersistentDataContainer container = lastLocationItemMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, Common.getLocationAsString(character.getLogoutLocation()));

        lastLocationItem.setItemMeta(lastLocationItemMeta);

        return lastLocationItem;
    }

    private ItemStack getSpawnLocationItem(String locationKey) {

        String itemDataKey = "spawn-locations." + locationKey + ".";

        Material material = Material.getMaterial(config.getString(itemDataKey + "icon"));
        String name = config.getString(itemDataKey + "name");
        Location location = new Location(
                Bukkit.getWorld(config.getString(itemDataKey + "world")),
                config.getDouble(itemDataKey + "x"),
                config.getDouble(itemDataKey + "y"),
                config.getDouble(itemDataKey + "z"),
                config.getInt(itemDataKey + "yaw"),
                config.getInt(itemDataKey + "pitch")
        );

        ItemStack locationItem = new ItemStack(material, 1);
        ItemMeta locationItemMeta = locationItem.getItemMeta();
        locationItemMeta.setDisplayName(Common.colorize(name));

        PersistentDataContainer container = locationItemMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, Common.getLocationAsString(location));

        locationItem.setItemMeta(locationItemMeta);

        return locationItem;

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (inventory == guiInventory && !canClose) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.openInventory(guiInventory);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(InventoryClickEvent event) {

        if (event.getClickedInventory() == guiInventory) {

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            event.setCancelled(true);

            if (clickedItem == null) {
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();

            PersistentDataContainer container = meta.getPersistentDataContainer();
            String locationString = container.get(namespacedKey, PersistentDataType.STRING);

            Location spawnLocation = Common.getLocationFromString(locationString);

            canClose = true;
            player.closeInventory();

            plugin.getAnimateToLocation().put(this.player.getUniqueId(), spawnLocation);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
        }

    }


    @NotNull
    @Override
    public Inventory getInventory() {
        return guiInventory;
    }
}
