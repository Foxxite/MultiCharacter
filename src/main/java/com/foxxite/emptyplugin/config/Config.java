package com.foxxite.emptyplugin.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Config {

    private final Plugin plugin;
    private FileConfiguration config;

    public Config(Plugin plugin) {

        this.plugin = plugin;
        try {
            createOrLoadConfig();
        } catch (Exception ex) {
            plugin.getLogger().log(new LogRecord(Level.SEVERE, ex.getMessage() + " " + ex.getCause()));
            ex.printStackTrace();
        }

    }

    public boolean reloadConfig() {
        if (createOrLoadConfig()) {
            return true;
        }
        return false;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    private boolean createOrLoadConfig() {
        final String filename = "config.yml";
        File configFile = new File(plugin.getDataFolder(), filename);

        //If we don't have a config file, save it to the data folder
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(filename, false);
        } else {
            //Check if the config is outdated, if so update it.
            try {
                //Load the config from the jar file
                YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(configFile);
                InputStream internalConfigFile = plugin.getResource(filename);

                //Load the config from the disk
                Reader internalConfigFileReader = new InputStreamReader(internalConfigFile);
                YamlConfiguration internalConfig = YamlConfiguration.loadConfiguration(internalConfigFileReader);

                //Check if the disk version is lower then the jar version
                if (tempConfig.getInt("file-version") < internalConfig.getInt("file-version") || tempConfig.getInt("file-version") == -1) {

                    //Save the old config to config_old.yml
                    File oldConfigFile = new File(plugin.getDataFolder(), filename + "_old.yml");
                    configFile.getParentFile().mkdirs();
                    tempConfig.save(oldConfigFile);

                    //Save the new config to the disk
                    plugin.saveResource(filename, true);
                }

                //Close the file reader
                internalConfigFileReader.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage() + " " + ex.getCause());
                ex.printStackTrace();
            }
        }

        //Load config from disk
        config = new YamlConfiguration();
        try {
            config.load(configFile);
            return true;
        } catch (Exception ex) {
            plugin.getLogger().log(new LogRecord(Level.SEVERE, ex.getMessage() + " " + ex.getCause()));
            ex.printStackTrace();
            return false;
        }
    }

}
