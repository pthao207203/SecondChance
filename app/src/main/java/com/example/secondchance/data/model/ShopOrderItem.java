package com.example.secondchance.data.model;

public class ShopOrderItem {
    private String imageUrl;
    private int imageResId;
    private String title;
    private String description;
    private String price;

    public ShopOrderItem(int imageResId, String title, String description, String price) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    public ShopOrderItem(String imageUrl, String title, String description, String price) {
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
