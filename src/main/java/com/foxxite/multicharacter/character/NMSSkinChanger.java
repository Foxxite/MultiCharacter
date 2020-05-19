package com.foxxite.multicharacter.character;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.UUIDHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

public class NMSSkinChanger {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final Language language;

    public NMSSkinChanger(final MultiCharacter plugin, final Player player, UUID characterUUID, final String skinTexture, final String skinSignature) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.language = plugin.getLanguage();
        final boolean wasOP = player.isOp();

        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        final GameProfile gp = ep.getProfile();

        UUID newUUID = player.getUniqueId();

        //Reset OP before skin change, or OP access will be lost
        if (wasOP) {
            player.setOp(false);
        }

        final PropertyMap pm = gp.getProperties();

        final Collection<Property> properties = pm.get("textures");

        final Property property = pm.get("textures").iterator().next();

        final String textureValue = skinTexture;

        final String textureSignature = skinSignature;

        pm.remove("textures", property);
        pm.put("textures", new Property("textures", textureValue, textureSignature));

        plugin.getPluginLogger().info("Old UUID: " + gp.getId());
        plugin.getPluginLogger().info("Old UUID Spigot: " + player.getUniqueId());

        if(config.getBoolean("use-character-uuid"))
        {
            UUIDHandler.CHANGE_UUID(player, characterUUID);
            newUUID = characterUUID;
        }

        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
            p.showPlayer(player);
        }

        this.reloadSkinForSelf(player);

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

    public void reloadSkinForSelf(final Player player) {
        Location l = player.getLocation();
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();

        net.minecraft.server.v1_15_R1.World w = ((CraftWorld)l.getWorld()).getHandle();
        World.Environment environment = player.getWorld().getEnvironment();

        int dimension = 0;
        if (environment.equals(World.Environment.NETHER))
            dimension = -1;
        else if (environment.equals(World.Environment.THE_END))
            dimension = 1;
        //send packet to player
        DimensionManager dm = DimensionManager.a(dimension);

        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(dm,w.worldData.getSeed(),w.worldData.getType(),ep.playerInteractManager.getGameMode());
        final PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep);
        final PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep);

        final Location loc = player.getLocation().clone();

        ep.playerConnection.sendPacket(respawn);
        ep.playerConnection.sendPacket(removeInfo);
        ep.playerConnection.sendPacket(addInfo);
        ep.updateAbilities();
    }

}
