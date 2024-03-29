package com.foxxite.multicharacter.misc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class UUIDHandler {

    public static void CHANGE_UUID(Player player, UUID newUUID) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(player);
            Class entityHuman = entityPlayer.getClass().getSuperclass();
            Class entityLiving = entityHuman.getSuperclass();
            Class entity = entityLiving.getSuperclass();

            Field uniqueId = entity.getDeclaredField("uniqueID");
            boolean originalAccessibleValue = uniqueId.isAccessible();
            uniqueId.setAccessible(true);
            uniqueId.set(entityPlayer, newUUID);
            uniqueId.setAccessible(originalAccessibleValue);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void CHANGE_NAME(Player player, String newName) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(player);
            Class entityHuman = entityPlayer.getClass().getSuperclass();
            Class entityLiving = entityHuman.getSuperclass();
            Class entity = entityLiving.getSuperclass();

            Field uniqueId = entity.getDeclaredField("name");
            boolean originalAccessibleValue = uniqueId.isAccessible();
            uniqueId.setAccessible(true);
            uniqueId.set(entityPlayer, newName);
            uniqueId.setAccessible(originalAccessibleValue);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void RESET_UUID(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        GameProfile gp = ep.getProfile();

        UUIDHandler.CHANGE_UUID(player, gp.getId());
    }

}
