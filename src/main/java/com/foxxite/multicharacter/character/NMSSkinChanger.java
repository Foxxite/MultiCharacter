package com.foxxite.multicharacter.character;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_15_R1.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class NMSSkinChanger {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final Language language;

    public NMSSkinChanger(final MultiCharacter plugin, final Player player, final String skinTexture, final String skinSignature) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.language = plugin.getLanguage();
        final boolean wasOP = player.isOp();

        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        final GameProfile gp = ep.getProfile();

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

        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
            p.showPlayer(player);
        }

        this.reloadSkinForSelf(player);

        //Set OP after skin change, or OP access will be lost
        new BukkitRunnable() {
            @Override
            public void run() {
                if (wasOP) {
                    player.setOp(true);
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    public void reloadSkinForSelf(final Player player) {
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        final PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep);
        final PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep);
        final Location loc = player.getLocation().clone();
        ep.playerConnection.sendPacket(removeInfo);
        ep.playerConnection.sendPacket(addInfo);

        World skinWorld = Bukkit.getWorld(this.config.getString("skin-dimension-location.world"));

        if (skinWorld == null) {
            skinWorld = player.getWorld();
            player.sendMessage(this.language.getMessage("character-selection.no-dimension"));
        }

        final Location tpLoc;

        tpLoc = new Location(skinWorld, 0, 256, 0, 0, 0);

        tpLoc.getChunk().load();

        player.teleport(tpLoc);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(loc);
                ep.playerConnection.sendPacket(new PacketPlayOutRespawn(ep.dimension, 4, ep.getWorld().getWorldData().getType(), ep.playerInteractManager.getGameMode()));
                player.updateInventory();
            }
        }.runTaskLater(this.plugin, 2L);
    }

}
