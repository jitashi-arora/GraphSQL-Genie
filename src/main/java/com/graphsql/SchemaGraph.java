package com.graphsql;

import java.util.*;

public class SchemaGraph {
    private Map<String, List<String>> adjList = new HashMap<>();
    private Map<String, String> joinConditions = new HashMap<>();

    public void addRelationship(String tableA, String tableB, String condition) {
        // Convert to lowercase before storing
        tableA = tableA.toLowerCase();
        tableB = tableB.toLowerCase();

        adjList.computeIfAbsent(tableA, k -> new ArrayList<>()).add(tableB);
        adjList.computeIfAbsent(tableB, k -> new ArrayList<>()).add(tableA);
        
        joinConditions.put(tableA + "-" + tableB, condition);
        joinConditions.put(tableB + "-" + tableA, condition);
    }

    public List<String> findPath(String startTable, String endTable) {
        // Convert input to lowercase to match our stored keys
        startTable = startTable.toLowerCase();
        endTable = endTable.toLowerCase();

        if (!adjList.containsKey(startTable) || !adjList.containsKey(endTable)) {
            return null;
        }

        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(Arrays.asList(startTable));
        visited.add(startTable);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String lastTable = path.get(path.size() - 1);

            if (lastTable.equals(endTable)) {
                return path;
            }

            for (String neighbor : adjList.getOrDefault(lastTable, new ArrayList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }
        return null;
    }

    public String getJoinCondition(String t1, String t2) {
        return joinConditions.get(t1.toLowerCase() + "-" + t2.toLowerCase());
    }
}