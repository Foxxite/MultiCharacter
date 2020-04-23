package com.foxxite.multicharacter.misc;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.sql.SQLHandler;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class Character {

    private final MultiCharacter plugin;
    private final SQLHandler sqlHandler;
    @Getter
    private UUID characterID;
    @Getter
    private UUID owningPlayer;
    @Getter
    private String name;
    @Getter
    private String skinUUID;
    @Getter
    private String birthday;
    @Getter
    private String nationality;
    @Getter
    private String sex;
    @Getter
    private Location logoutLocation;
    @Getter
    private ItemStack[] inventoryContent;
    @Getter
    private float Hunger;
    @Getter
    private float Health;

    public Character(final MultiCharacter plugin, final UUID characterUUID) {
        this.plugin = plugin;
        this.sqlHandler = plugin.getSqlHandler();

        final HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");
        tableLayout.put("Name", "string");
        tableLayout.put("Skin", "string");
        tableLayout.put("Birthday", "string");
        tableLayout.put("Nationality", "string");
        tableLayout.put("Sex", "string");

        final HashMap<Integer, HashMap<String, Object>> queryResult = this.sqlHandler.executeQuery("SELECT * FROM Characters WHERE UUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            final HashMap<String, Object> queryResultRow = queryResult.get(0);
            this.characterID = characterUUID;
            this.owningPlayer = UUID.fromString((String) queryResultRow.get("OwnerUUID"));
            this.name = (String) queryResultRow.get("Name");
            this.skinUUID = (String) queryResultRow.get("Skin");
            this.birthday = (String) queryResultRow.get("Birthday");
            this.nationality = (String) queryResultRow.get("Nationality");
            this.sex = (String) queryResultRow.get("Sex");
        }
    }

    public void saveData() {

        final Player player = Common.getPlayerByUuid(this.owningPlayer);

        final Location lastLocation = player.getLocation();
        this.logoutLocation = lastLocation;

        final String updateLogoutLocation = "UPDATE LogoutLocations SET World = '" + this.logoutLocation.getWorld().getName() + "', X = " + this.logoutLocation.getX() + ", Y = " + this.logoutLocation.getY() + ", Z = " + this.logoutLocation.getZ() + ", Yaw = " + this.logoutLocation.getYaw() + ", Pitch = " + this.logoutLocation.getPitch() + " WHERE CharacterUUID = '" + this.characterID.toString() + "';";

        this.sqlHandler.executeUpdateQuery(updateLogoutLocation);

        final String playerInventory = Common.inventoryToString(player.getInventory().getContents());

        final String updateInventory = "UPDATE Inventories SET Contents = '" + playerInventory + "', Health = " + player.getHealth() + ", Hunger = " + player.getFoodLevel() + " WHERE CharacterUUID = '" + this.characterID.toString() + "'";

        this.sqlHandler.executeUpdateQuery(updateInventory);

    }
}
