package com.example.secondchance.data.model;

import java.util.List;

public class ShopOrder {
    public enum ShopOrderType { UNCONFIRMED, CONFIRMED_FIXED, CONFIRMED_AUCTION }
    public enum RefundStatus { NOT_CONFIRMED, CONFIRMED, REJECTED, SUCCESSFUL,DELIVERING, }
    public enum DeliveryOverallStatus { PACKAGED, AT_POST_OFFICE, DELIVERING, DELIVERED }

    String id;
    String date;
    String statusText;
    String description;
    ShopOrderType type;
    boolean isEvaluated;
    DeliveryOverallStatus deliveryStatus;
    RefundStatus refundStatus;

    // --- THAY ĐỔI Ở ĐÂY ---
    List<ShopOrderProduct> items; // <--- THAY THẾ title, price, quantity, subtitle
    String totalPrice; // <--- Thêm tổng tiền cho cả đơn

    // Constructor đã được sửa đổi
    public ShopOrder(String id, List<ShopOrderProduct> items, String totalPrice, String date, String statusText, String description, ShopOrderType type, boolean isEvaluated, RefundStatus refundStatus, DeliveryOverallStatus deliveryStatus) {
        this.id = id;
        this.items = items; // <--- Sửa
        this.totalPrice = totalPrice; // <--- Sửa
        this.date = date;
        this.statusText = statusText;
        this.description = description;
        this.type = type;
        this.isEvaluated = isEvaluated;
        this.refundStatus = refundStatus;
        this.deliveryStatus = deliveryStatus;
    }

    // --- THÊM GETTER MỚI ---
    public List<ShopOrderProduct> getItems() { return items; }
    public String getTotalPrice() { return totalPrice; }

    // (Xóa các getter cho title, price, quantity, subtitle)
    public boolean isEvaluated() { return isEvaluated; }
    public RefundStatus getRefundStatus() { return refundStatus; }
    public DeliveryOverallStatus getDeliveryStatus() { return deliveryStatus; }
    public String getId() { return id; }
    public String getDate() { return date; }
    public String getStatusText() { return statusText; }
    public String getDescription() { return description; }
    public ShopOrderType getType() { return type; }
}