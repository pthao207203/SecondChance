// ui/negotiation/NegotiationAccepted.java
package com.example.secondchance.ui.negotiation;

public class NegotiationAccepted {
    private String productId;
    private String negotiationId;
    private String userName = "Người dùng";
    private String date = "Chưa có thông tin";
    private String negotiationText = "Thương lượng lần 1";
    private String productTitle = "Sản phẩm không xác định";
    private String price = "Chưa có thông tin";
    private int quantity;
    private String createdDate = "Chưa có thông tin";
    // Avatar + hình
    private String userAvatarUrl;     // avatar người thương lượng / counterpart
    private String shopAvatarUrl;     // nếu khác, có thể tách
    private String productImageUrl;   // ảnh sản phẩm
    // Phản hồi từ shop
    private String shopName = "Người bán";
    private String replyDate = "Chưa có thông tin";
    private String replyMessage = "Vui lòng thanh toán trong vòng 24h kể từ ngày yêu cầu chấp nhận. Nếu trong 24h không thanh toán thì đơn hàng sẽ tự động hủy";
    private String shopId;
    public String getNegotiationId() {
        return negotiationId;
    }
    
    public void setNegotiationId(String negotiationId) {
        this.negotiationId = negotiationId;
    }
    public String getId() {
        return productId;
    }
    
    public void setId(String id) {
        this.productId = id;
    }
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    // Trạng thái thanh toán
    private boolean paid = false;

    // Getters
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public String getNegotiationText() { return negotiationText; }
    public String getProductTitle() { return productTitle; }
    public String getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getCreatedDate() { return createdDate; }
    public String getShopName() { return shopName; }
    public String getReplyDate() { return replyDate; }
    public String getReplyMessage() { return replyMessage; }
    public boolean isPaid() { return paid; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    public String getShopAvatarUrl() { return shopAvatarUrl; }
    public String getProductImageUrl() { return productImageUrl; }

    // Setters (nếu cần cập nhật)
    public void setPaid(boolean paid) { this.paid = paid; }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public void setNegotiationText(String negotiationText) {
        this.negotiationText = negotiationText;
    }
    
    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public void setReplyDate(String replyDate) {
        this.replyDate = replyDate;
    }
    
    public void setReplyMessage(String replyMessage) {
        this.replyMessage = replyMessage;
    }
    
    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
    
    public void setShopAvatarUrl(String shopAvatarUrl) {
        this.shopAvatarUrl = shopAvatarUrl;
    }
    
    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }
}
