package com.example.secondchance.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.repo.AuthRepository;
import com.example.secondchance.databinding.FragmentRegisterBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.PhoneAuthProvider;

public class RegisterFragment extends Fragment {
  
  public static final String EXTRA_VERIFICATION_ID = "extra_verification_id";
  public static final String EXTRA_NAME            = "extra_name";
  public static final String EXTRA_PASSWORD        = "extra_password";
  public static final String EXTRA_NONCE           = "extra_nonce";
  
  private FragmentRegisterBinding binding;
  private final AuthRepository repo = new AuthRepository();
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = FragmentRegisterBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    binding.btnRegister.setOnClickListener(v -> onSignUp());
    
    binding.tvSignup.setOnClickListener(v -> {
      NavController nav = NavHostFragment.findNavController(RegisterFragment.this);
      nav.navigate(R.id.action_register_to_login);
    });
  }
  
  private void onSignUp() {
    String rawPhone = get(binding.edtPhone);
    String name     = get(binding.edtName);
    String pass     = get(binding.edtPassword);
    String confirm  = get(binding.edtRepassword);
    
    if (TextUtils.isEmpty(rawPhone) || TextUtils.isEmpty(name)
      || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
      toast("Please fill all fields");
      return;
    }
    if (!pass.equals(confirm)) {
      toast("Password & Confirm password are not the same");
      return;
    }
    
    // 1) Precheck -> lấy nonce từ server của bạn
    String e164 = AuthRepository.toE164(rawPhone);
    repo.precheck(e164, new AuthRepository.PrecheckCallback() {
      @Override public void onSuccess(String nonce, long expiresInSec) {
        // 2) Gửi OTP Firebase (cần Activity context)
        repo.startPhoneVerification(requireActivity(), e164,
          new AuthRepository.OtpSendCallback() {
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
              // 3) Điều hướng sang OTP Fragment, truyền data qua Bundle
              Bundle args = new Bundle();
              args.putString(EXTRA_VERIFICATION_ID, verificationId);
              args.putString(EXTRA_NAME, name);
              args.putString(EXTRA_PASSWORD, pass);
              args.putString(EXTRA_NONCE, nonce);
              
              NavController nav = NavHostFragment.findNavController(RegisterFragment.this);
              nav.navigate(R.id.action_register_to_otp, args);
            }
            
            @Override
            public void onVerificationFailed(@NonNull String message) {
              Log.e("OTP", message);
              toast("Send OTP failed: " + message);
            }
            
            @Override
            public void onInstantVerified(@NonNull String idToken) {
              // (Hiếm) Firebase xác thực tức thời -> đăng ký luôn
              repo.register(idToken, name, pass, nonce, new AuthRepository.RegisterCallback() {
                @Override public void onSuccess(@NonNull AuthApi.User user, @NonNull String jwt) {
                  // Có thể chuyển thẳng sang Main ở đây, hoặc điều hướng ra nav_main
                  toast("Register success");
                  // Ví dụ: popBackStack đến AuthActivity rồi mở MainActivity ngoài flow
                  requireActivity().finish(); // nếu AuthActivity sẽ đóng
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
    Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
  }
  
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
