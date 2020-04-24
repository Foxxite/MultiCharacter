package com.foxxite.multicharacter.events;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.misc.Character;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.HashMap;
import java.util.UUID;

public class WorldSaveEventListener implements Listener {

    private final MultiCharacter plugin;

    public WorldSaveEventListener(final MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onWorldSave(final WorldSaveEvent event) {

        Bukkit.broadcastMessage("World Save Event");

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final HashMap<UUID, Character> localActiveCharacters = (HashMap<UUID, Character>) this.plugin.getActiveCharacters().clone();
            if (localActiveCharacters.containsKey(player.getUniqueId())) {
                localActiveCharacters.get(player.getUniqueId()).saveData();
            }
        }

    }

}
