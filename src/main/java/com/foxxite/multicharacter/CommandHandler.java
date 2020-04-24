package com.foxxite.multicharacter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CommandHandler implements TabExecutor {

    Plugin plugin;

    public CommandHandler(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        return null;
    }
}
