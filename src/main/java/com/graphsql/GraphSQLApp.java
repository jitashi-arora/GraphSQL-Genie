package com.graphsql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GraphSQLApp {
    public static void main(String[] args) {
        // This line starts the built-in Tomcat web server
        SpringApplication.run(GraphSQLApp.class, args);
        System.out.println("--------------------------------------");
        System.out.println("GraphSQL Web UI is running at: http://localhost:8080");
        System.out.println("--------------------------------------");
    }
}