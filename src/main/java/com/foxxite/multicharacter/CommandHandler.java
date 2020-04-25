package com.foxxite.multicharacter;

import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import com.foxxite.multicharacter.misc.Common;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements TabExecutor {

    private final MultiCharacter plugin;
    private final Config config;
    private final Language language;

    public CommandHandler(final MultiCharacter plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigRaw();
        this.language = plugin.getLanguage();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {

        if (command.getName().equalsIgnoreCase("multicharacter"))
            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (args.length > 0) {
                    switch (args[0]) {
                        case "reload":
                            if (player.hasPermission("multicharacter.admin")) {
                                this.config.reloadConfig();
                                this.language.reloadLanguage();
                                player.sendMessage(this.language.getMessage("prefix") + " Config and Lang reloaded");
                            } else {
                                player.sendMessage(this.language.getMessage("prefix") + Common.colorize("&c You don't have permission for this command."));
                            }
                            break;
                        case "logout":
                        case "switch":
                            if (player.hasPermission("multicharacter.switch")) {
                                Bukkit.getScheduler().runTask(this.plugin, () -> {

                                    if (this.plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
                                        if (!this.saveData(player)) {
                                            player.sendMessage(ChatColor.RED + "Error occurred while switching characters, please try again later.");
                                            return;
                                        }
                                        player.sendMessage(this.language.getMessage("prefix") + Common.colorize("&a Character data saved to the database."));
                                    }

                                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                                        final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);
                                        this.plugin.getActiveCharacters().remove(player.getUniqueId());
                                    }, 10L);
                                });


                            } else {
                                player.sendMessage(this.language.getMessage("prefix") + Common.colorize("&c You don't have permission for this command."));
                            }
                            break;
                        case "save":
                            if (player.hasPermission("multicharacter.save")) {
                                Bukkit.getScheduler().runTask(this.plugin, () -> {

                                    if (!this.saveData(player)) {
                                        player.sendMessage(ChatColor.RED + "Error occurred while switching characters, please try again later.");
                                        return;
                                    }

                                    player.sendMessage(this.language.getMessage("prefix") + Common.colorize("&a Character data saved to the database."));
                                });
                            } else {
                                player.sendMessage(this.language.getMessage("prefix") + Common.colorize("&c You don't have permission for this command."));
                            }
                            break;
                        default:
                            player.sendMessage(this.language.getMessage("prefix") + Common.colorize("&7 Unknown Sub Command"));
                            break;
                    }
                    return true;
                }
            }
        sender.sendMessage(this.language.getMessage("prefix") + Common.colorize("&7 Unknown Sub Command"));
        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] agrs) {

        if (agrs.length > 0) {
            final ArrayList<String> returns = new ArrayList<>();
            returns.add("logout");
            returns.add("switch");
            returns.add("save");
            returns.add("reload");

            return returns;
        }

        return null;
    }

    private boolean saveData(final Player player) {
        if (this.plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
            if (!this.plugin.getActiveCharacters().get(player.getUniqueId()).saveData())
                return false;
            return true;
        }
        return false;
    }

}
