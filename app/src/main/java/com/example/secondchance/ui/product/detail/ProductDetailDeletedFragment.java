package com.example.secondchance.ui.product.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.secondchance.R;

import java.util.ArrayList;

public class ProductDetailDeletedFragment extends BaseProductDetailFragment {

    private LinearLayout btnRestore;

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_product_detail_deleted;
    }

    @Override
    protected String getProductType() {
        return "deleted";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        btnRestore = view.findViewById(R.id.btn_restore);
        setupButtons();
    }

    private void setupButtons() {
        if (btnRestore != null) {
            btnRestore.setOnClickListener(v -> {
                if (currentProduct == null) return;

                Bundle bundle = new Bundle();
                bundle.putString("productId", currentProduct.getId());
                bundle.putString("productName", currentProduct.getName());
                bundle.putFloat("price", (float) currentProduct.getPrice());
                bundle.putInt("quantity", currentProduct.getQuantity());
                bundle.putStringArrayList("imageUrls", new ArrayList<>(currentProduct.getImageUrls()));
                bundle.putString("description", currentProduct.getDescription());
                bundle.putString("source", currentProduct.getSource());
                bundle.putString("proof", currentProduct.getProof());
                bundle.putString("otherInfo", currentProduct.getOtherInfo());
                
                String originalStatus = currentProduct.getOriginalStatus();
                bundle.putString("productType", originalStatus);

                int actionId;
                switch (originalStatus) {
                    case "fixed":
                        actionId = R.id.action_deleted_to_edit_fixed;
                        break;
                    case "negotiable":
                        actionId = R.id.action_deleted_to_edit_negotiable;
                        break;
                    case "auction":
                        actionId = R.id.action_deleted_to_edit_auction;
                        break;
                    default:
                        return; 
                }

                Navigation.findNavController(v).navigate(actionId, bundle);
            });
        }
    }
}