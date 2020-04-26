package com.foxxite.multicharacter.events;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.misc.Character;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {

    private final MultiCharacter plugin;

    public PlayerQuitEventListener(final MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (this.plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
            final Character character = this.plugin.getActiveCharacters().get(player.getUniqueId());
            character.saveData(player);
            this.plugin.getActiveCharacters().remove(player.getUniqueId());
        }

    }


}
