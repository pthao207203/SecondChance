package com.example.secondchance.ui.auth;

import static com.example.secondchance.ui.auth.RegisterFragment.EXTRA_NAME;
import static com.example.secondchance.ui.auth.RegisterFragment.EXTRA_NONCE;
import static com.example.secondchance.ui.auth.RegisterFragment.EXTRA_PASSWORD;
import static com.example.secondchance.ui.auth.RegisterFragment.EXTRA_VERIFICATION_ID;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.secondchance.MainActivity;
import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.repo.AuthRepository;
import com.example.secondchance.databinding.FragmentOtpBinding;
import com.google.android.material.button.MaterialButton;

public class OtpFragment extends Fragment {
  
  private FragmentOtpBinding binding;
  private EditText[] cells = new EditText[6];
  private MaterialButton btnConfirm;
  
  private String verificationId, name, password, nonce;
  
  private final AuthRepository repo = new AuthRepository();
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    binding = FragmentOtpBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // Lấy args từ RegisterFragment
    Bundle args = getArguments();
    if (args != null) {
      verificationId = args.getString(EXTRA_VERIFICATION_ID, "");
      name           = args.getString(EXTRA_NAME, "");
      password       = args.getString(EXTRA_PASSWORD, "");
      nonce          = args.getString(EXTRA_NONCE, "");
    }
    
    // Map view
    cells[0] = binding.edtOtp1;
    cells[1] = binding.edtOtp2;
    cells[2] = binding.edtOtp3;
    cells[3] = binding.edtOtp4;
    cells[4] = binding.edtOtp5;
    cells[5] = binding.edtOtp6;
    btnConfirm = binding.btnConfirmOtp;
    
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
    // Focus ô đầu khi mở màn
    cells[0].requestFocus();
  }
  
  private String getOtpCode() {
    StringBuilder sb = new StringBuilder();
    for (EditText e : cells) {
      CharSequence c = e.getText();
      sb.append(c == null ? "" : c.toString());
    }
    return sb.toString();
  }
  
  private void verifyAndRegister(String code) {
    // 1) Verify OTP -> idToken (Firebase)
    repo.verifyOtpAndGetIdToken(verificationId, code, new AuthRepository.OtpVerifyCallback() {
      @Override public void onSuccess(@NonNull String idToken) {
        // 2) Register lên server của bạn
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
    // Tuỳ kiến trúc: có thể pop toàn bộ backstack auth & mở MainActivity
    Intent i = new Intent(requireContext(), MainActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
    requireActivity().finishAffinity();
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
