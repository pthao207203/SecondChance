package com.example.secondchance.data.remote;

import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
  // => POST http://localhost:3000/api/auth/login
  @POST("auth/login")
  Call<LoginEnvelope> login(@Body LoginRequest body);
  
  class LoginRequest {
    @SerializedName("userPhone")
    public String userPhone;
    @SerializedName("password")
    public String password;
    
    public LoginRequest(String userPhone, String password) {
      this.userPhone = userPhone;
      this.password = password;
    }
  }
  
  // Tùy JSON backend trả về, điều chỉnh lại fields cho khớp
  class LoginEnvelope {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public Data data;
    @SerializedName("meta")
    public Object meta; // không dùng tới
    
    public static class Data {
      @SerializedName("user")
      public User user;
      @SerializedName("token")
      public Token token;
    }
  }
  
  class User {
    @SerializedName("id")
    public String id;
    @SerializedName("userName")
    public String userName;
    @SerializedName("userMail")
    public String userMail;
    @SerializedName("userRole")
    public String userRole;
    @SerializedName("userStatus")
    public String userStatus;
  }
  
  class Token {
    @SerializedName("accessToken")
    public String accessToken;
    @SerializedName("tokenType")
    public String tokenType;  // "Bearer"
    @SerializedName("expiresIn")
    public long expiresIn;  // 3600
  }
}
