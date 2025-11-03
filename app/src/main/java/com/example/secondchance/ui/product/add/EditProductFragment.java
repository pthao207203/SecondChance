package com.example.secondchance.ui.product.add;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.ui.product.Product;
import java.util.ArrayList;

public class EditProductFragment extends BaseAddProductFragment {

    private String productId;
    private Product currentProduct;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // This is MANDATORY. The previous code was incorrect.
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            // This is the crucial part: Get the type from the arguments bundle FIRST.
            // This will be the originalStatus ("auction") when restoring.
            productType = getArguments().getString("productType");

            currentProduct = SampleProductData.getProductById(productId);

            // The productType is already set by the super.onCreate() call.
            // We just need to load the images.
            if (currentProduct != null) {
                selectedImages.clear(); // Clear any previous images
                ArrayList<String> imageUrls = new ArrayList<>(currentProduct.getImageUrls());
                for (String url : imageUrls) {
                    selectedImages.add(Uri.parse(url));
                }
            }
        }
    }

    @Override
    protected void initStep1Views(View view) {
        super.initStep1Views(view);
        // Populate the fields with the existing product data
        if (currentProduct != null) {
            etProductName.setText(currentProduct.getName());
            etPrice.setText(String.valueOf((int) currentProduct.getPrice()));
            if (!selectedImages.isEmpty()) {
                updateThumbnails();
                updateImageSlider();
            }
        }
        // CRITICAL: Disable the spinner in edit/restore mode to prevent type changes.
        if (spinnerType != null) {
            spinnerType.setEnabled(false);
            spinnerType.setClickable(false);
        }
    }

    @Override
    protected void initStep2Views(View view) {
        super.initStep2Views(view);
        if (currentProduct != null && etDescription != null) {
            etDescription.setText(currentProduct.getDescription());
        }
    }

    @Override
    protected void initStep3Views(View view) {
        super.initStep3Views(view);
        if (currentProduct != null) {
            if (etSourceLink != null) {
                etSourceLink.setText(currentProduct.getSource());
            }
            if (etProof != null) {
                etProof.setText(currentProduct.getProof());
            }
        }
    }

    @Override
    protected void showPreview() {
        Product product = collectProductData();

        Bundle bundle = new Bundle();
        bundle.putString("productId", product.getId());
        bundle.putString("productName", product.getName());
        bundle.putDouble("price", product.getPrice());
        bundle.putInt("quantity", product.getQuantity());
        bundle.putString("description", product.getDescription());
        bundle.putString("source", product.getSource());
        bundle.putString("proof", product.getProof());
        bundle.putString("otherInfo", product.getOtherInfo());
        bundle.putLong("endTime", product.getEndTime());

        ArrayList<String> imageUrls = new ArrayList<>();
        for (Uri uri : selectedImages) {
            imageUrls.add(uri.toString());
        }
        bundle.putStringArrayList("imageUrls", imageUrls);
        // Pass the correct, immutable productType
        bundle.putString("productType", this.productType);
        bundle.putBoolean("isEditMode", true);

        try {
            Navigation.findNavController(requireView()).navigate(getPreviewNavigationAction(), bundle);
        } catch (Exception e) {
            Log.e("EditProductFragment", "Navigation failed", e);
            Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected Product collectProductData() {
        Product product = super.collectProductData();
        product.setId(this.productId);
        return product;
    }

    @Override
    protected String getDefaultProductType() {
        return productType != null ? productType : "fixed";
    }

    @Override
    protected int getSuccessNavigationAction() {
        return -1; // Not used in edit mode
    }

    @Override
    protected int getPreviewNavigationAction() {
        if (productType == null) return -1;
        switch (productType) {
            case "fixed":
                return R.id.action_editProductFixed_to_preview;
            case "negotiable":
                return R.id.action_editProductNegotiable_to_preview;
            case "auction":
                return R.id.action_editProductAuction_to_preview;
            default:
                return -1;
        }
    }
}
