package com.foxxite.multicharacter;

import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
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
                            this.config.reloadConfig();
                            this.language.reloadLanguage();
                            player.sendMessage(this.language.getMessage("prefix") + " Config and Lang reloaded");
                            break;
                        case "logout":
                        case "switch":
                            this.saveData(player);
                            final CharacterSelector characterSelector = new CharacterSelector(this.plugin, player);
                            break;
                        case "save":
                            this.saveData(player);
                            break;
                        default:
                            player.sendMessage(this.language.getMessage("prefix") + " Unknown Sub Command");
                            break;
                    }
                    return true;
                }
            }

        return false;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] agrs) {

        if (agrs.length == 1) {
            final ArrayList<String> returns = new ArrayList<>();
            returns.add("logout");
            returns.add("switch");
            returns.add("save");
            returns.add("reload");
        }

        return null;
    }

    private boolean saveData(final Player player) {
        if (this.plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
            this.plugin.getActiveCharacters().get(player.getUniqueId()).saveData();
        }
        return false;
    }

}
