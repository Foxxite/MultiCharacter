package com.foxxite.multicharacter.character;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.misc.Common;
import com.foxxite.multicharacter.sql.SQLHandler;
import lombok.Getter;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    private Player owningPlayer;
    @Getter
    private UUID owningPlayerUUID;
    @Getter
    private String name;
    @Getter
    private String skinUrl;
    @Getter
    private String skinTexture;
    @Getter
    private String skinSignature;
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
    private int hunger;
    @Getter
    private double health;
    @Getter
    private double exp;
    @Getter
    private int expLevel;

    public Character(final MultiCharacter plugin, final UUID characterUUID) {
        this.plugin = plugin;
        this.sqlHandler = plugin.getSqlHandler();

        final HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");
        tableLayout.put("Name", "string");
        tableLayout.put("Skin", "string");
        tableLayout.put("Texture", "string");
        tableLayout.put("Signature", "string");
        tableLayout.put("Birthday", "string");
        tableLayout.put("Nationality", "string");
        tableLayout.put("Sex", "string");
        HashMap<Integer, HashMap<String, Object>> queryResult = this.sqlHandler.executeQuery("SELECT * FROM Characters WHERE UUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            final HashMap<String, Object> queryResultRow = queryResult.get(0);
            this.characterID = characterUUID;
            this.owningPlayerUUID = UUID.fromString((String) queryResultRow.get("OwnerUUID"));
            this.owningPlayer = Common.getPlayerByUuid(this.owningPlayerUUID);
            this.name = (String) queryResultRow.get("Name");
            this.skinUrl = (String) queryResultRow.get("Skin");
            this.skinTexture = (String) queryResultRow.get("Texture");
            this.skinSignature = (String) queryResultRow.get("Signature");
            this.birthday = (String) queryResultRow.get("Birthday");
            this.nationality = (String) queryResultRow.get("Nationality");
            this.sex = (String) queryResultRow.get("Sex");
        }

        tableLayout.clear();
        tableLayout.put("CharacterUUID", "string");
        tableLayout.put("Contents", "string");
        tableLayout.put("Health", "double");
        tableLayout.put("Hunger", "int");
        tableLayout.put("EXP", "double");
        tableLayout.put("EXPLevel", "int");
        queryResult = this.sqlHandler.executeQuery("SELECT * FROM Inventories WHERE CharacterUUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            final HashMap<String, Object> queryResultRow = queryResult.get(0);

            final String yamlInventory = (String) queryResultRow.get("Contents");

            if (yamlInventory == null || yamlInventory.isEmpty())
                this.inventoryContent = null;
            else
                this.inventoryContent = Common.stringToInventory((String) queryResultRow.get("Contents"));

            this.hunger = (int) queryResultRow.get("Hunger");
            this.health = (double) queryResultRow.get("Health");

            this.exp = (double) queryResultRow.get("EXP");
            this.expLevel = (int) queryResultRow.get("EXPLevel");

        }

        tableLayout.clear();
        tableLayout.put("ID", "int");
        tableLayout.put("CharacterUUID", "string");
        tableLayout.put("World", "string");
        tableLayout.put("X", "double");
        tableLayout.put("Y", "double");
        tableLayout.put("Z", "double");
        tableLayout.put("Yaw", "double");
        tableLayout.put("Pitch", "double");
        queryResult = this.sqlHandler.executeQuery("SELECT * FROM LogoutLocations WHERE CharacterUUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            final HashMap<String, Object> queryResultRow = queryResult.get(0);

            final World world = Bukkit.getWorld((String) queryResultRow.get("World"));
            final double X = (double) queryResultRow.get("X");
            final double Y = (double) queryResultRow.get("Y");
            final double Z = (double) queryResultRow.get("Z");
            final double Yaw = (double) queryResultRow.get("Yaw");
            final double Pitch = (double) queryResultRow.get("Pitch");

            this.logoutLocation = new Location(world, X, Y, Z, (float) Yaw, (float) Pitch);
        }

    }

    public void saveData(final Player player) {
        this.owningPlayer = player;
        this.saveData();
    }

    public boolean saveData() {
        try {
            final Player player = this.owningPlayer;

            this.plugin.getPluginLogger().info("Saving data for " + player.getName() + " for character " + this.name);

            final Location lastLocation = player.getLocation();
            this.logoutLocation = lastLocation;

            final String updateLogoutLocation = "UPDATE LogoutLocations SET World = '" + this.logoutLocation.getWorld().getName() + "', X = " + this.logoutLocation.getX() + ", Y = " + this.logoutLocation.getY() + ", Z = " + this.logoutLocation.getZ() + ", Yaw = " + this.logoutLocation.getYaw() + ", Pitch = " + this.logoutLocation.getPitch() + " WHERE CharacterUUID = '" + this.characterID.toString() + "';";

            this.sqlHandler.executeUpdateQuery(updateLogoutLocation);

            final String playerInventory = Common.inventoryToString(player.getInventory().getContents());

            final String updateInventory = "UPDATE Inventories SET Contents = '" + StringEscapeUtils.escapeSql(playerInventory) + "', Health = " + player.getHealth() + ", Hunger = " + player.getFoodLevel() + ", EXP = " + player.getExp() + ", EXPLevel = " + player.getLevel() + " WHERE CharacterUUID = '" + this.characterID.toString() + "'";

            this.sqlHandler.executeUpdateQuery(updateInventory);
            return true;
        } catch (final Exception ex) {
            this.plugin.getPluginLogger().severe(ex.getMessage());
            ex.printStackTrace();
            return false;
        }


    }
}
