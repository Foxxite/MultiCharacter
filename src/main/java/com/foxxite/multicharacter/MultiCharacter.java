package com.foxxite.multicharacter;

import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.creator.CharacterCreator;
import com.foxxite.multicharacter.events.*;
import com.foxxite.multicharacter.misc.Character;
import com.foxxite.multicharacter.sql.SQLHandler;
import com.foxxite.multicharacter.tasks.AnimateToPosition;
import com.foxxite.multicharacter.tasks.SaveCharacterTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MultiCharacter extends JavaPlugin {

    @Getter
    private final ArrayList<UUID> playersInCreation = new ArrayList<>();
    @Getter
    private final HashMap<UUID, Character> activeCharacters = new HashMap<>();
    @Getter
    private final HashMap<UUID, Location> animateToLocation = new HashMap<>();

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

        //Check Dependencies
        if (!this.checkDependencies()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Register config files
        this.language = new Language(this);
        this.configRaw = new Config(this);
        this.configuration = this.configRaw.getConfig();

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

        this.pluginLogger.log(new LogRecord(Level.INFO, "Foxxite's Multi Character plugin enabled"));
    }

    @Override
    public void onDisable() {

        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("Fatal plugin unloaded, kicking to prevent data corruption.");
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

    private boolean checkDependencies() {
        boolean shouldNotDisable = true;

        if (!this.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            this.pluginLogger.log(new LogRecord(Level.SEVERE, "ProtocolLib is not enabled! Multi Character disabled"));
            shouldNotDisable = false;
        }

        return shouldNotDisable;
    }


}
