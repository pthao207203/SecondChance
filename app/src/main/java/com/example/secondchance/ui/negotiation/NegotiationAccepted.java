// ui/negotiation/NegotiationAccepted.java
package com.example.secondchance.ui.negotiation;

public class NegotiationAccepted {
    private String userName = "Fish can Fly";
    private String date = "18/02/2025";
    private String negotiationText = "Thương lượng lần 1";
    private String productTitle = "Giỏ gỗ cắm hoa";
    private String price = "₫ 50.000";
    private String quantity = "1";
    private String createdDate = "17/06/2025";

    // Phản hồi từ shop
    private String shopName = "Cá biết bay";
    private String replyDate = "18/02/2025";
    private String replyMessage = "Vui lòng thanh toán trong vòng 24h kể từ ngày yêu cầu chấp nhận. Nếu trong 24h không thanh toán thì đơn hàng sẽ tự động hủy";

    // Trạng thái thanh toán
    private boolean paid = false;

    // Getters
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public String getNegotiationText() { return negotiationText; }
    public String getProductTitle() { return productTitle; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getCreatedDate() { return createdDate; }
    public String getShopName() { return shopName; }
    public String getReplyDate() { return replyDate; }
    public String getReplyMessage() { return replyMessage; }
    public boolean isPaid() { return paid; }

    // Setters (nếu cần cập nhật)
    public void setPaid(boolean paid) { this.paid = paid; }
}
