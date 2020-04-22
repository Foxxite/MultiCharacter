package com.foxxite.emptyplugin.tasks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.TimerTask;

public class Task extends TimerTask {

    Plugin plugin;

    public Task(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

		Bukkit.getScheduler().runTask(this.plugin, () -> {

        });


    }

}
