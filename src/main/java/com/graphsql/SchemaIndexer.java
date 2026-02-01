package com.graphsql;

import java.sql.*;
import java.util.*;

public class SchemaIndexer {

    // This will hold the "Table(col1, col2)" string for the AI
    private String schemaSummary = "";

    public Set<String> indexDatabase(Connection conn, SchemaGraph graph) throws SQLException {
        Set<String> allTables = new HashSet<>();
        StringBuilder summaryBuilder = new StringBuilder();
        DatabaseMetaData metaData = conn.getMetaData();

        // 1. Get all table names
        // Note: Using null for catalog to make it work across different MySQL setups
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            // Skip system tables if any
            if (tableName.contains("SYSTEM") || tableName.contains("sys_")) continue;
            
            allTables.add(tableName);
            
            // 2. FOR EACH TABLE, GET COLUMNS (The "Zero-Maintenance" Secret)
            summaryBuilder.append(tableName).append("(");
            ResultSet columns = metaData.getColumns(null, null, tableName, "%");
            List<String> colNames = new ArrayList<>();
            while (columns.next()) {
                colNames.add(columns.getString("COLUMN_NAME"));
            }
            summaryBuilder.append(String.join(", ", colNames)).append("); ");
            System.out.println("Indexed Table & Columns: " + tableName);
        }

        this.schemaSummary = summaryBuilder.toString();

        // 3. Get Relationships (Foreign Keys)
        for (String tableName : allTables) {
            ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName);
            
            while (foreignKeys.next()) {
                String pkTable = foreignKeys.getString("PKTABLE_NAME"); 
                String fkTable = foreignKeys.getString("FKTABLE_NAME"); 
                String pkCol = foreignKeys.getString("PKCOLUMN_NAME");
                String fkCol = foreignKeys.getString("FKCOLUMN_NAME");

                String condition = pkTable + "." + pkCol + " = " + fkTable + "." + fkCol;
                graph.addRelationship(pkTable, fkTable, condition);
            }
        }
        return allTables;
    }

    // This is the new method we will use to give the AI the "Context"
    public String getSchemaSummary() {
        return schemaSummary;
    }
}