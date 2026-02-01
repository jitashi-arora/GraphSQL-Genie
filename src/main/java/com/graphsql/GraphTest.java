package com.graphsql;

import java.util.List;

public class GraphTest {
    public static void main(String[] args) {
        SchemaGraph graph = new SchemaGraph();

        // Let's manually simulate our database structure
        graph.addRelationship("Users", "Orders", "Users.user_id = Orders.user_id");
        graph.addRelationship("Orders", "Order_Items", "Orders.order_id = Order_Items.order_id");

        // Try to find a path from Users to Order_Items
        System.out.println("Searching for path: Users -> Order_Items");
        List<String> path = graph.findPath("Users", "Order_Items");

        if (path != null) {
            System.out.println("SUCCESS! Path found: " + String.join(" -> ", path));
        } else {
            System.out.println("FAILURE: No path found.");
        }
    }
}