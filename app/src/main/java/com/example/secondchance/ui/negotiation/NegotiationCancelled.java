package com.example.secondchance.ui.negotiation;

public class NegotiationCancelled {
    private String productId;
    
    private String productName;
    private String productDate;
    private String negotiationRound;
    private String title;
    private String price;
    private String quantity;
    private String fixedPriceText;
    private String createdDate;
    private String shopName;
    private String shopDate;
    private String replyMessage;
    private String shopId;
    private String shopAvatarUrl;
    private Number status;
    private String userName;
    private String userAvatarUrl;
    private String productImageUrl;
    
    public String getId() {
        return productId;
    }
    
    public void setId(String id) {
        this.productId = id;
    }
    public Number getStatus() {
        return status;
    }
    
    public void setStatus(Number status) {
        this.status = status;
    }
    public boolean isCancel() {
        return (4==(int)status);
    }
    
    public boolean isReject() {
        return (3==(int)status);
    }
    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }
    
    public String getProductImageUrl() {
        return productImageUrl;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
    
    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }
    
    public void setShopId(String shopId) {
        this.shopId = shopId;
    }
    
    public void setShopAvatarUrl(String shopAvatarUrl) {
        this.shopAvatarUrl = shopAvatarUrl;
    }
    
    public String getShopId() {
        return shopId;
    }
    
    public String getShopAvatarUrl() {
        return shopAvatarUrl;
    }
    
    private boolean isOverdue;  // true = quá hạn, false = bị từ chối bình thường

    public NegotiationCancelled(String productName, String productDate, String negotiationRound,
                                String title, String price, String quantity, String fixedPriceText,
                                String createdDate, String shopName, String shopDate,
                                String replyMessage, boolean isOverdue) {
        this.productName = productName;
        this.productDate = productDate;
        this.negotiationRound = negotiationRound;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.fixedPriceText = fixedPriceText;
        this.createdDate = createdDate;
        this.shopName = shopName;
        this.shopDate = shopDate;
        this.replyMessage = replyMessage;
        this.isOverdue = isOverdue;
    }
    
    public NegotiationCancelled() {
    }
    
    public String getProductName() { return productName; }
    public String getProductDate() { return productDate; }
    public String getNegotiationRound() { return negotiationRound; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getFixedPriceText() { return fixedPriceText; }
    public String getCreatedDate() { return createdDate; }
    public String getShopName() { return shopName; }
    public String getShopDate() { return shopDate; }
    public String getReplyMessage() { return replyMessage; }
    public boolean isOverdue() { return isOverdue; }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public void setProductDate(String productDate) {
        this.productDate = productDate;
    }
    
    public void setNegotiationRound(String negotiationRound) {
        this.negotiationRound = negotiationRound;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
    
    public void setFixedPriceText(String fixedPriceText) {
        this.fixedPriceText = fixedPriceText;
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public void setShopDate(String shopDate) {
        this.shopDate = shopDate;
    }
    
    public void setReplyMessage(String replyMessage) {
        this.replyMessage = replyMessage;
    }
    
    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }
}
