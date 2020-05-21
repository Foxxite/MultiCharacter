package com.foxxite.multicharacter.events.custom;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CharacterQuitEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
