package com.foxxite.multicharacter.events.listeners;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
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

    public WorldSaveEventListener(MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onWorldSave(WorldSaveEvent event) {

        for (Player player : Bukkit.getOnlinePlayers()) {
            HashMap<UUID, Character> localActiveCharacters = (HashMap<UUID, Character>) plugin.getActiveCharacters().clone();
            if (localActiveCharacters.containsKey(player.getUniqueId())) {
                localActiveCharacters.get(player.getUniqueId()).saveData();
            }
        }

    }

}
