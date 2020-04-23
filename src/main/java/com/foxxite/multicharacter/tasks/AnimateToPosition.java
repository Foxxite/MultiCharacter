package com.foxxite.multicharacter.tasks;

import com.foxxite.multicharacter.MultiCharacter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.TimerTask;

public class AnimateToPosition extends TimerTask implements Listener {

    MultiCharacter plugin;

    public AnimateToPosition(final MultiCharacter plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        Bukkit.getScheduler().runTask(this.plugin, () -> {

        });

    }

}
