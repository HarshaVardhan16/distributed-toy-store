package com.toystore.frontendservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CatalogService {
    private final RestTemplate restTemplate;
    private final String catalogServiceUrl;

    @Autowired
    public CatalogService(RestTemplate restTemplate, @Value("${catalog.service.url}") String catalogServiceUrl) {
        this.restTemplate = restTemplate;
        this.catalogServiceUrl = catalogServiceUrl;
    }

    public Object getProduct(String productName) {
        String url = catalogServiceUrl + "/products/" + productName;
        return restTemplate.getForObject(url, Object.class);
    }
}