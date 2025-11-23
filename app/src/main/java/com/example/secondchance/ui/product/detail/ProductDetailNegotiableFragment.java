package com.example.secondchance.ui.product.detail;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ProductDetailNegotiableFragment extends BaseProductDetailFragment {

    private LinearLayout btnEdit, btnDelete;

    @Override
    protected String getProductType() {
        return "negotiable";
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

        setupButtons();
    }

    private void setupButtons() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("productId", productId);

                Navigation.findNavController(v)
                        .navigate(R.id.action_detailNegotiable_to_editNegotiable, bundle);
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
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
}