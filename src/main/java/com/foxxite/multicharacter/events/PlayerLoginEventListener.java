package com.foxxite.multicharacter.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(final PlayerJoinEvent event) {


    }

}
