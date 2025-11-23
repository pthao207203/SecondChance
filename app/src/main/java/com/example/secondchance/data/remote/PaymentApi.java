package com.example.secondchance.data.remote;

import com.example.secondchance.dto.request.PaymentRequest;
import com.example.secondchance.dto.response.PaymentResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PaymentApi {
    @POST("client/payment/create-url/zalopay")
    Call<PaymentResponse> createZaloPayUrl(@Body PaymentRequest request);
}