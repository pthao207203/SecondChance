package com.example.secondchance.data.model;

public class Order {
    public enum OrderType {
        UNCONFIRMED,
        CONFIRMED_FIXED,
        CONFIRMED_AUCTION
    }

    public enum RefundStatus {
        NOT_CONFIRMED,
        CONFIRMED,
        REJECTED,
        SUCCESSFUL
    }

    public enum DeliveryOverallStatus {
        PACKAGED,
        AT_POST_OFFICE,
        DELIVERING,
        DELIVERED
    }
    String id;
    String title;
    String price;
    String quantity;
    String subtitle;
    String date;
    String statusText;
    String description;
    OrderType type;
    boolean isEvaluated;
    DeliveryOverallStatus deliveryStatus;
    RefundStatus refundStatus;

    public Order(String id, String title, String price, String quantity, String subtitle, String date, String statusText, String description, OrderType type, boolean isEvaluated, RefundStatus refundStatus, DeliveryOverallStatus deliveryStatus) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.subtitle = subtitle;
        this.date = date;
        this.statusText = statusText;
        this.description = description;
        this.type = type;
        this.isEvaluated = isEvaluated;
        this.refundStatus = refundStatus;
        this.deliveryStatus = deliveryStatus;
    }

    public boolean isEvaluated() { return isEvaluated; }
    public RefundStatus getRefundStatus() { return refundStatus; }
    public DeliveryOverallStatus getDeliveryStatus() { return deliveryStatus; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getSubtitle() { return subtitle; }
    public String getDate() { return date; }
    public String getStatusText() { return statusText; }
    public String getDescription() { return description; }
    public OrderType getType() { return type; }
}