package com.foxxite.multicharacter.sql;

import com.foxxite.multicharacter.MultiCharacter;

import javax.sql.rowset.CachedRowSet;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class SQLHandler {

    private final MultiCharacter plugin;
    private final File sqlFile;
    private Connection conn;

    public SQLHandler(final MultiCharacter plugin) {

        this.plugin = plugin;

        this.sqlFile = new File(this.plugin.getDataFolder(), "storage.db");

        if (!this.sqlFile.exists()) {
            this.sqlFile.getParentFile().mkdirs();
            try {
                this.sqlFile.createNewFile();
            } catch (final IOException e) {
                plugin.getPluginLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
                e.printStackTrace();
            }
        }

        this.connect();
        this.setupDatabase();
    }

    private void connect() {

        try {
            // db parameters
            final String url = "jdbc:sqlite:" + this.sqlFile.getAbsolutePath();
            // create a connection to the database
            this.conn = DriverManager.getConnection(url);

            this.plugin.getPluginLogger().log(new LogRecord(Level.INFO, "Connection to SQLite has been established."));

        } catch (final SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (final SQLException e) {
            this.plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }
    }

    private void setupDatabase() {
        final String playerTable =
                "CREATE TABLE IF NOT EXISTS \"Players\" (\n" +
                        "\t\"UUID\"\tTEXT UNIQUE,\n" +
                        "\t\"Username\"\tTEXT,\n" +
                        "\t\"IP\"\tTEXT,\n" +
                        "\tPRIMARY KEY(\"UUID\")\n" +
                        ");";

        final String characterTable =
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
                        ");";

        final String inventoryTable =
                "CREATE TABLE IF NOT EXISTS \"Inventories\" (\n" +
                        "\t\"CharacterUUID\"\tTEXT UNIQUE,\n" +
                        "\t\"Contents\"\tTEXT,\n" +
                        "\t\"Health\"\tREAL DEFAULT 20,\n" +
                        "\t\"Hunger\"\tINTEGER DEFAULT 20,\n" +
                        "\t\"EXP\"\tREAL DEFAULT 0,\n" +
                        "\t\"EXPLevel\"\tINTEGER DEFAULT 0,\n" +
                        "\tPRIMARY KEY(\"CharacterUUID\")\n" +
                        ");";

        final String locationTable =
                "CREATE TABLE IF NOT EXISTS \"LogoutLocations\" (\n" +
                        "\t\"ID\"\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                        "\t\"CharacterUUID\"\tTEXT,\n" +
                        "\t\"World\"\tTEXT,\n" +
                        "\t\"X\"\tREAL,\n" +
                        "\t\"Y\"\tREAL,\n" +
                        "\t\"Z\"\tREAL,\n" +
                        "\t\"Yaw\"\tREAL,\n" +
                        "\t\"Pitch\"\tREAL\n" +
                        ");";

        try {
            final Statement stmt = this.conn.createStatement();
            // create a new table
            stmt.execute(playerTable);
            stmt.execute(characterTable);
            stmt.execute(inventoryTable);
            stmt.execute(locationTable);
        } catch (final Exception e) {
            this.plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }
    }

    public void executeUpdateQuery(final String query) {
        try {
            final Statement stmt = this.conn.createStatement();
            stmt.executeUpdate(query);
        } catch (final Exception e) {
            this.plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }
    }

    public HashMap<Integer, HashMap<String, Object>> executeQuery(final String query, final HashMap<String, String> columns) {
        try {
            final Statement stmt = this.conn.createStatement();
            final ResultSet resultSet = stmt.executeQuery(query);

            final HashMap<Integer, HashMap<String, Object>> rows = new HashMap<>();

            int rowCount = 0;
            while (resultSet.next()) {

                final HashMap<String, Object> resultRows = new HashMap<>();

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
                    } catch (final SQLException throwables) {
                        throwables.printStackTrace();
                    }


                });

                rows.put(rowCount, resultRows);

                // Process the row.
                rowCount++;
            }
            return rows;
        } catch (final Exception e) {
            this.plugin.getLogger().log(new LogRecord(Level.SEVERE, e.getMessage() + " " + e.getCause()));
            e.printStackTrace();
        }

        return null;
    }

    public int getRows(final ResultSet rs) throws SQLException {

        final CachedRowSet localResult = (CachedRowSet) rs;

        int rowCount = 0;
        while (localResult.next()) {
            // Process the row.
            rowCount++;
        }
        return rowCount;
    }
}

