package com.foxxite.multicharacter.tasks;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.misc.Character;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;

public class SaveCharacterTask extends TimerTask {

    private final MultiCharacter plugin;

    public SaveCharacterTask(final MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (final Player player : Bukkit.getOnlinePlayers()) {

            final HashMap<UUID, Character> localActiveCharacters = (HashMap<UUID, Character>) this.plugin.getActiveCharacters().clone();

            if (localActiveCharacters.containsKey(player.getUniqueId())) {
                localActiveCharacters.get(player.getUniqueId()).saveData();
            }
        }
    }
}
