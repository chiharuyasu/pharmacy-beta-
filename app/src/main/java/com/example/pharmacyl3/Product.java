package com.example.pharmacyl3;

import java.io.Serializable;

public class Product implements Serializable {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String expiryDate;
    private String manufacturer;
    private String imageUri; // New field for image
    private String barcode;
    private String category;
    private int quantity = 1; // For cart usage

    // Constructor for new products (without ID)
    public Product(String name, String description, double price, int stock, String expiryDate, String manufacturer, String imageUri, String barcode, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.expiryDate = expiryDate;
        this.manufacturer = manufacturer;
        this.imageUri = imageUri;
        this.barcode = barcode;
        this.category = category;
    }

    // Constructor for products retrieved from the database (with ID)
    public Product(int id, String name, String description, double price, int stock, String expiryDate, String manufacturer, String imageUri, String barcode, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.expiryDate = expiryDate;
        this.manufacturer = manufacturer;
        this.imageUri = imageUri;
        this.barcode = barcode;
        this.category = category;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
