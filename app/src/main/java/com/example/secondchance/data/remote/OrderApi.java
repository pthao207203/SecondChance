package com.example.secondchance.data.remote;

import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.dto.request.PaymentRequest; // Dùng lại cái này cho placeOrder
import com.example.secondchance.dto.request.PreviewOrderRequest;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.dto.response.PreviewOrderResponse;
import com.google.gson.annotations.SerializedName;

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

    @POST("orders/{id}/confirm-delivery")
    Call<BasicResponse> confirmDelivery(@Path("id") String orderId);

    @POST("orders/preview")
    Call<PreviewOrderResponse> previewOrder(@Body PreviewOrderRequest request);

    @POST("orders/place")
    Call<BasicResponse> placeOrder(@Body PaymentRequest request);

    class ReturnRequestBody {
        @SerializedName("description") public String description;
        @SerializedName("media")       public List<String> media;

        @SerializedName("items")       public List<ReturnRequestItem> items;

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
        @SerializedName("data")    public OrderListData data;
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
    }
}
