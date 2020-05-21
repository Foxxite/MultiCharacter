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

    public CharacterSelector(MultiCharacter plugin, Player player) {

        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.plugin = plugin;
        config = plugin.getConfiguration();
        language = plugin.getLanguage();
        this.player = player;
        sqlHandler = plugin.getSqlHandler();

        double x = config.getDouble("menu-location.x");
        double y = config.getDouble("menu-location.y");
        double z = config.getDouble("menu-location.z");
        float yaw = (float) config.getDouble("menu-location.yaw");
        float pitch = (float) config.getDouble("menu-location.pitch");

        String world = config.getString("menu-location.world");
        menuLocation = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);

        namespacedKey = new NamespacedKey(plugin, "character");

        currPlayerInventory = player.getInventory().getContents();
        player.getInventory().clear();
        this.player.updateInventory();

        selectorGui = Bukkit.createInventory(this, 9, "Character Selection");

        openGuiForPlayer();

        populateGUI();
    }

    private void openGuiForPlayer() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            playerLoginLocation = player.getLocation();

            player.teleport(menuLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setGameMode(GameMode.SPECTATOR);

            player.openInventory(getInventory());
        }, 5l);
    }

    private void populateGUI() {

        selectorGui.setItem(0, getLogoutItem());

        selectorGui.setItem(1, getNewCharacterSkull());
        selectorGui.setItem(4, getNewCharacterSkull());
        selectorGui.setItem(7, getNewCharacterSkull());

        HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");

        String selectQuery = "SELECT UUID, OwnerUUID FROM Characters WHERE Deleted = 0 AND OwnerUUID = '" + player.getUniqueId().toString() + "'";

        HashMap<Integer, HashMap<String, Object>> queryResult = sqlHandler.executeQuery(selectQuery, tableLayout);

        //Overwrite new character with excising characters
        if (queryResult != null && queryResult.size() > 0) {

            for (int i = 0; i < queryResult.size(); i++) {
                switch (i) {
                    case 0:
                        selectorGui.setItem(1, getCharacterSkull(UUID.fromString((String) queryResult.get(i).get("UUID"))));
                        break;
                    case 1:
                        selectorGui.setItem(4, getCharacterSkull(UUID.fromString((String) queryResult.get(i).get("UUID"))));
                        break;
                    case 2:
                        selectorGui.setItem(7, getCharacterSkull(UUID.fromString((String) queryResult.get(i).get("UUID"))));
                        break;
                }
            }
        }

        if (player.hasPermission("multicharacter.admin")) {
            selectorGui.setItem(8, getStaffItem());
        }

    }

    private ItemStack getLogoutItem() {
        ItemStack logoutItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta logoutItemMeta = logoutItem.getItemMeta();
        logoutItemMeta.setDisplayName(Common.colorize("&4&lLOGOUT"));
        logoutItem.setItemMeta(logoutItemMeta);

        return logoutItem;
    }

    private ItemStack getNewCharacterSkull() {

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        meta.setDisplayName(language.getMessage("character-selection.new-character.name"));
        meta.setLore(language.getMultiLineMessage("character-selection.new-character.lore"));

        GameProfile profile = new GameProfile(UUID.randomUUID(), "skull");
        PropertyMap pm = profile.getProperties();

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

    private ItemStack getCharacterSkull(UUID characterUUID) {

        Character character = new Character(plugin, characterUUID);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        String characterName = ChatColor.GOLD + character.getName();
        meta.setDisplayName(characterName);

        GameProfile profile = new GameProfile(UUID.randomUUID(), "skull");
        PropertyMap pm = profile.getProperties();

        String textureValue = character.getSkinTexture();
        String textureSignature = character.getSkinSignature();

        pm.put("textures", new Property("textures", textureValue, textureSignature));

        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        HashMap<String, String> placeholdersLore = new HashMap<>();
        placeholdersLore.put("{birthday}", character.getBirthday());
        placeholdersLore.put("{nationality}", character.getNationality());
        placeholdersLore.put("{sex}", character.getSex());
        placeholdersLore.put("{balance}", String.valueOf(character.getVaultBalance()));
        placeholdersLore.put("{group}", character.getVaultGroup());

        meta.setLore(language.getMultiLineMessageCustom("character-selection.character.lore", placeholdersLore));

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, characterUUID.toString());

        skull.setItemMeta(meta);
        return skull;
    }

    private ItemStack getStaffItem() {

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        meta.setDisplayName(language.getMessage("character-selection.staff-mode.name"));
        meta.setLore(language.getMultiLineMessage("character-selection.staff-mode.lore"));

        GameProfile profile = new GameProfile(UUID.randomUUID(), "skull");
        PropertyMap pm = profile.getProperties();

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

    private void teleportToLogoutLocation(Location staffLocation) {
        canClose = true;
        player.closeInventory();
        player.getInventory().setContents(currPlayerInventory);
        player.updateInventory();

        plugin.getAnimateToLocation().put(player.getUniqueId(), staffLocation);
    }

    @Override
    public Inventory getInventory() {
        return selectorGui;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerLogout(PlayerQuitEvent event) {
        if (event.getPlayer() == player) {
            if (player.getLocation().equals(menuLocation)) {
                teleportToLogoutLocation(playerLoginLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (inventory == selectorGui && !canClose) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.openInventory(selectorGui);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (inventory == selectorGui) {
            event.setCancelled(true);

            if (clickedItem == null) {
                return;
            }

            if (event.getSlot() == 8) {
                //Staff Mode
                teleportToLogoutLocation(playerLoginLocation);
                player.setDisplayName(player.getName());

                String json = getMojangSkinData(player.getUniqueId().toString());

                if (json == null) {
                    player.kickPlayer("Could not get data from Mojang");
                    return;
                }

                String[] skinData = deserializeMojangData(json);

                NMSSkinChanger nmsSkinChanger = new NMSSkinChanger(plugin, player, player.getUniqueId(), skinData[0], skinData[1]);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showPlayer(player);
                }
            } else if (event.getSlot() == 0) {
                //Disconnect BTN
                player.kickPlayer("Disconnected");
            } else {
                if (clickedItem.getItemMeta().getDisplayName().equals(language.getMessage("character-selection.new-character.name"))) {
                    //New Character
                    canClose = true;
                    player.closeInventory();
                    plugin.getPlayersInCreation().add(player.getUniqueId());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);

                } else if (clickedItem.getType() == Material.PLAYER_HEAD) {
                    //Character Selector Stuff

                    ItemMeta meta = clickedItem.getItemMeta();

                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    UUID characterUUID = UUID.fromString(container.get(namespacedKey, PersistentDataType.STRING));

                    ClickType clickType = event.getClick();

                    //Delete Character
                    if (clickType == ClickType.DOUBLE_CLICK) {
                        sqlHandler.executeUpdateQuery("UPDATE Characters SET Deleted = 1 WHERE UUID = '" + characterUUID + "';");
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1f);
                        populateGUI();
                    }

                    //Select Character
                    else if (clickType == ClickType.RIGHT) {

                        Character character = new Character(plugin, characterUUID);

                        canClose = true;
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

                        NMSSkinChanger nmsSkinChanger = new NMSSkinChanger(plugin, player, characterUUID, character.getSkinTexture(), character.getSkinSignature());

                        plugin.getActiveCharacters().put(this.player.getUniqueId(), character);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                            SpawnLocationSelector spawnLocationSelector = new SpawnLocationSelector(plugin, player, character);

                        }, 5L);
                    }
                }
            }
        }
    }

    private String getMojangSkinData(String uuid) {

        try {
            String url = ("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", "") + "?unsigned=false");
            OkHttpClient httpClient = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "OkHttp Bot")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Get response body
                String responseStr = response.body().string();
                return responseStr;
            }

        } catch (Exception ex) {
            plugin.getPluginLogger().severe(ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    private String[] deserializeMojangData(String json) {

        Gson gson = new Gson();
        MojangResponse response = gson.fromJson(json, MojangResponse.class);

        String[] output = new String[2];
        output[0] = response.getProperties().get(0).getValue();
        output[1] = response.getProperties().get(0).getSignature();

        return output;
    }

}
