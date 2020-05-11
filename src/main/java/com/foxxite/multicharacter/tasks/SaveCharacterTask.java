package com.foxxite.multicharacter.tasks;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.misc.Common;
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

        Common.broadcastActionBar(this.plugin.getLanguage().getMessage("saving.start"));

        final HashMap<UUID, Character> localActiveCharacters = (HashMap<UUID, Character>) this.plugin.getActiveCharacters().clone();

        localActiveCharacters.forEach((uuid, character) -> {
            character.saveData();

            final Player player = Common.getPlayerByUuid(uuid);

            if (!player.isOnline() || player == null) {
                this.plugin.getActiveCharacters().remove(player.getUniqueId());
            }
        });

        Common.broadcastActionBar(this.plugin.getLanguage().getMessage("saving.complete"));
    }
}
