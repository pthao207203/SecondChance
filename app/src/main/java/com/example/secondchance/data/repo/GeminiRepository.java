package com.example.secondchance.data.repo;

import com.example.secondchance.data.remote.GeminiApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiRepository {
  private static GeminiApi instance;
  
  public static GeminiApi getInstance() {
    if (instance == null) {
      Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();
      
      instance = retrofit.create(GeminiApi.class);
    }
    return instance;
  }
}
