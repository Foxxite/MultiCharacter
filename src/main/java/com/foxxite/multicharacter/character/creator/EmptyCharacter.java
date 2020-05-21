package com.foxxite.multicharacter.character.creator;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.sql.SQLHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class EmptyCharacter {

    private final MultiCharacter plugin;
    private final SQLHandler sqlHandler;
    @Getter
    private final UUID owningPlayer;
    boolean hasBeenSaved = false;
    @Getter
    private UUID characterID;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String skinUrl;
    @Getter
    @Setter
    private String skinValue;
    @Getter
    @Setter
    private String skinSignature;
    @Getter
    @Setter
    private String birthday;
    @Getter
    @Setter
    private String nationality;
    @Getter
    @Setter
    private String sex;
    @Getter
    @Setter
    private double vaultBalance;
    @Getter
    @Setter
    private String vaultGroup;
    @Getter
    @Setter
    private String[] vaultPermissions;

    public EmptyCharacter(MultiCharacter plugin, UUID owningPlayer) {
        this.plugin = plugin;
        sqlHandler = plugin.getSqlHandler();
        this.owningPlayer = owningPlayer;
        FileConfiguration config = plugin.getConfiguration();

        characterID = UUID.randomUUID();

        vaultBalance = config.getDouble("vault.default-balance");
        vaultGroup = config.getString("vault.default-group");
        vaultPermissions = (String[]) config.getStringList("vault.default-perms").toArray();
    }

    public void saveToDatabase() {

        if (characterID == null) {
            characterID = UUID.randomUUID();
        }

        if (!hasBeenSaved) {
            String insertIntoCharacter = "INSERT INTO Characters (UUID, OwnerUUID, Name, Skin, Texture, Signature, Birthday, Nationality, Sex) VALUES ('" + characterID + "', '" + owningPlayer + "', '" + name + "', '" + skinUrl + "', '" + skinValue + "', '" + skinSignature + "', '" + birthday + "', '" + nationality + "', '" + sex + "' ); ";

            String insertIntoInventories = "INSERT INTO Inventories (CharacterUUID, Contents, Health, Hunger)\n" +
                    "VALUES ('" + characterID + "', '', '20.0', '20'); ";

            String insertIntoLogout = "INSERT INTO LogoutLocations (CharacterUUID, World, X, Y, Z, Yaw, Pitch)\n" +
                    "VALUES ('" + characterID + "', 'World', '0', '0', '0', '0', '0'); ";

            sqlHandler.executeUpdateQuery(insertIntoCharacter);
            sqlHandler.executeUpdateQuery(insertIntoInventories);
            sqlHandler.executeUpdateQuery(insertIntoLogout);

            hasBeenSaved = true;
        }
    }

}
