package com.example.secondchance.data.remote;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
  @POST("/api/auth/login")
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

  class LoginEnvelope {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public Data data;
    @SerializedName("meta")
    public Object meta;
    
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
    public String tokenType;
    @SerializedName("expiresIn")
    public long expiresIn;
  }
  
  @POST("/api/auth/precheck")
  Call<PrecheckEnvelope> precheck(@Body PrecheckRequest body);
  
  class PrecheckRequest {
    @SerializedName("phone")   public String phone;
    @SerializedName("purpose") public String purpose = "register";
    public PrecheckRequest(String phone) { this.phone = phone; }
  }
  
  class PrecheckEnvelope {
    @SerializedName("success") public boolean success;
    @SerializedName("data")    public Data data;
    @SerializedName("meta")    public Object meta;
    public static class Data {
      @SerializedName("allowed")      public boolean allowed;
      @SerializedName("nonce")        public String nonce;
      @SerializedName("expiresInSec") public long   expiresInSec;
    }
  }
  
  // ====== REGISTER (mới) ======
  @POST("/api/auth/register")
  Call<RegisterEnvelope> register(@Body RegisterRequest body);
  
  class RegisterRequest {
    @SerializedName("idToken")  public String idToken;
    @SerializedName("name")     public String name;
    @SerializedName("password") public String password;
    @SerializedName("nonce")    public String nonce;
    public RegisterRequest(String idToken, String name, String password, String nonce) {
      this.idToken = idToken; this.name = name; this.password = password; this.nonce = nonce;
    }
  }

  class RegisterEnvelope {
    @SerializedName("success") public Boolean success;
    @SerializedName("data")    public Data data;
    @SerializedName("user")    public User user;
    @SerializedName("token")   public String token;
    @SerializedName("meta")    public Object meta;
    
    public static class Data {
      @SerializedName("user")  public User user;
      @SerializedName("token") public JsonElement token;

      public String getTokenString() {
        try {
          if (token == null) return null;
          if (token.isJsonPrimitive()) {
            return token.getAsString();
          }
          if (token.isJsonObject()) {
            JsonObject o = token.getAsJsonObject();
            if (o.has("accessToken") && !o.get("accessToken").isJsonNull()) {
              return o.get("accessToken").getAsString();
            }

            if (o.has("token") && !o.get("token").isJsonNull()) {
              return o.get("token").getAsString();
            }
          }
        } catch (Exception ignored) {}
        return null;
      }
    }

    public User getUser()  { return data != null ? data.user  : user; }
    public String getToken(){ return data != null ? data.getTokenString() : token; }
  }
}
