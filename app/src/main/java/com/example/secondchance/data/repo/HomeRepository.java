package com.example.secondchance.data.repo;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.remote.RetrofitProvider;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeRepository {
  
  private final HomeApi api = RetrofitProvider.home();
  
  public interface HomeCallback {
    void onSuccess(HomeApi.HomeEnvelope.Data data);
    void onError(String message);
  }
  
  public void fetchHome(HomeCallback cb) {
    api.getHome().enqueue(new Callback<HomeApi.HomeEnvelope>() {
      @Override public void onResponse(Call<HomeApi.HomeEnvelope> call, Response<HomeApi.HomeEnvelope> res) {
        if (res.isSuccessful() && res.body()!=null && res.body().success && res.body().data!=null) {
          cb.onSuccess(res.body().data);
        } else cb.onError("HTTP " + res.code());
      }
      @Override public void onFailure(Call<HomeApi.HomeEnvelope> call, Throwable t) { cb.onError(t.getMessage()); }
    });
  }
}
