package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.google.android.material.card.MaterialCardView;

public class RuleSellerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rule_seller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Checkbox
        ImageView checkbox = view.findViewById(R.id.checkbox);
        checkbox.setTag("unchecked");
        checkbox.setOnClickListener(v -> {
            if ("unchecked".equals(checkbox.getTag())) {
                checkbox.setImageResource(R.drawable.ic_checkbox_checked);
                checkbox.setTag("checked");
            } else {
                checkbox.setImageResource(R.drawable.ic_checkbox_unchecked);
                checkbox.setTag("unchecked");
            }
        });

        // Nút "TIẾP TỤC"
        MaterialCardView btnNext = view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if ("checked".equals(checkbox.getTag())) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_rule_seller_to_fill_info_seller);
            } else {
                Toast.makeText(requireContext(), "Vui lòng đồng ý quy tắc!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
