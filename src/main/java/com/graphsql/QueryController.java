package com.graphsql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;
import java.util.*;

@Controller
public class QueryController {

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.user}")
    private String dbUser;

    @Value("${db.password}")
    private String dbPassword;

    @Autowired 
    private AIClient aiClient;

    @GetMapping("/")
    public String index() {
        return "index"; 
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question, Model model) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // 1. Setup Indexing and Graph
            SchemaGraph graph = new SchemaGraph();
            SchemaIndexer indexer = new SchemaIndexer();
            Set<String> tableNames = indexer.indexDatabase(conn, graph); 
            String schemaSummary = indexer.getSchemaSummary(); 

            // 2. Get AI Logic
            String aiRawResponse = aiClient.getAIResponse(question, schemaSummary); 

            // 3. Generate SQL
            SQLGenie genie = new SQLGenie(graph, tableNames);
            String generatedSQL = genie.generateSQL(aiRawResponse);

            // 4. Clean SQL for JDBC (Remove semicolons which cause errors in Java)
            generatedSQL = generatedSQL.trim().replace(";", "");

            model.addAttribute("userQuestion", question);
            model.addAttribute("resultSQL", generatedSQL);

            List<Map<String, Object>> dataRows = new ArrayList<>();
            
            if (generatedSQL.toUpperCase().startsWith("SELECT")) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(generatedSQL)) {
                    
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>(); 
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnLabel(i), rs.getObject(i));
                        }
                        dataRows.add(row);
                    }
                }
            }
            
            model.addAttribute("dataRows", dataRows);
            if (!dataRows.isEmpty()) {
                model.addAttribute("columns", dataRows.get(0).keySet());
            }

        } catch (Exception e) {
            // UPDATED: Show the error on the webpage so you can debug live!
            model.addAttribute("error", "Database/Logic Error: " + e.getMessage());
            e.printStackTrace();
        }
        return "index";
    }
}