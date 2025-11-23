package com.example.secondchance.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.AuthApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.data.repo.AuthRepository;
import com.example.secondchance.databinding.FragmentRegisterBinding;
import com.example.secondchance.util.Prefs;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterFragment extends Fragment {

  private static final String TAG = "RegisterFragment";
  public static final String EXTRA_VERIFICATION_ID = "extra_verification_id";
  public static final String EXTRA_NAME = "extra_name";
  public static final String EXTRA_PASSWORD = "extra_password";
  public static final String EXTRA_NONCE = "extra_nonce";

  private static final int RC_SIGN_IN = 9001;

  private FragmentRegisterBinding binding;
  private final AuthRepository repo = new AuthRepository();

  private GoogleSignInClient mGoogleSignInClient;
  private FirebaseAuth mAuth;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
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

    setupGoogleSignIn();
    binding.btnGoogleRegister.setOnClickListener(v -> signInWithGoogle());
  }

  private void setupGoogleSignIn() {
    try {
      String serverClientId = getString(R.string.server_client_id);

      GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestIdToken(serverClientId)
              .requestEmail()
              .requestProfile()
              .build();

      mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
      mAuth = FirebaseAuth.getInstance();

      Log.d(TAG, "Google Sign-In configured");
    } catch (Exception e) {
      Log.e(TAG, "Failed to setup Google Sign-In", e);
      toast("Lỗi cấu hình Google Sign-In");
    }
  }

  private void signInWithGoogle() {
    if (mGoogleSignInClient == null) {
      toast("Google Sign-In chưa được cấu hình");
      return;
    }

    mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
      Intent signInIntent = mGoogleSignInClient.getSignInIntent();
      startActivityForResult(signInIntent, RC_SIGN_IN);
      Log.d(TAG, "Launched Google Sign-In intent");
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      handleSignInResult(task);
    }
  }

  private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
    try {
      GoogleSignInAccount account = completedTask.getResult(ApiException.class);

      if (account == null || account.getIdToken() == null) {
        toast("Lỗi lấy thông tin tài khoản Google");
        Log.e(TAG, "Account or idToken is null");
        return;
      }

      Log.d(TAG, "Google Sign-In success: " + account.getEmail());
      firebaseAuthWithGoogle(account.getIdToken());

    } catch (ApiException e) {
      Log.w(TAG, "Google sign in failed with code: " + e.getStatusCode(), e);

      String errorMsg;
      switch (e.getStatusCode()) {
        case 10:
          errorMsg = "Lỗi cấu hình (SHA-1 không khớp)";
          break;
        case 12500:
          errorMsg = "Đăng nhập thất bại, vui lòng thử lại";
          break;
        case 7:
          errorMsg = "Lỗi kết nối mạng";
          break;
        default:
          errorMsg = "Đăng nhập Google thất bại (code: " + e.getStatusCode() + ")";
      }

      toast(errorMsg);
    }
  }

  private void firebaseAuthWithGoogle(String idToken) {
    setLoading(true);

    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
    mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity(), task -> {
              if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                  user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                    if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                      String firebaseIdToken = tokenTask.getResult().getToken();
                      Log.d(TAG, "Got Firebase ID token, sending to backend");
                      sendGoogleLoginToBackend(firebaseIdToken);
                    } else {
                      setLoading(false);
                      Log.e(TAG, "Failed to get Firebase token", tokenTask.getException());
                      toast("Không lấy được token từ Firebase");
                    }
                  });
                } else {
                  setLoading(false);
                  toast("Firebase user is null");
                }
              } else {
                setLoading(false);
                Log.e(TAG, "Firebase auth failed", task.getException());
                toast("Xác thực Firebase thất bại");
              }
            });
  }
  private void sendGoogleLoginToBackend(String idToken) {
    AuthApi api = RetrofitProvider.auth();
    api.loginGoogle(new AuthApi.LoginGoogleRequest(idToken))
            .enqueue(new Callback<AuthApi.GoogleLoginEnvelope>() {
              @Override
              public void onResponse(@NonNull Call<AuthApi.GoogleLoginEnvelope> call,
                                     @NonNull Response<AuthApi.GoogleLoginEnvelope> res) {
                if (!isAdded()) return;

                Log.d(TAG, "Backend response code: " + res.code());

                // ✅ PRIORITY 1: Check for HTTP 400 MISSING_PHONE first
                if (res.code() == 400) {
                  setLoading(false);

                  // Parse errorBody for 4xx responses
                  AuthApi.GoogleLoginEnvelope body = null;

                  try {
                    ResponseBody errorBody = res.errorBody();
                    if (errorBody != null) {
                      String errorJson = errorBody.string();
                      Log.d(TAG, "Error body JSON: " + errorJson);

                      Gson gson = new Gson();
                      body = gson.fromJson(errorJson, AuthApi.GoogleLoginEnvelope.class);

                      Log.d(TAG, "Parsed error body successfully");
                    }
                  } catch (Exception e) {
                    Log.e(TAG, "Failed to parse error body", e);
                  }

                  if (body != null) {
                    Log.d(TAG, "Body.error is null: " + (body.error == null));
                    if (body.error != null) {
                      Log.d(TAG, "Error code: " + body.error.code);
                    }
                    Log.d(TAG, "Body.data is null: " + (body.data == null));
                    if (body.data != null) {
                      Log.d(TAG, "SocialNonce exists: " + (body.data.socialNonce != null));
                    }
                  }

                  // Check error.code = "MISSING_PHONE"
                  if (body != null && body.error != null && "MISSING_PHONE".equals(body.error.code)) {
                    Log.d(TAG, "MISSING_PHONE detected!");

                    if (body.data != null && body.data.socialNonce != null) {
                      String name = (body.data.profile != null && body.data.profile.name != null)
                              ? body.data.profile.name : "bạn";
                      Log.d(TAG, "Showing phone prompt for: " + name);
                      showPhonePrompt(body.data.socialNonce, name);
                      return;
                    } else {
                      Log.e(TAG, "Missing socialNonce in response data");
                    }
                  }

                  // Generic 400 error
                  String msg = (body != null && body.error != null && body.error.message != null)
                          ? body.error.message : "Lỗi xác thực";
                  toast(msg);
                  return;
                }

                // ✅ PRIORITY 2: SUCCESS case (HTTP 200)
                if (res.isSuccessful() && res.body() != null && res.body().success) {
                  setLoading(false);
                  AuthApi.GoogleLoginEnvelope.Data data = res.body().data;
                  if (data != null && data.token != null) {
                    String bearer = data.getBearerToken();
                    if (bearer != null) {
                      Prefs.saveToken(requireContext(), bearer);
                      toast("Đăng nhập thành công!");
                      goToMainAndFinish();
                      return;
                    }
                  }
                }

                // ✅ PRIORITY 3: Other errors
                setLoading(false);
                Log.e(TAG, "Unhandled response code: " + res.code());
                toast("Lỗi không xác định từ server");
              }

              @Override
              public void onFailure(@NonNull Call<AuthApi.GoogleLoginEnvelope> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setLoading(false);
                Log.e(TAG, "Network error", t);
                toast("Không kết nối được máy chủ: " + t.getMessage());
              }
            });
  }

  /**
   *  Show dialog to collect phone number
   */
  private void showPhonePrompt(String socialNonce, String name) {
    if (!isAdded()) return;

    Log.d(TAG, "Showing phone prompt for user: " + name);

    requireActivity().runOnUiThread(() -> {
      // Inflate custom layout
      View dialogView = LayoutInflater.from(requireContext())
              .inflate(R.layout.dialog_phone_input, null);

      // Get views
      TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
      TextInputLayout tilPhone = dialogView.findViewById(R.id.tilPhone);
      TextInputEditText edtPhone = dialogView.findViewById(R.id.edtPhone);

      // Set personalized message
      tvMessage.setText("Chào " + name + "! Vui lòng cung cấp số điện thoại để hoàn tất đăng ký.");

      // Build dialog
      AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
      builder.setView(dialogView);
      builder.setCancelable(false);

      AlertDialog dialog = builder.create();

      // Set background transparent to avoid default white corners
      if (dialog.getWindow() != null) {
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      }

      dialog.show();

      // Must call setBackgroundDrawable AFTER show() or BEFORE via window?
      // Actually, with AlertDialog.Builder, best to set it on the window after create or show.
      // But wait, we need to set it on the window. Let's do it after create, before show, or after show.
      // Standard practice:
      if (dialog.getWindow() != null) {
          dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      }
      
      // Button listeners
      dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> {
        dialog.dismiss();
        Log.d(TAG, "User cancelled phone prompt");
      });

      dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
        String rawPhone = edtPhone.getText() == null ? "" : edtPhone.getText().toString().trim();

        if (TextUtils.isEmpty(rawPhone)) {
          tilPhone.setError("Vui lòng nhập số điện thoại");
          return;
        }

        // Simple validation: must be 10-11 digits
        String digits = rawPhone.replaceAll("[^\\d]", "");
        if (digits.length() < 10 || digits.length() > 11) {
          tilPhone.setError("Số điện thoại không hợp lệ");
          return;
        }

        tilPhone.setError(null);
        String e164 = AuthRepository.toE164(rawPhone);
        Log.d(TAG, "Phone entered: " + e164);

        dialog.dismiss();
        completeSocialLogin(socialNonce, e164);
      });
    });
  }
  /**
   * ✅ Complete social login by sending phone to backend
   */
  private void completeSocialLogin(String socialNonce, String phone) {
    setLoading(true);
    Log.d(TAG, "Completing social login with phone");

    AuthApi api = RetrofitProvider.auth();
    api.completeLoginGoogle(new AuthApi.CompleteSocialRequest(socialNonce, phone))
            .enqueue(new Callback<AuthApi.LoginEnvelope>() {
              @Override
              public void onResponse(@NonNull Call<AuthApi.LoginEnvelope> call,
                                     @NonNull Response<AuthApi.LoginEnvelope> res) {
                if (!isAdded()) return;
                setLoading(false);

                Log.d(TAG, "Complete social response code: " + res.code());

                if (res.isSuccessful() && res.body() != null && res.body().success && res.body().data != null) {
                  String bearer = res.body().data.getBearerToken();
                  if (bearer != null) {
                    Prefs.saveToken(requireContext(), bearer);
                    toast("Hoàn tất đăng ký thành công!");
                    goToMainAndFinish();
                    return;
                  }
                }

                Log.e(TAG, "Complete social login failed: " + res.code());
                toast("Hoàn tất đăng ký thất bại");
              }

              @Override
              public void onFailure(@NonNull Call<AuthApi.LoginEnvelope> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setLoading(false);
                Log.e(TAG, "Network error completing social", t);
                toast("Không kết nối được server");
              }
            });
  }

  private void goToMainAndFinish() {
    if (!isAdded()) return;

    NavController nav = androidx.navigation.Navigation.findNavController(
            requireActivity(), R.id.nav_host_fragment_activity_main);

    boolean hasHome = nav.getGraph().findNode(R.id.navigation_home) != null;
    if (!hasHome) {
      nav.setGraph(R.navigation.mobile_navigation);
    }

    NavOptions opts = new NavOptions.Builder()
            .setPopUpTo(R.id.mobile_navigation, true)
            .build();

    nav.navigate(R.id.action_global_home, null, opts);
  }

  private void setLoading(boolean loading) {
    if (binding == null) return;
    binding.btnRegister.setEnabled(!loading);
    binding.btnGoogleRegister.setEnabled(!loading);
    binding.btnRegister.setText(loading ? "Đang xử lý…" : "Đăng ký");
    binding.btnGoogleRegister.setText(loading ? "Đang xử lý…" : "Đăng ký bằng Google");
  }

  // ==================== Phone Registration ====================

  private void onSignUp() {
    String rawPhone = get(binding.edtPhone);
    String name = get(binding.edtName);
    String pass = get(binding.edtPassword);
    String confirm = get(binding.edtRepassword);

    if (TextUtils.isEmpty(rawPhone) || TextUtils.isEmpty(name)
            || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
      toast("Vui lòng điền đầy đủ thông tin");
      return;
    }

    if (!pass.equals(confirm)) {
      toast("Mật khẩu xác nhận không khớp");
      return;
    }

    String e164 = AuthRepository.toE164(rawPhone);
    repo.precheck(e164, new AuthRepository.PrecheckCallback() {
      @Override
      public void onSuccess(String nonce, long expiresInSec) {
        repo.startPhoneVerification(requireActivity(), e164,
                new AuthRepository.OtpSendCallback() {
                  @Override
                  public void onCodeSent(@NonNull String verificationId,
                                         @NonNull PhoneAuthProvider.ForceResendingToken token) {
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
                    Log.e(TAG, "OTP send failed: " + message);
                    toast("Gửi OTP thất bại: " + message);
                  }

                  @Override
                  public void onInstantVerified(@NonNull String idToken) {
                    repo.register(idToken, name, pass, nonce, new AuthRepository.RegisterCallback() {
                      @Override
                      public void onSuccess(@NonNull AuthApi.User user, @NonNull String jwt) {
                        toast("Đăng ký thành công!");
                        requireActivity().finish();
                      }

                      @Override
                      public void onError(@NonNull String message) {
                        toast("Đăng ký thất bại: " + message);
                        Log.e(TAG, message);
                      }
                    });
                  }
                });
      }

      @Override
      public void onError(String message) {
        toast(message);
      }
    });
  }

  private String get(TextInputEditText e) {
    return e.getText() == null ? "" : e.getText().toString().trim();
  }

  private void toast(String s) {
    if (!isAdded()) return;
    Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}