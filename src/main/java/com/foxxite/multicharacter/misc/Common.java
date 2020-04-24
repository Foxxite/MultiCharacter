package com.foxxite.multicharacter.misc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

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

}