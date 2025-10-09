package com.example.secondchance.util;

import android.content.Context;

public class Prefs {
  private static final String FILE = "sc_prefs";
  private static final String KEY_TOKEN = "token";
  
  public static void saveToken(Context c, String token) {
    c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY_TOKEN, token).apply();
  }
  
  public static String getToken(Context c) {
    return c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_TOKEN, "");
  }
}
