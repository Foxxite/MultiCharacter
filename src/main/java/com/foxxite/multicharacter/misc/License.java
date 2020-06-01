package com.foxxite.multicharacter.misc;

import com.foxxite.multicharacter.MultiCharacter;
import lombok.Getter;
import okhttp3.*;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import static com.foxxite.multicharacter.MultiCharacter.*;

public class License {

    private final MultiCharacter plugin;
    private final String filename = "license.txt";
    private final int secondsInDay = 86400;

    @Getter
    private boolean continueLoad = false;

    private FileWriter fileWriter;
    private FileInputStream fileInputStream;

    public License(MultiCharacter plugin) {
        this.plugin = plugin;

        File disableFile = new File(this.plugin.getDataFolder(), filename);

        int resourceID;
        try {
            resourceID = Integer.parseInt(RESOURCE_ID);
        } catch (Exception ex) {
            resourceID = plugin.getSpigotResourceID();
        }

        long currTime = (System.currentTimeMillis() / 1000);// * resourceID;

        try {
            fileWriter = new FileWriter(disableFile, false);
            fileInputStream = new FileInputStream(disableFile);

            // Check if license timed out
            String savedTime = "";
            int i;
            while ((i = fileInputStream.read()) != -1) {
                savedTime += (char) i;
            }

            // New License
            if (savedTime.isEmpty()) {
                String nexTimeout = "" + (currTime + secondsInDay);

                savedTime = nexTimeout;
                resetLicenseFile(nexTimeout);
            }

            // If license timed out, check if we can renew.
            BigInteger timeInFile = new BigInteger(savedTime);
            if (timeInFile.longValue() < currTime) {

                HttpUrl url = new HttpUrl.Builder()
                        .scheme("https")
                        .host("api.foxxite.com")
                        .build();
                OkHttpClient httpClient = new OkHttpClient();

                Request request = null;

                // form parameters
                RequestBody formBody = new FormBody.Builder()
                        .add("action", "spigot")
                        .add("user", USER_ID)
                        .add("resource", RESOURCE_ID)
                        .add("platform", PLATFORM)
                        .build();

                request = new Request.Builder()
                        .url(url)
                        .addHeader("User-Agent", "OkHttp Bot")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
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
                    plugin.getPluginLogger().info(responseStr);
                }
            }

            fileInputStream.close();
            fileWriter.close();
        } catch (Exception ex) {
            plugin.getPluginLogger().severe("Fatal error occurred, could not verify license. !!! Disabling Plugin !!!");
            plugin.getPluginLogger().severe(ex.getMessage() + " " + ex.getCause());
            ex.printStackTrace();

            continueLoad = false;

            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        try {
            fileInputStream.close();
            fileWriter.close();
        } catch (IOException e) {
            plugin.getPluginLogger().severe(e.getMessage() + " " + e.getCause());
            e.printStackTrace();
        }

    }

    void resetLicenseFile(String newTimeout) {
        try {
            plugin.getPluginLogger().info("Write to file: " + newTimeout);

            // Reset license file
            fileWriter.write(newTimeout);
        } catch (Exception ex) {
            plugin.getPluginLogger().severe("Fatal error occurred, could not verify license. !!! Disabling Plugin !!!");
            plugin.getPluginLogger().severe(ex.getMessage() + " " + ex.getCause());
            ex.printStackTrace();

            continueLoad = false;

            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

}
