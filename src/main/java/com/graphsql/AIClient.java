package com.graphsql;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONObject;
import org.json.JSONArray;

@Component
public class AIClient {
     @Value("${ai.api.key}") 
    private String apiKey;

    // CHANGE: We now take 'schemaSummary' instead of just 'tableNames'
    public String getAIResponse(String userInput, String schemaSummary) {
        try {
            // This prompt is the secret to "Zero-Maintenance"
            String systemPrompt = "You are a SQL expert. Use ONLY these tables and columns: " + schemaSummary + 
                                  ". Identify which tables are needed and write a SQL WHERE clause. " +
                                  "Return ONLY this format: TABLES: table1, table2 | WHERE: condition";

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "llama-3.1-8b-instant"); 
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", userInput));
            requestBody.put("messages", messages);

            // Added Connect Timeout to prevent hanging
            HttpClient client = HttpClient.newBuilder()
                                          .connectTimeout(Duration.ofSeconds(10))
                                          .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(10)) // Request timeout
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());

            if (jsonResponse.has("error")) {
                String errorMsg = jsonResponse.getJSONObject("error").getString("message");
                System.out.println("!!! AI API ERROR: " + errorMsg);
                return "TABLES: customers | WHERE: 1=1"; 
            }

            return jsonResponse.getJSONArray("choices")
                               .getJSONObject(0)
                               .getJSONObject("message")
                               .getString("content");

        } catch (Exception e) {
            System.out.println("AI CONNECTION ERROR (Timeout or Network): " + e.getMessage());
            return "TABLES: customers | WHERE: 1=1"; 
        }
    }
}