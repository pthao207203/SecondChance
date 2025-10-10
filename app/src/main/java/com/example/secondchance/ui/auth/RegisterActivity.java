package com.example.secondchance.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secondchance.MainActivity;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.repo.AuthRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.PhoneAuthProvider;

public class RegisterActivity extends AppCompatActivity {
  
  public static final String EXTRA_VERIFICATION_ID = "extra_verification_id";
  public static final String EXTRA_NAME            = "extra_name";
  public static final String EXTRA_PASSWORD        = "extra_password";
  public static final String EXTRA_NONCE           = "extra_nonce";
  
  private TextInputEditText edtPhone, edtName, edtPassword, edtConfirm;
  private MaterialButton btnSignUp;
  
  private final AuthRepository repo = new AuthRepository();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    
    edtPhone    = findViewById(R.id.edtPhone);
    edtName     = findViewById(R.id.edtAccountName);
    edtPassword = findViewById(R.id.edtPassword);
    edtConfirm  = findViewById(R.id.edtPasswordConfirm);
    btnSignUp   = findViewById(R.id.btnRegister);
    
    btnSignUp.setOnClickListener(v -> onSignUp());
  }
  
  private void onSignUp() {
    String rawPhone = get(edtPhone);
    String name     = get(edtName);
    String pass     = get(edtPassword);
    String confirm  = get(edtConfirm);
    
    if (TextUtils.isEmpty(rawPhone) || TextUtils.isEmpty(name)
      || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
      toast("Please fill all fields");
      return;
    }
    if (!pass.equals(confirm)) {
      toast("Password & Confirm password are not the same");
      return;
    }
    
    // 1) Precheck -> lấy nonce
    String e164 = AuthRepository.toE164(rawPhone);
    repo.precheck(e164, new AuthRepository.PrecheckCallback() {
      @Override public void onSuccess(String nonce, long expiresInSec) {
        // 2) Gửi OTP Firebase
        repo.startPhoneVerification(RegisterActivity.this, e164,
          new AuthRepository.OtpSendCallback() {
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
              // 3) Mở màn hình OTP
              Intent i = new Intent(RegisterActivity.this, OtpActivity.class);
              i.putExtra(EXTRA_VERIFICATION_ID, verificationId);
              i.putExtra(EXTRA_NAME, name);
              i.putExtra(EXTRA_PASSWORD, pass);
              i.putExtra(EXTRA_NONCE, nonce);
              startActivity(i);
            }
            
            @Override
            public void onVerificationFailed(@NonNull String message) {
              Log.e("OTP", message);
              toast("Send OTP failed: " + message);
            }
            
            @Override
            public void onInstantVerified(@NonNull String idToken) {
              // (Hiếm) Firebase xác thực tức thời, đăng ký luôn rồi vào main
              repo.register(idToken, name, pass, nonce, new AuthRepository.RegisterCallback() {
                @Override public void onSuccess(@NonNull AuthApi.User user, @NonNull String jwt) {
                  goToMain();
                }
                @Override public void onError(@NonNull String message) {
                  toast("Register error: " + message);
                  Log.e("OTP", message);
                }
              });
            }
          });
      }
      
      @Override public void onError(String message) {
        toast(message);
      }
    });
  }
  
  private String get(TextInputEditText e) {
    return e.getText() == null ? "" : e.getText().toString().trim();
  }
  
  private void toast(String s) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
  }
  
  private void goToMain() {
    Intent i = new Intent(this, MainActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
    finishAffinity();
  }
}
