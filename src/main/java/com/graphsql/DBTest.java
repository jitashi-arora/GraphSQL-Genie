package com.graphsql;
import java.sql.Connection;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBTest {
    public static void main(String[] args) {
        // Change 'your_password' to the one you set in MySQL during Step 1
        String url = "jdbc:mysql://localhost:3306/graphsql_db";
        String user = "root";
        String password = "Khanakarora@12";

        try {
            System.out.println("Connecting to database...");
            Connection connection = DriverManager.getConnection(url, user, password);
            if (connection != null) {
                System.out.println("----------------------------------------------");
                System.out.println("Step 2 Success: VS Code is connected to MySQL!");
                System.out.println("----------------------------------------------");
            }
        } catch (Exception e) {
            System.out.println("Error: Could not connect to the database.");
            System.out.println("Check: 1. Is MySQL Server running? 2. Is your password correct?");
            e.printStackTrace();
        }
    }
}