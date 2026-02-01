package com.graphsql;

import java.util.*;

public class SQLGenie {
    private SchemaGraph graph;
    private Set<String> allTables;

    public SQLGenie(SchemaGraph graph, Set<String> allTables) {
        this.graph = graph;
        this.allTables = allTables;
    }

    public String generateSQL(String aiResponse) {
        try {
            // 1. Robust Parsing of AI Response
            if (!aiResponse.contains("|") || !aiResponse.contains("TABLES:")) {
                return "SELECT * FROM " + (allTables.isEmpty() ? "dual" : allTables.iterator().next()) + " WHERE 1=1; -- (AI format error)";
            }

            String[] parts = aiResponse.split("\\|");
            String tablesPart = parts[0].replaceAll("(?i)TABLES:", "").trim();
            String filterPart = parts[1].replaceAll("(?i)WHERE:", "").trim();

            // Clean up the filter (remove trailing semicolon if AI added one)
            if (filterPart.endsWith(";")) filterPart = filterPart.substring(0, filterPart.length() - 1);

            String[] rawDetectedTables = tablesPart.split(",");
            List<String> cleanDetectedTables = new ArrayList<>();
            for (String t : rawDetectedTables) {
                String clean = t.trim().toLowerCase();
                if (!clean.isEmpty()) cleanDetectedTables.add(clean);
            }

            if (cleanDetectedTables.isEmpty()) {
                return "SELECT * FROM " + allTables.iterator().next() + " WHERE 1=1;";
            }

            // 2. Build the FULL Join Path
            // We use a LinkedHashSet to maintain order and prevent duplicate tables
            LinkedHashSet<String> fullPath = new LinkedHashSet<>();
            
            if (cleanDetectedTables.size() == 1) {
                fullPath.add(cleanDetectedTables.get(0));
            } else {
                for (int i = 0; i < cleanDetectedTables.size() - 1; i++) {
                    String start = cleanDetectedTables.get(i);
                    String end = cleanDetectedTables.get(i + 1);
                    
                    List<String> segment = graph.findPath(start, end);
                    if (segment != null) {
                        fullPath.addAll(segment);
                    } else {
                        // If no path, at least add the individual tables
                        fullPath.add(start);
                        fullPath.add(end);
                    }
                }
            }

            List<String> finalPath = new ArrayList<>(fullPath);
            
            // 3. Construct the SQL String
            StringBuilder sql = new StringBuilder();
            String firstTable = finalPath.get(0);
            sql.append("SELECT * FROM ").append(firstTable);

            for (int i = 0; i < finalPath.size() - 1; i++) {
                String t1 = finalPath.get(i);
                String t2 = finalPath.get(i + 1);
                String condition = graph.getJoinCondition(t1, t2);
                
                if (condition != null) {
                    sql.append("\nJOIN ").append(t2).append(" ON ").append(condition);
                } else {
                    // Fallback: If BFS found a path but we lost a condition, 
                    // we skip the join to prevent SQL syntax errors
                    System.out.println("Warning: Missing join condition between " + t1 + " and " + t2);
                }
            }

            // 4. Add the AI-generated WHERE clause
            sql.append("\nWHERE ").append(filterPart);
            
            return sql.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "-- Error in SQL Generation: " + e.getMessage();
        }
    }
}