package com.orderservice;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class OrderService {
    // URL of the catalog service
    private static final String CATALOG_SERVICE_URL = System.getenv("CATALOG_SERVICE_URL") != null ? System.getenv("CATALOG_SERVICE_URL") : "http://localhost:12345";

    // File to log orders
    private static final String ORDER_LOG_FILE = "order_log.csv";
    // Counter for generating order IDs
    private static final AtomicInteger orderIdCounter = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        // Create a thread pool for handling incoming requests
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("Order Service started on port 12346!");
        // Initialize the order ID counter
        initializeOrderIdCounter();
        // Create an HTTP server listening on port 12346
        HttpServer server = HttpServer.create(new InetSocketAddress(12346), 0);
        // Set the handler for "/orders" endpoint
        server.createContext("/orders", new OrderHandler());
        server.setExecutor(executor);
        server.start();
    }

    // Method to initialize the order ID counter from the order log file
    private static void initializeOrderIdCounter() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDER_LOG_FILE))) {
            String lastLine = "";
            String line;
            // Read the last line of the order log file
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            // If the last line is not empty, extract the last order ID and set the counter
            if (!lastLine.isEmpty()) {
                String[] parts = lastLine.split(",");
                // Assuming the first column is the order ID
                int lastOrderId = Integer.parseInt(parts[0]);
                orderIdCounter.set(lastOrderId);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Order log file not found. Starting with order ID 0.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Error parsing order ID from log file. Starting with order ID 0.");
        }
    }

    // Handler for processing incoming HTTP requests to "/orders" endpoint
    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                StringBuilder requestBody = new StringBuilder();
                String line;
                // Read the request body
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
                reader.close();

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(requestBody.toString(), JsonObject.class);
                String productName = jsonObject.get("productName").getAsString();
                int quantity = jsonObject.get("quantity").getAsInt();

                // Process the order and check if it was successful
                boolean orderSuccessful = processOrder(productName, quantity);

                JsonObject responseObject = new JsonObject();
                String response ="";
                if (orderSuccessful) {
                    // Generate a new order ID and log the order
                    int orderId = orderIdCounter.getAndIncrement();
                    responseObject.addProperty("order_number", orderId);
                    logOrder(orderId, productName, quantity);
                    response = gson.toJson(responseObject);
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    responseObject.addProperty("error", "Order failed.");
                    response = gson.toJson(responseObject);
                    exchange.sendResponseHeaders(404, response.length());
                }

                // Send the response back to the client
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Return "Method Not Allowed" for non-POST requests
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }

        // Method to process the order by making a request to the catalog service
        private boolean processOrder(String productName, int quantity) throws IOException {
            String catalogServiceUrl = CATALOG_SERVICE_URL + "/?updateItemStock=" + productName + "&quantity=" + quantity;
            URL url = new URL(catalogServiceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine();
                in.close();
                return true;
            } else {
                return false;
            }
        }

        // Method to log the order in the order log file
        private void logOrder(int orderId, String productName, int quantity) {
            try (FileWriter writer = new FileWriter(ORDER_LOG_FILE, true)) {
                writer.write(orderId + "," + productName + "," + quantity + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}