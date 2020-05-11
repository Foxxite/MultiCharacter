package com.foxxite.multicharacter.misc;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.character.Character;
import com.foxxite.multicharacter.config.Language;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PAPIPlaceholders extends PlaceholderExpansion {

    private final MultiCharacter plugin;
    private final Language language;
    private final FileConfiguration config;

    public PAPIPlaceholders(final MultiCharacter plugin) {
        this.plugin = plugin;
        this.language = plugin.getLanguage();
        this.config = plugin.getConfiguration();
    }

    /**
     * This method should always return true unless we
     * have a dependency we need to make sure is on the server
     * for our placeholders to work!
     *
     * @return always true since we do not have any dependencies.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().get(0);
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier() {
        return "multicharacter";
    }

    /**
     * This is the version of this expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
     * @param identifier A String containing the identifier/value.
     * @return Possibly-null String of the requested identifier.
     */
    @Override
    public String onRequest(final OfflinePlayer player, final String identifier) {

        try {
            final String[] parts = identifier.split("_");
            final Character character;

            if (parts.length == 2) {
                final Player player1 = Bukkit.getPlayer(parts[1]);
                if (player1 == null) return this.returnError();

                final UUID uuid = player1.getUniqueId();
                character = this.plugin.getActiveCharacters().get(uuid);
            } else {
                character = this.plugin.getActiveCharacters().get(player.getUniqueId());
            }

            if (character == null) return this.returnError();

            switch (parts[0]) {
                case "name":
                    return character.getName();
                case "birthday":
                    return character.getBirthday();
                case "nationality":
                    return character.getNationality();
                case "sex":
                    return character.getSex();
                case "owner":
                    return character.getOwningPlayer().getName();
                case "skintexture":
                    return character.getSkinTexture();
                case "skinsignature":
                    return character.getSkinSignature();
                case "skinimgur":
                    return character.getSkinUrl();
                case "id":
                    return character.getCharacterID().toString();
            }
        } catch (final Exception ex) {
            this.plugin.getPluginLogger().severe(ex.getMessage());
            ex.printStackTrace();
        }

        // We return null if an invalid placeholder (f.e. %example_placeholder3%)
        // was provided
        return this.returnError();
    }

    private String returnError() {
        return Common.colorize("&cN/A");
    }
}
