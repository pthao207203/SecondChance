package com.example.secondchance.data.model;

public class OrderProduct {
    private final String productId;
    private final String title;
    private final String subtitle;
    private final String price;
    private final int imageRes;
    private final int quantity;
    private boolean selected;

    public OrderProduct(String productId, String title, String subtitle,
                        String price, int imageRes, int quantity) {
        this.productId = productId;
        this.title = title;
        this.subtitle = subtitle;
        this.price = price;
        this.imageRes = imageRes;
        this.quantity = quantity;
        this.selected = false;
    }

    public String getProductId() { return productId; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getPrice() { return price; }
    public int getImageRes() { return imageRes; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}