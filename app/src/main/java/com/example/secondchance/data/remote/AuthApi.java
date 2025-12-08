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

  @POST("auth/precheck")
  Call<PrecheckEnvelope> precheck(@Body PrecheckRequest body);

  @POST("/api/auth/register")
  Call<RegisterEnvelope> register(@Body RegisterRequest body);

  @POST("auth/google")
  Call<GoogleLoginEnvelope> loginGoogle(@Body LoginGoogleRequest body);

  @POST("auth/google/complete")
  Call<LoginEnvelope> completeLoginGoogle(@Body CompleteSocialRequest body);


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

  class PrecheckRequest {
    @SerializedName("phone")
    public String phone;
    @SerializedName("purpose")
    public String purpose = "register";

    public PrecheckRequest(String phone) {
      this.phone = phone;
    }
  }

  class RegisterRequest {
    @SerializedName("idToken")
    public String idToken;
    @SerializedName("name")
    public String name;
    @SerializedName("password")
    public String password;
    @SerializedName("nonce")
    public String nonce;

    public RegisterRequest(String idToken, String name, String password, String nonce) {
      this.idToken = idToken; this.name = name; this.password = password; this.nonce = nonce;
    }
  }

  class LoginGoogleRequest {
    @SerializedName("idToken")
    public String idToken;

    public LoginGoogleRequest(String idToken) {
      this.idToken = idToken;
    }
  }

  class CompleteSocialRequest {
    @SerializedName("socialNonce")
    public String socialNonce;
    @SerializedName("phone")
    public String phone;

    public CompleteSocialRequest(String socialNonce, String phone) {
      this.socialNonce = socialNonce;
      this.phone = phone;
    }
  }

  class User {
    @SerializedName("id")
    public String id;
    @SerializedName("userName")
    public String userName;
    @SerializedName("userMail")
    public String userMail;
    @SerializedName("userPhone")
    public String userPhone;
    @SerializedName("userRole")
    public String userRole;
    @SerializedName("userStatus")
    public String userStatus;
  }

  public static class Profile {
    @SerializedName("id")
    public String id;
    @SerializedName("email")
    public String email;
    @SerializedName("name")
    public String name;
  }

  /**
   * Error model
   */
  public static class ErrorDetail {
    @SerializedName("code")
    public String code;
    @SerializedName("message")
    public String message;
  }

  class LoginEnvelope {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public Data data;
    @SerializedName("meta")
    public Object meta;
    @SerializedName("error")
    public ErrorDetail error;

    public static class Data {
      @SerializedName("user")
      public User user;
      @SerializedName("token")
      public JsonElement token;

      @SerializedName("socialNonce")
      public String socialNonce;
      @SerializedName("profile")
      public Profile profile;
      @SerializedName("missing")
      public String[] missing;

      public String getBearerToken() {
        if (token == null) return null;

        if (token.isJsonPrimitive()) {
          return "Bearer " + token.getAsString();
        }

        if (token.isJsonObject()) {
          JsonObject obj = token.getAsJsonObject();
          String type = "Bearer";

          if (obj.has("tokenType") && !obj.get("tokenType").isJsonNull()) {
            type = obj.get("tokenType").getAsString();
          }

          if (obj.has("accessToken") && !obj.get("accessToken").isJsonNull()) {
            return type + " " + obj.get("accessToken").getAsString();
          }
        }
        return null;
      }

      public String getTokenString() {
        String bearer = getBearerToken();
        if (bearer != null && bearer.startsWith("Bearer ")) {
          return bearer.substring(7);
        }
        return null;
      }
    }

    public User getUser() { return data != null ? data.user : null; }
    public String getBearerToken() { return data != null ? data.getBearerToken() : null; }
  }

  class PrecheckEnvelope {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public Data data;
    @SerializedName("meta")
    public Object meta;

    public static class Data {
      @SerializedName("allowed")
      public boolean allowed;
      @SerializedName("nonce")
      public String nonce;
      @SerializedName("expiresInSec")
      public long expiresInSec;
    }
  }

  class RegisterEnvelope {
    @SerializedName("success") public Boolean success;
    @SerializedName("data")    public Data data;
    @SerializedName("user")    public User user;
    @SerializedName("token")   public String token;
    @SerializedName("meta")    public Object meta;
    @SerializedName("error")   public ErrorDetail error;

    public static class Data {
      @SerializedName("user")
      public User user;
      @SerializedName("token")
      public JsonElement token;

      public String getBearerToken() {
        if (token == null) return null;

        if (token.isJsonPrimitive()) {
          return "Bearer " + token.getAsString();
        }

        if (token.isJsonObject()) {
          JsonObject o = token.getAsJsonObject();

          if (o.has("accessToken") && !o.get("accessToken").isJsonNull()) {
            return "Bearer " + o.get("accessToken").getAsString();
          }

          if (o.has("token") && !o.get("token").isJsonNull()) {
            return "Bearer " + o.get("token").getAsString();
          }
        }
        return null;
      }

      public String getTokenString() {
        String bearer = getBearerToken();
        if (bearer != null && bearer.startsWith("Bearer ")) {
          return bearer.substring(7);
        }
        return null;
      }
    }

    public User getUser() { return data != null ? data.user : null; }
    public String getBearerToken() { return data != null ? data.getBearerToken() : null; }
  }

  class GoogleLoginEnvelope {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public Data data;
    @SerializedName("error")
    public ErrorDetail error;

    public static class Data {

      @SerializedName("user")
      public User user;
      @SerializedName("token")
      public JsonElement token;

      @SerializedName("profile")
      public Profile profile;
      @SerializedName("missing")
      public String[] missing;
      @SerializedName("socialNonce")
      public String socialNonce;

      public String getBearerToken() {
        if (token == null) return null;

        if (token.isJsonPrimitive()) {
          return "Bearer " + token.getAsString();
        }

        if (token.isJsonObject()) {
          JsonObject obj = token.getAsJsonObject();
          String type = "Bearer";

          if (obj.has("tokenType") && !obj.get("tokenType").isJsonNull()) {
            type = obj.get("tokenType").getAsString();
          }

          if (obj.has("accessToken") && !obj.get("accessToken").isJsonNull()) {
            return type + " " + obj.get("accessToken").getAsString();
          }

          if (obj.has("token") && !obj.get("token").isJsonNull()) {
            return type + " " + obj.get("token").getAsString();
          }
        }
        return null;
      }
    }
  }
}
