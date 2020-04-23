package com.foxxite.emptyplugin.configs;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Language {

    private final Plugin plugin;
    private Boolean hasPlaceholderAPI = false;
    private FileConfiguration languageConfig;

    public Language(final Plugin plugin) {

        this.plugin = plugin;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            plugin.getLogger().log(new LogRecord(Level.INFO, "Hooked into PAPI"));
            this.hasPlaceholderAPI = true;
        }

        try {
            this.createOrLoadLanguage();
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

    }

    public Boolean reloadLanguage() {
        try {
            this.createOrLoadLanguage();
            return true;
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    private FileConfiguration getLanguageConfig() {
        return this.languageConfig;
    }

    public String getMessage(final String key) {
        String output = this.getLanguageConfig().getString(key);

        if (output == null) {
            final HashMap<String, String> placeholder = new HashMap<>();
            placeholder.put("{key}", key);

            return Common.colorize(this.getMessagePlaceholders("key-missing", placeholder));
        }

        output = this.addInternalPlaceholders(output);

        return Common.colorize(output);
    }

    public String getMessagePAPI(final String key, final Player player) {
        return this.addPAPIPlaceholders(this.getMessage(key), player);
    }

    public String getMessagePlaceholders(final String key, final HashMap<String, String> customPlaceholders) {
        String output = this.getMessage(key);

        output = this.addCustomPlaceholders(output, customPlaceholders);

        return Common.colorize(output);
    }

    public String getMessagePAPIAndCustom(final String key, final Player player, final HashMap<String, String> customPlaceholders) {
        String output = this.getMessagePlaceholders(key, customPlaceholders);

        output = this.addPAPIPlaceholders(output, player);
        output = this.addCustomPlaceholders(output, customPlaceholders);

        return Common.colorize(output);
    }


    public ArrayList<String> getMultiLineMessage(final String key) {

        final ArrayList<String> outputList = new ArrayList<>();
        final List<String> rawList = this.getLanguageConfig().getStringList(key);

        for (final String line : rawList) {
            outputList.add(Common.colorize(this.addInternalPlaceholders(line)));
        }

        return outputList;
    }

    public ArrayList<String> getMultiLineMessagePAPI(final String key, final Player player) {

        final ArrayList<String> outputList = new ArrayList<>();
        final List<String> rawList = this.getLanguageConfig().getStringList(key);

        for (final String line : rawList) {
            outputList.add(Common.colorize(this.addPAPIPlaceholders(this.addInternalPlaceholders(line), player)));
        }

        return outputList;
    }

    public ArrayList<String> getMultiLineMessageCustom(final String key, final HashMap<String, String> customPlaceholders) {

        final ArrayList<String> outputList = new ArrayList<>();
        final List<String> rawList = this.getLanguageConfig().getStringList(key);

        for (final String line : rawList) {
            outputList.add(Common.colorize(this.addCustomPlaceholders(this.addInternalPlaceholders(line), customPlaceholders)));
        }

        return outputList;
    }

    public ArrayList<String> getMultiLineMessagePAPIAndCustom(final String key, final Player player, final HashMap<String, String> customPlaceholders) {

        final ArrayList<String> outputList = new ArrayList<>();
        final List<String> rawList = this.getLanguageConfig().getStringList(key);

        for (final String line : rawList) {
            outputList.add(Common.colorize(this.addCustomPlaceholders(this.addPAPIPlaceholders(this.addInternalPlaceholders(line), player), customPlaceholders)));
        }

        return outputList;
    }


    private String addInternalPlaceholders(final String string) {
        String output = string;
        output = output.replace("{prefix}", Objects.requireNonNull(this.getLanguageConfig().getString("prefix")));
        output = output.replace("{version}", this.plugin.getDescription().getVersion());
        output = output.replace("{author}", this.plugin.getDescription().getAuthors().get(0));

        return output;
    }

    private String addPAPIPlaceholders(String string, final Player player) {
        if (this.hasPlaceholderAPI) {
            string = PlaceholderAPI.setPlaceholders(player, string);
        }
        return string;
    }

    private String addCustomPlaceholders(String string, final HashMap<String, String> customPlaceholders) {
        for (final Map.Entry<String, String> customPlaceholderEntry : customPlaceholders.entrySet()) {
            string = string.replace(customPlaceholderEntry.getKey(), customPlaceholderEntry.getValue());
        }

        return string;
    }

    private void createOrLoadLanguage() {
        final String filename = "lang.yml";
        final File configFile = new File(this.plugin.getDataFolder(), filename);

        //If we don't have a config file, save it to the data folder
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            this.plugin.saveResource(filename, false);
        } else {
            //Check if the config is outdated, if so update it.
            try {
                //Load the config from the jar file
                final YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(configFile);
                final InputStream internalConfigFile = this.plugin.getResource(filename);

                //Load the config from the disk
                final Reader internalConfigFileReader = new InputStreamReader(internalConfigFile);
                final YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(internalConfigFileReader);

                //Check if the disk version is lower then the jar version
                if (tempConfig.getInt("file-version") < internalConfig.getInt("file-version") || tempConfig.getInt("file-version") == -1) {

                    //Save the old config to config_old.yml
                    final File oldConfigFile = new File(this.plugin.getDataFolder(), filename + "_old.yml");
                    configFile.getParentFile().mkdirs();
                    tempConfig.save(oldConfigFile);

                    //Save the new config to the disk
                    this.plugin.saveResource(filename, true);
                }

                //Close the file reader
                internalConfigFileReader.close();
            } catch (final Exception ex) {
                System.out.println(ex.getMessage() + " " + ex.getCause());
                ex.printStackTrace();
            }
        }

        //Load config from disk
        this.languageConfig = new YamlConfiguration();
        try {
            this.languageConfig.load(configFile);
        } catch (final Exception ex) {
            System.out.println(ex.getMessage() + " " + ex.getCause());
            ex.printStackTrace();
        }
    }
}
