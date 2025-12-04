package com.example.secondchance.dto.response;

import java.util.List;

public class OrderDetailResponse {
  public boolean success;
  public Data data;
  
  public static class Data {
    public Order order;
    public Shipment shipment;
    public Seller seller;
  }
  
  public static class Order {
    public String id;
    public List<OrderItem> orderItems;
    public long orderSubtotal;
    public long orderShippingFee;
    public long orderTotalAmount;
    public int  orderStatus;
    public String orderPaymentMethod;
    public String orderPaymentStatus;
    public ShippingAddress orderShippingAddress;
    public String createdAt;
    public String updatedAt;
    public ReturnRequest returnRequest;
    public boolean isReviewed;
  }
  
  public static class OrderItem {
    public String productId;
    public String name;
    public String imageUrl;
    public long price;
    public int  qty;
    public String shopId;
    public long lineTotal;
  }
  
  public static class ShippingAddress {
    public String name, phone, label, country, province, ward, street;
  }
  
  public static class ReturnRequest {
    public String status;
    public List<String> media;
    public String description;
    public boolean refundProcessed;
    public Long refundAmount;
    public String createdAt, reviewedAt;
  }
  
  public static class Shipment {
    public String courierCode;
    public Integer currentStatus;
    public String rawStatus;
    public List<Event> events;
    public String trackingNumber;
  }
  
  public static class Event {
    public Integer eventCode;
    public String description;
    public String eventTime;
  }
  
  public static class Seller {
    public String id;
    public String userName;
    public String userAvatar;
  }
}
