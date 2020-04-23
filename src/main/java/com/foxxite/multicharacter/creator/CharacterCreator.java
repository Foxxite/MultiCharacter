package com.foxxite.multicharacter.creator;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;

public class CharacterCreator extends TimerTask implements Listener {

    private final MultiCharacter plugin;
    private final HashMap<UUID, CreatorSate> playerState = new HashMap<>();
    private final Language language;

    public CharacterCreator(final MultiCharacter plugin) {
        this.plugin = plugin;
        this.language = plugin.getLanguage();
    }


    public void run() {
        final String creatorTitle = this.language.getMessage("character-creator.title");

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {

            for (final UUID playerUUID : this.plugin.getPlayersInCreation()) {
                final Player player = Common.getPlayerByUuid(playerUUID);

                if (!this.playerState.containsKey(playerUUID)) {
                    this.playerState.put(playerUUID, CreatorSate.NAME);
                    return;
                }

                switch (this.playerState.get(playerUUID)) {
                    case NAME:
                        player.sendTitle(creatorTitle, this.language.getMessage("character-creator.name"), 0, 20, 0);
                        break;
                    case BIRTHDAY:
                        player.sendTitle(this.language.getMessage("character-creator.title"), this.language.getMessage("character-creator.birthday"), 0, 200, 0);
                        break;
                    case SEX:
                        player.sendTitle(this.language.getMessage("character-creator.title"), this.language.getMessage("character-creator.sex"), 0, 200, 0);
                        break;
                    case NATIONALITY:
                        player.sendTitle(this.language.getMessage("character-creator.title"), this.language.getMessage("character-creator.nationality"), 0, 200, 0);
                        break;
                    case SKIN:
                        player.sendTitle(this.language.getMessage("character-creator.skin"), this.language.getMessage("character-creator.name"), 0, 200, 0);
                        break;
                    case COMPLETE:
                        break;
                    default:
                        this.playerState.put(playerUUID, CreatorSate.NAME);
                        break;
                }

            }

        }, 1L);
    }
}

