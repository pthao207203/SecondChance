package com.example.secondchance.ui.product.detail;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.repo.CartRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailFixedFragment extends BaseProductDetailFragment {

    private LinearLayout btnEdit, btnDelete, btnAddToCart;

    @Override
    protected String getProductType() {
        return "fixed";
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
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart);

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
                bundle.putString("productType", "fixed");

                Navigation.findNavController(v)
                        .navigate(R.id.action_detailFixed_to_editFixed, bundle);
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        }

        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                CartRepository.getInstance().addToCart(productId, 1, new CartRepository.CartCallback() {
                    @Override
                    public void onSuccess(List<CartApi.CartItem> items) {
                        Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
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
}