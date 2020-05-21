package com.foxxite.multicharacter.events.listeners;

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

    public ItemPickupEventListener(MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof Player) {

            Player player = (Player) entity;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.updateInventory();
            }, 1L);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
                    plugin.getActiveCharacters().get(player.getUniqueId()).saveData();
                }
            }, 1l);

        }
    }

}
