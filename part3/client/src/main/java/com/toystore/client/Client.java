package com.toystore.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Client {
    private static final String BASE_URL = "http://ec2-54-159-33-11.compute-1.amazonaws.com:12343/api";
    private static final List<String> TOYS = Arrays.asList(
            "Dolphin", "Whale", "Tux", "Elephant", "Fox", "Python", "Lion",
            "Tiger", "Bear", "Giraffe", "Zebra", "Kangaroo", "Panda", "Hippo",
            "Rabbit", "Owl"
    );
    private static final Random RANDOM = new Random();
    private double totalQueryLatency = 0;
    private double totalBuyLatency = 0;
    private int queryCount = 0;
    private int buyCount = 0;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<Long, Order> placedOrders = new HashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
        if (args.length < 2) {
            System.out.println("Usage: java -jar <jar-file> <orderProbability> <iterations>");
            System.exit(1);
        }
        double orderProbability = Double.parseDouble(args[0]);
        int iterations = Integer.parseInt(args[1]);
        Client client = new Client();
        client.run(orderProbability, iterations);
    }

    public void run(double orderProbability, int iterations) {
        for (int i = 0; i < iterations; i++) {
            String toy = getRandomToy();
    
            long startTime = System.currentTimeMillis();
            int quantity = getProductQuantity(toy);
            long queryLatency = System.currentTimeMillis() - startTime;
            totalQueryLatency += queryLatency;  // Accumulate query latency
            queryCount++;  // Increment query count
    
            System.out.println("Query Latency: " + queryLatency + "ms");
    
            if (quantity > 0 && RANDOM.nextDouble() < orderProbability) {
                startTime = System.currentTimeMillis();
                Order order = placeOrder(toy, 1);
                long purchaseLatency = System.currentTimeMillis() - startTime;
                totalBuyLatency += purchaseLatency;  // Accumulate buy latency
                buyCount++;  // Increment buy count
    
                if (order != null) {
                    placedOrders.put(order.getOrderNumber(), order);
                    System.out.println("Purchase Latency: " + purchaseLatency + "ms");
                }
            }
        }
    
        printAverageLatencies();
        validateOrders();
    }
    private void printAverageLatencies() {
        if (queryCount > 0) {
            double averageQueryLatency = totalQueryLatency / queryCount;
            System.out.println("Average Query Latency: " + averageQueryLatency + "ms");
        }
        if (buyCount > 0) {
            double averageBuyLatency = totalBuyLatency / buyCount;
            System.out.println("Average Buy Latency: " + averageBuyLatency + "ms");
        }
    }

    private String getRandomToy() {
        int index = RANDOM.nextInt(TOYS.size());
        return TOYS.get(index);
    }

    private int getProductQuantity(String productName) {
        String url = BASE_URL + "/products/" + productName;
        ProductResponse response = restTemplate.getForObject(url, ProductResponse.class);
        return response != null ? response.getQuantity() : 0;
    }

    @SuppressWarnings("unchecked")
    private Order placeOrder(String productName, int quantity) {
        String url = BASE_URL + "/orders";
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setProductName(productName);
        orderRequest.setQuantity(quantity);
        Map<String, Object> response = restTemplate.postForObject(url, orderRequest, Map.class);
        long orderNumber = ((Number) response.get("success")).longValue();
        return response != null ? new Order(orderNumber, productName, quantity) : null;
    }

    private void validateOrders() {
        placedOrders.forEach((orderNumber, expectedOrder) -> {
            try {
                String url = BASE_URL + "/orders/" + orderNumber;
                Order actualOrder = restTemplate.getForObject(url, Order.class);
                if (actualOrder == null) {
                    System.out.println("No order found for order number: " + orderNumber);
                    return;
                }

                // Check individual fields
                boolean mismatchFound = false;
                if (!Objects.equals(expectedOrder.getOrderNumber(), actualOrder.getOrderNumber())) {
                    System.out.println("Order Number mismatch: Expected " + expectedOrder.getOrderNumber() + ", but got " + actualOrder.getOrderNumber());
                    mismatchFound = true;
                }
                if (!Objects.equals(expectedOrder.getProductName(), actualOrder.getProductName())) {
                    System.out.println("Product Name mismatch: Expected " + expectedOrder.getProductName() + ", but got " + actualOrder.getProductName());
                    mismatchFound = true;
                }
                if (expectedOrder.getQuantity() != actualOrder.getQuantity()) {
                    System.out.println("Quantity mismatch: Expected " + expectedOrder.getQuantity() + ", but got " + actualOrder.getQuantity());
                    mismatchFound = true;
                }

                if (!mismatchFound) {
                    System.out.println("Order validated successfully for order number: " + orderNumber);
                }
            } catch (HttpServerErrorException e) {
                System.err.println("Server error occurred while validating order: " + orderNumber + ", Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("An unexpected error occurred while validating order: " + orderNumber + ", Error: " + e.getMessage());
            }
        });
        System.out.println("All Orders Validated!");
    }
}

class ProductResponse {
    private String name;
    private int quantity;
    private double price;

    
    public ProductResponse(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    
}

class OrderRequest {
	
    private String productName;
    private int quantity;
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}

class OrderResponse {
    private long orderNumber;

	public long getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(long orderNumber) {
		this.orderNumber = orderNumber;
	}

}

class Order {
    private long orderNumber;
    private String productName;
    private int quantity;

    public Order(long orderNumber, String productName, int quantity) {
        this.orderNumber = orderNumber;
        this.productName = productName;
        this.quantity = quantity;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}