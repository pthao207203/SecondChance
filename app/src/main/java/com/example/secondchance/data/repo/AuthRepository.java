package com.example.secondchance.data.repo;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthRepository
 * Gói toàn bộ luồng Precheck -> OTP -> Verify -> Register cho màn Đăng ký.
 * Tái sử dụng RetrofitProvider.auth() & AuthApi sẵn có.
 */
public class AuthRepository {
  
  private final AuthApi api;
  private final FirebaseAuth firebaseAuth;
  
  public AuthRepository() {
    this.api = RetrofitProvider.auth();
    this.firebaseAuth = FirebaseAuth.getInstance();
  }
  
  // ---------- 1) PRECHECK (kiểm tra SĐT & lấy nonce) ----------
  public interface PrecheckCallback {
    void onSuccess(String nonce, long expiresInSec);
    void onError(String message);
  }
  
  public void precheck(String rawPhone, PrecheckCallback cb) {
    if (TextUtils.isEmpty(rawPhone)) {
      cb.onError("Phone is empty");
      return;
    }
    AuthApi.PrecheckRequest body = new AuthApi.PrecheckRequest(rawPhone);
    api.precheck(body).enqueue(new Callback<AuthApi.PrecheckEnvelope>() {
      @Override public void onResponse(@NonNull Call<AuthApi.PrecheckEnvelope> call,
                                       @NonNull Response<AuthApi.PrecheckEnvelope> res) {
        if (!res.isSuccessful() || res.body() == null) {
          cb.onError("Precheck failed: " + res.code());
          return;
        }
        AuthApi.PrecheckEnvelope b = res.body();
        if (b.success && b.data != null && b.data.allowed) {
          cb.onSuccess(b.data.nonce, b.data.expiresInSec);
        } else {
          cb.onError("Phone already used or not allowed");
        }
      }
      @Override public void onFailure(@NonNull Call<AuthApi.PrecheckEnvelope> call, @NonNull Throwable t) {
        cb.onError("Precheck error: " + t.getMessage());
      }
    });
  }
  
  // ---------- 2) GỬI OTP BẰNG FIREBASE ----------
  public interface OtpSendCallback {
    void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token);
    void onVerificationFailed(@NonNull String message);
    void onInstantVerified(@NonNull String idToken); // trong TH very hiếm, Firebase auto verify & trả luôn idToken
  }
  
  /**
   * Gửi OTP. YÊU CẦU SĐT dạng E.164 (ví dụ +84xxxxxxxxx).
   */
  public void startPhoneVerification(Activity activity,
                                     String e164Phone,
                                     OtpSendCallback cb) {
    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
      .setPhoneNumber(e164Phone)
      .setTimeout(60L, TimeUnit.SECONDS)
      .setActivity(activity)
      .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
          // Có thể xảy ra "instant verification" (không cần nhập OTP).
          firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
              if (!task.isSuccessful()) {
                cb.onVerificationFailed("Instant verify failed");
                return;
              }
              FirebaseUser u = ((AuthResult) task.getResult()).getUser();
              if (u == null) {
                cb.onVerificationFailed("Firebase user null");
                return;
              }
              u.getIdToken(true).addOnCompleteListener(tt -> {
                if (tt.isSuccessful() && tt.getResult() != null) {
                  cb.onInstantVerified(Objects.requireNonNull(tt.getResult().getToken()));
                } else {
                  cb.onVerificationFailed("Cannot get idToken");
                }
              });
            });
        }
        
        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
          Log.e("OTP", "onVerificationFailed: " + e);
          cb.onVerificationFailed(e.getMessage() != null ? e.getMessage() : e.toString());
        }
        
        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
          cb.onCodeSent(verificationId, token);
        }
      })
      .build();
    
    PhoneAuthProvider.verifyPhoneNumber(options);
  }
  
  // ---------- 3) XÁC THỰC OTP -> LẤY idToken ----------
  public interface OtpVerifyCallback {
    void onSuccess(@NonNull String idToken);
    void onError(@NonNull String message);
  }
  
  public void verifyOtpAndGetIdToken(String verificationId, String code, OtpVerifyCallback cb) {
    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
    firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        cb.onError("OTP verification failed");
        return;
      }
      FirebaseUser user = ((AuthResult) task.getResult()).getUser();
      if (user == null) {
        cb.onError("Firebase user null");
        return;
      }
      user.getIdToken(true).addOnCompleteListener(tk -> {
        if (tk.isSuccessful() && tk.getResult() != null) {
          String idToken = tk.getResult().getToken();
          String[] parts = idToken.split("\\.");
          android.util.Log.d("OTP", "idToken parts=" + parts.length
            + ", headerPrefix=" + parts[0].substring(0, 12));
          Log.d("OTP", "idToken=" + idToken);
          cb.onSuccess(idToken);
        } else {
          cb.onError("Cannot get idToken");
        }
      });
    });
  }
  
  // ---------- 4) REGISTER (idToken + name + password + nonce) ----------
  public interface RegisterCallback {
    void onSuccess(@NonNull AuthApi.User user, @NonNull String jwt);
    void onError(@NonNull String message);
  }
  
  public void register(String idToken, String name, String password, String nonce, RegisterCallback cb) {
    AuthApi.RegisterRequest body = new AuthApi.RegisterRequest(idToken, name, password, nonce);
    api.register(body).enqueue(new Callback<AuthApi.RegisterEnvelope>() {
      @Override public void onResponse(@NonNull Call<AuthApi.RegisterEnvelope> call,
                                       @NonNull Response<AuthApi.RegisterEnvelope> res) {
        if (!res.isSuccessful() || res.body() == null) {
          cb.onError("Register failed: " + res.code());
          return;
        }
        AuthApi.RegisterEnvelope env = res.body();
        AuthApi.User user = env.getUser();
        String token = env.getToken();
        if (user != null && !TextUtils.isEmpty(token)) {
          cb.onSuccess(user, token);
        } else {
          cb.onError("Register response invalid");
        }
      }
      @Override public void onFailure(@NonNull Call<AuthApi.RegisterEnvelope> call, @NonNull Throwable t) {
        cb.onError("Register error: " + t.getMessage());
      }
    });
  }
  
  // ---------- 5) Helper ----------
  /** Chuẩn hoá số VN: 09xxxx -> +849xxxx. Nếu đã có + thì giữ nguyên. */
  public static String toE164(String phone) {
    if (phone == null) return "";
    String p = phone.replaceAll("\\s+", "");
    if (p.startsWith("+")) return p;
    if (p.startsWith("0"))  return "+84" + p.substring(1);
    return p;
  }
}
