package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import com.example.secondchance.util.Prefs;

public class SettingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nút Chỉnh sửa hồ sơ
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_setting_to_editProfile)
        );

        // Xử lý hiển thị phần Cài đặt tài khoản dựa trên loại đăng nhập
        View llAccountSettingsContainer = view.findViewById(R.id.llAccountSettingsContainer);
        String loginType = Prefs.getLoginType(requireContext());

        if (Prefs.TYPE_GOOGLE.equals(loginType)) {
            // Nếu đăng nhập bằng Google, ẩn phần cài đặt tài khoản (đổi pass, username)
            if (llAccountSettingsContainer != null) {
                llAccountSettingsContainer.setVisibility(View.GONE);
            }
        } else {
            // Nếu đăng nhập thường, hiển thị và gán sự kiện click
            if (llAccountSettingsContainer != null) {
                llAccountSettingsContainer.setVisibility(View.VISIBLE);
            }
            
            // Nút Chỉnh sửa tài khoản
            View btnAccountSettings = view.findViewById(R.id.btnAccountSettings);
            if (btnAccountSettings != null) {
                btnAccountSettings.setOnClickListener(v ->
                        Navigation.findNavController(v).navigate(R.id.action_setting_to_accountSettings)
                );
            }
        }

        // Nút Cài đặt hệ thống
        view.findViewById(R.id.btnSystemSettings).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_setting_to_systemSettings)
        );
    }
}