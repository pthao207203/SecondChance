package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDetailResponse {
  @SerializedName("success")
  public boolean success;

  @SerializedName("data")
  public Data data;

  public static class Data {
    @SerializedName("order")
    public Order order;

    @SerializedName("shipment")
    public Shipment shipment;

    @SerializedName("seller")
    public Seller seller;
  }

  public static class Order {
    // --- ID Đơn hàng (Hứng cả _id và id) ---
    @SerializedName(value = "id", alternate = {"_id", "orderId", "order_id"})
    public String id;

    @SerializedName("orderItems")
    public List<OrderItem> orderItems;

    @SerializedName("orderSubtotal")
    public long orderSubtotal;

    @SerializedName("orderShippingFee")
    public long orderShippingFee;

    @SerializedName("orderTotalAmount")
    public long orderTotalAmount;

    @SerializedName("orderStatus")
    public int orderStatus;

    @SerializedName("orderPaymentMethod")
    public String orderPaymentMethod;

    @SerializedName("orderPaymentStatus")
    public String orderPaymentStatus;

    // --- Object Địa chỉ (Hứng shippingAddress và orderShippingAddress) ---
    @SerializedName(value = "orderShippingAddress", alternate = {"shippingAddress", "shipping_address", "address"})
    public ShippingAddress orderShippingAddress;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    @SerializedName("returnRequest")
    public ReturnRequest returnRequest;
  }

  public static class OrderItem {
    @SerializedName(value = "productId", alternate = {"product_id", "_id", "id"})
    public String productId;

    @SerializedName("name")
    public String name;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("price")
    public long price;

    @SerializedName("qty")
    public int qty;

    @SerializedName("shopId")
    public String shopId;

    @SerializedName("lineTotal")
    public long lineTotal;
  }

  // --- Chi tiết địa chỉ (Hứng đủ các biến thể tên) ---
  public static class ShippingAddress {
    // Tên người nhận
    @SerializedName(value = "name", alternate = {"fullName", "full_name", "receiverName", "receiver_name", "user_name", "userName"})
    public String name;

    // Số điện thoại
    @SerializedName(value = "phone", alternate = {"phoneNumber", "phone_number", "tel", "mobile"})
    public String phone;

    @SerializedName("label")
    public String label;

    @SerializedName("country")
    public String country;

    // Tỉnh / Thành phố
    @SerializedName(value = "province", alternate = {"city", "city_name", "provinceName", "tinh", "thanhPho", "thanh_pho"})
    public String province;

    // Quận / Huyện / Phường / Xã
    @SerializedName(value = "ward", alternate = {"district", "district_name", "wardName", "phuong", "xa", "quan", "huyen"})
    public String ward;

    // Số nhà / Đường / Địa chỉ chi tiết
    @SerializedName(value = "street", alternate = {"address", "address_line_1", "streetName", "street_name", "detail", "soNha", "dia_chi_cu_the"})
    public String street;

    // --- MỚI THÊM: Trường hợp API trả về 1 chuỗi địa chỉ gộp sẵn ---
    @SerializedName(value = "fullAddress", alternate = {"full_address", "formattedAddress", "formatted_address"})
    public String fullAddress;
  }

  public static class ReturnRequest {
    @SerializedName("status")
    public String status;

    @SerializedName("media")
    public List<String> media;

    @SerializedName("description")
    public String description;

    @SerializedName("refundProcessed")
    public boolean refundProcessed;

    @SerializedName("refundAmount")
    public Long refundAmount;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("reviewedAt")
    public String reviewedAt;
  }

  public static class Shipment {
    @SerializedName("courierCode")
    public String courierCode;

    @SerializedName("currentStatus")
    public Integer currentStatus;

    @SerializedName("rawStatus")
    public String rawStatus;

    @SerializedName("events")
    public List<Event> events;

    @SerializedName("trackingNumber")
    public String trackingNumber;
  }

  public static class Event {
    @SerializedName("eventCode")
    public Integer eventCode;

    @SerializedName("description")
    public String description;

    @SerializedName("eventTime")
    public String eventTime;
  }

  public static class Seller {
    @SerializedName(value = "id", alternate = {"_id", "userId"})
    public String id;

    @SerializedName("userName")
    public String userName;

    @SerializedName("userAvatar")
    public String userAvatar;
  }
}
