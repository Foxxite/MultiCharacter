package com.foxxite.multicharacter.sql;

import com.foxxite.multicharacter.MultiCharacter;

import javax.sql.rowset.CachedRowSet;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class SQLHandler {

    private final MultiCharacter plugin;
    private final File sqlFile;
    private Connection conn;

    public SQLHandler(MultiCharacter plugin) {

        this.plugin = plugin;

        sqlFile = new File(this.plugin.getDataFolder(), "storage.db");

        if (!sqlFile.exists()) {
            sqlFile.getParentFile().mkdirs();
            try {
                sqlFile.createNewFile();
            } catch (IOException e) {
                plugin.getPluginLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
                e.printStackTrace();
            }
        }

        connect();
        setupDatabase();
    }

    private void connect() {

        try {
            // db parameters
            String url = "jdbc:sqlite:" + sqlFile.getAbsolutePath();
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            plugin.getPluginLogger().log(new LogRecord(Level.INFO, "Connection to SQLite has been established."));

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }
    }

    private void setupDatabase() {

        ArrayList<String> tables = new ArrayList<>();

        tables.add(
                "CREATE TABLE IF NOT EXISTS \"Vault\" (\n" +
                        "\t\"CharacterUUID\"\tTEXT UNIQUE,\n" +
                        "\t\"Balance\"\tREAL,\n" +
                        "\t\"Group\"\tTEXT,\n" +
                        "\t\"Permissions\"\tTEXT,\n" +
                        "\tPRIMARY KEY(\"CharacterUUID\")\n" +
                        ");"
        );

        tables.add(
                "CREATE TABLE IF NOT EXISTS \"Characters\" (\n" +
                        "\t\"UUID\"\tTEXT UNIQUE,\n" +
                        "\t\"OwnerUUID\"\tTEXT,\n" +
                        "\t\"Name\"\tTEXT,\n" +
                        "\t\"Birthday\"\tTEXT,\n" +
                        "\t\"Nationality\"\tREAL,\n" +
                        "\t\"Sex\"\tTEXT,\n" +
                        "\t\"Skin\"\tTEXT,\n" +
                        "\t\"Texture\"\tTEXT,\n" +
                        "\t\"Signature\"\tTEXT,\n" +
                        "\t\"Deleted\"\tINTEGER DEFAULT 0,\n" +
                        "\tPRIMARY KEY(\"UUID\")\n" +
                        ");"
        );

        tables.add(
                "CREATE TABLE IF NOT EXISTS \"Inventories\" (\n" +
                        "\t\"CharacterUUID\"\tTEXT UNIQUE,\n" +
                        "\t\"Contents\"\tTEXT,\n" +
                        "\t\"Health\"\tREAL DEFAULT 20,\n" +
                        "\t\"Hunger\"\tINTEGER DEFAULT 20,\n" +
                        "\t\"EXP\"\tREAL DEFAULT 0,\n" +
                        "\t\"EXPLevel\"\tINTEGER DEFAULT 0,\n" +
                        "\tPRIMARY KEY(\"CharacterUUID\")\n" +
                        ");"
        );

        tables.add(
                "CREATE TABLE IF NOT EXISTS \"LogoutLocations\" (\n" +
                        "\t\"ID\"\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                        "\t\"CharacterUUID\"\tTEXT,\n" +
                        "\t\"World\"\tTEXT,\n" +
                        "\t\"X\"\tREAL,\n" +
                        "\t\"Y\"\tREAL,\n" +
                        "\t\"Z\"\tREAL,\n" +
                        "\t\"Yaw\"\tREAL,\n" +
                        "\t\"Pitch\"\tREAL\n" +
                        ");"
        );

        try {
            Statement stmt = conn.createStatement();
            // create a new table
            for (String createQuery : tables) {
                stmt.execute(createQuery);
            }

        } catch (Exception e) {
            plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }
    }

    public void executeUpdateQuery(String query) {
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }
    }

    public HashMap<Integer, HashMap<String, Object>> executeQuery(String query, HashMap<String, String> columns) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet resultSet = stmt.executeQuery(query);

            HashMap<Integer, HashMap<String, Object>> rows = new HashMap<>();

            int rowCount = 0;
            while (resultSet.next()) {

                HashMap<String, Object> resultRows = new HashMap<>();

                columns.forEach((columnName, columnType) -> {

                    try {
                        switch (columnName) {
                            case "int":
                            case "integer":
                                resultRows.put(columnName, resultSet.getInt(columnName));
                                break;
                            case "float":
                                resultRows.put(columnName, resultSet.getFloat(columnName));
                                break;
                            case "double":
                                resultRows.put(columnName, resultSet.getDouble(columnName));
                                break;
                            case "string":
                                resultRows.put(columnName, resultSet.getString(columnName));
                                break;
                            case "long":
                                resultRows.put(columnName, resultSet.getLong(columnName));
                                break;
                            default:
                                resultRows.put(columnName, resultSet.getObject(columnName));
                                break;
                        }
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }


                });

                rows.put(rowCount, resultRows);

                // Process the row.
                rowCount++;
            }
            return rows;
        } catch (Exception e) {
            plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }

        return null;
    }

    public int getRows(ResultSet rs) throws SQLException {

        CachedRowSet localResult = (CachedRowSet) rs;

        int rowCount = 0;
        while (localResult.next()) {
            // Process the row.
            rowCount++;
        }
        return rowCount;
    }
}

