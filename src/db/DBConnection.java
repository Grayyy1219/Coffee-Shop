package com.coffeeshop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for creating JDBC connections to the coffee_shop database.
 */
public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/coffee_shop";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DBConnection() {
        // Utility class
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
