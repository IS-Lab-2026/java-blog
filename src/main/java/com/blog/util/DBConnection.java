package com.blog.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = setting("DB_URL",
            "jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC");
    private static final String USER = setting("DB_USER", "admin");
    private static final String PASSWORD = setting("DB_PASSWORD", "Ingeri@49276");

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        return value == null || value.isBlank() ? fallback : value;
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
