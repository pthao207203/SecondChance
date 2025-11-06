package com.example.secondchance.data.remote;

import android.content.Context;

import com.example.secondchance.util.Prefs;

import java.util.concurrent.TimeUnit;

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
  //private static Retrofit retrofit;
  private static Context appCtx; // Application context

  public static void init(Context applicationContext) {
    appCtx = applicationContext.getApplicationContext();
  }

  private static Retrofit ensureRetrofit() {
    if (retrofit != null) return retrofit;

    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
    log.setLevel(HttpLoggingInterceptor.Level.BODY);

    Interceptor authHeader = chain -> {
      Request orig = chain.request();
      HttpUrl url = orig.url();
      boolean isAuthEndpoint =
              url.encodedPath().startsWith("/api/auth/") || url.encodedPath().equals("/api/auth");

      Request.Builder b = orig.newBuilder();
      if (!isAuthEndpoint && orig.header("Authorization") == null && appCtx != null) {
        String token = Prefs.getToken(appCtx); // đã là "Bearer xxxxx"
        if (token != null && !token.isEmpty()) b.addHeader("Authorization", token);
      }
      return chain.proceed(b.build());
    };

    OkHttpClient ok = new OkHttpClient.Builder()
            .addInterceptor(log)
            .addInterceptor(authHeader)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")   // Emulator ↔ server local
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
  public static Retrofit getRetrofit() {
    if (retrofit == null) {
      createRetrofit();
    }
    return retrofit;
  }

  private static void createRetrofit() {
    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
    log.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient ok = new OkHttpClient.Builder()
            .addInterceptor(log)
            .build();

    retrofit = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/api/")
            .client(ok)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
  }
}

