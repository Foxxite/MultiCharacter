package com.foxxite.multicharacter.events;

import com.foxxite.multicharacter.MultiCharacter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveEventListener implements Listener {

    private final MultiCharacter plugin;

    public PlayerMoveEventListener(final MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        if (this.plugin.getPlayersInCreation().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (this.plugin.getPlayersInCreation().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
