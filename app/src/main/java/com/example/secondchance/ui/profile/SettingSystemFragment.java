package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.secondchance.R;

public class SettingSystemFragment extends Fragment {

    private CheckBox swLanguage, swTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting_system, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo switches
        swLanguage = view.findViewById(R.id.swLanguage);
        swTheme = view.findViewById(R.id.swTheme);

        // Language switch
        view.findViewById(R.id.btnSetLanguage).setOnClickListener(v -> {
            swLanguage.setChecked(!swLanguage.isChecked());
            String lang = swLanguage.isChecked() ? "Tiếng Anh" : "Tiếng Việt";
            Toast.makeText(requireContext(), "Ngôn ngữ: " + lang, Toast.LENGTH_SHORT).show();
        });

        swLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String lang = isChecked ? "Tiếng Anh" : "Tiếng Việt";
            // TODO: Implement language change
        });

        // Theme switch
        view.findViewById(R.id.btnSetTheme).setOnClickListener(v -> {
            swTheme.setChecked(!swTheme.isChecked());
            String theme = swTheme.isChecked() ? "Chế độ tối" : "Chế độ sáng";
            Toast.makeText(requireContext(), theme, Toast.LENGTH_SHORT).show();
        });

        swTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: Implement theme change
        });
    }
}