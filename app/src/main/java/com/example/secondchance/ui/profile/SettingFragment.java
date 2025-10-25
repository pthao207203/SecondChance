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

        // Nút Chỉnh sửa tài khoản
        view.findViewById(R.id.btnAccountSettings).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_setting_to_accountSettings)
        );

        // Nút Cài đặt hệ thống
        view.findViewById(R.id.btnSystemSettings).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_setting_to_systemSettings)
        );
    }
}