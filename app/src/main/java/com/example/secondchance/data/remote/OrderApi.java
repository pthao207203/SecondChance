package com.example.secondchance.data.remote;

import com.example.secondchance.dto.response.OrderDetailResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OrderApi {
  @GET("orders/{id}")                   // => /api/orders/{id}
  Call<OrderDetailResponse> getOrderDetail(@Path("id") String orderId);
}
