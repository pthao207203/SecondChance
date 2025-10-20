package com.example.secondchance.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.MainActivity;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentLoginBinding;
import com.example.secondchance.util.Prefs;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
  
  private FragmentLoginBinding b;
  private NavController nav;
  private static final Pattern VN_PHONE =
    Pattern.compile("^(0|84)(\\d){9}$|^(\\d){10}$");
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    b = FragmentLoginBinding.inflate(inflater, container, false);
    return b.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view,
                            @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    nav = NavHostFragment.findNavController(this);
    
    // Nếu đã có token thì vào Main luôn (bật lại nếu bạn muốn)
    // if (!Prefs.getToken(requireContext()).isEmpty()) {
    //     goToMainAndFinish();
    //     return;
    // }
    
    b.btnLogin.setOnClickListener(v -> tryLogin());
    
    b.tvForgot.setOnClickListener(v ->
      Toast.makeText(requireContext(),
        "Đi tới màn hình quên mật khẩu…",
        Toast.LENGTH_SHORT).show());
    
    // Điều hướng sang màn Register (action trong nav_auth.xml)
    b.tvSignup.setOnClickListener(v ->
      nav.navigate(R.id.action_login_to_register));
  }
  
  private void tryLogin() {
    String phone = safe(b.edtPhone.getText());
    String pwd   = safe(b.edtPassword.getText());
    
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
          if (!isAdded()) return; // Fragment đã detach
          setLoading(false);
          
          if (res.isSuccessful()
            && res.body() != null
            && res.body().success
            && res.body().data != null
            && res.body().data.token != null) {
            
            AuthApi.Token tk = res.body().data.token;
            String bearer = (tk.tokenType != null ? tk.tokenType : "Bearer")
              + " " + tk.accessToken;
            
            Prefs.saveToken(requireContext(), bearer); // "Bearer xxxxx"
            
            Toast.makeText(requireContext(),
              "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            goToMainAndFinish();
          } else {
            Toast.makeText(requireContext(),
              "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
          }
        }
        
        @Override
        public void onFailure(Call<AuthApi.LoginEnvelope> call, Throwable t) {
          if (!isAdded()) return;
          setLoading(false);
          Toast.makeText(requireContext(),
            "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show();
        }
      });
  }
  
  private void setLoading(boolean loading) {
    b.btnLogin.setEnabled(!loading);
    b.btnLogin.setText(loading ? "Đang đăng nhập…" : "Đăng nhập");
    // có thể thêm progressBar nếu bạn có trong layout
  }
  
  private boolean isValidPhone(String s) {
    if (TextUtils.isEmpty(s)) return false;
    String digits = s.replaceAll("[^\\d]", "");
    return VN_PHONE.matcher(digits).matches();
  }
  
  private static String safe(CharSequence cs) {
    return cs == null ? "" : cs.toString().trim();
  }
  
  private void goToMainAndFinish() {
    Intent i = new Intent(requireContext(), MainActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(i);
    requireActivity().finish();
  }
  
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    b = null;
  }
}
