package com.example.secondchance.ui.auth;

import android.content.Context;
import android.content.Intent;

import com.example.secondchance.MainActivity;
import com.example.secondchance.util.Prefs;

public final class LogoutRouter {
  private LogoutRouter() {}
  
  public static void forceLogout(Context appCtx) {
    // 1) Xoá toàn bộ state đăng nhập
    Prefs.saveToken(appCtx, "");
    AuthManager.getInstance(appCtx).clear();
    
    // 2) Reset lại task & mở MainActivity với cờ "forceLogout"
    Intent i = new Intent(appCtx, MainActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    i.putExtra("forceLogout", true);
    appCtx.startActivity(i);
  }
}
