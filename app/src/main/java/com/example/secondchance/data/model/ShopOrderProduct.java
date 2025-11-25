package com.example.secondchance.data.model;

public class ShopOrderProduct {
    private final String productId;
    private final String title;
    private final String subtitle;
    private final String price;
    private final int imageRes;
    private final int quantity;
    private boolean selected;

    // TRƯỜNG MỚI: Dùng cho ảnh từ API (URL string)
    private final String imageUrl;

    // --- Constructor 1: CŨ (Giữ lại để tương thích code cũ dùng dummy data) ---
    public ShopOrderProduct(String productId, String title, String subtitle,
                            String price, int imageRes, int quantity) {
        this.productId = productId;
        this.title = title;
        this.subtitle = subtitle;
        this.price = price;
        this.imageRes = imageRes;
        this.quantity = quantity;
        this.selected = false;
        this.imageUrl = null; // Mặc định null
    }

    // --- Constructor 2: MỚI (Dùng cho API khi có URL ảnh) ---
    public ShopOrderProduct(String productId, String title, String subtitle,
                            String price, int imageRes, int quantity, String imageUrl) {
        this.productId = productId;
        this.title = title;
        this.subtitle = subtitle;
        this.price = price;
        this.imageRes = imageRes; // Vẫn có thể truyền 0 nếu không dùng
        this.quantity = quantity;
        this.selected = false;
        this.imageUrl = imageUrl;
    }

    // --- Getters ---
    public String getProductId() { return productId; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getPrice() { return price; }
    public int getImageRes() { return imageRes; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Getter mới cho URL ảnh
    public String getImageUrl() { return imageUrl; }
}