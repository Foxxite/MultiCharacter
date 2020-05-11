package com.foxxite.multicharacter.events;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import com.foxxite.multicharacter.misc.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;

import static com.foxxite.multicharacter.misc.UpdateChecker.UpdateCheckResult.UP_TO_DATE;

public class PlayerLoginEventListener implements Listener {

    private final MultiCharacter plugin;
    private final FileConfiguration config;
    private final Language language;
    private final UpdateChecker updateChecker;

    public PlayerLoginEventListener(final MultiCharacter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.language = plugin.getLanguage();
        this.updateChecker = plugin.getUpdateChecker();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(final PlayerJoinEvent event) {

        final Player player = event.getPlayer();

        if (player.isOp()) {
            if (this.updateChecker.getUpdateCheckResult() != (UP_TO_DATE)) {
                final HashMap<String, String> placeholders = new HashMap<>();

                final String newVersion = (this.updateChecker.getLatestVersionString() != null ? this.updateChecker.getLatestVersionString() : "N/A");
                final String updateUrl = (this.updateChecker.getResourceURL() != null ? this.updateChecker.getResourceURL() : "N/A");

                placeholders.put("{newVersion}", newVersion);
                placeholders.put("{updateUrl}", updateUrl);
                placeholders.put("{checkResult}", this.updateChecker.getUpdateCheckResult().toString());

                final List<String> updateMSG = this.language.getMultiLineMessageCustom("update", placeholders);
                for (final String message : updateMSG) {
                    player.sendMessage(message);
                }
            }
        }

        if (player.isDead())
            player.spigot().respawn();

        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
        }

        final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);

    }

}
