package com.example.secondchance.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.secondchance.databinding.FragmentLoginBinding;
import com.example.secondchance.util.Prefs;

// ✅ FIXED IMPORTS - Modern Google Sign-In API
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;  // ✅ FIX: Import Task từ gms.tasks
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding b;
    private NavController nav;
    private static final Pattern VN_PHONE = Pattern.compile("^(0|84)(\\d){9}$|^(\\d){10}$");
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentLoginBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nav = NavHostFragment.findNavController(this);

        // Check if already logged in
        if (!Prefs.getToken(requireContext()).isEmpty()) {
            goToMainAndFinish();
            return;
        }

        // Setup click listeners
        b.btnLogin.setOnClickListener(v -> tryLogin());
        b.tvForgot.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Đi tới màn hình quên mật khẩu…", Toast.LENGTH_SHORT).show());
        b.tvSignup.setOnClickListener(v -> nav.navigate(R.id.action_login_to_register));

        // ✅ FIXED: Setup Google Sign-In với Web Client ID
        setupGoogleSignIn();

        b.btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    /**
     * ✅ FIXED: Modern Google Sign-In setup
     */
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

            Log.d(TAG, "Google Sign-In configured with client ID: " + serverClientId.substring(0, 20) + "...");
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup Google Sign-In", e);
            Toast.makeText(requireContext(), "Lỗi cấu hình Google Sign-In", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ FIXED: Initiate Google Sign-In flow
     */
    private void signInWithGoogle() {
        if (mGoogleSignInClient == null) {
            Toast.makeText(requireContext(), "Google Sign-In chưa được cấu hình", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign out first để luôn hiện account picker
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            Log.d(TAG, "Launched Google Sign-In intent");
        });
    }

    /**
     * ✅ FIXED: Handle Google Sign-In result
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * ✅ FIXED: Process Google Sign-In result
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account == null) {
                Log.w(TAG, "Google Sign-In account is null");
                Toast.makeText(requireContext(), "Lỗi lấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Google Sign-In success: " + account.getEmail());
            String idToken = account.getIdToken();

            if (idToken == null) {
                Log.e(TAG, "IdToken is null - check Web Client ID configuration");
                Toast.makeText(requireContext(), "Lỗi: Không lấy được idToken", Toast.LENGTH_LONG).show();
                return;
            }

            // Authenticate với Firebase
            firebaseAuthWithGoogle(idToken);

        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed with code: " + e.getStatusCode(), e);

            String errorMsg;
            switch (e.getStatusCode()) {
                case 10: // DEVELOPER_ERROR
                    errorMsg = "Lỗi cấu hình (SHA-1 không khớp hoặc Web Client ID sai)";
                    break;
                case 12500: // SIGN_IN_FAILED
                    errorMsg = "Đăng nhập thất bại, vui lòng thử lại";
                    break;
                case 7: // NETWORK_ERROR
                    errorMsg = "Lỗi kết nối mạng";
                    break;
                default:
                    errorMsg = "Đăng nhập Google thất bại (code: " + e.getStatusCode() + ")";
            }

            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Authenticate với Firebase using Google credential
     */
    private void firebaseAuthWithGoogle(String idToken) {
        setLoading(true);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            setLoading(false);
                            Toast.makeText(requireContext(), "Lỗi: Firebase user is null", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get fresh Firebase ID token để gửi cho backend
                        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                String firebaseIdToken = tokenTask.getResult().getToken();
                                Log.d(TAG, "Got Firebase ID token, sending to backend");
                                sendGoogleLoginToBackend(firebaseIdToken);
                            } else {
                                setLoading(false);
                                Log.e(TAG, "Failed to get Firebase ID token", tokenTask.getException());
                                Toast.makeText(requireContext(), "Không lấy được token từ Firebase", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        setLoading(false);
                        Log.e(TAG, "Firebase authentication failed", task.getException());
                        Toast.makeText(requireContext(), "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Send Firebase ID token to backend
     */
    private void sendGoogleLoginToBackend(String idToken) {
        AuthApi api = RetrofitProvider.auth();

        api.loginGoogle(new AuthApi.LoginGoogleRequest(idToken))
                .enqueue(new Callback<AuthApi.GoogleLoginEnvelope>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthApi.GoogleLoginEnvelope> call,
                                           @NonNull Response<AuthApi.GoogleLoginEnvelope> res) {
                        if (!isAdded()) return;
                        setLoading(false);

                        // ✅ SUCCESS: User exists with phone
                        if (res.isSuccessful() && res.body() != null && res.body().success) {
                            AuthApi.GoogleLoginEnvelope.Data data = res.body().data;
                            if (data != null && data.token != null) {
                                String bearer = data.getBearerToken();
                                if (bearer != null) {
                                    Prefs.saveToken(requireContext(), bearer);
                                    Prefs.saveLoginType(requireContext(), Prefs.TYPE_GOOGLE);
                                    Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                    goToMainAndFinish();
                                    return;
                                }
                            }
                        }

                        // ✅ MISSING_PHONE case (HTTP 400)
                        if (res.code() == 400 && res.body() != null) {
                            AuthApi.GoogleLoginEnvelope body = res.body();

                            // Backend trả success=false + error.code=MISSING_PHONE + data.socialNonce
                            if (body.error != null && "MISSING_PHONE".equals(body.error.code)) {
                                if (body.data != null && body.data.socialNonce != null) {
                                    String name = (body.data.profile != null && body.data.profile.name != null)
                                            ? body.data.profile.name : "bạn";
                                    showPhonePrompt(body.data.socialNonce, name);
                                    return;
                                }
                            }

                            // Generic error
                            String msg = (body.error != null && body.error.message != null)
                                    ? body.error.message : "Đăng nhập thất bại";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Other errors
                        Log.e(TAG, "Backend login failed with code: " + res.code());
                        Toast.makeText(requireContext(), "Lỗi từ server: " + res.code(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthApi.GoogleLoginEnvelope> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        setLoading(false);
                        Log.e(TAG, "Network error calling backend", t);
                        Toast.makeText(requireContext(), "Không kết nối được máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Show dialog to collect phone number
     */
    private void showPhonePrompt(String socialNonce, String name) {
        if (!isAdded()) return;

        requireActivity().runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Bổ sung số điện thoại");
            builder.setMessage("Chào " + name + "! Vui lòng cung cấp số điện thoại để hoàn tất đăng ký.");

            final EditText input = new EditText(requireContext());
            input.setHint("Ví dụ: 0912345678");
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            input.setPadding(padding, padding, padding, padding);
            builder.setView(input);

            builder.setPositiveButton("Xác nhận", null); // Override sau
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
            builder.setCancelable(false);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Override positive button để validate
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String rawPhone = input.getText() == null ? "" : input.getText().toString().trim();
                if (TextUtils.isEmpty(rawPhone)) {
                    input.setError("Vui lòng nhập số điện thoại");
                    return;
                }

                String e164 = AuthRepository.toE164(rawPhone);
                dialog.dismiss();
                completeSocialLogin(socialNonce, e164);
            });
        });
    }

    /**
     * Complete social login by sending phone to backend
     */
    private void completeSocialLogin(String socialNonce, String phone) {
        setLoading(true);

        AuthApi api = RetrofitProvider.auth();
        api.completeLoginGoogle(new AuthApi.CompleteSocialRequest(socialNonce, phone))
                .enqueue(new Callback<AuthApi.LoginEnvelope>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthApi.LoginEnvelope> call,
                                           @NonNull Response<AuthApi.LoginEnvelope> res) {
                        if (!isAdded()) return;
                        setLoading(false);

                        if (res.isSuccessful() && res.body() != null && res.body().success && res.body().data != null) {
                            String bearer = res.body().data.getBearerToken();
                            if (bearer != null) {
                                Prefs.saveToken(requireContext(), bearer);
                                Prefs.saveLoginType(requireContext(), Prefs.TYPE_GOOGLE);
                                Toast.makeText(requireContext(), "Hoàn tất đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                goToMainAndFinish();
                                return;
                            }
                        }

                        Log.e(TAG, "Complete social login failed: " + res.code());
                        Toast.makeText(requireContext(), "Hoàn tất đăng ký thất bại", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthApi.LoginEnvelope> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        setLoading(false);
                        Log.e(TAG, "Network error", t);
                        Toast.makeText(requireContext(), "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ==================== Regular Login ====================

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
                    public void onResponse(@NonNull Call<AuthApi.LoginEnvelope> call,
                                           @NonNull Response<AuthApi.LoginEnvelope> res) {
                        if (!isAdded()) return;
                        setLoading(false);

                        if (res.isSuccessful() && res.body() != null && res.body().success && res.body().data != null) {
                            String bearer = res.body().data.getBearerToken();
                            if (bearer != null) {
                                Prefs.saveToken(requireContext(), bearer);
                                Prefs.saveLoginType(requireContext(), Prefs.TYPE_NORMAL);
                                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                goToMainAndFinish();
                                return;
                            }
                        }

                        Toast.makeText(requireContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthApi.LoginEnvelope> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        setLoading(false);
                        Toast.makeText(requireContext(), "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ==================== Helpers ====================

    private void setLoading(boolean loading) {
        if (b == null) return;
        b.btnLogin.setEnabled(!loading);
        b.btnGoogleLogin.setEnabled(!loading);
        b.btnLogin.setText(loading ? "Đang xử lý…" : "Đăng nhập");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}