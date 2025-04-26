package com.example.pharmacyl3;

public class Order {
    public int id;
    public int customerId;
    public String productName;
    public int quantity;
    public double totalPrice;
    public String orderDate;

    public Order(int id, int customerId, String productName, int quantity, double totalPrice, String orderDate) {
        this.id = id;
        this.customerId = customerId;
        this.productName = productName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }
}
