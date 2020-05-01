package com.foxxite.multicharacter.creator;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import com.foxxite.multicharacter.mineskin.MineskinResponse;
import com.foxxite.multicharacter.misc.Common;
import com.google.gson.Gson;
import okhttp3.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

        final ArrayList<UUID> localPlayersInCreation = this.plugin.getPlayersInCreation();
        for (final UUID playerUUID : localPlayersInCreation) {
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
                case CREATING:
                    player.sendTitle(creatorTitle, this.language.getMessage("character-creator.creating"), 0, 200, 0);
                    break;
                case COMPLETE:
                    player.sendTitle("", "", 0, 200, 0);
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
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerLogout(final PlayerQuitEvent event) {

        final Player player = event.getPlayer();

        if (this.playerState.containsKey(player.getUniqueId())) {
            this.plugin.getPlayersInCreation().remove(player.getUniqueId());
            this.playerState.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerChat(final PlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();

        if (this.playerState.containsKey(playerUUID)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerChatAsync(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();

        if (this.playerState.containsKey(playerUUID)) {
            event.setCancelled(true);

            final String message = event.getMessage();

            if (message.isEmpty())
                return;

            if (message.equalsIgnoreCase("cancel")) {
                this.playerCharacter.remove(playerUUID);
                this.playerState.remove(playerUUID);
                this.plugin.getPlayersInCreation().remove(player.getUniqueId());

                final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);
                return;
            }

            switch (this.playerState.get(playerUUID)) {
                case NAME:
                    this.playerCharacter.get(playerUUID).setName(StringEscapeUtils.escapeSql(message));
                    this.updateCreatorState(playerUUID, CreatorSate.BIRTHDAY);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case BIRTHDAY:
                    if (this.isDateValid(message)) {
                        this.playerCharacter.get(playerUUID).setBirthday(StringEscapeUtils.escapeSql(message));
                        this.updateCreatorState(playerUUID, CreatorSate.SEX);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    } else {
                        player.sendMessage(this.language.getMessage("character-creator.birthday-format-incorrect"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
                    }
                    break;
                case SEX:
                    this.playerCharacter.get(playerUUID).setSex(StringEscapeUtils.escapeSql(message));
                    this.updateCreatorState(playerUUID, CreatorSate.NATIONALITY);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case NATIONALITY:
                    this.playerCharacter.get(playerUUID).setNationality(StringEscapeUtils.escapeSql(message));
                    this.updateCreatorState(playerUUID, CreatorSate.SKIN);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case SKIN:

                    player.sendMessage(this.language.getMessage("character-creator.skin-download"));
                    if (this.isValidImage(message)) {

                        this.playerCharacter.get(playerUUID).setSkinUrl(StringEscapeUtils.escapeSql(message));

                        player.sendMessage(this.language.getMessage("character-creator.skin-generate"));
                        this.updateCreatorState(playerUUID, CreatorSate.CREATING);
                        final String skinData = this.getMineskinData(message);
                        if (skinData == null) {
                            this.playerState.remove(playerUUID);
                            Bukkit.getScheduler().runTask(this.plugin, () -> {
                                player.kickPlayer("An error occurred while creating your character, please try again later.");
                            });
                            return;
                        }

                        this.deserializeMineskin(skinData, playerUUID);

                        this.playerCharacter.get(playerUUID).saveToDatabase();
                        this.updateCreatorState(playerUUID, CreatorSate.COMPLETE);
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

    private boolean isValidImage(final String imgUrl) {
        Image image = null;
        try {
            final URL url = new URL(imgUrl);
            image = ImageIO.read(url);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    private String getMineskinData(final String imageURL) {

        try {
            final String url = ("https://api.mineskin.org/generate/url");
            final OkHttpClient httpClient = new OkHttpClient();

            // form parameters
            final RequestBody formBody = new FormBody.Builder()
                    .add("url", imageURL)
                    .build();

            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "OkHttp Bot")
                    .post(formBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Get response body
                final String responseStr = response.body().string();
                return responseStr;
            }

        } catch (final Exception ex) {
            this.plugin.getPluginLogger().severe(ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    private void deserializeMineskin(final String json, final UUID playerUUID) {
        final Gson gson = new Gson();
        final MineskinResponse response = gson.fromJson(json, MineskinResponse.class);

        this.playerCharacter.get(playerUUID).setSkinValue(response.getData().getTexture().getValue());
        this.playerCharacter.get(playerUUID).setSkinSignature(response.getData().getTexture().getSignature());
    }

}

