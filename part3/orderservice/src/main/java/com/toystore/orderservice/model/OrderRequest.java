package com.toystore.orderservice.model;

public class OrderRequest {
    private String productName;
    private int quantity;

    // Parameterized constructor
    public OrderRequest(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    // Getter and setter for productName
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    // Getter and setter for quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}