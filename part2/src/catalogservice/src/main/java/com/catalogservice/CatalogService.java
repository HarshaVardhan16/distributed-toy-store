package com.catalogservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.google.gson.JsonObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// Main class for the Catalog Service microservice
public class CatalogService{
    // In-memory representation of catalog items
    private Map<String, Toy> catalog;
    // Lock for thread-safe read/write operations on the catalog
    private ReadWriteLock lock;

    // Main method - entry point of the application
    public static void main(String[] args) throws IOException {
        // Executor service for handling concurrent requests
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("Catalog Service started on port 12345!");
        // Creating and starting the HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(12345), 0);
        CatalogService catalogService = new CatalogService();
        server.createContext("/", new RequestHandler(catalogService));
        server.setExecutor(executor); // Set the executor for asynchronous handling
        server.start();
    }

    // HTTP request handler class
    static class RequestHandler implements HttpHandler {
        private CatalogService catalogService;

        // Constructor, receives the main CatalogService instance
        public RequestHandler(CatalogService catalogService) {
            this.catalogService = catalogService;
        }

        // Handles incoming HTTP requests
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestMethod = exchange.getRequestMethod();
            // Handling GET requests
            if (requestMethod.equalsIgnoreCase("GET")) {
                String query = exchange.getRequestURI().getQuery();
                // Querying item stock
                if (query != null && query.startsWith("queryItemStock")) {
                    String productName = query.substring(query.indexOf("=") + 1);
                    JsonObject itemDetails = catalogService.queryItemStock(productName);
                    String response = itemDetails.toString();
                    if(itemDetails.has("error")){
                        exchange.sendResponseHeaders(404, response.length());
                    }
                    else{
                        exchange.sendResponseHeaders(200, response.length());
                    }
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } 
            // Handling POST requests
            else if (requestMethod.equalsIgnoreCase("POST")) {
                String query = exchange.getRequestURI().getQuery();
                // Updating item stock
                if (query != null && query.startsWith("updateItemStock")) {
                    String[] params = query.split("&");
                    String productName = params[0].substring(params[0].indexOf("=") + 1);
                    int quantity = Integer.parseInt(params[1].substring(params[1].indexOf("=") + 1));
                    boolean orderSuccess = catalogService.updateItemStock(productName, quantity);
                    String response = "";
                    if(orderSuccess){
                        response = "Item stock updated successfully!";
                        exchange.sendResponseHeaders(200, response.length());
                    }
                    else{
                        response = "Item stock update failed!";
                        exchange.sendResponseHeaders(404, response.length());
                    }   
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        }
    }

    // CatalogService constructor
    public CatalogService() {
        catalog = new HashMap<>();
        lock = new ReentrantReadWriteLock();
        initializeFromDatabase(); // Initialize the catalog from the database file
    }

    // Initializes the catalog from database.txt
    private void initializeFromDatabase() {
        try {
            // Reading the database.txt file
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/database.txt"));
            catalog = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entry = line.split(", ");
                String productName = entry[0];
                int stock = Integer.parseInt(entry[2]);
                double price = Double.parseDouble(entry[1]);
                catalog.put(productName, new Toy(productName, price, stock));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Queries stock for a specific item
    public JsonObject queryItemStock(String productName) {
        lock.readLock().lock();
        JsonObject itemDetails = new JsonObject();
        try {
            if (catalog.containsKey(productName)) {
                Toy toy = catalog.get(productName);
                int stock = toy.getStock();
                double price = toy.getPrice();
                itemDetails.addProperty("name", productName);
                itemDetails.addProperty("price", price);
                itemDetails.addProperty("quantity", stock);
            }
            else{
                JsonObject error = new JsonObject();
                error.addProperty("code", "404");
                error.addProperty("message", "Product not found");
                itemDetails.add("error", error);
            }
        } finally {
            lock.readLock().unlock();
        }
        return itemDetails;
    }
    
    // Updates the stock for a specific item
    public boolean updateItemStock(String productName, int quantity) {
        lock.writeLock().lock();
        try {
            if (catalog.containsKey(productName) && quantity > 0) {
                Toy toy = catalog.get(productName);
                int currentStock = toy.getStock();
                int newStock = currentStock - quantity;
                if (newStock >= 0) {
                    // Create a new Toy object with updated stock
                    Toy updatedToy = new Toy(toy.getName(), toy.getPrice(), newStock);
                    catalog.put(productName, updatedToy); // Update the catalog
                    writeCatalogToFile(productName, newStock); // Persist changes to file
                    return true;
                }
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Writes the updated catalog back to the database.txt file
    private void writeCatalogToFile(String productName, int newStock) { //AI GENERATED CODE
        try {
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/database.txt"));
            StringBuilder inputBuffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(productName)) {
                    String[] parts = line.split(", ");
                    // Update the stock for the product
                    line = parts[0] + ", " + parts[1] + ", " + newStock;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            reader.close();

            // Writing the updated content back to database.txt
            BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/database.txt"));
            writer.write(inputBuffer.toString());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class representing a Toy
    static class Toy {
        private String name;
        private double price;
        private int stock;
        
        public Toy(String name, double price, int stock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        // Getters and setters
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
        public void setPrice(double price) { this.price = price; }
        public void setStock(int stock) { this.stock = stock; }
    }
}
