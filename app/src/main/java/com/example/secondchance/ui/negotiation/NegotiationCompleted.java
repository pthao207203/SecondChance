package com.example.secondchance.ui.negotiation;

public class NegotiationCompleted {
    
    // productName: thực ra là tên người dùng (buyer) giống Accepted/Cancelled
    private String userName;
    private String productDate;
    private String negotiationRound;
    private String title;
    private String price;
    private String quantity;        // ví dụ: "x1"
    private String fixedPriceText;
    private String createdDate;     // ví dụ: "Đã tạo ngày: 17/06/2025"
    private String replyMessage;
    private String userAvatarUrl;
    private String shopName;
    private String shopAvatarUrl;
    private String shopId;
    private String productImageUrl;
    public NegotiationCompleted() {
    }
    
    public NegotiationCompleted(String userName, String productDate, String negotiationRound,
                                String title, String price, String quantity,
                                String fixedPriceText, String createdDate,
                                String replyMessage) {
        this.userName = userName;
        this.productDate = productDate;
        this.negotiationRound = negotiationRound;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.fixedPriceText = fixedPriceText;
        this.createdDate = createdDate;
        this.replyMessage = replyMessage;
    }
    
    public String getUserName() { return userName; }
    public String getProductDate() { return productDate; }
    public String getNegotiationRound() { return negotiationRound; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getFixedPriceText() { return fixedPriceText; }
    public String getCreatedDate() { return createdDate; }
    public String getReplyMessage() { return replyMessage; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public String getShopName() { return shopName; }
    public String getShopAvatarUrl() { return shopAvatarUrl; }
    public String getShopId() { return shopId; }
    public String getProductImageUrl() { return productImageUrl; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setProductDate(String productDate) { this.productDate = productDate; }
    public void setNegotiationRound(String negotiationRound) { this.negotiationRound = negotiationRound; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(String price) { this.price = price; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setFixedPriceText(String fixedPriceText) { this.fixedPriceText = fixedPriceText; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    public void setReplyMessage(String replyMessage) { this.replyMessage = replyMessage; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setShopAvatarUrl(String shopAvatarUrl) { this.shopAvatarUrl = shopAvatarUrl; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
}
