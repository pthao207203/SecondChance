package com.example.secondchance.data.model;

import java.util.List;

public class ShopOrder {
    // --- SỬA Ở ĐÂY: Thêm các trạng thái còn thiếu ---
    public enum ShopOrderType {
        UNCONFIRMED,
        CONFIRMED,        // Thêm cái này nếu cần dùng chung
        CONFIRMED_FIXED,
        CONFIRMED_AUCTION,
        DELIVERING,       // <--- QUAN TRỌNG: Thêm dòng này để fix lỗi
        SOLD,             // Thêm cho tab "Đã bán"
        CANCELED,         // Thêm cho tab "Hủy"
        REFUND            // Thêm cho tab "Trả hàng"
    }

    public enum RefundStatus { NOT_CONFIRMED, CONFIRMED, REJECTED, SUCCESSFUL, DELIVERING }
    public enum DeliveryOverallStatus { PACKAGED, AT_POST_OFFICE, DELIVERING, DELIVERED }

    String id;
    String date;
    String statusText;
    String description;
    ShopOrderType type;
    boolean isEvaluated;
    DeliveryOverallStatus deliveryStatus;
    RefundStatus refundStatus;

    List<ShopOrderProduct> items;
    String totalPrice;

    public ShopOrder(String id, List<ShopOrderProduct> items, String totalPrice, String date, String statusText, String description, ShopOrderType type, boolean isEvaluated, RefundStatus refundStatus, DeliveryOverallStatus deliveryStatus) {
        this.id = id;
        this.items = items;
        this.totalPrice = totalPrice;
        this.date = date;
        this.statusText = statusText;
        this.description = description;
        this.type = type;
        this.isEvaluated = isEvaluated;
        this.refundStatus = refundStatus;
        this.deliveryStatus = deliveryStatus;
    }

    public List<ShopOrderProduct> getItems() { return items; }
    public String getTotalPrice() { return totalPrice; }
    public boolean isEvaluated() { return isEvaluated; }
    public RefundStatus getRefundStatus() { return refundStatus; }
    public DeliveryOverallStatus getDeliveryStatus() { return deliveryStatus; }
    public String getId() { return id; }
    public String getDate() { return date; }
    public String getStatusText() { return statusText; }
    public String getDescription() { return description; }
    public ShopOrderType getType() { return type; }
}
