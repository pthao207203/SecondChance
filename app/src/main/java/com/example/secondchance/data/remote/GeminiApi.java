package com.example.secondchance.data.remote;

import com.example.secondchance.dto.request.GeminiRequest;
import com.example.secondchance.dto.response.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApi {
  // Sử dụng model gemini-1.5-flash cho nhanh và miễn phí
  @POST("v1beta/models/gemini-2.0-flash:generateContent")
  Call<GeminiResponse> generateContent(
    @Query("key") String apiKey,
    @Body GeminiRequest request
  );
}

