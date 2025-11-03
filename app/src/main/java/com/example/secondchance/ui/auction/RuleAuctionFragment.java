package com.example.secondchance.ui.auction;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
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

        // Khi nhấn nút "THAM GIA"
        MaterialCardView btnJoin = view.findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(v -> showAuctionDialog());

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
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_rule_auction_to_auction_detail);
        });

        dialog.show();
    }
}

