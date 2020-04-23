package com.foxxite.multicharacter.events;

import com.foxxite.multicharacter.MultiCharacter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class ItemPickupEventListener implements Listener {

    private final MultiCharacter plugin;

    public ItemPickupEventListener(final MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onItemPickup(final EntityPickupItemEvent event) {
        final LivingEntity entity = event.getEntity();

        if (entity instanceof Player) {

            final Player player = (Player) entity;
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                player.updateInventory();
            }, 1L);

        }
    }

}
