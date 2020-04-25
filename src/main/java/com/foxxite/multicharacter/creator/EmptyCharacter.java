package com.foxxite.multicharacter.creator;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.sql.SQLHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class EmptyCharacter {

    private final MultiCharacter plugin;
    private final SQLHandler sqlHandler;

    @Getter
    private final UUID owningPlayer;

    @Getter
    private final UUID characterID;

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

    public EmptyCharacter(final MultiCharacter plugin, final UUID owningPlayer) {
        this.plugin = plugin;
        this.sqlHandler = plugin.getSqlHandler();
        this.owningPlayer = owningPlayer;
        this.characterID = UUID.randomUUID();
    }


    public void saveToDatabase() {

        final String insertIntoCharacter = "INSERT INTO Characters (UUID, OwnerUUID, Name, Skin, Texture, Signature, Birthday, Nationality, Sex) VALUES ('" + this.characterID + "', '" + this.owningPlayer + "', '" + this.name + "', '" + this.skinUrl + "', '" + this.skinValue + "', '" + this.skinSignature + "', '" + this.birthday + "', '" + this.nationality + "', '" + this.sex + "' ); ";

        final String insertIntoInventories = "INSERT INTO Inventories (CharacterUUID, Contents, Health, Hunger)\n" +
                "VALUES ('" + this.characterID + "', '', '20.0', '20'); ";

        final String insertIntoLogout = "INSERT INTO LogoutLocations (CharacterUUID, World, X, Y, Z, Yaw, Pitch)\n" +
                "VALUES ('" + this.characterID + "', 'World', '0', '0', '0', '0', '0'); ";

        this.sqlHandler.executeUpdateQuery(insertIntoCharacter);
        this.sqlHandler.executeUpdateQuery(insertIntoInventories);
        this.sqlHandler.executeUpdateQuery(insertIntoLogout);

    }

}
