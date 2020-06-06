package com.foxxite.multicharacter.misc;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.config.Config;
import com.foxxite.multicharacter.config.Language;
import com.foxxite.multicharacter.inventories.CharacterSelector;
import com.foxxite.multicharacter.worldspacemenu.WorldSpaceMenu;
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

    public CommandHandler(MultiCharacter plugin) {
        this.plugin = plugin;
        config = plugin.getConfigRaw();
        language = plugin.getLanguage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("multicharacter")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length > 0) {
                    switch (args[0]) {
                        case "reload":
                            if (player.hasPermission("multicharacter.admin")) {
                                config.reloadConfig();
                                language.reloadLanguage();
                                player.sendMessage(language.getMessage("reload"));
                            } else {
                                player.sendMessage(language.getMessage("no-perms"));
                            }
                            break;
                        case "logout":
                        case "switch":
                            if (player.hasPermission("multicharacter.switch")) {

                                UUIDHandler.RESET_UUID(player);

                                if (plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
                                    if (!saveData(player)) {
                                        player.sendMessage(language.getMessage("saving.error"));
                                        return true;
                                    }

                                    player.sendMessage(language.getMessage("saving.complete"));

                                    plugin.getActiveCharacters().remove(player.getUniqueId());
                                }

                                CharacterSelector characterSelector = new CharacterSelector(plugin, player);
                            } else {
                                player.sendMessage(language.getMessage("no-perms"));
                            }
                            break;
                        case "save":
                            if (player.hasPermission("multicharacter.save")) {
                                if (!saveData(player)) {
                                    player.sendMessage(language.getMessage("saving.error"));
                                    return true;
                                }

                                player.sendMessage(language.getMessage("saving.complete"));
                            } else {
                                player.sendMessage(language.getMessage("no-perms"));
                            }
                            break;
                        case "id":
                            if (player.hasPermission("multicharacter.id")) {
                                Character character = plugin.getActiveCharacters().get(player.getUniqueId());
                                if (character != null) {

                                    HashMap<String, String> placeholder = new HashMap<>();
                                    placeholder.put("{name}", character.getName());
                                    placeholder.put("{uuid}", player.getUniqueId().toString());
                                    placeholder.put("{id}", character.getCharacterID().toString());

                                    player.sendMessage(language.getMessagePAPIAndCustom("character-data.id", player, placeholder));
                                } else {
                                    player.sendMessage(language.getMessage("character-data.non-active"));
                                }
                            }
                            break;
                        case "lookup":
                            if (player.hasPermission("multicharacter.lookup")) {
                                if (args.length == 2 || !args[1].isEmpty()) {
                                    lookupCharacter(args[1], player);
                                } else {
                                    player.sendMessage(language.getMessage("character-data.missing-param"));
                                }
                            }
                            break;
                        case "3DMenu":
                            plugin.getPlayersInWorldMenu().put(player.getUniqueId(), new WorldSpaceMenu(plugin, player));
                            break;
                        case "close3DMenu":
                            plugin.getPlayersInWorldMenu().get(player.getUniqueId()).closeMenu(true);
                            plugin.getPlayersInWorldMenu().remove(player.getUniqueId());
                            break;
                        default:
                            player.sendMessage(language.getMessage("unknown-command"));
                            break;
                    }
                    return true;
                }
            } else {
                sender.sendMessage(Common.colorize("&cYou have to be a player to use this command."));
                return true;
            }
        }
        return false;
    }

    private void lookupCharacter(String characterID, Player player) {

        Character lookupCharacter;

        //Is UUID
        Pattern uuidPattern = Pattern.compile("^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-4[0-9A-Fa-f]{3}-[89ABab][0-9A-Fa-f]{3}-[0-9A-Fa-f]{12}$");

        if (uuidPattern.matcher(characterID.trim()).matches()) {
            lookupCharacter = new Character(plugin, UUID.fromString(characterID));
        } else {
            Player localPlayer = Bukkit.getPlayer(characterID);
            if (localPlayer == null) {
                player.sendMessage(language.getMessage("character-data.non-active"));
                return;
            }

            lookupCharacter = plugin.getActiveCharacters().get(localPlayer.getUniqueId());
        }

        if (lookupCharacter == null) {
            player.sendMessage(language.getMessage("character-data.non-active"));
            return;
        }

        HashMap<String, String> placeholder = new HashMap<>();
        placeholder.put("{id}", lookupCharacter.getCharacterID().toString());
        placeholder.put("{name}", lookupCharacter.getName());
        placeholder.put("{birthday}", lookupCharacter.getBirthday());
        placeholder.put("{nationality}", lookupCharacter.getNationality());
        placeholder.put("{sex}", lookupCharacter.getSex());
        placeholder.put("{balance}", String.valueOf(lookupCharacter.getVaultBalance()));
        placeholder.put("{group}", lookupCharacter.getVaultGroup());

        ArrayList<String> data = language.getMultiLineMessagePAPIAndCustom("character-data.data", player, placeholder);

        for (String line : data) {
            player.sendMessage(line);
        }

    }


    private ArrayList<String> getSubCommands() {
        ArrayList<String> returns = new ArrayList<>();
        returns.add("logout");
        returns.add("switch");
        returns.add("save");
        returns.add("reload");
        returns.add("id");
        returns.add("lookup");
        returns.add("3DMenu");
        returns.add("close3DMenu");

        returns.sort(String::compareToIgnoreCase);
        return returns;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] agrs) {

        if (agrs.length > 0) {
            ArrayList<String> autoComplete = new ArrayList<>();
            int activeArg = 0;

            if (agrs.length == 1) {

                activeArg = 0;
                if (agrs[0].length() == 0) {
                    return getSubCommands();
                }

                autoComplete = getSubCommands();

            } else if (agrs.length == 2 && agrs[0].equalsIgnoreCase("lookup")) {

                activeArg = 1;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    autoComplete.add(player.getName());
                }

                ArrayList<String> finalAutoComplete = autoComplete;
                plugin.getActiveCharacters().forEach((playerUUID, character) -> {
                    finalAutoComplete.add(character.getCharacterID().toString());
                });

            }

            ArrayList<String> returnList = new ArrayList<>();

            //Intelligent Auto Complete
            for (String subCommand : autoComplete) {
                //Check if args contain subcommand, ignore case
                if (subCommand.startsWith(agrs[activeArg]) || subCommand.toLowerCase().startsWith(agrs[activeArg])) {
                    returnList.add(subCommand);
                }
            }

            return returnList;
        }

        return null;
    }

    private boolean saveData(Player player) {
        if (plugin.getActiveCharacters().containsKey(player.getUniqueId())) {
            if (!plugin.getActiveCharacters().get(player.getUniqueId()).saveData()) {
                return false;
            }
            return true;
        }
        return false;
    }

}
