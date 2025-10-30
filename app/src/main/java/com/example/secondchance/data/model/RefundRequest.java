package com.example.secondchance.data.model;
import java.util.List;
public class RefundRequest {
    private String refundId;
    private String orderId;
    private List<OrderProduct> items;
    private String reason;
    private String status;
    private long createdAt;

    public RefundRequest(String refundId, String orderId, List<OrderProduct> items, String reason) {
        this.refundId = refundId;
        this.orderId = orderId;
        this.items = items;
        this.reason = reason;
        this.status = "Chưa xác nhận";
        this.createdAt = System.currentTimeMillis();
    }

    public String getRefundId() { return refundId; }
    public String getOrderId() { return orderId; }
    public List<OrderProduct> getItems() { return items; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
}
