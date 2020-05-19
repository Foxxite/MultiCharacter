package com.foxxite.multicharacter;

import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.character.creator.CharacterCreator;
import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.events.*;
import com.foxxite.multicharacter.misc.CommandHandler;
import com.foxxite.multicharacter.misc.PAPIPlaceholders;
import com.foxxite.multicharacter.misc.UUIDHandler;
import com.foxxite.multicharacter.misc.UpdateChecker;
import com.foxxite.multicharacter.sql.SQLHandler;
import com.foxxite.multicharacter.tasks.AnimateToPosition;
import com.foxxite.multicharacter.tasks.SaveCharacterTask;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static com.foxxite.multicharacter.misc.UpdateChecker.UpdateCheckResult.UP_TO_DATE;

public class MultiCharacter extends JavaPlugin {

    @Getter
    private final ArrayList<UUID> playersInCreation = new ArrayList<>();
    @Getter
    private final HashMap<UUID, Character> activeCharacters = new HashMap<>();
    @Getter
    private final HashMap<UUID, Location> animateToLocation = new HashMap<>();
    @Getter
    private final int resourceID = 78441;
    private final String userID = "%%__USER__%%";
    private final String nonce = "%%__NONCE__%%";
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

        this.pluginLogger = new PluginLogger(this);

        //Register config files
        this.language = new Language(this);
        this.configRaw = new Config(this);
        this.configuration = this.configRaw.getConfig();

        //Setup BSTATS
        if (this.configuration.getBoolean("bstats")) {
            final Metrics metrics = new Metrics(this, 7480);
        }

        //Update Checker
        this.updateChecker = new UpdateChecker(this.resourceID, this);
        if (this.updateChecker.getUpdateCheckResult() != (UP_TO_DATE)) {
            final HashMap<String, String> placeholders = new HashMap<>();

            final String newVersion = (this.updateChecker.getLatestVersionString() != null ? this.updateChecker.getLatestVersionString() : "N/A");
            final String updateUrl = (this.updateChecker.getResourceURL() != null ? this.updateChecker.getResourceURL() : "N/A");

            placeholders.put("{newVersion}", newVersion);
            placeholders.put("{updateUrl}", updateUrl);
            placeholders.put("{checkResult}", this.updateChecker.getUpdateCheckResult().toString());

            final List<String> updateMSG = this.language.getMultiLineMessageCustom("update", placeholders);
            for (final String message : updateMSG) {
                this.pluginLogger.info(message);
            }
        } else {
            System.out.println("Multi Character is up to date.");
        }

        //Register SQL handler
        this.sqlHandler = new SQLHandler(this);

        //Register other
        this.characterCreator = new CharacterCreator(this);

        //Register commands
        this.commandHandler = new CommandHandler(this);
        this.getCommand("multicharacter").setExecutor(this.commandHandler);

        //Register Timers
        this.timer.schedule(new AnimateToPosition(this), 0, 100);
        this.timer.schedule(new SaveCharacterTask(this), 0, 30 * 1000);
        this.timer.schedule(this.characterCreator, 0, 500);

        //Register event listeners
        this.playerLoginEventListener = new PlayerLoginEventListener(this);
        this.playerMoveEventListener = new PlayerMoveEventListener(this);
        this.itemPickupEventListener = new ItemPickupEventListener(this);
        this.playerQuitEventListener = new PlayerQuitEventListener(this);
        this.worldSaveEventListener = new WorldSaveEventListener(this);

        this.getServer().getPluginManager().registerEvents(this.playerLoginEventListener, this);
        this.getServer().getPluginManager().registerEvents(this.playerMoveEventListener, this);
        this.getServer().getPluginManager().registerEvents(this.itemPickupEventListener, this);
        this.getServer().getPluginManager().registerEvents(this.playerQuitEventListener, this);
        this.getServer().getPluginManager().registerEvents(this.worldSaveEventListener, this);

        //Register PAPI placeholders
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.pluginLogger.info("PlaceholderAPI registering placeholders");
            new PAPIPlaceholders(this).register();
        }

        this.pluginLogger.log(new LogRecord(Level.INFO, "Foxxite's Multi Character plugin enabled"));
    }

    @Override
    public void onDisable() {

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("Fatal plugin unloaded, kicking to prevent data corruption.");
            UUIDHandler.RESET_UUID(player);
        }

        this.playerLoginEventListener = null;
        this.playerMoveEventListener = null;
        this.itemPickupEventListener = null;
        this.playerQuitEventListener = null;
        this.worldSaveEventListener = null;

        this.timer.cancel();
        this.timer = null;

        this.getCommand("multicharacter").setExecutor(null);
        this.commandHandler = null;

        this.sqlHandler.closeConnection();
        this.sqlHandler = null;

        this.pluginLogger.log(new LogRecord(Level.INFO, "Foxxite's Multi Character plugin disabled"));
    }
}
