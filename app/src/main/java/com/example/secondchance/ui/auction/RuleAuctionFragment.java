package com.example.secondchance.ui.auction;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.google.android.material.card.MaterialCardView;

public class RuleAuctionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule_auction, container, false);
        
        
        MaterialCardView btnJoin = view.findViewById(R.id.btnJoin);
        ImageView checkbox = view.findViewById(R.id.checkbox);
        
        // Khi nhấn nút "THAM GIA"
        btnJoin.setOnClickListener(v -> {
            if ("checked".equals(checkbox.getTag())) {
                showAuctionDialog();
            }
        });
        
        // Checkbox
        checkbox.setTag("unchecked");
        checkbox.setOnClickListener(v -> {
            if ("unchecked".equals(checkbox.getTag())) {
                checkbox.setImageResource(R.drawable.ic_checkbox_checked);
                checkbox.setTag("checked");
                btnJoin.setCardBackgroundColor(
                  ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.normalDay))
                );
            } else {
                checkbox.setImageResource(R.drawable.ic_checkbox_unchecked);
                checkbox.setTag("unchecked");
                btnJoin.setCardBackgroundColor(
                  ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.lightActiveDay))
                );
            }
        });
        

        return view;
    }

    private void showAuctionDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rule_auction);

        ImageView btnClose = dialog.findViewById(R.id.btnCloseSuccess);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        MaterialCardView btnAuction = dialog.findViewById(R.id.btnAuction);
        btnAuction.setOnClickListener(v -> {
            dialog.dismiss();
            Bundle bundle = new Bundle();
            String productIdArg = getArguments() != null ? getArguments().getString("productId") : null;
            bundle.putString("productId", productIdArg);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_rule_auction_to_auction_detail, bundle);
        });

        dialog.show();
    }
}

