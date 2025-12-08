package com.example.secondchance.util;

import android.util.Log;

import retrofit2.Response;

public final class LogApiError {
  
  // Ẩn constructor để không ai new class này
  private LogApiError() {}
  
  public static void log(String tag, String apiName, Response<?> response) {
    int code = response.code();
    String httpMsg = response.message();
    String errorBody = "";
    
    try {
      if (response.errorBody() != null) {
        errorBody = response.errorBody().string();
      }
    } catch (Exception e) {
      errorBody = "Không đọc được errorBody: " + e.getMessage();
    }
    
    Log.e(
      tag,
      apiName + " failed | code=" + code +
        ", httpMsg=" + httpMsg +
        ", errorBody=" + errorBody
    );
  }
  
  // Optional: dùng cho onFailure
  public static void logFailure(String tag, String apiName, Throwable t) {
    Log.e(tag, apiName + " onFailure: " + t.getMessage(), t);
  }
}
