package com.foxxite.multicharacter.misc;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Common {

    public static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        // Getting the version by splitting the package
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";

        // Combining the prefix + version + nmsClassString for the full class path
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }

    public static Entity createEntity(EntityType entityType, Location location) {
        try {
            // We get the craftworld class with nms so it can be used in multiple versions
            Class<?> craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");

            // Cast the bukkit world to the craftworld
            Object craftWorldObject = craftWorldClass.cast(location.getWorld());

            // Create variable with the method that creates the entity
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/CraftWorld.java#896
            Method createEntityMethod = craftWorldObject.getClass().getMethod("createEntity", Location.class, Class.class);

            // Attempt to invoke the method that creates the entity itself. This returns a net.minecraft.server entity
            Object entity = createEntityMethod.invoke(craftWorldObject, location, entityType.getEntityClass());

            // finally we run the getBukkitEntity method in the entity class to get a usable object
            return (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
        }


        // If something went wrong we just return null
        return null;
    }

    public static LivingEntity createLivingEntity(EntityType entityType, Location location) {
        try {
            // We get the craftworld class with nms so it can be used in multiple versions
            Class<?> craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");

            // Cast the bukkit world to the craftworld
            Object craftWorldObject = craftWorldClass.cast(location.getWorld());

            // Create variable with the method that creates the entity
            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/CraftWorld.java#896
            Method createEntityMethod = craftWorldObject.getClass().getMethod("createEntity", Location.class, Class.class);

            // Attempt to invoke the method that creates the entity itself. This returns a net.minecraft.server entity
            Object entity = createEntityMethod.invoke(craftWorldObject, location, entityType.getEntityClass());

            // finally we run the getBukkitEntity method in the entity class to get a usable object
            return (LivingEntity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
        }


        // If something went wrong we just return null
        return null;
    }

    public static void tell(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void setRepairCost(ItemStack itemStack, int repairCost) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Repairable) {
            Repairable repairable = (Repairable) itemMeta;
            repairable.setRepairCost(repairCost);
            itemStack.setItemMeta(itemMeta);
        }
    }

    public static int getRepairCost(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return -1;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Repairable) {
            Repairable repairable = (Repairable) itemMeta;
            return repairable.getRepairCost();
        }
        return -1;
    }


    public static String inventoryToString(ItemStack[] inventory) {
        YamlConfiguration inventoryConfig = new YamlConfiguration();

        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null) {
                inventoryConfig.set(String.valueOf(i), item);
            } else {
                inventoryConfig.set(String.valueOf(i), new ItemStack(Material.AIR, 1));
            }
        }
        return inventoryConfig.saveToString();
    }


    public static ItemStack[] stringToInventory(String yamlInventory) {
        YamlConfiguration inventoryConfig = new YamlConfiguration();

        if (yamlInventory.isEmpty() || yamlInventory.equalsIgnoreCase("")) {
            return null;
        }

        try {
            inventoryConfig.loadFromString(yamlInventory);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        ItemStack[] inventory = new ItemStack[inventoryConfig.getKeys(false).size()];
        int counter = 0;
        for (String configKey : inventoryConfig.getKeys(false)) {
            inventory[counter] = (inventoryConfig.getItemStack(configKey, null));
            counter++;
        }

        return inventory;
    }

    public static String getLocationAsString(Location location) {
        String output =
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ", " +
                        location.getWorld().getName();
        return output;
    }

    public static Location getLocationFromString(String location) {
        String[] locationString = location.split(",");

        Location output = new Location(Bukkit.getServer().getWorld(locationString[3].trim()),
                Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]),
                Double.parseDouble(locationString[2]));

        return output;
    }

    public static void broadcastActionBar(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }

    public static void sendActionBarMessage(String message, Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static byte toPackedByte(float f) {
        return (byte) ((int) (f * 256.0F / 360.0F));
    }

    /**
     * Gets online player from UUID
     */
    public static Player getPlayerByUuid(UUID uuid) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getUniqueId().equals(uuid)) {
                return p;
            }
        }

        return null;
    }

}