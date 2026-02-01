package com.graphsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Set;

public class IntegrationTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/graphsql_db";
        String user = "root";
        String password = "Khanakarora@12"; // CHANGE THIS!

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            SchemaGraph graph = new SchemaGraph();
            SchemaIndexer indexer = new SchemaIndexer();

            System.out.println("Starting Database Indexing...");
            Set<String> tables = indexer.indexDatabase(conn, graph);

            System.out.println("\nIndexing Complete. Testing BFS on real data...");
            
            // This path should be found automatically from your MySQL Foreign Keys!
            List<String> path = graph.findPath("users", "order_items");
            if (path != null) {
                System.out.println("SUCCESS! Real Join Path found: " + String.join(" -> ", path));
            } else {
                System.out.println("No path found. Did you run the SQL script in Step 1?");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}