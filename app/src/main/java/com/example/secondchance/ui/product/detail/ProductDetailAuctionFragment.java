package com.example.secondchance.ui.product.detail;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.ui.product.AuctionSessionFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ProductDetailAuctionFragment extends BaseProductDetailFragment {

    private LinearLayout btnEdit, btnDelete;
    private Button btnWatchAuction;

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_product_detail_auction;
    }

    @Override
    protected String getProductType() {
        return "auction";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

        btnEdit = view.findViewById(R.id.btn_edit);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnWatchAuction = view.findViewById(R.id.btn_watch_auction);

        setupButtons();
    }

    private void setupButtons() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("productId", productId);
                bundle.putString("productName", productName);
                bundle.putFloat("price", price);
                bundle.putInt("quantity", quantity);
                bundle.putStringArrayList("imageUrls", new ArrayList<>(imageUrls));
                bundle.putString("productType", "auction");

                Navigation.findNavController(v)
                        .navigate(R.id.action_detailAuction_to_editAuction, bundle);
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        if (btnWatchAuction != null) {
            btnWatchAuction.setOnClickListener(v -> {
                navigateToAuctionSession();
            });
        }
    }

    private void showDeleteConfirmationDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_product_confirm_delete);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnConfirm = dialog.findViewById(R.id.btnConfirmDelete);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancelDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            SampleProductData.deleteProduct(productId);
            dialog.dismiss();
            showDeleteSuccessDialog();
        });

        dialog.show();
    }

    private void showDeleteSuccessDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_delete_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView btnClose = dialog.findViewById(R.id.btnCloseSuccess);
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).popBackStack();
        });
        dialog.show();
    }

    private void navigateToAuctionSession() {
        long endTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000) + (39 * 60 * 1000) + (12 * 1000);

        AuctionSessionFragment fragment = AuctionSessionFragment.newInstance(
                productId,
                productName,
                price,
                quantity,
                endTime
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
