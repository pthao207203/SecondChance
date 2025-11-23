package com.example.secondchance.ui.product.add;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.secondchance.R;
// ❌ bỏ SampleProductData
// import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.AdminProductDetailResponse;
import com.example.secondchance.ui.product.Product;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductFragment extends BaseAddProductFragment {
    
    private String productId;
    private Product currentProduct;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            productType = getArguments().getString("productType"); // fixed / negotiable / auction
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Sau khi view + step views đã init → gọi API
        Log.d("EditProductFragment", "productId = " + productId);
        if (productId != null) {
            fetchProductFromBackend();
        }
    }
    
    /**
     * Gọi API /admin/products/{id} để lấy dữ liệu sản phẩm
     */
    private void fetchProductFromBackend() {
        ProductApi api = RetrofitProvider.product();
        Call<AdminProductDetailResponse> call = api.getAdminProductById(productId);
        
        call.enqueue(new Callback<AdminProductDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminProductDetailResponse> call,
                                   @NonNull Response<AdminProductDetailResponse> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    AdminProductDetailResponse.AdminProduct data = response.body().data;
                    if (data == null) {
                        Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Map từ AdminProduct → Product model local
                    currentProduct = new Product();
                    currentProduct.setId(data.id);
                    currentProduct.setName(data.name);
                    currentProduct.setDescription(data.description);
                    currentProduct.setPrice(data.price);
                    currentProduct.setQuantity(data.quantity);
                    currentProduct.setPostedDate(data.createdAt);
                    currentProduct.setType(productType != null ? productType : "fixed");
                    
                    // Ảnh
                    currentProduct.setImageUrls(
                      data.media != null ? new ArrayList<>(data.media) : new ArrayList<>()
                    );
                    
                    // selectedImages để hiển thị slider / thumbnail
                    selectedImages.clear();
                    if (currentProduct.getImageUrls() != null) {
                        for (String url : currentProduct.getImageUrls()) {
                            selectedImages.add(Uri.parse(url));
                        }
                    }
                    
                    // Đổ dữ liệu vào UI
                    applyCurrentProductToViews();
                    
                } else {
                    Toast.makeText(getContext(),
                      "Lỗi load sản phẩm: " + response.code(),
                      Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<AdminProductDetailResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                Log.e("EditProductFragment", "fetchProductFromBackend error", t);
                Toast.makeText(getContext(),
                  "Lỗi kết nối: " + t.getMessage(),
                  Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Sau khi currentProduct đã có dữ liệu, đổ vào các view step 1–3
     */
    private void applyCurrentProductToViews() {
        if (currentProduct == null) return;
        
        // Step 1: tên, giá, ảnh
        if (etProductName != null) {
            etProductName.setText(currentProduct.getName());
        }
        if (etPrice != null) {
            etPrice.setText(String.valueOf((int) currentProduct.getPrice()));
        }
        if (!selectedImages.isEmpty()) {
            updateThumbnails();
            updateImageSlider();
        }
        
        // Step 2: mô tả
        if (etDescription != null) {
            etDescription.setText(currentProduct.getDescription());
        }
        
        // Step 3: hiện tại backend chưa có source/proof/otherInfo → giữ trống
        if (etSourceLink != null) {
            etSourceLink.setText(currentProduct.getSource()); // thường sẽ là null
        }
        if (etProof != null) {
            etProof.setText(currentProduct.getProof());
        }
        
        
        // Khóa spinner loại sản phẩm để không đổi type khi edit
        if (spinnerType != null) {
            spinnerType.setEnabled(false);
            spinnerType.setClickable(false);
        }
    }
    
    // --- Các hàm override cũ, rút gọn lại ---
    
    @Override
    protected void initStep1Views(View view) {
        super.initStep1Views(view);
        // Không đổ data ở đây nữa, chờ API rồi applyCurrentProductToViews()
        if (spinnerType != null) {
            spinnerType.setEnabled(false);
            spinnerType.setClickable(false);
        }
    }
    
    @Override
    protected void initStep2Views(View view) {
        super.initStep2Views(view);
        // Dữ liệu được set trong applyCurrentProductToViews()
    }
    
    @Override
    protected void initStep3Views(View view) {
        super.initStep3Views(view);
        // Dữ liệu được set trong applyCurrentProductToViews()
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
