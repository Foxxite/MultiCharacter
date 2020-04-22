package com.foxxite.emptyplugin;

import com.foxxite.emptyplugin.Config.Config;
import com.foxxite.emptyplugin.Config.Language;
import com.foxxite.emptyplugin.Events.EventListener;
import com.foxxite.emptyplugin.Tasks.Task;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;

public class EmptyPlugin extends JavaPlugin {

    public static Language pluginLanguage;
    public static Config pluginConfig;

    private final Timer timer = new Timer();

    private EventListener eventListener;
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        System.out.println("Foxxite's Empty plugin enabled");

        //Register config files
        this.pluginLanguage = new Language(this);
        this.pluginConfig = new Config(this);

        //Register commands
        this.commandHandler = new CommandHandler(this);


        this.getCommand("tiab").setExecutor(this.commandHandler);

        //Register Timers
        this.timer.schedule(new Task(this), 0, 1000);

        //Register event listeners
        this.eventListener = new EventListener();
        this.getServer().getPluginManager().registerEvents(this.eventListener, this);
    }

    @Override
    public void onDisable() {
        System.out.println("Foxxite's Empty plugin disabled");
        this.eventListener = null;
    }

}
