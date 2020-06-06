package com.foxxite.multicharacter;

import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.character.creator.CharacterCreator;
import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.events.listeners.*;
import com.foxxite.multicharacter.misc.CommandHandler;
import com.foxxite.multicharacter.misc.PAPIPlaceholders;
import com.foxxite.multicharacter.misc.UUIDHandler;
import com.foxxite.multicharacter.misc.UpdateChecker;
import com.foxxite.multicharacter.sql.SQLHandler;
import com.foxxite.multicharacter.tasks.AnimateToPosition;
import com.foxxite.multicharacter.tasks.SaveCharacterTask;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static com.foxxite.multicharacter.misc.UpdateChecker.UpdateCheckResult.UP_TO_DATE;

public class MultiCharacter extends JavaPlugin {

    public static final String RESOURCE_ID = "%%__RESOURCE__%%";
    public static final String USER_ID = "%%__USER__%%";
    public static final String NONCE = "%%__NONCE__%%";
    public static String PLATFORM = "%%__SONGODA__%%";

    @Getter
    private final ArrayList<UUID> playersInCreation = new ArrayList<>();
    @Getter
    private final HashMap<UUID, Character> activeCharacters = new HashMap<>();
    @Getter
    private final HashMap<UUID, Location> animateToLocation = new HashMap<>();
    @Getter
    private final int spigotResourceID = 78441;
    @Getter
    private UpdateChecker updateChecker;
    @Getter
    private SQLHandler sqlHandler;
    @Getter
    private Language language;
    @Getter
    private Config configRaw;
    @Getter
    private FileConfiguration configuration;
    @Getter
    private PluginLogger pluginLogger;
    @Getter
    private Permission vaultPermission;
    @Getter
    private Economy vaultEconomy;

    private Timer timer = new Timer();

    private PlayerLoginEventListener playerLoginEventListener;
    private PlayerMoveEventListener playerMoveEventListener;
    private ItemPickupEventListener itemPickupEventListener;
    private PlayerQuitEventListener playerQuitEventListener;
    private WorldSaveEventListener worldSaveEventListener;
    private CharacterCreator characterCreator;

    private CommandHandler commandHandler;

    @Override
    public void onEnable() {

        // Make sure platform is correct
        if (PLATFORM.equalsIgnoreCase("%%__SONGODA__%%")) {
            PLATFORM = "Spigot";
        } else if (PLATFORM.equalsIgnoreCase("true")) {
            PLATFORM = "Songoda";
        }

        pluginLogger = new PluginLogger(this);

        //Dependencies Check
        if (!checkDependencies()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        /*
        //License check
        License license = new License(this);
        if (!license.isContinueLoad()) {
            return;
        }
        */

        //Register config files
        language = new Language(this);
        configRaw = new Config(this);
        configuration = configRaw.getConfig();

        //Setup BSTATS
        if (configuration.getBoolean("bstats")) {
            Metrics metrics = new Metrics(this, 7480);

            metrics.addCustomChart(new Metrics.SimplePie("uuid_changer", new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return configuration.getBoolean("use-character-uuid") ? "yes" : "no";
                }
            }));
        }

        //Setup Vault Classes
        if (!setupEconomy()) {
            pluginLogger.severe("No economy plugin found by Vault, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }

        if (!setupPermissions()) {
            pluginLogger.severe("No permissions plugin found by Vault, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }

        //Update Checker
        updateChecker = new UpdateChecker(spigotResourceID, this);
        showUpdateMessage();

        //Register SQL handler
        sqlHandler = new SQLHandler(this);

        //Register other
        characterCreator = new CharacterCreator(this);

        //Register commands
        commandHandler = new CommandHandler(this);
        getCommand("multicharacter").setExecutor(commandHandler);

        //Register Timers
        timer.schedule(new AnimateToPosition(this), 0, 100);
        timer.schedule(new SaveCharacterTask(this), 0, 30 * 1000);
        timer.schedule(characterCreator, 0, 500);

        //Register event listeners
        playerLoginEventListener = new PlayerLoginEventListener(this);
        playerMoveEventListener = new PlayerMoveEventListener(this);
        itemPickupEventListener = new ItemPickupEventListener(this);
        playerQuitEventListener = new PlayerQuitEventListener(this);
        worldSaveEventListener = new WorldSaveEventListener(this);

        getServer().getPluginManager().registerEvents(playerLoginEventListener, this);
        getServer().getPluginManager().registerEvents(playerMoveEventListener, this);
        getServer().getPluginManager().registerEvents(itemPickupEventListener, this);
        getServer().getPluginManager().registerEvents(playerQuitEventListener, this);
        getServer().getPluginManager().registerEvents(worldSaveEventListener, this);

        //Register PAPI placeholders
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            pluginLogger.info("PlaceholderAPI registering placeholders");
            new PAPIPlaceholders(this).register();
        }

        pluginLogger.log(new LogRecord(Level.INFO, "Foxxite's Multi Character plugin enabled"));
    }

    @Override
    public void onDisable() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                UUIDHandler.RESET_UUID(player);
            } catch (Exception e) {
                pluginLogger.warning(e.getMessage() + " " + e.getCause());
                e.printStackTrace();
            }

            player.kickPlayer("Fatal plugin unloaded, kicking to prevent data corruption.");
        }

        playerLoginEventListener = null;
        playerMoveEventListener = null;
        itemPickupEventListener = null;
        playerQuitEventListener = null;
        worldSaveEventListener = null;

        timer.cancel();
        timer = null;

        getCommand("multicharacter").setExecutor(null);
        commandHandler = null;

        sqlHandler.closeConnection();
        sqlHandler = null;

        pluginLogger.log(new LogRecord(Level.INFO, "Foxxite's Multi Character plugin disabled"));
    }

    private boolean checkDependencies() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            pluginLogger.severe("Vault not found, disabling plugin");
            return false;
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            pluginLogger.severe("PlaceholderAPI not found, some function might not work.");
        }
        return true;
    }

    private void showUpdateMessage() {
        if (updateChecker.getUpdateCheckResult() != (UP_TO_DATE)) {
            HashMap<String, String> placeholders = new HashMap<>();

            String newVersion = (updateChecker.getLatestVersionString() != null ? updateChecker.getLatestVersionString() : "N/A");
            String updateUrl = (updateChecker.getResourceURL() != null ? updateChecker.getResourceURL() : "N/A");

            placeholders.put("{newVersion}", newVersion);
            placeholders.put("{updateUrl}", updateUrl);
            placeholders.put("{checkResult}", updateChecker.getUpdateCheckResult().toString());

            List<String> updateMSG = language.getMultiLineMessageCustom("update", placeholders);
            for (String message : updateMSG) {
                pluginLogger.info(message);
            }
        } else {
            pluginLogger.info("Multi Character is up to date.");
        }
    }


    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        vaultPermission = rsp.getProvider();
        return vaultPermission != null;
    }
}
