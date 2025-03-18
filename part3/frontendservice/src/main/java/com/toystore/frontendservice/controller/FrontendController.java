package com.toystore.frontendservice.controller;


import com.toystore.frontendservice.LRUCache;
import com.toystore.frontendservice.model.InvalidationRequest;
import com.toystore.frontendservice.model.OrderRequest;
import com.toystore.frontendservice.service.CatalogService;
import com.toystore.frontendservice.service.OrderService;

import java.util.Collections;

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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FrontendController {
    private final CatalogService catalogService;
    private final OrderService orderService;
    private final LRUCache<String, Object> cache; // Using the custom cache class
    private int cacheActive;

    @Autowired
    public FrontendController(CatalogService catalogService, OrderService orderService, @Value("${cache.active}") int cacheActive) {
        this.catalogService = catalogService;
        this.orderService = orderService;
        this.cache = new LRUCache<>(10); // Assuming the catalog has more than 10 items, sets LRU cache size
        this.cacheActive = cacheActive;
    }

    @GetMapping("/products/{productName}")
    public ResponseEntity<Object> getProduct(@PathVariable String productName) {
        if(cacheActive == 1){
            Object cachedResponse = cache.getCache(productName);
            cache.printCache();
            if (cachedResponse != null) {
                System.out.println("Helloooooo");
                return ResponseEntity.ok(cachedResponse);
            }
        }

        try {
            Object response = catalogService.getProduct(productName);
            cache.putCache(productName, response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse("Failed to fetch product", 500));
        }
    }

    @PostMapping("/orders")
    public ResponseEntity<Object> placeOrder(@RequestBody OrderRequest orderRequest) {
        try {
            Object response = orderService.placeOrder(orderRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse("Failed to place order", 500));
        }
    }

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<Object> getOrder(@PathVariable long orderNumber) {
        try {
            Object response = orderService.getOrder(orderNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse("Failed to fetch order", 500));
        }
    }

    @PostMapping("/invalidate-cache")
    public ResponseEntity<Void> invalidateCache(@RequestBody InvalidationRequest request) {
        cache.invalidateCache(request.getProductName());
        return ResponseEntity.ok().build();
    }
    
    private Map<String, Object> errorResponse(String message, int code) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", message);
        error.put("code", code);
        return Collections.singletonMap("error", error);
    }
    @GetMapping("/leader")
    public ResponseEntity<String> getLeaderUrl() {
        try {
            String leaderUrl = orderService.getLeaderUrl();
            return ResponseEntity.ok(leaderUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve leader URL");
        }
    }
}