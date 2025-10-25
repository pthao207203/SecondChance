package com.example.secondchance.ui.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.secondchance.R;

public class AccountSettingFragment extends Fragment {

    private EditText etUsername, etCurrentPassword, etNewPassword, etConfirmPassword;
    private AppCompatButton btnUpdate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etUsername = view.findViewById(R.id.etUsername);
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnUpdate = view.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(v -> updateAccount());
    }

    private void updateAccount() {
        String username = etUsername.getText().toString().trim();
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.isEmpty()) {
            if (currentPass.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(requireContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(requireContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        showUpdateSuccessDialog();
    }

    private void showUpdateSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        View btnCloseSuccess = dialog.findViewById(R.id.btnCloseSuccess);
        btnCloseSuccess.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).popBackStack(R.id.settingFragment, false);
        });

        dialog.show();
    }
}