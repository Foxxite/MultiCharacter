package com.foxxite.multicharacter.inventories;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.Character;
import com.foxxite.multicharacter.sql.SQLHandler;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.UUID;

public class CharacterSelector implements InventoryHolder, Listener {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final Language language;
    private final Player player;
    private final Inventory selectorGui;
    private final ItemStack[] currPlayerInventory;
    private final SQLHandler sqlHandler;
    private final NamespacedKey namespacedKey;
    private final Location menuLocation;
    private boolean canClose = false;
    private Location playerLoginLocation;

    public CharacterSelector(final MultiCharacter plugin, final Player player) {

        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.language = plugin.getLanguage();
        this.player = player;
        this.sqlHandler = plugin.getSqlHandler();

        final double x = this.config.getDouble("menu-location.x");
        final double y = this.config.getDouble("menu-location.y");
        final double z = this.config.getDouble("menu-location.z");
        final float pitch = 90;
        final float yaw = 0;
        final String world = this.config.getString("menu-location.world");
        this.menuLocation = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

        this.namespacedKey = new NamespacedKey(plugin, "character");

        this.currPlayerInventory = player.getInventory().getContents();
        player.getInventory().clear();
        this.player.updateInventory();

        this.selectorGui = Bukkit.createInventory(this, 9, "Character Selection");

        this.openGuiForPlayer();

        this.populateGUI();
    }

    private void openGuiForPlayer() {
        this.playerLoginLocation = this.player.getLocation();

        this.player.teleport(this.menuLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        this.player.setAllowFlight(true);
        this.player.setFlying(true);
        this.player.setGameMode(GameMode.SPECTATOR);

        this.player.openInventory(this.getInventory());
    }

    private ItemStack getCharacterSkull(final UUID characterUUID) {

        final Character character = new Character(this.plugin, characterUUID);

        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(character.getSkinUUID())));
        skull.setItemMeta(meta);

        final String characterName = character.getName();
        meta.setDisplayName(characterName);

        final HashMap<String, String> placeholdersLore = new HashMap<>();
        placeholdersLore.put("{birthday}", character.getBirthday());
        placeholdersLore.put("{nationality}", character.getNationality());
        placeholdersLore.put("{sex}", character.getSex());
        meta.setLore(this.language.getMultiLineMessageCustom("character-selection.character.lore", placeholdersLore));

        final PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.namespacedKey, PersistentDataType.STRING, characterUUID.toString());

        skull.setItemMeta(meta);

        return skull;
    }

    private void populateGUI() {

        final ItemStack newCharacter = new ItemStack(Material.LIME_CONCRETE, 1);
        final ItemMeta newCharacterMeta = newCharacter.getItemMeta();

        newCharacterMeta.setDisplayName(this.language.getMessage("character-selection.new-character.name"));
        newCharacterMeta.setLore(this.language.getMultiLineMessage("character-selection.new-character.lore"));
        newCharacter.setItemMeta(newCharacterMeta);

        this.selectorGui.setItem(1, newCharacter);
        this.selectorGui.setItem(4, newCharacter);
        this.selectorGui.setItem(7, newCharacter);


        final HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");

        final HashMap<Integer, HashMap<String, Object>> queryResult = this.sqlHandler.executeQuery("SELECT UUID FROM Characters WHERE OwnerUUID = '" + this.player.getUniqueId().toString() + "'", tableLayout);

        //Overwrite new character with excising characters
        if (queryResult != null && queryResult.size() > 0) {

            for (int i = 0; i < queryResult.size(); i++) {
                switch (i) {
                    case 0:
                        this.selectorGui.setItem(1, this.getCharacterSkull(UUID.fromString((String) queryResult.get(i).get("UUID"))));
                        break;
                    case 1:
                        this.selectorGui.setItem(4, this.getCharacterSkull(UUID.fromString((String) queryResult.get(i).get("UUID"))));
                        break;
                    case 2:
                        this.selectorGui.setItem(7, this.getCharacterSkull(UUID.fromString((String) queryResult.get(i).get("UUID"))));
                        break;
                }

            }

        }


        if (this.player.hasPermission("multicharacter.admin")) {
            final ItemStack staffMode = new ItemStack(Material.CREEPER_HEAD, 1);
            final ItemMeta staffModeMeta = staffMode.getItemMeta();

            staffModeMeta.setDisplayName(this.language.getMessage("character-selection.staff-mode.name"));
            staffModeMeta.setLore(this.language.getMultiLineMessage("character-selection.staff-mode.lore"));
            staffMode.setItemMeta(staffModeMeta);

            this.selectorGui.setItem(8, staffMode);
        }


    }

    void teleportToSpawnLocation(final Location spawnLocation) {
        this.canClose = true;
        this.player.closeInventory();
        this.player.getInventory().setContents(this.currPlayerInventory);
        this.player.updateInventory();

        this.plugin.getAnimateToLocation().put(this.player.getUniqueId(), spawnLocation);

    }

    @Override
    public Inventory getInventory() {
        return this.selectorGui;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerLogout(final PlayerQuitEvent event) {
        if (event.getPlayer() == this.player) {
            if (this.player.getLocation().equals(this.menuLocation)) {
                this.teleportToSpawnLocation(this.playerLoginLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClose(final InventoryCloseEvent event) {
        final Inventory inventory = event.getInventory();
        final Player player = (Player) event.getPlayer();

        if (inventory == this.selectorGui && !this.canClose) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                player.openInventory(this.selectorGui);
            }, 5L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        final ItemStack clickedItem = event.getCurrentItem();
        final Player player = (Player) event.getWhoClicked();

        if (inventory == this.selectorGui) {
            event.setCancelled(true);

            if (event.getSlot() == 8) {
                this.teleportToSpawnLocation(this.playerLoginLocation);
            } else {
                if (clickedItem.getType() == Material.LIME_CONCRETE) {

                    this.canClose = true;
                    player.closeInventory();
                    this.plugin.getPlayersInCreation().add(player.getUniqueId());

                } else if (clickedItem.getType() == Material.PLAYER_HEAD) {

                }
            }
        }
    }
}
