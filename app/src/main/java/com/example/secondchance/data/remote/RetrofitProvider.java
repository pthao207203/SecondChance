package com.example.secondchance.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitProvider {
  private static AuthApi authApi;
  
  public static AuthApi auth() {
    if (authApi == null) {
      HttpLoggingInterceptor log = new HttpLoggingInterceptor();
      log.setLevel(HttpLoggingInterceptor.Level.BODY);
      
      OkHttpClient ok = new OkHttpClient.Builder()
        .addInterceptor(log)
        .build();
      
      Retrofit r = new Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/api/") // 👈 base URL
        .client(ok)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
      
      authApi = r.create(AuthApi.class);
    }
    return authApi;
  }
}
