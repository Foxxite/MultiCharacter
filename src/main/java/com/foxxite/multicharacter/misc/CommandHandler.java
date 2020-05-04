package com.foxxite.multicharacter.misc;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import org.bukkit.Bukkit;
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
                                player.sendMessage(this.language.getMessage("reload"));
                            } else {
                                player.sendMessage(this.language.getMessage("no-perms"));
                            }
                            break;
                        case "logout":
                        case "switch":
                            if (player.hasPermission("multicharacter.switch")) {

                                if (this.plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
                                    if (!this.saveData(player)) {
                                        player.sendMessage(this.language.getMessage("saving.error"));
                                        return true;
                                    }

                                    player.sendMessage(this.language.getMessage("saving.complete"));

                                    this.plugin.getActiveCharacters().remove(player.getUniqueId());

                                    final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);
                                } else {
                                    final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);
                                }
                            } else {
                                player.sendMessage(this.language.getMessage("no-perms"));
                            }
                            break;
                        case "save":
                            if (player.hasPermission("multicharacter.save")) {
                                if (!this.saveData(player)) {
                                    player.sendMessage(this.language.getMessage("saving.error"));
                                    return true;
                                }

                                player.sendMessage(this.language.getMessage("saving.complete"));
                            } else {
                                player.sendMessage(this.language.getMessage("no-perms"));
                            }
                            break;
                        default:
                            player.sendMessage(this.language.getMessage("unknown-command"));
                            break;
                    }
                    return true;
                }
            }
        sender.sendMessage(this.language.getMessage("unknown-command"));
        return false;
    }


    private List<String> getSubCommands() {
        final ArrayList<String> returns = new ArrayList<>();
        returns.add("logout");
        returns.add("switch");
        returns.add("save");
        returns.add("reload");
        returns.add("id");
        returns.add("lookup");

        returns.sort(String::compareToIgnoreCase);
        return returns;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] agrs) {

        if (agrs.length > 0) {
            final ArrayList<String> autoComplete = new ArrayList<>();
            if (agrs.length == 1) {

                if (agrs[0].length() == 0) return this.getSubCommands();

                for (final String subCommand : this.getSubCommands()) {
                    if (subCommand.startsWith(agrs[0]))
                        autoComplete.add(subCommand);
                }

            } else if (agrs.length == 2 && agrs[0].equalsIgnoreCase("lookup")) {

                for (final Player player : Bukkit.getOnlinePlayers()) {
                    autoComplete.add(player.getDisplayName());
                }

            }
            return autoComplete;
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
