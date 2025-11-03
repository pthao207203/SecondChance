// ui/negotiation/NegotiationRequest.java
package com.example.secondchance.ui.negotiation;

public class NegotiationRequest {
    private String userName;
    private String date;
    private String negotiationText;
    private String productTitle;
    private String price;
    private String quantity;
    private String createdDate;
    private boolean hasReply;

    // Constructor mặc định (dữ liệu mẫu)
    public NegotiationRequest() {
        this.userName = "Fish can Fly";
        this.date = "18/02/2025";
        this.negotiationText = "Thương lượng lần 1";
        this.productTitle = "Giỏ gỗ cắm hoa";
        this.price = "₫ 50.000";
        this.quantity = "1";
        this.createdDate = "17/06/2025";
        this.hasReply = false;
    }

    // Constructor đầy đủ
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

    // Getters
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public String getNegotiationText() { return negotiationText; }
    public String getProductTitle() { return productTitle; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getCreatedDate() { return createdDate; }
    public boolean isHasReply() { return hasReply; }

    // Setters (nếu cần cập nhật)
    public void setUserName(String userName) { this.userName = userName; }
    public void setDate(String date) { this.date = date; }
    public void setNegotiationText(String negotiationText) { this.negotiationText = negotiationText; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public void setPrice(String price) { this.price = price; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    public void setHasReply(boolean hasReply) { this.hasReply = hasReply; }
}
