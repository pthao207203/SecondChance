package com.example.secondchance.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
  private static final String FILE = "sc_prefs";
  private static final String KEY_TOKEN = "token";
  private static final String KEY_EXPIRE_AT = "token_expire_at";
  private static final String KEY_LOGIN_TYPE = "login_type";

  public static final String TYPE_NORMAL = "normal";
  public static final String TYPE_GOOGLE = "google";
  
  public static void saveToken(Context c, String token) {
    long expireAt = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000;
    SharedPreferences.Editor e = c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit();
    e.putString(KEY_TOKEN, token);
    e.putLong(KEY_EXPIRE_AT, expireAt);
    e.apply();
  }

  public static void saveLoginType(Context c, String type) {
    c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString(KEY_LOGIN_TYPE, type)
            .apply();
  }

  public static String getLoginType(Context c) {
    return c.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_LOGIN_TYPE, TYPE_NORMAL);
  }
  
  public static String getToken(Context c) {
    SharedPreferences sp = c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    String token = sp.getString(KEY_TOKEN, "");
    long expireAt = sp.getLong(KEY_EXPIRE_AT, 0);
    
    // Nếu chưa có token hoặc hết hạn -> xoá
    if (token == null || token.isEmpty() || System.currentTimeMillis() > expireAt) {
      clearToken(c);
      return "";
    }
    return token;
  }
  
  public static void clearToken(Context c) {
    c.getSharedPreferences(FILE, Context.MODE_PRIVATE)
      .edit()
      .remove(KEY_TOKEN)
      .remove(KEY_LOGIN_TYPE)
      .apply();
  }
}
