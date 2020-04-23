package com.foxxite.multicharacter.events;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.configs.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerLoginEventListener implements Listener {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final Language language;

    public PlayerLoginEventListener(final MultiCharacter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.language = plugin.getLanguage();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(final PlayerJoinEvent event) {

        final Player player = event.getPlayer();

        final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);

    }

}
