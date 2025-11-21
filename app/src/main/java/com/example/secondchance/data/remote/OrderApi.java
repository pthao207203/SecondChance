package com.example.secondchance.data.remote;

import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.dto.request.PaymentRequest;
import com.example.secondchance.dto.request.PreviewOrderRequest;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.dto.response.PreviewOrderResponse;
import com.google.gson.annotations.SerializedName;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderApi {

    @GET("orders")
    Call<OrderListEnvelope> getOrdersByStatus(@Query("status") String status);

    @GET("orders/{id}")
    Call<OrderDetailResponse> getOrderDetail(@Path("id") String orderId);

    @POST("orders/{id}/cancel")
    Call<BasicResponse> cancelOrder(@Path("id") String orderId);

    @POST("orders/{id}/return")
    Call<BaseEnvelope> createReturnRequest(
            @Path("id") String orderId,
            @Body ReturnRequestBody body
    );
    @POST("/api/orders/{id}/confirm-delivery")
    Call<BasicResponse> confirmDelivery(@Path("id") String orderId);

    @POST("orders/preview")
    Call<OrderPreviewResponse> previewOrder(@Body PreviewRequestBody body);

    @POST("orders/preview")
    Call<PreviewOrderResponse> previewOrder(@Body PreviewOrderRequest request);

    @POST("orders/place")
    Call<PlaceOrderResponse> placeOrder(@Body PlaceOrderRequestBody body);

    @POST("orders/place")
    Call<BasicResponse> placeOrder(@Body PaymentRequest request);

    class PlaceOrderRequestBody {
        @SerializedName("items")
        public List<CartItemInfo> items;
        @SerializedName("paymentMethod")
        public String paymentMethod;
        @SerializedName("shippingAddressId")
        public String shippingAddressId;
        @SerializedName("shippingFee")
        public long shippingFee;
        @SerializedName("note")
        public String note;
    }

    class PlaceOrderResponse {
        @SerializedName("success")
        public boolean success;
        @SerializedName("data")
        public PlaceOrderData data;
    }

    class PlaceOrderData {
        @SerializedName("orders")
        public List<PlacedOrderWrapper> orders;
    }

    class PlacedOrderWrapper {
        @SerializedName("id")
        public String id;
        @SerializedName("order")
        public PlacedOrder order;
    }

    class PlacedOrder {
        @SerializedName("orderBuyerId") public String orderBuyerId;
        @SerializedName("orderSellerIds") public List<String> orderSellerIds;
        @SerializedName("orderItems") public List<PlacedOrderItem> orderItems;
        @SerializedName("orderSubtotal") public long orderSubtotal;
        @SerializedName("orderShippingFee") public long orderShippingFee;
        @SerializedName("orderTotalAmount") public long orderTotalAmount;
        @SerializedName("orderStatus") public int orderStatus;
        @SerializedName("orderPaymentMethod") public String orderPaymentMethod;
        @SerializedName("orderPaymentStatus") public String orderPaymentStatus;
        @SerializedName("orderShippingAddress") public PlacedOrderShippingAddress orderShippingAddress;
        @SerializedName("orderNote") public String orderNote;
        @SerializedName("orderLocked") public boolean orderLocked;
        @SerializedName("_id") public String _id;
        @SerializedName("__v") public int __v;
        @SerializedName("createdAt") public String createdAt;
        @SerializedName("updatedAt") public String updatedAt;
    }

    class PlacedOrderItem {
        @SerializedName("productId") public String productId;
        @SerializedName("name") public String name;
        @SerializedName("imageUrl") public String imageUrl;
        @SerializedName("price") public long price;
        @SerializedName("qty") public int qty;
        @SerializedName("shopId") public String shopId;
        @SerializedName("lineTotal") public long lineTotal;
        @SerializedName("_id") public String _id;
    }

    class PlacedOrderShippingAddress {
        @SerializedName("name") public String name;
        @SerializedName("phone") public String phone;
        @SerializedName("label") public String label;
        @SerializedName("country") public String country;
        @SerializedName("province") public String province;
        @SerializedName("ward") public String ward;
        @SerializedName("street") public String street;
        @SerializedName("location") public Location location;
    }

    class Location {
        @SerializedName("lat") public double lat;
        @SerializedName("lng") public double lng;
    }


    // --- Các lớp đã có ---

    class ReturnRequestBody {
        @SerializedName("description") public String description;
        @SerializedName("items") public List<ReturnRequestItem> items;
        @SerializedName("media") public List<String> media;
        public ReturnRequestBody(String desc, List<ReturnRequestItem> items, List<String> mediaUrls) {
            this.description = desc;
            this.items = items;
            this.media = mediaUrls;
        }
    }

    class ReturnRequestItem {
        @SerializedName("productId") public String productId;
        @SerializedName("qty")       public int quantity;

        public ReturnRequestItem(String id, int qty) {
            this.productId = id;
            this.quantity = qty;
        }
    }

    class OrderListEnvelope {
        @SerializedName("success") public boolean success;
        @SerializedName("data") public OrderListData data;
        @SerializedName("meta") public Object meta;
    }

    class OrderListData {
        @SerializedName("page")   public int page;
        @SerializedName("limit")  public int limit;
        @SerializedName("total")  public int total;
        @SerializedName("orders") public List<OrderWrapper> orders;
    }

    class BaseEnvelope {
        @SerializedName("success") public boolean success;
        @SerializedName("message") public String message;
        @SerializedName("data") public Object data;
    }

    class PreviewRequestBody {
        @SerializedName("items") public List<CartItemInfo> items;
    }

    class CartItemInfo {
        @SerializedName("productId") public String productId;
        @SerializedName("qty") public int qty;
    }

    class OrderPreviewResponse {
        @SerializedName("success") public boolean success;
        @SerializedName("data") public PreviewData data;
    }

    class PreviewData {
        @SerializedName("items") public List<ShopItems> items;
        @SerializedName("paymentMethods") public List<PaymentMethod> paymentMethods;
        @SerializedName("addresses") public List<Address> addresses;
        @SerializedName("totalPrice") public long totalPrice;
        @SerializedName("walletBalance") public long walletBalance;
    }

    class ShopItems {
        @SerializedName("shopId") public String shopId;
        @SerializedName("shopName") public String shopName;
        @SerializedName("items") public List<ProductItem> items;
    }

    class ProductItem {
        @SerializedName("productId") public String productId;
        @SerializedName("name") public String name;
        @SerializedName("shortDescription") public String shortDescription;
        @SerializedName("imageUrl") public String imageUrl;
        @SerializedName("price") public long price;
        @SerializedName("qty") public int qty;
        @SerializedName("lineTotal") public long lineTotal;
    }

    class PaymentMethod {
        @SerializedName("code") public String code;
        @SerializedName("label") public String label;
    }

    class Address {
        @SerializedName("_id") public String id;
        @SerializedName("name") public String name;
        @SerializedName("phone") public String phone;
        @SerializedName("label") public String label;
        @SerializedName("street") public String street;
        @SerializedName("ward") public String ward;
        @SerializedName("province") public String province;
        @SerializedName("isDefault") public boolean isDefault;
    }
}
