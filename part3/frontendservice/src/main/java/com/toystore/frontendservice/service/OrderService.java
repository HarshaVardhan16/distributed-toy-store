package com.toystore.frontendservice.service;

import com.toystore.frontendservice.model.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final RestTemplate restTemplate;
    private final List<String> orderServiceReplicas;
    private String cachedLeaderUrl;

    @Autowired
    public OrderService(RestTemplate restTemplate,
                        @Value("${order.service.replicas}") List<String> orderServiceReplicas) {
        this.restTemplate = restTemplate;
        this.orderServiceReplicas = orderServiceReplicas;
        this.cachedLeaderUrl = null;
        getLeaderUrl();
    }

    public Object placeOrder(OrderRequest orderRequest) {
        String leaderUrl = getLeaderUrl();
        String url = leaderUrl + "/orders";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderRequest> request = new HttpEntity<>(orderRequest, headers);

        return restTemplate.postForObject(url, request, Object.class);
    }

    public Object getOrder(long orderNumber) {
        String leaderUrl = getLeaderUrl();
        System.out.println(leaderUrl);
        String url = leaderUrl + "/orders/" + orderNumber;
        return restTemplate.getForObject(url, Object.class);
    }

    public String getLeaderUrl() {
        if (cachedLeaderUrl != null) {
            // Check if the cached leader is still available
            if (isReplicaAvailable(cachedLeaderUrl)) {
                return cachedLeaderUrl;
            }
        }

        String leaderUrl = null;
        int maxId = -1;
        List<String> availableReplicas = new ArrayList<>(orderServiceReplicas);
        System.out.println(maxId);
        while (!availableReplicas.isEmpty()) {
            for (String replicaUrl : availableReplicas) {
                try {
                    ResponseEntity<Object> response = restTemplate.postForEntity(replicaUrl + "/orders/health", null, Object.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        int replicaId = (int) response.getBody();
                        System.out.println(replicaId);
                        if (replicaId > maxId) {
                            maxId = replicaId;
                            leaderUrl = replicaUrl;
                        }
                    }
                } catch (Exception e) {
                    // Log error or handle as needed
                }
            }

            if (leaderUrl != null) {
                break;
            }
        }

        if (leaderUrl == null) {
            throw new RuntimeException("No available order service replicas");
        }

        cachedLeaderUrl = leaderUrl;

        // Notify all replicas about the selected leader
        for (String replicaUrl : orderServiceReplicas) {
            try {
                restTemplate.postForEntity(replicaUrl + "/orders/leader", maxId, null);
            } catch (Exception e) {
                System.out.println("Replica is not found");
            }
        }

        return leaderUrl;
    }

    private boolean isReplicaAvailable(String replicaUrl) {
        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(replicaUrl + "/orders/health", null, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}