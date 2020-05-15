package com.foxxite.multicharacter.misc;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

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

                                UUIDHandler.RESET_UUID(player);

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
                        case "id":
                            if (player.hasPermission("multicharacter.id")) {
                                final Character character = this.plugin.getActiveCharacters().get(player.getUniqueId());
                                if (character != null) {

                                    final HashMap<String, String> placeholder = new HashMap<>();
                                    placeholder.put("{name}", character.getName());
                                    placeholder.put("{id}", character.getCharacterID().toString());

                                    player.sendMessage(this.language.getMessagePAPIAndCustom("character-data.id", player, placeholder));
                                } else {
                                    player.sendMessage(this.language.getMessage("character-data.non-active"));
                                }
                            }
                            break;
                        case "lookup":
                            if (player.hasPermission("multicharacter.lookup")) {
                                if (args.length == 2 || !args[1].isEmpty()) {
                                    this.lookupCharacter(args[1], player);
                                } else {
                                    player.sendMessage(this.language.getMessage("character-data.missing-param"));
                                }
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

    private void lookupCharacter(final String characterID, final Player player) {

        final Character lookupCharacter;

        //Is UUID
        final Pattern uuidPattern = Pattern.compile("^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$");

        if (uuidPattern.matcher(characterID.trim()).matches()) {
            lookupCharacter = new Character(this.plugin, UUID.fromString(characterID));
        } else {
            final Player localPlayer = Bukkit.getPlayer(characterID);
            if (localPlayer == null) {
                player.sendMessage(this.language.getMessage("character-data.non-active"));
                return;
            }

            lookupCharacter = this.plugin.getActiveCharacters().get(localPlayer.getUniqueId());
        }

        if (lookupCharacter == null) {
            player.sendMessage(this.language.getMessage("character-data.non-active"));
            return;
        }

        final HashMap<String, String> placeholder = new HashMap<>();
        placeholder.put("{id}", lookupCharacter.getCharacterID().toString());
        placeholder.put("{name}", lookupCharacter.getName());
        placeholder.put("{birthday}", lookupCharacter.getBirthday());
        placeholder.put("{nationality}", lookupCharacter.getNationality());
        placeholder.put("{sex}", lookupCharacter.getSex());

        final ArrayList<String> data = this.language.getMultiLineMessagePAPIAndCustom("character-data.data", player, placeholder);

        for (final String line : data) {
            player.sendMessage(line);
        }

    }


    private ArrayList<String> getSubCommands() {
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
            ArrayList<String> autoComplete = new ArrayList<>();
            int activeArg = 0;

            if (agrs.length == 1) {

                activeArg = 0;
                if (agrs[0].length() == 0) return this.getSubCommands();

                autoComplete = this.getSubCommands();

            } else if (agrs.length == 2 && agrs[0].equalsIgnoreCase("lookup")) {

                activeArg = 1;
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    autoComplete.add(player.getName());
                }

                final ArrayList<String> finalAutoComplete = autoComplete;
                this.plugin.getActiveCharacters().forEach((playerUUID, character) -> {
                    finalAutoComplete.add(character.getCharacterID().toString());
                });

            }

            final ArrayList<String> returnList = new ArrayList<>();

            //Intelligent Auto Complete
            for (final String subCommand : autoComplete) {
                //Check if args contain subcommand, ignore case
                if (subCommand.startsWith(agrs[activeArg]) || subCommand.toLowerCase().startsWith(agrs[activeArg]))
                    returnList.add(subCommand);
            }

            return returnList;
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
