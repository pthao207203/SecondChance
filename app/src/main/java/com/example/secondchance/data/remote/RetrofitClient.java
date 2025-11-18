package com.example.secondchance.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
  private static Retrofit retrofit;
  private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
  
  public static GeminiApi getGeminiApi() {
    if (retrofit == null) {
      // 1. Tạo bộ theo dõi (Interceptor)
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
      // Level.BODY giúp in ra toàn bộ nội dung gửi đi và nhận về
      logging.setLevel(HttpLoggingInterceptor.Level.BODY);
      
      // 2. Gắn nó vào OkHttpClient
      OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(logging)
        .build();
      
      // 3. Đưa Client vào Retrofit
      retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client) // <--- QUAN TRỌNG: Đừng quên dòng này!
        .build();
    }
    return retrofit.create(GeminiApi.class);
  }
}