// ui/negotiation/NegotiationRequest.java
package com.example.secondchance.ui.negotiation;

public class NegotiationRequest {
    // === HEADER (user/buyer) ===
    private String userName;
    private String date;
    private String userAvatarUrl;
    
    // === THÔNG TIN THƯƠNG LƯỢNG / SẢN PHẨM ===
    private String negotiationText;
    private String productTitle;
    private String price;
    private String quantity;
    private String createdDate;
    private String productImageUrl;
    
    // === THÔNG TIN SHOP (để mở chat) ===
    private String shopId;
    private String shopName;
    private String shopAvatarUrl;
    
    // === TRẠNG THÁI ===
    private boolean hasReply;
    
    // Constructor đầy đủ (nếu muốn dùng tay)
    public NegotiationRequest(String userName, String date, String negotiationText,
                              String productTitle, String price, String quantity,
                              String createdDate, boolean hasReply) {
        this.userName = userName;
        this.date = date;
        this.negotiationText = negotiationText;
        this.productTitle = productTitle;
        this.price = price;
        this.quantity = quantity;
        this.createdDate = createdDate;
        this.hasReply = hasReply;
    }
    
    public NegotiationRequest() {
    
    }
    
    // ===== Getters cũ =====
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public String getNegotiationText() { return negotiationText; }
    public String getProductTitle() { return productTitle; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getCreatedDate() { return createdDate; }
    public boolean isHasReply() { return hasReply; }
    
    // ===== Setters cũ =====
    public void setUserName(String userName) { this.userName = userName; }
    public void setDate(String date) { this.date = date; }
    public void setNegotiationText(String negotiationText) { this.negotiationText = negotiationText; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public void setPrice(String price) { this.price = price; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    public void setHasReply(boolean hasReply) { this.hasReply = hasReply; }
    
    // ===== Getters mới =====
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public String getProductImageUrl() { return productImageUrl; }
    public String getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getShopAvatarUrl() { return shopAvatarUrl; }
    
    // ===== Setters mới =====
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setShopAvatarUrl(String shopAvatarUrl) { this.shopAvatarUrl = shopAvatarUrl; }
}
