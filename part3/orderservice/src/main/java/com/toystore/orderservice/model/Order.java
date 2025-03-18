package com.toystore.orderservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

    private String productName;

    private int quantity;

    private long orderNumber;

    public Order() {}

    // All-arguments constructor for other uses
    @JsonCreator
    public Order(@JsonProperty("productName") String productName,
                 @JsonProperty("quantity") int quantity,
                 @JsonProperty("orderNumber") long orderNumber) {
        this.productName = productName;
        this.quantity = quantity;
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

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderNumber) {
        this.orderNumber = orderNumber;
    }
}