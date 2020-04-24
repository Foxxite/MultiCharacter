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
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class SkinChanger {

    private final PacketAdapter adapter;
    private final MultiCharacter plugin;
    private volatile Collection<WrappedSignedProperty> properties;

    public SkinChanger(final MultiCharacter plugin) {

        this.plugin = plugin;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                WrappedGameProfile profile = WrappedGameProfile.fromOfflinePlayer(Bukkit.getOfflinePlayer("Notch"));
                final Object handle = profile.getHandle();
                final Object sessionService = SkinChanger.this.getSessionService();
                try {
                    final Method method = SkinChanger.this.getFillMethod(sessionService);
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
            }
        });

        this.adapter = new PacketAdapter(this, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                if (SkinChanger.this.properties == null) {
                    return;
                }
                final PacketContainer packet = event.getPacket();
                final EnumWrappers.PlayerInfoAction action = packet.getPlayerInfoAction().read(0);
                if (action != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                    return;
                }
                final List<PlayerInfoData> data = packet.getPlayerInfoDataLists().read(0);
                for (final PlayerInfoData pid : data) {
                    final WrappedGameProfile profile = pid.getProfile();
                    profile.getProperties().removeAll("textures");

                    profile.getProperties().put("textures", new WrappedSignedProperty("textures",
                            "ewogICJ0aW1lc3RhbXAiIDogMTU4Nzc1MDg0NDQ0MSwKICAicHJvZmlsZUlkIiA6ICI0M2RjMDg0MmY4YWQ0MDQ2ODExYTM0ZTkyNjEwNTk0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLSUxBMTQyMyIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84OTNmOWM5OWMwMjZjNTA0NGVhZjcyMzhiM2RkNmQwZDUxMTQ3Y2FkNzJkYWY3YWQ0ZjM3NDMxYjc2MmE4NTUzIgogICAgfQogIH0KfQ==",
                            "hjFIPO6sGXXBG0SIFIYBPWtST/OoBKCZ8vgtGhoQlh04hMn5eS/FdiCW1C9IloHl2nrmCxFnIPGJeycwmdU5eIZ9+sRvfEo/JODnE4BliSYDUE7xgs2byNFG5y2WeerB60PAwTSYz+sNp/Nz5r1MUO9eOER9YC95K8tt4AjAEMCC6WLnwAeV9S2OsKhCac/2UoT5yQTKI6x9ZbNsFEUpRBZhx7cZZ2RKJFix3eld/BnHzAPU4XfDJ7EnUOQU1fki5AprJjD7QFmLL7rMebCIBx6QxguF1XvG8Bj8RCajciyzYzpdgIRSms24t9ItCy4B4tLUHbWKYsAYzTxxeAA6JLac5xPEAdGrb/qf6EI9X7g7PwHvNGu9W5UIPnEJ80yxk8gm65Ng6TvfJ34w5wsm22tphUVqZ7irWL5L44crMZi7BDgBmQl4dZwwRJoNcIvpg2d5GKyYMIj9dBd3ENM7fTueXDVWpg2++vRAQZDG1+n6JUezeqUelUdjGk4YDsawCzWHiwX9JNPbcbO7R3NJWdbqRj/um5b/qYYDg6+5Xr4NhcjZfeK/dDCJ9aNpeNqGVxhpG9cLXhNW710diU5hcFOYroAMQaVNN15nuRslnFoWmXjJG3wYmCC2SE6y9Y9lZkrTaePmH7g1saMAt1rspgG+u+27Et/Hf+uz7zXBLD4="));
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(this.adapter);
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
