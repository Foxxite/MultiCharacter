package com.foxxite.multicharacter.creator;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import com.foxxite.multicharacter.misc.Common;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.UUID;

public class CharacterCreator extends TimerTask implements Listener {

    private final MultiCharacter plugin;
    private final HashMap<UUID, CreatorSate> playerState = new HashMap<>();
    private final HashMap<UUID, EmptyCharacter> playerCharacter = new HashMap<>();
    private final Language language;

    public CharacterCreator(final MultiCharacter plugin) {
        this.plugin = plugin;
        this.language = plugin.getLanguage();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void run() {
        final String creatorTitle = this.language.getMessage("character-creator.title");

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {

            final Iterator<UUID> iterator = this.plugin.getPlayersInCreation().iterator();
            while (iterator.hasNext()) {
                final UUID playerUUID = iterator.next();
                final Player player = Common.getPlayerByUuid(playerUUID);

                if (!this.playerState.containsKey(playerUUID)) {
                    this.playerState.put(playerUUID, CreatorSate.NAME);
                    this.playerCharacter.put(playerUUID, new EmptyCharacter(this.plugin, playerUUID));
                    player.sendMessage(this.language.getMessagePAPI("character-creator.guide", player));
                    return;
                }

                switch (this.playerState.get(playerUUID)) {
                    case NAME:
                        player.sendTitle(creatorTitle, this.language.getMessage("character-creator.name"), 0, 20, 0);
                        break;
                    case BIRTHDAY:
                        player.sendTitle(creatorTitle, this.language.getMessage("character-creator.birthday"), 0, 200, 0);
                        break;
                    case SEX:
                        player.sendTitle(creatorTitle, this.language.getMessage("character-creator.sex"), 0, 200, 0);
                        break;
                    case NATIONALITY:
                        player.sendTitle(creatorTitle, this.language.getMessage("character-creator.nationality"), 0, 200, 0);
                        break;
                    case SKIN:
                        player.sendTitle(creatorTitle, this.language.getMessage("character-creator.skin"), 0, 200, 0);
                        break;
                    case COMPLETE:
                        this.playerCharacter.get(playerUUID).saveToDatabase();
                        this.playerCharacter.remove(playerUUID);
                        this.playerState.remove(playerUUID);
                        this.plugin.getPlayersInCreation().remove(player.getUniqueId());

                        final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);

                        break;
                    default:
                        this.playerState.put(playerUUID, CreatorSate.NAME);
                        break;
                }

            }

        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerLogout(final PlayerQuitEvent event) {

        final Player player = event.getPlayer();

        if (this.playerState.containsKey(player.getUniqueId())) {
            this.plugin.getPlayersInCreation().remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();

        if (this.playerState.containsKey(player.getUniqueId())) {
            event.setCancelled(true);

            final String message = event.getMessage();

            if (message.isEmpty())
                return;

            switch (this.playerState.get(playerUUID)) {
                case NAME:
                    if (message.equalsIgnoreCase("cancel")) {
                        this.playerCharacter.remove(playerUUID);
                        this.playerState.remove(playerUUID);
                        this.plugin.getPlayersInCreation().remove(player.getUniqueId());

                        final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);
                        return;
                    }

                    this.playerCharacter.get(playerUUID).setName(message);
                    this.updateCreatorState(playerUUID, CreatorSate.BIRTHDAY);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case BIRTHDAY:
                    if (this.isDateValid(message)) {
                        this.playerCharacter.get(playerUUID).setBirthday(message);
                        this.updateCreatorState(playerUUID, CreatorSate.SEX);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    } else {
                        player.sendMessage(this.language.getMessage("character-creator.birthday-format-incorrect"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
                    }
                    break;
                case SEX:
                    this.playerCharacter.get(playerUUID).setSex(message);
                    this.updateCreatorState(playerUUID, CreatorSate.NATIONALITY);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case NATIONALITY:
                    this.playerCharacter.get(playerUUID).setNationality(message);
                    this.updateCreatorState(playerUUID, CreatorSate.SKIN);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case SKIN:
                    if (this.isUUIDValid(message)) {
                        this.playerCharacter.get(playerUUID).setSkinUUID(message);
                        this.updateCreatorState(playerUUID, CreatorSate.COMPLETE);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    } else {
                        player.sendMessage(this.language.getMessage("character-creator.skin-format-incorrect"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
                    }
                    break;
            }

        }
    }

    private void updateCreatorState(final UUID playerUUID, final CreatorSate newState) {
        this.playerState.remove(playerUUID);
        this.playerState.put(playerUUID, newState);
    }

    private boolean isDateValid(final String date) {

        final String dateFormat = "dd-MM-yyyy";

        try {
            final DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (final ParseException e) {
            return false;
        }
    }

    private boolean isUUIDValid(final String someUUID) {
        try {
            final UUID uuid = UUID.fromString(someUUID);
            return true;
        } catch (final IllegalArgumentException exception) {
            return false;
        }
    }

}

