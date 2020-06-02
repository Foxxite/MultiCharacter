package com.foxxite.multicharacter.character.creator;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import com.foxxite.multicharacter.misc.Common;
import com.foxxite.multicharacter.restapi.mineskin.MineskinResponse;
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
import org.bukkit.scheduler.BukkitRunnable;

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

    public CharacterCreator(MultiCharacter plugin) {
        this.plugin = plugin;
        language = plugin.getLanguage();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void run() {
        String creatorTitle = language.getMessage("character-creator.title");

        ArrayList<UUID> localPlayersInCreation = plugin.getPlayersInCreation();
        for (UUID playerUUID : localPlayersInCreation) {
            Player player = Common.getPlayerByUuid(playerUUID);

            if (!playerState.containsKey(playerUUID)) {
                playerState.put(playerUUID, CreatorSate.NAME);
                playerCharacter.put(playerUUID, new EmptyCharacter(plugin, playerUUID));
                player.sendMessage(language.getMessagePAPI("character-creator.guide", player));
                return;
            }

            switch (playerState.get(playerUUID)) {
                case NAME:
                    player.sendTitle(creatorTitle, language.getMessage("character-creator.name"), 0, 20, 0);
                    break;
                case BIRTHDAY:
                    player.sendTitle(creatorTitle, language.getMessage("character-creator.birthday"), 0, 200, 0);
                    break;
                case SEX:
                    player.sendTitle(creatorTitle, language.getMessage("character-creator.sex"), 0, 200, 0);
                    break;
                case NATIONALITY:
                    player.sendTitle(creatorTitle, language.getMessage("character-creator.nationality"), 0, 200, 0);
                    break;
                case SKIN:
                    player.sendTitle(creatorTitle, language.getMessage("character-creator.skin"), 0, 200, 0);
                    break;
                case CREATING:
                    player.sendTitle(creatorTitle, language.getMessage("character-creator.creating"), 0, 200, 0);
                    break;
                case COMPLETE:
                    player.sendTitle("", "", 0, 200, 0);

                    playerCharacter.get(playerUUID).saveToDatabase();
                    updateCreatorState(playerUUID, CreatorSate.DONE);

                    break;
                case DONE:
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            playerState.remove(playerUUID);
                            plugin.getPlayersInCreation().remove(player.getUniqueId());
                            playerCharacter.remove(playerUUID);

                            CharacterSelector characterSelector = new CharacterSelector(plugin, player);
                        }
                    }.runTask(plugin);

                    break;
                default:
                    playerState.put(playerUUID, CreatorSate.NAME);
                    break;
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerLogout(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (playerState.containsKey(player.getUniqueId())) {
            plugin.getPlayersInCreation().remove(player.getUniqueId());
            playerState.remove(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (playerState.containsKey(playerUUID)) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerChatAsync(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (playerState.containsKey(playerUUID)) {
            event.setCancelled(true);

            String message = event.getMessage();

            if (message.isEmpty()) {
                return;
            }

            if (message.equalsIgnoreCase("cancel")) {
                playerCharacter.remove(playerUUID);
                playerState.remove(playerUUID);
                plugin.getPlayersInCreation().remove(player.getUniqueId());

                CharacterSelector characterSelector = new CharacterSelector(plugin, player);
                return;
            }

            switch (playerState.get(playerUUID)) {
                case NAME:
                    playerCharacter.get(playerUUID).setName(StringEscapeUtils.escapeSql(message));
                    updateCreatorState(playerUUID, CreatorSate.BIRTHDAY);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case BIRTHDAY:
                    if (isDateValid(message)) {
                        playerCharacter.get(playerUUID).setBirthday(StringEscapeUtils.escapeSql(message));
                        updateCreatorState(playerUUID, CreatorSate.SEX);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    } else {
                        player.sendMessage(language.getMessage("character-creator.birthday-format-incorrect"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
                    }
                    break;
                case SEX:
                    playerCharacter.get(playerUUID).setSex(StringEscapeUtils.escapeSql(message));
                    updateCreatorState(playerUUID, CreatorSate.NATIONALITY);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case NATIONALITY:
                    playerCharacter.get(playerUUID).setNationality(StringEscapeUtils.escapeSql(message));
                    updateCreatorState(playerUUID, CreatorSate.SKIN);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case SKIN:
                    player.sendMessage(language.getMessage("character-creator.skin-download"));
                    if (isValidImage(message)) {

                        playerCharacter.get(playerUUID).setSkinUrl(StringEscapeUtils.escapeSql(message));

                        player.sendMessage(language.getMessage("character-creator.skin-generate"));
                        updateCreatorState(playerUUID, CreatorSate.CREATING);
                        String skinData = getMineskinData(message);

                        if (!skinData.startsWith("{")) {
                            playerState.remove(playerUUID);
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                player.kickPlayer("An error occurred while getting the Skin data from Mineskin. \n" +
                                        "Please report the following error to staff: " + skinData +
                                        "\n Please try again later.");
                            });
                            return;
                        }

                        deserializeMineskin(skinData, playerUUID);

                        playerCharacter.get(playerUUID).saveToDatabase();
                        updateCreatorState(playerUUID, CreatorSate.COMPLETE);
                    } else {
                        player.sendMessage(language.getMessage("character-creator.skin-format-incorrect"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
                    }
                    break;
            }
        }
    }

    private void updateCreatorState(UUID playerUUID, CreatorSate newState) {
        playerState.remove(playerUUID);
        playerState.put(playerUUID, newState);
    }

    private boolean isDateValid(String date) {

        final String dateFormat = "dd-MM-yyyy";

        try {
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidImage(String imgUrl) {
        Image image = null;
        try {
            URL url = new URL(imgUrl);
            image = ImageIO.read(url);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String getMineskinData(String imageURL) {

        Request request = null;

        try {
            final String url = ("https://api.mineskin.org/generate/url");
            OkHttpClient httpClient = new OkHttpClient();

            // form parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("url", imageURL)
                    .build();

            request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Foxxite's MultiCharacter Spigot Plugin")
                    .post(formBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {

                //Dump request for debugging.
                plugin.getPluginLogger().info("Request:");
                plugin.getPluginLogger().info(request.toString());
                plugin.getPluginLogger().info(request.body().toString());

                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Get response body
                String responseStr = response.body().string();
                return responseStr;
            }

        } catch (Exception ex) {
            plugin.getPluginLogger().severe(ex.getMessage());
            ex.printStackTrace();

            //Dump request for debugging.
            plugin.getPluginLogger().info("Request:");
            plugin.getPluginLogger().info(request.toString());
            plugin.getPluginLogger().info(request.body().toString());

            return ex.getMessage();
        }
    }

    private void deserializeMineskin(String json, UUID playerUUID) {
        Gson gson = new Gson();
        MineskinResponse response = gson.fromJson(json, MineskinResponse.class);

        playerCharacter.get(playerUUID).setSkinValue(response.getData().getTexture().getValue());
        playerCharacter.get(playerUUID).setSkinSignature(response.getData().getTexture().getSignature());
    }

}

