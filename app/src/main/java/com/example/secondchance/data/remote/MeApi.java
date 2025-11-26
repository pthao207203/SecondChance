package com.example.secondchance.data.remote;

import com.example.secondchance.data.model.ShopProfileResponse;
import com.example.secondchance.dto.request.AddressRequest;
import com.example.secondchance.dto.request.BankListRequest;
import com.example.secondchance.dto.request.BankRequest;
import com.example.secondchance.dto.response.AddressItemResponse;
import com.example.secondchance.dto.response.AddressListResponse;
import com.example.secondchance.dto.response.BankItemResponse;
import com.example.secondchance.dto.response.BankListResponse;
import com.example.secondchance.dto.response.WalletHistoryResponse;
import com.example.secondchance.dto.response.WalletPurchasedHistoryResponse;
import com.example.secondchance.dto.response.WalletReceivedHistoryResponse;
import com.example.secondchance.ui.comment.Comment;
import com.example.secondchance.data.model.UserProfileResponse;
import java.util.List;
import com.example.secondchance.data.model.dto.BecomeSellerRequest;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MeApi {
  @GET("/api/me/wallet/history")
  Call<WalletHistoryResponse> getHistory(
    @Query("start") String startIsoUtc,
    @Query("end") String endIsoUtc
  );

  @GET("/api/me/wallet/history")
  Call<WalletHistoryResponse> getHistoryAll();
  
  @GET("/api/me/wallet/purchases")
  Call<WalletPurchasedHistoryResponse> getPurchasedHistory();
  
  @GET("/api/me/wallet/received")
  Call<WalletReceivedHistoryResponse> getReceivedHistory();

  @GET("/api/me/profile")
  Call<UserProfileResponse> getUserProfile();

  @GET("/api/me")
  Call<ShopProfileResponse> getShopProfile();

  @GET("/api/sellers/{sellerId}/rates")
  Call<GetShopCommentsResponse> getShopComments(@Path("sellerId") String sellerId);

  @POST("/api/me/become-seller")
  Call<ShopProfileResponse> registerAsSeller(@Body BecomeSellerRequest request);

  // --- BANK APIs ---
  @GET("/api/me/banks")
  Call<BankListResponse> getBanks();

  @POST("/api/me/banks")
  Call<BankItemResponse> addBank(@Body BankRequest request);

  @GET("/api/me/banks/{bankId}")
  Call<BankItemResponse> getBankDetail(@Path("bankId") String bankId);

  @PUT("/api/me/banks/{bankId}")
  Call<BankItemResponse> updateBank(@Path("bankId") String bankId, @Body BankRequest request);

  @DELETE("/api/me/banks/{bankId}")
  Call<BankListResponse> deleteBank(@Path("bankId") String bankId);

  // --- ADDRESS APIs ---
  @GET("/api/me/addresses")
  Call<AddressListResponse> getAddresses();

  @POST("/api/me/addresses")
  Call<AddressItemResponse> addAddress(@Body AddressRequest request);

  @GET("/api/me/addresses/{addressId}")
  Call<AddressItemResponse> getAddressDetail(@Path("addressId") String addressId);

  @PUT("/api/me/addresses/{addressId}")
  Call<AddressItemResponse> updateAddress(@Path("addressId") String addressId, @Body AddressRequest request);

  @DELETE("/api/me/addresses/{addressId}")
  Call<AddressListResponse> deleteAddress(@Path("addressId") String addressId);

  // -----------------

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
