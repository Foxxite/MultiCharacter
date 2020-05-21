package com.foxxite.multicharacter.events.listeners;

import com.foxxite.multicharacter.MultiCharacter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveEventListener implements Listener {

    private final MultiCharacter plugin;

    public PlayerMoveEventListener(MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPlayersInCreation().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (plugin.getPlayersInCreation().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }

    }

}
