package com.foxxite.multicharacter.tasks;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.misc.Common;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;

public class AnimateToPosition extends TimerTask implements Listener {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final HashMap<UUID, Long> flyingPlayer = new HashMap<>();

    public AnimateToPosition(MultiCharacter plugin) {
        this.plugin = plugin;
        config = plugin.getConfiguration();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void run() {

        new BukkitRunnable() {
            @Override
            public void run() {

                if (plugin.getAnimateToLocation().size() == 0) {
                    return;
                }

                HashMap<UUID, Location> localAnimateToLocation = (HashMap<UUID, Location>) plugin.getAnimateToLocation().clone();

                localAnimateToLocation.forEach((uuid, destination) -> {

                    Player player = Common.getPlayerByUuid(uuid);

                    if (player == null) {
                        return;
                    }

                    if (config.getBoolean("debug")) {
                        Bukkit.broadcastMessage("Animation run for: " + player.getName());
                    }

                    //Add player to list of animated players
                    if (!flyingPlayer.containsKey(uuid)) {

                        if (config.getBoolean("debug")) {
                            Bukkit.broadcastMessage("Playing animation sound for player...");
                        }

                        flyingPlayer.put(uuid, Instant.now().getEpochSecond());
                        player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, SoundCategory.MASTER, 1f, 1f);

                        //Clear player chat
                        if (config.getBoolean("clear-chat")) {
                            for (int i = 0; i < 500; i++) {
                                player.sendMessage("");
                            }
                        }
                    }

                    //Put player in spectator mode, enable fly mode
                    if (player.getGameMode() != GameMode.SPECTATOR) {

                        if (config.getBoolean("debug")) {
                            Bukkit.broadcastMessage("Putting player into correct mode...");
                        }

                        player.setGameMode(GameMode.SPECTATOR);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    }

                    //Make player face down
                    Location startLocation = player.getLocation().clone();
                    startLocation.setY(255);
                    startLocation.setPitch(90);
                    startLocation.setYaw(0);

                    Location realDestination = destination.clone();
                    realDestination.setY(255);

                    // Cancel animation if dimensions don't match
                    if (!startLocation.getWorld().equals(realDestination.getWorld())) {

                        if (config.getBoolean("debug")) {
                            Bukkit.broadcastMessage("Teleporting player to correct dimension....");
                        }

                        player.teleport(destination);
                    }

                    double distance = startLocation.distance(realDestination);

                    //Teleport player closer if logout location is too far from menu location
                    if (distance > 500) {

                        if (config.getBoolean("debug")) {
                            Bukkit.broadcastMessage("Teleporting player closer to destination...");
                        }

                        Location shortStartLocation = realDestination.clone();
                        shortStartLocation.setX(shortStartLocation.getX() - 350);
                        shortStartLocation.setZ(shortStartLocation.getZ() - 350);
                        shortStartLocation.setYaw(0);
                        shortStartLocation.setPitch(90);

                        shortStartLocation.getChunk().load();

                        player.teleport(shortStartLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

                        return;
                    }

                    //Make player fly in the right direction
                    Vector direction = realDestination.toVector().subtract(startLocation.toVector());
                    direction.normalize();
                    direction.multiply(clamp((float) distance, 0, 5000));

                    // Fix finite issues
                    if (!NumberConversions.isFinite(direction.getX())) {
                        direction.setX(1.7976931348623157E308D);
                    }
                    if (!NumberConversions.isFinite(direction.getY())) {
                        direction.setY(1.7976931348623157E308D);
                    }
                    if (!NumberConversions.isFinite(direction.getZ())) {
                        direction.setZ(1.7976931348623157E308D);
                    }

                    if (config.getBoolean("debug")) {
                        Bukkit.broadcastMessage("Applying velocity to player with vector: " + direction.getX() + ", " + direction.getY() + ", " + direction.getZ() + ", " + distance);
                    }

                    player.teleport(startLocation);
                    player.setVelocity(direction);

                    //Play flying sound
                    if (flyingPlayer.get(uuid) < Instant.now().getEpochSecond() - 9) {
                        player.playSound(player.getLocation(), Sound.ITEM_ELYTRA_FLYING, SoundCategory.MASTER, 1f, 1f);
                        flyingPlayer.remove(uuid);
                        flyingPlayer.put(uuid, Instant.now().getEpochSecond());
                    }

                    //Add Blindness Effect
                    if (distance < 0.5) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 50, 1, true));
                    }

                    //Teleport player to logout location
                    if (distance < 0.2f) {

                        if (config.getBoolean("debug")) {
                            Bukkit.broadcastMessage("Ending animation for player: " + player.getName());
                        }

                        plugin.getAnimateToLocation().remove(uuid);
                        player.setVelocity(new Vector(0, 0, 0));
                        player.setGameMode(GameMode.SURVIVAL);
                        player.setFlying(false);
                        player.stopSound(Sound.ITEM_ELYTRA_FLYING);

                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 50, 1, true));

                        player.teleport(findSafeSpawnLocation(destination), PlayerTeleportEvent.TeleportCause.PLUGIN);

                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);

                        flyingPlayer.remove(uuid);

                        //Clear player chat
                        if (config.getBoolean("clear-chat")) {
                            for (int i = 0; i < 500; i++) {
                                player.sendMessage("");
                            }
                        }
                    }

                });

            }
        }.runTask(plugin);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getAnimateToLocation().containsKey(player.getUniqueId())) {
            player.teleport(plugin.getAnimateToLocation().get(player.getUniqueId()));
            plugin.getAnimateToLocation().remove(player.getUniqueId());
        }
    }

    private Location findSafeSpawnLocation(Location destination) {

        if (config.getBoolean("debug")) {
            Bukkit.broadcastMessage("Calculating safe spawn location...");
        }

        boolean foundVoidOnce = false;
        Location blockLoc = destination;

        Block prevBlock = null;
        Block currBlock = null;

        while (true) {

            currBlock = blockLoc.getBlock();

            if (prevBlock != null && prevBlock.isPassable() && !currBlock.isPassable()) {
                return prevBlock.getLocation();
            } else {
                prevBlock = blockLoc.getBlock();

                if (blockLoc.getBlockY() > 0) {
                    blockLoc.subtract(0, 1, 0);
                } else {
                    //Prevent infinite loop
                    if (foundVoidOnce) {
                        return blockLoc.add(0, 1, 0);
                    } else {
                        foundVoidOnce = true;
                        blockLoc.setY(255);
                    }
                }
            }
        }
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

}
