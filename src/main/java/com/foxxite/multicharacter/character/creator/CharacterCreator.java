package com.foxxite.multicharacter.character.creator;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.misc.Common;
import com.foxxite.multicharacter.restapi.mineskin.MineskinResponse;
import com.foxxite.multicharacter.worldspacemenu.WorldSpaceMenu;
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
    private int mineSkinTries = 0;

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

            if (!playerState.containsKey(playerUUID) && player != null) {
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
                case MODEL:
                    player.sendTitle(creatorTitle, language.getMessage("character-creator.model"), 0, 200, 0);
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
                            removePlayerFromCreator(player);

                            plugin.getPlayersInWorldMenu().put(player.getUniqueId(), new WorldSpaceMenu(plugin, player));

                            //CharacterSelector characterSelector = new CharacterSelector(plugin, player);
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
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        removePlayerFromCreator(player);

                        plugin.getPlayersInWorldMenu().put(player.getUniqueId(), new WorldSpaceMenu(plugin, player));

                        //CharacterSelector characterSelector = new CharacterSelector(plugin, player);
                    }
                }.runTask(plugin);
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
                    updateCreatorState(playerUUID, CreatorSate.MODEL);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                    break;
                case MODEL:
                    switch (message) {
                        case "NORMAL":
                        case "SLIM":
                            playerCharacter.get(playerUUID).setModel(StringEscapeUtils.escapeSql(message));
                            updateCreatorState(playerUUID, CreatorSate.SKIN);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f);
                            break;
                        default:
                            player.sendMessage(language.getMessage("character-creator.model-format-incorrect"));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
                            break;
                    }
                    break;
                case SKIN:
                    player.sendMessage(language.getMessage("character-creator.skin-download"));

                    HashMap<Boolean, String> imageData = isValidImage(message);

                    if (imageData.containsKey(true)) {

                        playerCharacter.get(playerUUID).setSkinUrl(StringEscapeUtils.escapeSql(message));

                        player.sendMessage(language.getMessage("character-creator.skin-generate"));
                        updateCreatorState(playerUUID, CreatorSate.CREATING);

                        while (mineSkinTries < 3) {
                            String skinData = getMineskinData(message, playerUUID);

                            if (!skinData.startsWith("{")) {

                                if (mineSkinTries == 2) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        removePlayerFromCreator(player);

                                        HashMap<String, String> placeholders = new HashMap<>();
                                        placeholders.put("{error}", skinData);
                                        player.kickPlayer(language.getMessagePlaceholders("mineskin-kick", placeholders));
                                    });
                                    return;
                                } else {
                                    HashMap<String, String> placeholders = new HashMap<>();

                                    placeholders.put("{error}", skinData);
                                    placeholders.put("{attempt}", String.valueOf((mineSkinTries + 1)));
                                    placeholders.put("{maxAttempts}", "3");

                                    player.sendMessage(language.getMessagePlaceholders("mineskin-error", placeholders));
                                }

                                mineSkinTries++;

                            } else {
                                deserializeMineskin(skinData, playerUUID);

                                playerCharacter.get(playerUUID).saveToDatabase();
                                updateCreatorState(playerUUID, CreatorSate.COMPLETE);

                                break;
                            }
                        }
                    } else {

                        HashMap<String, String> placeholders = new HashMap<>();
                        placeholders.put("{error}", imageData.get(false));

                        player.sendMessage(language.getMessagePlaceholders("character-creator.skin-format-incorrect", placeholders));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1f, 1f);
                    }
                    break;
            }
        }
    }

    private void removePlayerFromCreator(Player player) {
        UUID playerUUID = player.getUniqueId();

        playerState.remove(playerUUID);
        plugin.getPlayersInCreation().remove(player.getUniqueId());
        playerCharacter.remove(playerUUID);
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

    private HashMap<Boolean, String> isValidImage(String imgUrl) {
        Image image = null;

        HashMap<Boolean, String> output = new HashMap<>();

        try {
            URL url = new URL(imgUrl);
            image = ImageIO.read(url);

            output.put(true, imgUrl);
            return output;
        } catch (IOException e) {
            output.put(false, e.getMessage());
            return output;
        }
    }

    private String getMineskinData(String imageURL, UUID playerUUID) {

        Request request = null;

        try {
            final String url = ("https://api.mineskin.org/generate/url");
            OkHttpClient httpClient = new OkHttpClient();

            // form parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("url", imageURL)
                    .add("model", (playerCharacter.get(playerUUID).getModel().equalsIgnoreCase("SLIM") ? "slim" : ""))
                    .add("visibility", "1")
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
            plugin.getPluginLogger().severe(ex.getMessage() + " " + ex.getCause());
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

