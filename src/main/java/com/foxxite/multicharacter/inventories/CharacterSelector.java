package com.foxxite.multicharacter.inventories;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.character.NMSSkinChanger;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.Common;
import com.foxxite.multicharacter.mojangapi.MojangResponse;
import com.foxxite.multicharacter.sql.SQLHandler;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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

import java.io.IOException;
import java.lang.reflect.Field;
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
        final float yaw = (float) this.config.getDouble("menu-location.yaw");
        final float pitch = (float) this.config.getDouble("menu-location.pitch");

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
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            this.playerLoginLocation = this.player.getLocation();

            this.player.teleport(this.menuLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            this.player.setAllowFlight(true);
            this.player.setFlying(true);
            this.player.setGameMode(GameMode.SPECTATOR);

            this.player.openInventory(this.getInventory());
        }, 5l);
    }

    private void populateGUI() {

        this.selectorGui.setItem(0, this.getLogoutItem());

        this.selectorGui.setItem(1, this.getNewCharacterSkull());
        this.selectorGui.setItem(4, this.getNewCharacterSkull());
        this.selectorGui.setItem(7, this.getNewCharacterSkull());

        final HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");

        final String selectQuery = "SELECT UUID, OwnerUUID FROM Characters WHERE Deleted = 0 AND OwnerUUID = '" + this.player.getUniqueId().toString() + "'";

        final HashMap<Integer, HashMap<String, Object>> queryResult = this.sqlHandler.executeQuery(selectQuery, tableLayout);

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
            this.selectorGui.setItem(8, this.getStaffItem());
        }

    }

    private ItemStack getLogoutItem() {
        final ItemStack logoutItem = new ItemStack(Material.BARRIER, 1);
        final ItemMeta logoutItemMeta = logoutItem.getItemMeta();
        logoutItemMeta.setDisplayName(Common.colorize("&4&lLOGOUT"));
        logoutItem.setItemMeta(logoutItemMeta);

        return logoutItem;
    }

    private ItemStack getNewCharacterSkull() {

        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) skull.getItemMeta();

        meta.setDisplayName(this.language.getMessage("character-selection.new-character.name"));
        meta.setLore(this.language.getMultiLineMessage("character-selection.new-character.lore"));

        final GameProfile profile = new GameProfile(UUID.randomUUID(), "skull");
        final PropertyMap pm = profile.getProperties();

        final String textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGM5MjY5NDQ4YWZmZmY0NDQ1MTc3NTJmNzI1YWJiNDliOTFlZjM4MGU5YmQ3M2Y5YmY5ZDgzNzA0ZWYzZDZiNSJ9fX0=";
        final String textureSignature = "";

        pm.put("textures", new Property("textures", textureValue, textureSignature));

        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        skull.setItemMeta(meta);

        return skull;
    }

    private ItemStack getCharacterSkull(final UUID characterUUID) {

        final Character character = new Character(this.plugin, characterUUID);

        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) skull.getItemMeta();

        final String characterName = ChatColor.GOLD + character.getName();
        meta.setDisplayName(characterName);

        final GameProfile profile = new GameProfile(UUID.randomUUID(), "skull");
        final PropertyMap pm = profile.getProperties();

        final String textureValue = character.getSkinTexture();
        final String textureSignature = character.getSkinSignature();

        pm.put("textures", new Property("textures", textureValue, textureSignature));

        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

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

    private ItemStack getStaffItem() {

        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta) skull.getItemMeta();

        meta.setDisplayName(this.language.getMessage("character-selection.staff-mode.name"));
        meta.setLore(this.language.getMultiLineMessage("character-selection.staff-mode.lore"));

        final GameProfile profile = new GameProfile(UUID.randomUUID(), "skull");
        final PropertyMap pm = profile.getProperties();

        final String textureValue = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQxOWM2ODQ2MTY2NmFhY2Q3NjI4ZTM0YTFlMmFkMzlmZTRmMmJkZTMyZTIzMTk2M2VmM2IzNTUzMyJ9fX0=";
        final String textureSignature = "";

        pm.put("textures", new Property("textures", textureValue, textureSignature));

        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        skull.setItemMeta(meta);

        return skull;
    }

    private void teleportToLogoutLocation(final Location staffLocation) {
        this.canClose = true;
        this.player.closeInventory();
        this.player.getInventory().setContents(this.currPlayerInventory);
        this.player.updateInventory();

        this.plugin.getAnimateToLocation().put(this.player.getUniqueId(), staffLocation);
    }

    @Override
    public Inventory getInventory() {
        return this.selectorGui;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerLogout(final PlayerQuitEvent event) {
        if (event.getPlayer() == this.player) {
            if (this.player.getLocation().equals(this.menuLocation)) {
                this.teleportToLogoutLocation(this.playerLoginLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClose(final InventoryCloseEvent event) {
        final Inventory inventory = event.getInventory();
        final Player player = (Player) event.getPlayer();

        if (inventory == this.selectorGui && !this.canClose) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                player.openInventory(this.selectorGui);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        final ItemStack clickedItem = event.getCurrentItem();
        final Player player = (Player) event.getWhoClicked();

        if (inventory == this.selectorGui) {
            event.setCancelled(true);

            if (clickedItem == null) return;

            if (event.getSlot() == 8) {
                //Staff Mode
                this.teleportToLogoutLocation(this.playerLoginLocation);
                player.setDisplayName(player.getName());

                final String json = this.getMojangSkinData(player.getUniqueId().toString());

                if (json == null) {
                    player.kickPlayer("Could not get data from Mojang");
                    return;
                }

                final String[] skinData = this.deserializeMojangData(json);

                final NMSSkinChanger nmsSkinChanger = new NMSSkinChanger(this.plugin, player, player.getUniqueId(), skinData[0], skinData[1]);

                for (final Player p : Bukkit.getOnlinePlayers()) {
                    p.showPlayer(player);
                }
            } else if (event.getSlot() == 0) {
                //Disconnect BTN
                player.kickPlayer("Disconnected");
            } else {
                if (clickedItem.getItemMeta().getDisplayName().equals(this.language.getMessage("character-selection.new-character.name"))) {
                    //New Character
                    this.canClose = true;
                    player.closeInventory();
                    this.plugin.getPlayersInCreation().add(player.getUniqueId());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);

                } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
                    //Character Selector Stuff

                    final ItemMeta meta = clickedItem.getItemMeta();

                    final PersistentDataContainer container = meta.getPersistentDataContainer();
                    final UUID characterUUID = UUID.fromString(container.get(this.namespacedKey, PersistentDataType.STRING));

                    final ClickType clickType = event.getClick();

                    //Delete Character
                    if (clickType == ClickType.DOUBLE_CLICK) {
                        this.sqlHandler.executeUpdateQuery("UPDATE Characters SET Deleted = 1 WHERE UUID = '" + characterUUID + "';");
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1f);
                        this.populateGUI();
                    }

                    //Select Character
                    else if (clickType == ClickType.RIGHT) {

                        final Character character = new Character(this.plugin, characterUUID);

                        this.canClose = true;
                        player.closeInventory();

                        if (character.getInventoryContent() != null) {
                            player.getInventory().setContents(character.getInventoryContent());
                            player.updateInventory();
                        }

                        player.setHealth(character.getHealth());
                        player.setFoodLevel(character.getHunger());
                        player.setExp((float) character.getExp());
                        player.setLevel(character.getExpLevel());

                        player.setDisplayName(character.getName());

                        final NMSSkinChanger nmsSkinChanger = new NMSSkinChanger(this.plugin, player, characterUUID, character.getSkinTexture(), character.getSkinSignature());

                        this.plugin.getActiveCharacters().put(this.player.getUniqueId(), character);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {

                            final SpawnLocationSelector spawnLocationSelector = new SpawnLocationSelector(this.plugin, player, character);

                        }, 5L);
                    }
                }
            }
        }
    }

    private String getMojangSkinData(final String uuid) {

        try {
            final String url = ("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", "") + "?unsigned=false");
            final OkHttpClient httpClient = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "OkHttp Bot")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Get response body
                final String responseStr = response.body().string();
                return responseStr;
            }

        } catch (final Exception ex) {
            this.plugin.getPluginLogger().severe(ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    private String[] deserializeMojangData(final String json) {

        final Gson gson = new Gson();
        final MojangResponse response = gson.fromJson(json, MojangResponse.class);

        final String[] output = new String[2];
        output[0] = response.getProperties().get(0).getValue();
        output[1] = response.getProperties().get(0).getSignature();

        return output;
    }

}
