package com.foxxite.multicharacter.misc;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {
    private int resourceId;
    private URL resourceURL;
    private String currentVersionString;
    private String latestVersionString;
    private UpdateCheckResult updateCheckResult;

    public UpdateChecker(final int resourceId, final JavaPlugin plugin) {
        try {
            this.resourceId = resourceId;
            this.resourceURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        } catch (final Exception exception) {
            System.out.println(exception.getCause() + " " + exception.getMessage());
            System.out.println(exception.getStackTrace());
            return;
        }

        this.currentVersionString = plugin.getDescription().getVersion();
        this.latestVersionString = this.getLatestVersion();

        if (this.latestVersionString == null) {
            this.updateCheckResult = UpdateCheckResult.NO_RESULT;
            return;
        }

        final int currentVersion = Integer.parseInt(this.currentVersionString.replace("v", "").replace(".", ""));
        final int latestVersion = Integer.parseInt(this.getLatestVersion().replace("v", "").replace(".", ""));

        if (currentVersion < latestVersion) this.updateCheckResult = UpdateCheckResult.OUT_DATED;
        else if (currentVersion == latestVersion) this.updateCheckResult = UpdateCheckResult.UP_TO_DATE;
        else this.updateCheckResult = UpdateCheckResult.UNRELEASED;
    }

    public int getResourceId() {
        return this.resourceId;
    }

    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + this.resourceId;
    }

    public String getCurrentVersionString() {
        return this.currentVersionString;
    }

    public String getLatestVersionString() {
        return this.latestVersionString;
    }

    public UpdateCheckResult getUpdateCheckResult() {
        return this.updateCheckResult;
    }

    public String getLatestVersion() {
        try {
            final URLConnection urlConnection = this.resourceURL.openConnection();
            return new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
        } catch (final Exception exception) {
            return null;
        }
    }

    public enum UpdateCheckResult {
        NO_RESULT, OUT_DATED, UP_TO_DATE, UNRELEASED,
    }
}
