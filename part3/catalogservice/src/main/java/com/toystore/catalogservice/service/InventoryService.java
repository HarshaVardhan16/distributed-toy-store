package com.toystore.catalogservice.service;

import com.toystore.catalogservice.model.Product;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class InventoryService {
    private Map<String, Product> inventory;
    private ReadWriteLock lock;
    private RestTemplate restTemplate;

    @Autowired
    public InventoryService(RestTemplate restTemplate) {
        inventory = new HashMap<>();
        lock = new ReentrantReadWriteLock();
        this.restTemplate = restTemplate;
        initializeFromDatabase();
        startRestockingTask();
    }

    private void initializeFromDatabase() {
        try {
            // Reading the database.txt file
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/database.txt"));
            inventory = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entry = line.split(", ");
                String productName = entry[0];
                int stock = Integer.parseInt(entry[2]);
                double price = Double.parseDouble(entry[1]);
                inventory.put(productName, new Product(productName, stock, price));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Queries stock for a specific item
    public Product getProduct(String productName) {
        lock.readLock().lock();
        try {
            return inventory.get(productName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean updateProductStock(String productName, int quantity) {
        lock.writeLock().lock();
        try {
            Product product = inventory.get(productName);
            if (product != null && product.getQuantity() >= quantity) {
                int newStock = product.getQuantity() - quantity;
                Product updatedProduct = new Product(product.getName(), newStock, product.getPrice());
                inventory.put(productName, updatedProduct);
                writeCatalogToFile(productName, newStock);
                invalidateCache(productName);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    private void startRestockingTask() { // AI Generated code
        Runnable restockingTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000); // Sleep for 10 seconds
                    restockOutOfStockItems();
                } catch (InterruptedException e) {
                    System.out.println("Restocking thread was interrupted.");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.out.println("Error occurred during restocking: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        Thread restockingThread = new Thread(restockingTask);
        restockingThread.setDaemon(true);
        restockingThread.start();
        System.out.println("Restocking thread started.");
    }

    private void restockOutOfStockItems() {
        lock.writeLock().lock();
        try {
            for (Map.Entry<String, Product> entry : inventory.entrySet()) {
                String productName = entry.getKey();
                Product product = entry.getValue();
                if (product.getQuantity() == 0) {
                    int restockQuantity = 100;
                    Product restockedProduct = new Product(product.getName(), restockQuantity, product.getPrice());
                    inventory.put(productName, restockedProduct);
                    writeCatalogToFile(productName, restockQuantity);
                    invalidateCache(productName);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    // Writes the updated catalog back to the database.txt file
    private synchronized void writeCatalogToFile(String productName, int newStock) { //AI GENERATED CODE
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
    private void invalidateCache(String productName) {
        String frontendServiceUrl = "http://localhost:12343/api/invalidate-cache";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> map = new HashMap<>();
            map.put("productName", productName);  // Create a map to hold the product name
    
            HttpEntity<Map<String, String>> request = new HttpEntity<>(map, headers);  // Pass the map as the body
            restTemplate.postForEntity(frontendServiceUrl, request, Void.class);
        } catch (Exception e) {
            System.out.println("Failed to invalidate cache for " + productName + ": " + e.getMessage());
        }
    }
}