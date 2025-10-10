package com.example.secondchance.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secondchance.MainActivity;
import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.ActivityLoginBinding;
import com.example.secondchance.util.Prefs;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
  
  private ActivityLoginBinding b;
  private static final Pattern VN_PHONE = Pattern.compile("^(0|84)(\\d){9}$|^(\\d){10}$");
  
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    if (!Prefs.getToken(this).isEmpty()) {
      startActivity(new Intent(this, MainActivity.class));
      finish();
      return;
    }
    
    b = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    
    b.btnLogin.setOnClickListener(v -> tryLogin());
    b.tvForgot.setOnClickListener(v ->
      Toast.makeText(this, "Đi tới màn hình quên mật khẩu…", Toast.LENGTH_SHORT).show());
    b.tvSignup.setOnClickListener(v ->
      Toast.makeText(this, "Đi tới màn hình đăng ký…", Toast.LENGTH_SHORT).show());
  }
  
  private void tryLogin() {
    String phone = safe(b.edtPhone.getText());
    String pwd = safe(b.edtPassword.getText());
    
    if (!isValidPhone(phone)) {
      b.tilPhone.setError("SĐT không hợp lệ");
      return;
    } else {
      b.tilPhone.setError(null);
    }
    
    if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
      b.tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
      return;
    } else {
      b.tilPassword.setError(null);
    }
    
    doLogin(phone, pwd);
  }
  
  private void doLogin(String phone, String pwd) {
    setLoading(true);
    
    AuthApi api = RetrofitProvider.auth();
    api.login(new AuthApi.LoginRequest(phone, pwd))
      .enqueue(new Callback<AuthApi.LoginEnvelope>() {
        @Override
        public void onResponse(Call<AuthApi.LoginEnvelope> call,
                               Response<AuthApi.LoginEnvelope> res) {
          setLoading(false);
          if (res.isSuccessful() && res.body() != null && res.body().success
            && res.body().data != null && res.body().data.token != null) {
            
            AuthApi.Token tk = res.body().data.token;
            String bearer = (tk.tokenType != null ? tk.tokenType : "Bearer")
              + " " + tk.accessToken;
            
            Prefs.saveToken(LoginActivity.this, bearer); // "Bearer xxxxx"
            
            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
          } else {
            Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
          }
        }
        
        @Override
        public void onFailure(Call<AuthApi.LoginEnvelope> call, Throwable t) {
          setLoading(false);
          Toast.makeText(LoginActivity.this, "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show();
        }
      });
  }
  
  private void setLoading(boolean loading) {
    b.btnLogin.setEnabled(!loading);
    b.btnLogin.setText(loading ? "Đang đăng nhập…" : "Đăng nhập");
  }
  
  private boolean isValidPhone(String s) {
    if (TextUtils.isEmpty(s)) return false;
    String digits = s.replaceAll("[^\\d]", "");
    return VN_PHONE.matcher(digits).matches();
  }
  
  private static String safe(CharSequence cs) {
    return cs == null ? "" : cs.toString().trim();
  }
}
