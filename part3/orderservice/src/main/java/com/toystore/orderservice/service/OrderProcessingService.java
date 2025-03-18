package com.toystore.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.toystore.orderservice.model.Order;
import com.toystore.orderservice.model.OrderRequest;
import com.toystore.orderservice.model.ProductResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderProcessingService {  //AI Generated Code
    private final RestTemplate restTemplate;
    private final String catalogServiceUrl;
    private final List<String> replicaUrls;
    private final String frontendServiceUrl;
    private final int portNumber;
    private final int replicaId;
    private boolean isLeader = false;
    private int start = 0;
    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    private String leaderUrl;
    private boolean isSynchronized = false;

    @Autowired
    public OrderProcessingService(RestTemplate restTemplate,
                                  @Value("${catalog.service.url}") String catalogServiceUrl,
                                  @Value("${start}") int startId,
                                  @Value("${frontend.service.url}") String frontendServiceUrl,
                                  @Value("${replica.urls}") String[] replicaUrls,
                                  @Value("${replica.id}") int replicaId,
                                  @Value("${server.port}") int portNumber) {
        this.restTemplate = restTemplate;
        this.catalogServiceUrl = catalogServiceUrl;
        this.replicaUrls = Arrays.asList(replicaUrls);
        this.frontendServiceUrl = frontendServiceUrl;
        this.start = startId;
        this.replicaId = replicaId;
        this.portNumber = portNumber;
    }

    public void setLeaderUrl(String leaderUrl) {
        this.leaderUrl = leaderUrl;
    }

    public boolean isSynchronized() {
        return this.isSynchronized;
    }

    public void setSynchronized(boolean Synchronized) {
        this.isSynchronized = Synchronized;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!isLeader() && start == 0) {
            long lastOrderNumber = determineLastKnownOrderNumber();
            requestSyncFromLeader(lastOrderNumber);
        } else {
            setSynchronized(true);
        }
    }

    public void requestSyncFromLeader(long lastOrderNumber) {
        if (isLeader()) {
            System.out.println("This replica is the leader. No need to request synchronization.");
            return;
        }
        leaderUrl = getLeaderUrl();
        String syncUrl = leaderUrl + "/orders/synchronize/" + lastOrderNumber;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(syncUrl, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                String ordersJson = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                List<Order> missedOrders = mapper.readValue(ordersJson, new TypeReference<List<Order>>(){});
                processMissedOrders(missedOrders);
                this.isSynchronized = true;
                System.out.println("Synchronization successful.");
            } else {
                System.err.println("Failed to synchronize with leader: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Failed to synchronize with leader: " + e.getMessage());
        }
    }

    public void processMissedOrders(List<Order> orders) {
        for (Order order : orders) {
            persistOrderLog(order);
        }
        System.out.println("Processed and logged " + orders.size() + " missed orders successfully.");
    }

    public Map<String,Object> processOrder(OrderRequest orderRequest) {
        if (isLeader()) {
            boolean isInStock = checkStockAvailability(orderRequest.getProductName(), orderRequest.getQuantity());
            if (!isInStock) {
                return createErrorResponse("Product is out of stock");
            }

            long orderNumber = generateOrderNumber();

            boolean updateSuccess = updateStock(orderRequest.getProductName(), orderRequest.getQuantity());
            if (!updateSuccess) {
                return createErrorResponse("Failed to update stock in the catalog service");
            }
            Order order = new Order(orderRequest.getProductName(), orderRequest.getQuantity(), orderNumber);
            persistOrderLog(order);

            replicateOrderToFollowers(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", orderNumber);
            return response;
        } else {
            return createErrorResponse("This node is not leader");
        }
    }

    private boolean checkStockAvailability(String productName, int quantity) {
        String url = catalogServiceUrl + "/products/" + productName;
        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(url, ProductResponse.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            ProductResponse productResponse = response.getBody();
            return productResponse!=null && productResponse.getQuantity()>=quantity;
        }
        return false;
    }

    private boolean updateStock(String productName, int quantity) {
        String url = catalogServiceUrl + "/products/" + productName + "/update";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Integer> requestEntity = new HttpEntity<>(quantity, headers);
        
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
        
        if (response.getStatusCode() != HttpStatus.OK) {
            return false;
        }
        return true;
    }

    private long generateOrderNumber() {
        long timestamp = Instant.now().toEpochMilli();
        return timestamp;
    }
    

    public synchronized void persistOrderLog(Order order) {
        String fileName = "order_log_" + replicaId + ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName, true))) {
            writer.println(order.getOrderNumber() + "," + order.getProductName() + "," + order.getQuantity());
        } catch (IOException e) {
            // Handle the exception appropriately (e.g., log an error message)
        }
    }

    public synchronized Order getOrderDetails(long orderNumber) {
        String fileName = "order_log_" + replicaId + ".csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] orderData = line.split(",");
                if (orderData.length == 3) {
                    long orderNo = Long.parseLong(orderData[0]);
                    if (orderNo == orderNumber) {
                        String productName = orderData[1];
                        int quantity = Integer.parseInt(orderData[2]);
                        Order order = new Order(productName, quantity, orderNo);
                        return order;
                    }
                }
            }
        } catch (IOException e) {
            // Handle the exception appropriately (e.g., log an error message)
        }
        return null;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private void replicateOrderToFollowers(Order order) {
        if (isLeader()) {
            List<String> followerUrls = replicaUrls.stream()
                    .filter(url -> !url.equals(getOwnUrl()))
                    .collect(Collectors.toList());
    
            for (String followerUrl : followerUrls) {
                try {
                    restTemplate.postForObject(followerUrl + "/orders/replicate", order, Void.class);
                } catch (Exception e) {
                    System.err.println("Failed to replicate order to " + followerUrl + ": " + e.getMessage());
                }
            }
        }
    }
    
    public long determineLastKnownOrderNumber() {
        long lastOrderNumber = 0;
        String fileName = "order_log_" + this.replicaId + ".csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] orderData = line.split(",");
                if (orderData.length == 3) {
                    long orderNo = Long.parseLong(orderData[0]);
                    if (orderNo > lastOrderNumber) {
                        lastOrderNumber = orderNo;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the order file: " + e.getMessage());
        }
        return lastOrderNumber;
    }

    private String getOwnUrl() {
        return "http://localhost:" + this.portNumber;
    }

    public List<Order> getMissedOrders(long lastOrderNumber) {
        List<Order> missedOrders = new ArrayList<>();
        String fileName = "order_log_" + this.replicaId + ".csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] orderData = line.split(",");
                if (orderData.length == 3) {
                    long orderNo = Long.parseLong(orderData[0]);
                    if (orderNo > lastOrderNumber) {
                        String productName = orderData[1];
                        int quantity = Integer.parseInt(orderData[2]);
                        missedOrders.add(new Order(productName, quantity, orderNo));
                    }
                }
            }
        } catch (IOException e) {
            // Handle exception appropriately
        }
        return missedOrders;
    }
    
    public boolean isLeader() {
        return isLeader;
    }

    private String getLeaderUrl() {
        String leaderUrlEndpoint = frontendServiceUrl + "/api/leader";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(leaderUrlEndpoint, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to retrieve leader URL from frontend service. Status code: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while retrieving leader URL from frontend service: " + e.getMessage());
        }
    }
}