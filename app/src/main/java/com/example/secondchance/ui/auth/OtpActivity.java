package com.example.secondchance.ui.auth;

import static com.example.secondchance.ui.auth.RegisterActivity.EXTRA_NAME;
import static com.example.secondchance.ui.auth.RegisterActivity.EXTRA_NONCE;
import static com.example.secondchance.ui.auth.RegisterActivity.EXTRA_PASSWORD;
import static com.example.secondchance.ui.auth.RegisterActivity.EXTRA_VERIFICATION_ID;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.secondchance.MainActivity;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.repo.AuthRepository;
import com.google.android.material.button.MaterialButton;

public class OtpActivity extends AppCompatActivity {
  
  private EditText[] cells = new EditText[6];
  private MaterialButton btnConfirm;
  
  private String verificationId, name, password, nonce;
  
  private final AuthRepository repo = new AuthRepository();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_otp);
    
    verificationId = getIntent().getStringExtra(EXTRA_VERIFICATION_ID);
    name           = getIntent().getStringExtra(EXTRA_NAME);
    password       = getIntent().getStringExtra(EXTRA_PASSWORD);
    nonce          = getIntent().getStringExtra(EXTRA_NONCE);
    
    cells[0] = findViewById(R.id.edtOtp1);
    cells[1] = findViewById(R.id.edtOtp2);
    cells[2] = findViewById(R.id.edtOtp3);
    cells[3] = findViewById(R.id.edtOtp4);
    cells[4] = findViewById(R.id.edtOtp5);
    cells[5] = findViewById(R.id.edtOtp6);
    btnConfirm = findViewById(R.id.btnConfirmOtp);
    
    setupOtpInputs();
    
    btnConfirm.setOnClickListener(v -> {
      String code = getOtpCode();
      if (code.length() != 6) {
        toast("Please enter 6 digits");
        return;
      }
      verifyAndRegister(code);
    });
  }
  
  private void setupOtpInputs() {
    for (int i = 0; i < 6; i++) {
      final int idx = i;
      cells[i].addTextChangedListener(new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
          if (s.length() == 1 && idx < 5) cells[idx + 1].requestFocus();
          else if (s.length() == 0 && idx > 0) cells[idx - 1].requestFocus();
        }
      });
    }
  }
  
  private String getOtpCode() {
    StringBuilder sb = new StringBuilder();
    for (EditText e : cells) {
      sb.append(e.getText() == null ? "" : e.getText().toString());
    }
    return sb.toString();
  }
  
  private void verifyAndRegister(String code) {
    // 1) Verify OTP -> idToken
    repo.verifyOtpAndGetIdToken(verificationId, code, new AuthRepository.OtpVerifyCallback() {
      @Override public void onSuccess(@NonNull String idToken) {
        // 2) Register lên server
        repo.register(idToken, name, password, nonce, new AuthRepository.RegisterCallback() {
          @Override public void onSuccess(@NonNull AuthApi.User user, @NonNull String jwt) {
            goToMain();
          }
          
          @Override public void onError(@NonNull String message) {
            toast("Register error: " + message);
            Log.e("OTP", message);
          }
        });
      }
      
      @Override public void onError(@NonNull String message) {
        toast(message);
      }
    });
  }
  
  private void goToMain() {
    Intent i = new Intent(this, MainActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
    finishAffinity();
  }
  
  private void toast(String s) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
  }
}
