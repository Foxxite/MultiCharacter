package com.foxxite.emptyplugin.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(final Event event) {

    }

}
