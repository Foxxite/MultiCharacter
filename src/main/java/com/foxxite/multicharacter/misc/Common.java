package com.foxxite.multicharacter.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.UUID;

public class Common {
    public static void tell(final CommandSender sender, final String message) {
        sender.sendMessage(colorize(message));
    }

    public static String colorize(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void setRepairCost(final ItemStack itemStack, final int repairCost) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return;

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Repairable) {
            final Repairable repairable = (Repairable) itemMeta;
            repairable.setRepairCost(repairCost);
            itemStack.setItemMeta(itemMeta);
        }
    }

    public static int getRepairCost(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return -1;

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Repairable) {
            final Repairable repairable = (Repairable) itemMeta;
            return repairable.getRepairCost();
        }
        return -1;
    }


    public static String inventoryToString(final ItemStack[] inventory) {
        final YamlConfiguration inventoryConfig = new YamlConfiguration();

        for (int i = 0; i < inventory.length; i++) {
            final ItemStack item = inventory[i];
            if (item != null) {
                inventoryConfig.set(String.valueOf(i), item);
            }
        }
        return inventoryConfig.saveToString();
    }


    public static ItemStack[] stringToInventory(final String yamlInventory) {
        final YamlConfiguration inventoryConfig = new YamlConfiguration();

        if (yamlInventory.isEmpty() || yamlInventory.equalsIgnoreCase(""))
            return null;

        try {
            inventoryConfig.loadFromString(yamlInventory);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }

        final ItemStack[] inventory = new ItemStack[inventoryConfig.getKeys(false).size()];
        int counter = 0;
        for (final String configKey : inventoryConfig.getKeys(false)) {
            inventory[counter] = (inventoryConfig.getItemStack(configKey, null));
            counter++;
        }

        return inventory;
    }

    /**
     * Gets online player from UUID
     */
    public static Player getPlayerByUuid(final UUID uuid) {
        for (final Player p : Bukkit.getServer().getOnlinePlayers())
            if (p.getUniqueId().equals(uuid))
                return p;

        return null;
    }

    public static String getNameFromUUID(final String uuid) {
        final String url = "https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names";
        try {
            @SuppressWarnings("deprecation") final String nameJson = IOUtils.toString(new URL(url));
            final JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
            final String playerSlot = nameValue.get(nameValue.size() - 1).toString();
            final JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);
            return nameObject.get("name").toString();
        } catch (final Exception e) {
            System.out.println(e.getMessage() + " " + e.getCause());
            e.printStackTrace();
        }
        return null;
    }

}