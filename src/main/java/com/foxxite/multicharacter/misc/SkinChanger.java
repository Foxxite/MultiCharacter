package com.foxxite.multicharacter.misc;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.foxxite.multicharacter.MultiCharacter;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_15_R1.PacketPlayOutRespawn;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SkinChanger {

    private final PacketAdapter adapter;
    private final MultiCharacter multiCharacter;
    private volatile Collection<WrappedSignedProperty> properties;

    public SkinChanger(final MultiCharacter multiCharacter) {

        this.multiCharacter = multiCharacter;

        Bukkit.getScheduler().runTaskAsynchronously(multiCharacter, () -> {
            WrappedGameProfile profile = WrappedGameProfile.fromOfflinePlayer(Bukkit.getOfflinePlayer("Notch"));
            final Object handle = profile.getHandle();
            final Object sessionService = this.getSessionService();
            try {
                final Method method = this.getFillMethod(sessionService);
                method.invoke(sessionService, handle, true);
            } catch (final IllegalAccessException ex) {
                ex.printStackTrace();
                return;
            } catch (final InvocationTargetException ex) {
                ex.printStackTrace();
                return;
            }
            profile = WrappedGameProfile.fromHandle(handle);
            SkinChanger.this.properties = profile.getProperties().get("textures");
        });

        this.adapter = new PacketAdapter(multiCharacter, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(final PacketEvent event) {

                if (SkinChanger.this.properties == null) {
                    return;
                }

                final Player player = event.getPlayer();

                Bukkit.broadcastMessage("Player info packet for: " + player.getName());

                final HashMap<UUID, Character> localActiveCharacters = (HashMap<UUID, Character>) multiCharacter.getActiveCharacters().clone();

                if (!localActiveCharacters.containsKey(player.getUniqueId())) {
                    Bukkit.broadcastMessage("Player does not have an active character");
                    return;
                }

                final PacketContainer packet = event.getPacket();
                final EnumWrappers.PlayerInfoAction action = packet.getPlayerInfoAction().read(0);

                if (action != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                    Bukkit.broadcastMessage("Non important packet");
                    return;
                }

                final List<PlayerInfoData> data = packet.getPlayerInfoDataLists().read(0);


                final String textureValue = localActiveCharacters.get(player.getUniqueId()).getSkinTexture();
                final String textureSignature = localActiveCharacters.get(player.getUniqueId()).getSkinSignature();

                Bukkit.broadcastMessage("Applying skin for: " + player.getName());
                Bukkit.broadcastMessage("Texture: " + textureValue);
                Bukkit.broadcastMessage("Signature: " + textureSignature);


                for (final PlayerInfoData pid : data) {
                    final WrappedGameProfile profile = pid.getProfile();
                    profile.getProperties().removeAll("textures");

                    profile.getProperties().put("textures", new WrappedSignedProperty("textures",
                            "eyJ0aW1lc3RhbXAiOjE1NTk3NTA4MDczODUsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE3MjdjYjcyNjVmZTdhODc3YzU3MDFlZTMwNzRjNWFmNmM5MmM5NDI2NGZlNTZkNWQyNDk3YWZjYjVjZGNlOSJ9fX0=",
                            "srkwRv7R38T3J3b3MQBQlRhCpYk/iVwPXgiUD+YtYQwbNhm/xLY1B6Y0ONfJ+DCf84Nv5XmJmI4oA578LdmxLUCNrAM3GcEMpedntrro4YqL86a6tadpHeCgLC9SQxeNSDzhDaqCFNeSWyVpS7D7LFeSII71Av0oXTUbGQRt2+Ur+rssoctMeaTYLePYpTgkJhNphaATKO310WOZ98LnlwDTsIfdSQz3wwSCGhaRcco6ebuaFcpSL67SuR2YvJCksxTetmCSpvCPBKRGDw/aTVNw0Xv4n5eNPrhRCnMuBv0ICtKKs0Y4YGWm6gqzFDDa+x+E42DIk3OPl4gWJE7IFDUjYFEgWSDjLzUnACnNAXkEJnTaevVXMi0b5MUEbvdzr7WMGD7dSntF/Ln0mEAg+h1DAH+MortvzDfD//cv7LtELVG4UiMXAmeBI9hSxodR/YzI3HCHnx9aiZOvOs1YWxuLSj/N4dmIGhJKHxN44r/ZbzN4wtF/k5R2z4dWg0laHkfA5rDTXxirN+Rb/LtQLS5KzKCG/cl6SSy6qTlPrI1eaHFJ2+8T8LFlv30JLwVE7m0BcVbO6/Ak1vrjiBeSH7VHDrUFtNfz1tHJ1TT+NR2depgIFrZPn9J4CyyJR1ZV8W9PhU88EucnxfT8ugiphDDYRESObXcBND/xr6KE9fU="));
                }

                for (final Player p : Bukkit.getOnlinePlayers()) {
                    p.hidePlayer(player);
                    p.showPlayer(player);
                }

                SkinChanger.this.reloadSkinForSelf(player);
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(this.adapter);
    }

    public void reloadSkinForSelf(final Player player) {
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        final PacketPlayOutPlayerInfo removeInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep);
        final PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep);
        final Location loc = player.getLocation().clone();
        ep.playerConnection.sendPacket(removeInfo);
        ep.playerConnection.sendPacket(addInfo);
        player.teleport(new Location(Bukkit.getWorld("world_the_end"), 0, 0, 0, 0, 0));
        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(loc);
                ep.playerConnection.sendPacket(new PacketPlayOutRespawn(ep.dimension, 4, ep.getWorld().getWorldData().getType(), ep.playerInteractManager.getGameMode()));
                player.updateInventory();
            }
        }.runTaskLater(this.multiCharacter, 2L);
    }


    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this.adapter);
    }

    private Object getSessionService() {
        final Server server = Bukkit.getServer();
        try {
            final Object mcServer = server.getClass().getDeclaredMethod("getServer").invoke(server);
            for (final Method m : mcServer.getClass().getMethods()) {
                if (m.getReturnType().getSimpleName().equalsIgnoreCase("MinecraftSessionService")) {
                    return m.invoke(mcServer);
                }
            }
        } catch (final Exception ex) {
            throw new IllegalStateException("An error occurred while trying to get the session service", ex);
        }
        throw new IllegalStateException("No session service found :o");
    }

    private Method getFillMethod(final Object sessionService) {
        for (final Method m : sessionService.getClass().getDeclaredMethods()) {
            if (m.getName().equals("fillProfileProperties")) {
                return m;
            }
        }
        throw new IllegalStateException("No fillProfileProperties method found in the session service :o");
    }

}
