package com.example.secondchance.data.model;

public class OrderItem {
    private String imageUrl;
    private int imageResId;
    private String title;
    private String description;
    private String price;

    public OrderItem(int imageResId, String title, String description, String price) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.price = price;
    }
    public OrderItem(String imageUrl, String title, String description, String price) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.price = price;
    }
    
    public String getImageUrl() { return imageUrl; }
    public int getImageResId() { return imageResId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
}