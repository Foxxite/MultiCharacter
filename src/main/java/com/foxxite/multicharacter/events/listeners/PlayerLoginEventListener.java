package com.foxxite.multicharacter.events.listeners;

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

    public PlayerLoginEventListener(MultiCharacter plugin) {
        this.plugin = plugin;
        config = plugin.getConfiguration();
        language = plugin.getLanguage();
        updateChecker = plugin.getUpdateChecker();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        if (player.isOp()) {
            if (updateChecker.getUpdateCheckResult() != (UP_TO_DATE)) {
                HashMap<String, String> placeholders = new HashMap<>();

                String newVersion = (updateChecker.getLatestVersionString() != null ? updateChecker.getLatestVersionString() : "N/A");
                String updateUrl = (updateChecker.getResourceURL() != null ? updateChecker.getResourceURL() : "N/A");

                placeholders.put("{newVersion}", newVersion);
                placeholders.put("{updateUrl}", updateUrl);
                placeholders.put("{checkResult}", updateChecker.getUpdateCheckResult().toString());

                List<String> updateMSG = language.getMultiLineMessageCustom("update", placeholders);
                for (String message : updateMSG) {
                    player.sendMessage(message);
                }
            }
        }

        if (player.isDead()) {
            player.spigot().respawn();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
        }

        CharacterSelector characterSelector = new CharacterSelector(plugin, player);

    }

}
