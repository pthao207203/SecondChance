package com.example.secondchance.data.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import android.util.Log;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.example.secondchance.ui.auth.LoginFragment;
import com.example.secondchance.util.Prefs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import android.os.Handler;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitProvider {
  private static volatile Retrofit retrofit;
  private static volatile AuthApi authApi;
  private static volatile HomeApi homeApi;
  private static volatile MeApi  meApi;
  private static volatile OrderApi  orderApi;
  private static volatile CartApi cartApi;
  private static volatile ProductApi productApi;
  private static volatile CloudinaryApi cloudinaryApi;
  private static Context appCtx;
  private static final AtomicBoolean logoutInProgress = new AtomicBoolean(false);

  public static void init(Context applicationContext) {
    appCtx = applicationContext.getApplicationContext();
  }

  private static Retrofit ensureRetrofit() {
    if (retrofit != null) return retrofit;

    // 1) Gắn Authorization (trừ endpoint /api/auth)

    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
    log.setLevel(HttpLoggingInterceptor.Level.BODY);

    Interceptor authHeader = chain -> {
      Request orig = chain.request();
      HttpUrl url = orig.url();
      boolean isAuthEndpoint =
              url.encodedPath().startsWith("/api/auth/") || url.encodedPath().equals("/api/auth");

      Request.Builder b = orig.newBuilder();
      if (!isAuthEndpoint && orig.header("Authorization") == null && appCtx != null) {
        String token = Prefs.getToken(appCtx); // đã dạng "Bearer xxxxx"
        if (token != null && !token.isEmpty()) {
          b.addHeader("Authorization", token);
        }
      }
      return chain.proceed(b.build());
    };
    
    // 2) Nếu 401 => logout về cùng UI đăng xuất
    Interceptor authFailure = chain -> {
      okhttp3.Response res = chain.proceed(chain.request());
      if (res.code() == 401 && appCtx != null) {
        if (logoutInProgress.compareAndSet(false, true)) {
          new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            try {
              com.example.secondchance.ui.auth.LogoutRouter.forceLogout(appCtx);
            } finally {
              logoutInProgress.set(false);
            }
          });
        }
      }
      return res;
    };
    
    // 3) Log request/response (đặt sau cùng để log thấy header đã chèn)
    log.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient ok = new OkHttpClient.Builder()
            .addInterceptor(authHeader)
            .addInterceptor(authFailure)
            .addInterceptor(log)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    retrofit = new Retrofit.Builder()
      .baseUrl("http://52.195.233.219:3000/api/")
      .client(ok)
      .addConverterFactory(GsonConverterFactory.create())
      .build();

    return retrofit;
  }

  public static AuthApi auth() {
    if (authApi == null) authApi = ensureRetrofit().create(AuthApi.class);
    return authApi;
  }

  public static HomeApi home() {
    if (homeApi == null) homeApi = ensureRetrofit().create(HomeApi.class);
    return homeApi;
  }
  
  public static MeApi me() {
    if (meApi == null) meApi = ensureRetrofit().create(MeApi.class);
    return meApi;
  }
  
  public static OrderApi order() {
    if (orderApi == null) orderApi = ensureRetrofit().create(OrderApi.class);
    return orderApi;
  }

  public static CartApi cart() {
      if (cartApi == null) cartApi = ensureRetrofit().create(CartApi.class);
      return cartApi;
  }

  public static ProductApi product() {
      if (productApi == null) productApi = ensureRetrofit().create(ProductApi.class);
      return productApi;
  }
  public static CloudinaryApi cloudinary() {
    if (cloudinaryApi == null) cloudinaryApi = ensureRetrofit().create(CloudinaryApi.class);
    return cloudinaryApi;
  }
  
  
  public static Retrofit getRetrofit() {
    return ensureRetrofit();
  }
}
