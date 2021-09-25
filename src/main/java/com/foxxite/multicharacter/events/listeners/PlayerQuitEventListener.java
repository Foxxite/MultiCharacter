package com.foxxite.multicharacter.events.listeners;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.misc.UUIDHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {

    private final MultiCharacter plugin;

    public PlayerQuitEventListener(MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        UUIDHandler.RESET_UUID(player);

        plugin.getPluginLogger().info("Player quit event fired for: " + player.getName());

        if (plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
            plugin.getPluginLogger().info("PlayerData saved for: " + player.getName());

            Character character = plugin.getActiveCharacters().get(player.getUniqueId());
            character.saveData(player);
            plugin.getActiveCharacters().remove(player.getUniqueId());

            return;
        }

        plugin.getPluginLogger().info("No active characters found for: " + player.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();

        if (plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
            Character character = plugin.getActiveCharacters().get(player.getUniqueId());
            character.saveData(player);
            plugin.getActiveCharacters().remove(player.getUniqueId());
        }

    }
}
