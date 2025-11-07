package com.example.secondchance.data.remote;

import com.example.secondchance.dto.response.WalletHistoryResponse;
import com.example.secondchance.dto.response.WalletPurchasedHistoryResponse;
import com.example.secondchance.dto.response.WalletReceivedHistoryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MeApi {
  // Nếu backend hỗ trợ filter theo khoảng ngày: start/end (ISO 8601, UTC)
  @GET("me/wallet/history")
  Call<WalletHistoryResponse> getHistory(
    @Query("start") String startIsoUtc,
    @Query("end") String endIsoUtc
  );
  
  // Trường hợp backend chưa hỗ trợ query (fallback)
  @GET("me/wallet/history")
  Call<WalletHistoryResponse> getHistoryAll();
  
  @GET("me/wallet/purchases")
  Call<WalletPurchasedHistoryResponse> getPurchasedHistory();
  
  @GET("me/wallet/received")
  Call<WalletReceivedHistoryResponse> getReceivedHistory();
}
