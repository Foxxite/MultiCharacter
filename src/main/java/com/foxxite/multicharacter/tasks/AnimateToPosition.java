package com.foxxite.multicharacter.tasks;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.misc.Common;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;

public class AnimateToPosition extends TimerTask implements Listener {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    HashMap<UUID, Long> flyingPlayer = new HashMap<>();


    public AnimateToPosition(final MultiCharacter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run() {

        new BukkitRunnable() {
            @Override
            public void run() {

                if (AnimateToPosition.this.plugin.getAnimateToLocation().size() == 0)
                    return;

                final HashMap<UUID, Location> localAnimateToLocation = (HashMap<UUID, Location>) AnimateToPosition.this.plugin.getAnimateToLocation().clone();

                localAnimateToLocation.forEach((uuid, destination) -> {

                    final Player player = Common.getPlayerByUuid(uuid);

                    if (player == null) {
                        return;
                    }

                    if (!AnimateToPosition.this.flyingPlayer.containsKey(uuid)) {
                        AnimateToPosition.this.flyingPlayer.put(uuid, Instant.now().getEpochSecond());
                        player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, SoundCategory.MASTER, 1f, 1f);

                        //Clear player chat
                        if (AnimateToPosition.this.config.getBoolean("clear-chat")) {
                            for (int i = 0; i < 500; i++) {
                                player.sendMessage("");
                            }
                        }
                    }

                    if (player.getGameMode() != GameMode.SPECTATOR) {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }

                    final Location startLocation = player.getLocation().clone();
                    startLocation.setY(255);
                    startLocation.setPitch(90);
                    startLocation.setYaw(0);

                    final Location realDestination = destination.clone();
                    realDestination.setY(255);

                    if (!startLocation.getWorld().equals(realDestination.getWorld())) return;

                    final double distance = startLocation.distance(realDestination);

                    if (distance > 500) {
                        final Location shortStartLocation = realDestination.clone();
                        shortStartLocation.setX(shortStartLocation.getX() - 350);
                        shortStartLocation.setZ(shortStartLocation.getZ() - 350);
                        shortStartLocation.setYaw(0);
                        shortStartLocation.setPitch(90);

                        player.teleport(shortStartLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

                        return;
                    }


                    final Vector direction = AnimateToPosition.this.genVec(startLocation, realDestination);
                    direction.normalize();
                    direction.multiply(AnimateToPosition.this.clamp((float) startLocation.distance(realDestination), 0, 5000));

                    player.teleport(startLocation);
                    player.setVelocity(direction);

                    if (AnimateToPosition.this.flyingPlayer.get(uuid) < Instant.now().getEpochSecond() - 10) {
                        player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, SoundCategory.MASTER, 1f, 1f);
                        AnimateToPosition.this.flyingPlayer.remove(uuid);
                        AnimateToPosition.this.flyingPlayer.put(uuid, Instant.now().getEpochSecond());
                    }


                    if (distance < 0.5) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 50, 1, true));
                    }

                    if (distance < 0.2f) {
                        AnimateToPosition.this.plugin.getAnimateToLocation().remove(uuid);
                        player.setVelocity(new Vector(0, 0, 0));
                        player.setGameMode(GameMode.SURVIVAL);
                        player.setFlying(false);
                        player.stopSound(Sound.ITEM_ELYTRA_FLYING);

                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 50, 1, true));

                        player.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN);

                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);

                        AnimateToPosition.this.flyingPlayer.remove(uuid);

                        //Clear player chat
                        if (AnimateToPosition.this.config.getBoolean("clear-chat")) {
                            for (int i = 0; i < 500; i++) {
                                player.sendMessage("");
                            }
                        }
                    }

                });

            }
        }.runTask(this.plugin);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerLogout(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (this.plugin.getAnimateToLocation().containsKey(player.getUniqueId())) {
            player.teleport(this.plugin.getAnimateToLocation().get(player.getUniqueId()));
            this.plugin.getAnimateToLocation().remove(player.getUniqueId());
        }
    }

    private Vector genVec(final Location a, final Location b) {
        final double dX = a.getX() - b.getX();
        final double dY = a.getY() - b.getY();
        final double dZ = a.getZ() - b.getZ();
        final double yaw = Math.atan2(dZ, dX);
        final double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
        final double x = Math.sin(pitch) * Math.cos(yaw);
        final double y = Math.sin(pitch) * Math.sin(yaw);
        final double z = Math.cos(pitch);

        final Vector vector = new Vector(x, z, y);
        //If you want to: vector = vector.normalize();

        return vector;
    }

    private float clamp(final float val, final float min, final float max) {
        return Math.max(min, Math.min(max, val));
    }

}
