package com.example.secondchance.data.remote;

import com.example.secondchance.dto.response.CloudinarySignatureResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CloudinaryApi {
  
  @GET("/admin/cloudinary/signature")
  Call<CloudinarySignatureResponse> getSignature(
    @Query("folder") String folder // có thể null nếu không muốn dùng folder
  );
}
