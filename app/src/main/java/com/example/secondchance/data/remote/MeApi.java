package com.example.secondchance.data.remote;

import com.example.secondchance.data.model.ShopProfileResponse;
import com.example.secondchance.dto.response.WalletHistoryResponse;
import com.example.secondchance.dto.response.WalletPurchasedHistoryResponse;
import com.example.secondchance.dto.response.WalletReceivedHistoryResponse;
import com.example.secondchance.ui.comment.Comment;
import com.example.secondchance.data.model.UserProfileResponse;
import java.util.List;
import com.example.secondchance.data.model.dto.BecomeSellerRequest;
import com.example.secondchance.data.model.ShopProfileResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MeApi {
  @GET("me/wallet/history")
  Call<WalletHistoryResponse> getHistory(
    @Query("start") String startIsoUtc,
    @Query("end") String endIsoUtc
  );

  @GET("me/wallet/history")
  Call<WalletHistoryResponse> getHistoryAll();
  
  @GET("me/wallet/purchases")
  Call<WalletPurchasedHistoryResponse> getPurchasedHistory();
  
  @GET("me/wallet/received")
  Call<WalletReceivedHistoryResponse> getReceivedHistory();

  @GET("me/profile")
  Call<UserProfileResponse> getUserProfile();

  @GET("me")
  Call<ShopProfileResponse> getShopProfile();

  @GET("sellers/{sellerId}/rates")
  Call<GetShopCommentsResponse> getShopComments(@Path("sellerId") String sellerId);

  @POST("me/become-seller")
  Call<ShopProfileResponse> registerAsSeller(@Body BecomeSellerRequest request);

  // === RESPONSE ENVELOPE ===
  class GetShopCommentsResponse {
    public boolean success;
    public Data data;
    public List<Comment> comments;

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