package com.foxxite.multicharacter.character;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.UUIDHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_16_R1.DimensionManager;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R1.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;

public class NMSSkinChanger {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final Language language;
    private Character character = null;

    public NMSSkinChanger(MultiCharacter plugin, Player player, Character character, String skinTexture, String skinSignature) {
        this.plugin = plugin;
        config = plugin.getConfiguration();
        language = plugin.getLanguage();
        this.character = character;

        mainLogic(player, character.getCharacterID(), skinTexture, skinSignature);
    }

    public NMSSkinChanger(MultiCharacter plugin, Player player, UUID characterUUID, String skinTexture, String skinSignature) {
        this.plugin = plugin;
        config = plugin.getConfiguration();
        language = plugin.getLanguage();

        mainLogic(player, characterUUID, skinTexture, skinSignature);
    }

    private void mainLogic(Player player, UUID uuid, String skinTexture, String skinSignature) {
        boolean wasOP = player.isOp();

        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        GameProfile gp = ep.getProfile();

        UUID newUUID = player.getUniqueId();

        //Reset OP before skin change, or OP access will be lost
        if (wasOP) {
            player.setOp(false);
        }

        PropertyMap pm = gp.getProperties();

        Collection<Property> properties = pm.get("textures");

        // Offline player check
        if (properties != null) {
            Property property = properties.iterator().next();
            pm.remove("textures", property);
        }

        String textureValue = skinTexture;

        String textureSignature = skinSignature;

        pm.put("textures", new Property("textures", textureValue, textureSignature));

        plugin.getPluginLogger().info("Old UUID: " + gp.getId());
        plugin.getPluginLogger().info("Old UUID Spigot: " + player.getUniqueId());

        if (character != null) {
            if (config.getBoolean("use-character-uuid")) {
                UUID characterUUID = character.getCharacterID();
                UUIDHandler.CHANGE_UUID(player, characterUUID);
                newUUID = characterUUID;
            }
        }

        //Reset player visibility
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
            player.hidePlayer(p);

            p.showPlayer(player);
            player.showPlayer(p);
        }

        reloadSkinForSelf(player);

        //Set OP after skin change, or OP access will be lost
        UUID finalNewUUID = newUUID;
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getPluginLogger().info("New UUID: " + gp.getId());
                plugin.getPluginLogger().info("New UUID Spigot: " + player.getUniqueId());

                if (wasOP) {
                    Bukkit.getPlayer(finalNewUUID).setOp(true);
                    player.setOp(true);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    public void reloadSkinForSelf(Player player) {
        Location l = player.getLocation();
        EntityPlayer ep = ((CraftPlayer) player).getHandle();

        net.minecraft.server.v1_16_R1.World w = ((CraftWorld) l.getWorld()).getHandle();
        World.Environment environment = player.getWorld().getEnvironment();

        try {

            for (Field field : DimensionManager.class.getDeclaredFields()) {
                System.out.println(field.getName());
            }

            Field OVERWORLD_IMPL = DimensionManager.class.getDeclaredField("OVERWORLD_IMPL");
            Field NETHER_IMPL = DimensionManager.class.getDeclaredField("THE_NETHER_IMPL");
            Field THE_END_IMPL = DimensionManager.class.getDeclaredField("THE_END_IMPL");

            OVERWORLD_IMPL.setAccessible(true);
            NETHER_IMPL.setAccessible(true);
            THE_END_IMPL.setAccessible(true);

            //send packets to player
            DimensionManager dimension = (DimensionManager) OVERWORLD_IMPL.get(new Object());
            if (environment.equals(org.bukkit.World.Environment.NETHER)) {
                dimension = (DimensionManager) NETHER_IMPL.get(new Object());
            } else if (environment.equals(World.Environment.THE_END)) {
                dimension = (DimensionManager) THE_END_IMPL.get(new Object());
            }

            System.out.println(dimension);

            PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(dimension, w.worldData, w.getWorld().getSeed(), ep.playerInteractManager.getGameMode(), ep.playerInteractManager.getGameMode(), true, true, true);
            PacketPlayOutPlayerInfo infoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep);
            PacketPlayOutPlayerInfo infoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep);

            Location loc = player.getLocation().clone();

            ep.playerConnection.sendPacket(infoRemove);
            ep.playerConnection.sendPacket(infoAdd);

            ep.playerConnection.sendPacket(respawn);

            ep.updateAbilities();

        } catch (Exception e) {
            plugin.getPluginLogger().severe(e.getMessage() + " " + e.getCause());
            e.printStackTrace();
            return;
        }
    }

}
