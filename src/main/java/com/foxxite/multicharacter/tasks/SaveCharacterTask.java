package com.foxxite.multicharacter.tasks;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.misc.Common;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;

public class SaveCharacterTask extends TimerTask {

    private final MultiCharacter plugin;
    FileConfiguration config;

    public SaveCharacterTask(MultiCharacter plugin) {
        this.plugin = plugin;
        config = plugin.getConfiguration();
    }

    @Override
    public void run() {

        Common.broadcastActionBar(plugin.getLanguage().getMessage("saving.start"));

        HashMap<UUID, Character> localActiveCharacters = (HashMap<UUID, Character>) plugin.getActiveCharacters().clone();

        localActiveCharacters.forEach((uuid, character) -> {
            character.saveData();

            Player player = Common.getPlayerByUuid(uuid);

            if (player == null) {
                return;
            }
        });

        if (config.getBoolean("show-save")) {
            Common.broadcastActionBar(plugin.getLanguage().getMessage("saving.complete"));
        }

    }
}
