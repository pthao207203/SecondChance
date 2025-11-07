package com.example.secondchance.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class AuthManager {
  private static final String PREF_NAME = "auth_prefs";
  private static final String KEY_TOKEN = "auth_token";
  private static final String KEY_USER_ID = "user_id";
  
  private static AuthManager instance;
  private final SharedPreferences prefs;
  
  private AuthManager(Context ctx) {
    this.prefs = ctx.getApplicationContext()
      .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }
  
  public static synchronized AuthManager getInstance(Context ctx) {
    if (instance == null) instance = new AuthManager(ctx);
    return instance;
  }
  
  /** Lưu token + userId sau khi login thành công */
  public void saveAuth(String token) {
    prefs.edit()
      .putString(KEY_TOKEN, token)
      .apply();
  }
  
  /** Xoá phiên đăng nhập (logout) */
  public void clear() {
    prefs.edit().clear().apply();
  }
  
  /** Kiểm tra đã đăng nhập chưa (cứ có token là true) */
  public boolean isLoggedIn() {
    String token = prefs.getString(KEY_TOKEN, null);
    return !TextUtils.isEmpty(token);
  }
  
  public String getToken() {
    return prefs.getString(KEY_TOKEN, null);
  }
  
  public String getUserId() {
    return prefs.getString(KEY_USER_ID, null);
  }
}
