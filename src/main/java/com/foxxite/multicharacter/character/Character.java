package com.foxxite.multicharacter.character;

import com.foxxite.multicharacter.MultiCharacter;
import com.foxxite.multicharacter.misc.Common;
import com.foxxite.multicharacter.sql.SQLHandler;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class Character {

    private final MultiCharacter plugin;
    private final SQLHandler sqlHandler;
    private final FileConfiguration config;
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
    @Getter
    @Setter
    private double vaultBalance;
    @Getter
    @Setter
    private String vaultGroup;

    public Character(MultiCharacter plugin, UUID characterUUID) {
        this.plugin = plugin;
        config = plugin.getConfiguration();
        sqlHandler = plugin.getSqlHandler();

        HashMap<String, String> tableLayout = new HashMap<>();
        tableLayout.put("UUID", "string");
        tableLayout.put("OwnerUUID", "string");
        tableLayout.put("Name", "string");
        tableLayout.put("Skin", "string");
        tableLayout.put("Texture", "string");
        tableLayout.put("Signature", "string");
        tableLayout.put("Birthday", "string");
        tableLayout.put("Nationality", "string");
        tableLayout.put("Sex", "string");
        HashMap<Integer, HashMap<String, Object>> queryResult = sqlHandler.executeQuery("SELECT * FROM Characters WHERE UUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            HashMap<String, Object> queryResultRow = queryResult.get(0);
            characterID = characterUUID;
            owningPlayerUUID = UUID.fromString((String) queryResultRow.get("OwnerUUID"));
            owningPlayer = Common.getPlayerByUuid(owningPlayerUUID);
            name = (String) queryResultRow.get("Name");
            skinUrl = (String) queryResultRow.get("Skin");
            skinTexture = (String) queryResultRow.get("Texture");
            skinSignature = (String) queryResultRow.get("Signature");
            birthday = (String) queryResultRow.get("Birthday");
            nationality = (String) queryResultRow.get("Nationality");
            sex = (String) queryResultRow.get("Sex");
        }

        tableLayout.clear();
        tableLayout.put("CharacterUUID", "string");
        tableLayout.put("Contents", "string");
        tableLayout.put("Health", "double");
        tableLayout.put("Hunger", "int");
        tableLayout.put("EXP", "double");
        tableLayout.put("EXPLevel", "int");
        queryResult = sqlHandler.executeQuery("SELECT * FROM Inventories WHERE CharacterUUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            HashMap<String, Object> queryResultRow = queryResult.get(0);

            String yamlInventory = (String) queryResultRow.get("Contents");

            if (yamlInventory == null || yamlInventory.isEmpty()) {
                inventoryContent = null;
            } else {
                inventoryContent = Common.stringToInventory((String) queryResultRow.get("Contents"));
            }

            hunger = (int) queryResultRow.get("Hunger");
            health = (double) queryResultRow.get("Health");

            exp = (double) queryResultRow.get("EXP");
            expLevel = (int) queryResultRow.get("EXPLevel");

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
        queryResult = sqlHandler.executeQuery("SELECT * FROM LogoutLocations WHERE CharacterUUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            HashMap<String, Object> queryResultRow = queryResult.get(0);

            World world = Bukkit.getWorld((String) queryResultRow.get("World"));
            double X = (double) queryResultRow.get("X");
            double Y = (double) queryResultRow.get("Y");
            double Z = (double) queryResultRow.get("Z");
            double Yaw = (double) queryResultRow.get("Yaw");
            double Pitch = (double) queryResultRow.get("Pitch");

            logoutLocation = new Location(world, X, Y, Z, (float) Yaw, (float) Pitch);
        }

        tableLayout.clear();
        tableLayout.put("CharacterUUID", "string");
        tableLayout.put("Balance", "double");
        tableLayout.put("Group", "string");
        queryResult = sqlHandler.executeQuery("SELECT * FROM Vault WHERE CharacterUUID = '" + characterUUID.toString() + "'", tableLayout);

        if (queryResult != null && queryResult.size() > 0) {
            HashMap<String, Object> queryResultRow = queryResult.get(0);

            vaultBalance = (double) queryResultRow.get("Balance");
            vaultGroup = (String) queryResultRow.get("Group");
        } else {
            vaultBalance = config.getDouble("vault.default-balance");
            vaultGroup = config.getString("vault.default-group");

            String insertIntoVault = "INSERT INTO Vault (`CharacterUUID`, `Balance`, `Group`)\n" +
                    "VALUES ('" + characterID + "', '" + vaultBalance + "', '" + vaultGroup + "')";
            sqlHandler.executeUpdateQuery(insertIntoVault);
        }

        Economy eco = plugin.getVaultEconomy();
        Permission perm = plugin.getVaultPermission();

        if (eco == null || perm == null) {
            plugin.getPluginLogger().severe("A fatal error occurred while loading Vault data. Is Vault installed?");
            plugin.getPluginLogger().info("Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        // Reset balance to character balance.
        double currBalance = eco.getBalance(owningPlayer);
        EconomyResponse response = eco.withdrawPlayer(owningPlayer, currBalance);
        EconomyResponse response1 = eco.depositPlayer(owningPlayer, vaultBalance);

        if (!response.transactionSuccess() || !response1.transactionSuccess()) {
            plugin.getPluginLogger().warning("An vault error occurred while resetting the balance for " + owningPlayer.getName());
            plugin.getPluginLogger().warning(response.errorMessage);
            plugin.getPluginLogger().warning(response1.errorMessage);
        }

        // Reset player group.
        String currGroup = perm.getPrimaryGroup(owningPlayer);
        perm.playerRemoveGroup(owningPlayer, currGroup);
        perm.playerAddGroup(owningPlayer, vaultGroup);
    }

    public void saveData(Player player) {
        owningPlayer = player;
        saveData();
    }

    public boolean saveData() {
        try {
            Player player = owningPlayer;

            plugin.getPluginLogger().info("Saving data for " + player.getName() + " for character " + name);

            Location lastLocation = player.getLocation();
            logoutLocation = lastLocation;

            String updateLogoutLocation = "UPDATE LogoutLocations SET World = '" + logoutLocation.getWorld().getName() + "', X = " + logoutLocation.getX() + ", Y = " + logoutLocation.getY() + ", Z = " + logoutLocation.getZ() + ", Yaw = " + logoutLocation.getYaw() + ", Pitch = " + logoutLocation.getPitch() + " WHERE CharacterUUID = '" + characterID.toString() + "';";

            sqlHandler.executeUpdateQuery(updateLogoutLocation);

            String playerInventory = Common.inventoryToString(player.getInventory().getContents());

            String updateInventory = "UPDATE Inventories SET Contents = '" + StringEscapeUtils.escapeSql(playerInventory) + "', Health = " + player.getHealth() + ", Hunger = " + player.getFoodLevel() + ", EXP = " + player.getExp() + ", EXPLevel = " + player.getLevel() + " WHERE CharacterUUID = '" + characterID.toString() + "'";

            sqlHandler.executeUpdateQuery(updateInventory);

            Economy eco = plugin.getVaultEconomy();
            Permission perm = plugin.getVaultPermission();

            vaultBalance = eco.getBalance(owningPlayer);
            vaultGroup = perm.getPrimaryGroup(owningPlayer);

            String updateVault = "UPDATE Vault SET `Balance` = '" + vaultBalance + "', `Group` = '" + StringEscapeUtils.escapeSql(vaultGroup) + "' WHERE CharacterUUID = '" + characterID.toString() + "'";
            sqlHandler.executeUpdateQuery(updateVault);

            return true;
        } catch (Exception ex) {
            plugin.getPluginLogger().severe(ex.getMessage());
            ex.printStackTrace();
            return false;
        }


    }
}
