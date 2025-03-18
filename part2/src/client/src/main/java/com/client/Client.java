package com.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

// Main class for the client application
public class Client {
    // URL of the FrontendService the client will interact with
    private static final String FRONTEND_SERVICE_URL = "http://localhost:12340";
    private static final Random random = new Random();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // Variables for latency tracking
    private static long totalQueryLatency = 0;
    private static long totalOrderLatency = 0;
    private static int queryCount = 0;
    private static int orderCount = 0;

    // Main method - entry point of the client application
    public static void main(String[] args) {
        double orderProbability = 0.5; // Probability of an operation being an order request
        runSession(orderProbability);

        // Calculating and printing average latencies for queries and orders
        if (queryCount > 0) {
            System.out.println("Average Query Latency: " + (totalQueryLatency / queryCount) + " ms");
        }
        if (orderCount > 0) {
            System.out.println("Average Order Latency: " + (totalOrderLatency / orderCount) + " ms");
        }
    }

    // Runs a session of 1000 operations (either query or order based on probability)
    private static void runSession(double orderProbability) { //AI Generated Code
        for (int i = 0; i < 1000; i++) {
            String productName = getRandomProductName(); // Gets a random product name
            
            // Measures and records the latency of querying a product
            long startTime = System.currentTimeMillis();
            JsonObject queryResponse = queryProduct(productName);
            long endTime = System.currentTimeMillis();
            totalQueryLatency += (endTime - startTime);
            queryCount++;
            
            // If the product is in stock and based on the orderProbability, place an order
            if (queryResponse != null && queryResponse.has("quantity")) {
                int quantity = queryResponse.get("quantity").getAsInt();
                System.out.println("Product details: " + queryResponse);

                if (quantity > 0 && random.nextDouble() < orderProbability) {
                    int orderQuantity = random.nextInt(10) + 1; // Order quantity is random between 1 and 10

                    startTime = System.currentTimeMillis();
                    JsonObject orderResponse = placeOrder(productName, orderQuantity);
                    endTime = System.currentTimeMillis();
                    totalOrderLatency += (endTime - startTime);
                    orderCount++;

                    if (orderResponse != null) {
                        System.out.println("Order response: " + orderResponse);
                    }
                }
            }
        }
    }

    // Sends a GET request to query product details
    private static JsonObject queryProduct(String productName) {
        String endpoint = FRONTEND_SERVICE_URL + "/products/" + productName;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), JsonObject.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null; // Simplified error handling, for a real application consider more robust error handling
        }
    }

    // Sends a POST request to place an order
    private static JsonObject placeOrder(String productName, int quantity) {
        String endpoint = FRONTEND_SERVICE_URL + "/orders";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("productName", productName);
        requestBody.addProperty("quantity", quantity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), JsonObject.class);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null; // Simplified error handling, for a real application consider more robust error handling
        }
    }

    // Selects a random product name from a predefined list
    private static String getRandomProductName() {
        String[] products = {"Tux", "Whale", "Dolphin", "Elephant", "Fox", "Python"};
        return products[random.nextInt(products.length)];
    }
}
