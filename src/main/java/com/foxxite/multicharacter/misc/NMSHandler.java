package com.foxxite.multicharacter.misc;

import org.bukkit.Bukkit;

import java.util.HashMap;

public class NMSHandler {

    private static final NMSHandler single_instance = null;
    private static final HashMap<String, Class<?>> cachedNmsClasses = new HashMap<>();

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {

        Class<?> nmsClass;

        // Check cache for class to speed up performance
        if (cachedNmsClasses.containsKey(name)) {
            nmsClass = cachedNmsClasses.get(name);
        } else {
            nmsClass = Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
            cachedNmsClasses.put(name, nmsClass);
        }

        return nmsClass;
    }


}
