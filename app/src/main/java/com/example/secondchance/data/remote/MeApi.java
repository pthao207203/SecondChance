package com.example.secondchance.data.remote;

import com.example.secondchance.dto.response.WalletHistoryResponse;
import com.example.secondchance.dto.response.WalletPurchasedHistoryResponse;
import com.example.secondchance.dto.response.WalletReceivedHistoryResponse;
import com.example.secondchance.ui.comment.Comment;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
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

  @GET("me/")
  Call<Void> getMe(); // hoặc thêm DTO nếu cần

  // === ĐÁNH GIÁ SHOP (CHỈ 1 DÒNG @GET) ===
  @GET("sellers/{sellerId}/rates")
  Call<GetShopCommentsResponse> getShopComments(@Path("sellerId") String sellerId);

  // === RESPONSE ENVELOPE ===
  class GetShopCommentsResponse {
    public boolean success;
    public Data data;
    public List<Comment> comments; // fallback nếu BE trả root

    public List<Comment> getComments() {
      if (data != null && data.comments != null) return data.comments;
      if (comments != null) return comments;
      return java.util.Collections.emptyList();
    }

    public static class Data {
      public List<Comment> comments;
      public double userRate;
      public int total;
    }
  }
}