package com.toystore.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toystore.orderservice.model.Order;
import com.toystore.orderservice.model.OrderRequest;
import com.toystore.orderservice.service.OrderProcessingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderProcessingService orderProcessingService;
    private final int replicaId;

    @Autowired
    public OrderController(OrderProcessingService orderProcessingService,
                           @Value("${replica.id}") int replicaId) {
        this.orderProcessingService = orderProcessingService;
        this.replicaId = replicaId;
    }

    @PostMapping
    public ResponseEntity<Object> processOrder(@RequestBody OrderRequest orderRequest) {
        if (!orderProcessingService.isSynchronized()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Replica is synchronizing, please try later.");
        }
        Map<String, Object> response = orderProcessingService.processOrder(orderRequest);
        if (response.containsKey("error")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrder(@PathVariable long orderNumber) {
        Order order = orderProcessingService.getOrderDetails(orderNumber);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/leader")
    public ResponseEntity<Void> setLeader(@RequestBody int leaderId) {
        boolean isLeader = leaderId == replicaId;
        orderProcessingService.setLeader(isLeader);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/isLeader")
    public ResponseEntity<Object> isLeader() {
        return ResponseEntity.ok(orderProcessingService.isLeader());
    }

    @PostMapping("/health")
    public ResponseEntity<Object> isHealthy() {
        return ResponseEntity.ok(replicaId);
    }
    @PostMapping("/replicate")
    public ResponseEntity<Void> replicateOrder(@RequestBody Order order) {
        orderProcessingService.persistOrderLog(order);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/synchronize/{lastOrderNumber}")
    public ResponseEntity<String> synchronize(@PathVariable long lastOrderNumber) {
        List<Order> missedOrders = new ArrayList<>();
        long latestOrderNumber = lastOrderNumber;
        List<Order> currentBatch;

        do {
            currentBatch = orderProcessingService.getMissedOrders(latestOrderNumber);
            missedOrders.addAll(currentBatch);
            if (!currentBatch.isEmpty()) {
                latestOrderNumber = currentBatch.get(currentBatch.size() - 1).getOrderNumber(); // Assuming Order has getOrderNumber()
            }
        } while (!currentBatch.isEmpty());

        orderProcessingService.setSynchronized(true);

        // Convert the list of orders to JSON (or another format)
        ObjectMapper mapper = new ObjectMapper();
        try {
            String missedOrdersJson = mapper.writeValueAsString(missedOrders);
            return ResponseEntity.ok(missedOrdersJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error serializing orders: " + e.getMessage());
        }
    }

}