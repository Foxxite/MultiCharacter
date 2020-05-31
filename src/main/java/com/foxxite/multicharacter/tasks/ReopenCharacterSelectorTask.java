package com.foxxite.multicharacter.tasks;

import com.foxxite.multicharacter.MultiCharacter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.TimerTask;

public class ReopenCharacterSelectorTask extends TimerTask {

    private final Player player;
    private final Inventory selectorGUI;
    private final MultiCharacter plugin;

    public ReopenCharacterSelectorTask(MultiCharacter plugin, Player player, Inventory selectorGUI) {
        this.plugin = plugin;

        this.player = player;
        this.selectorGUI = selectorGUI;
    }

    @Override
    public void run() {

        if (player.getOpenInventory().getTopInventory() == null) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.closeInventory();

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        player.openInventory(selectorGUI);
                    }, 1L);

                }
            }.runTask(plugin);

        }

    }
}
