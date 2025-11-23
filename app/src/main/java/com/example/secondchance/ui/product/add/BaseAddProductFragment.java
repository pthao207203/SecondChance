package com.example.secondchance.ui.product.add;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.request.ProductCreateRequest;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.ProductMetaResponse;
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ImageSliderAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseAddProductFragment extends Fragment {

    protected int currentStep = 1;
    protected FrameLayout containerStep;

    protected View step1View, step2View, step3View;

    protected ViewPager2 viewPager;
    protected LinearLayout layoutIndicators;
    protected LinearLayout thumbnailContainer;
    protected LinearLayout proofThumbnailContainer;
    protected LinearLayout btnAddImage;
    protected EditText etProductName, etPrice, etConditionUsed, etConditionNew, etConditionDamage;
    protected EditText etWarranty, etReturnPolicy, etSource;
    protected Spinner spinnerType;
    protected LinearLayout btnNext1;

    protected EditText etDescription;
    protected LinearLayout btnBack2, btnNext2;

    protected EditText etSourceLink, etProof;
    protected LinearLayout btnBack3, btnPreview;

    protected List<Uri> selectedImages = new ArrayList<>();
    protected ActivityResultLauncher<String> imagePickerLauncher;
    protected ImageSliderAdapter imageSliderAdapter;

    protected String productType;
    protected TextView tvCategory, tvBrand;
    
    // lưu ID đã chọn
    protected ArrayList<String> selectedCategoryIds = new ArrayList<>();
    protected String selectedBrandId;
    protected Spinner spinnerBrand;
    protected LinearLayout layoutCategoryPicker;
    protected TextView tvSelectedCategories;
    
    // data meta
    protected List<ProductMetaResponse.Brand> brandOptions = new ArrayList<>();
    protected List<ProductMetaResponse.Category> categoryOptions = new ArrayList<>();
    protected Spinner spinnerHasOrigin;
    protected Spinner spinnerReturnPolicy;
    protected Spinner spinnerConditionUsed;
    protected boolean hasOrigin = false;
    protected boolean hasReturnPolicy = false;
    protected boolean hasConditionUsed = false;
    protected ProductApi productApi;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupImagePicker();

        if (getArguments() != null) {
            productType = getArguments().getString("productType", getDefaultProductType());
        } else {
            productType = getDefaultProductType();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FrameLayout mainContainer = new FrameLayout(requireContext());
        mainContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        containerStep = mainContainer;

        showStep(1);
        return mainContainer;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        step1View = null;
        step2View = null;
        step3View = null;
    }

    protected abstract String getDefaultProductType();
    protected abstract int getSuccessNavigationAction();
    protected abstract int getPreviewNavigationAction();

    protected void showStep(int step) {
        currentStep = step;
        containerStep.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        switch (step) {
            case 1:
                if (step1View == null) {
                    step1View = inflater.inflate(R.layout.fragment_add_product_fixed, containerStep, false);
                    initStep1Views(step1View);
                    setupStep1Listeners();
                }
                containerStep.addView(step1View);
                break;

            case 2:
                if (step2View == null) {
                    step2View = inflater.inflate(R.layout.fragment_add_product_fixed2, containerStep, false);
                    initStep2Views(step2View);
                    setupStep2Listeners();
                }
                containerStep.addView(step2View);
                break;

            case 3:
                if (step3View == null) {
                    step3View = inflater.inflate(R.layout.fragment_add_product_fixed3, containerStep, false);
                    initStep3Views(step3View);
                    setupStep3Listeners();
                }
                updateProofThumbnails(); 
                containerStep.addView(step3View);
                break;
        }
    }

    protected void initStep1Views(View view) {
        viewPager = view.findViewById(R.id.imageViewPager);
        layoutIndicators = view.findViewById(R.id.layoutIndicators);
        thumbnailContainer = view.findViewById(R.id.thumbnail_container);
        btnAddImage = view.findViewById(R.id.btn_add_image);
        etProductName = view.findViewById(R.id.et_product_name);
        etPrice = view.findViewById(R.id.et_price);
        etConditionNew = view.findViewById(R.id.et_condition_new);
        etConditionDamage = view.findViewById(R.id.et_condition_damage);
        etWarranty = view.findViewById(R.id.et_warranty);
        spinnerHasOrigin = view.findViewById(R.id.spinner_has_origin);
        spinnerReturnPolicy = view.findViewById(R.id.spinner_return_policy);
        spinnerConditionUsed = view.findViewById(R.id.spinner_condition_used);

        ArrayAdapter<String> boolAdapter = new ArrayAdapter<>(
          requireContext(),
          android.R.layout.simple_spinner_dropdown_item,
          new String[]{"Có", "Không"}
        );
        spinnerHasOrigin.setAdapter(boolAdapter);
        spinnerReturnPolicy.setAdapter(boolAdapter);
        
        ArrayAdapter<String> boolAdapterCondition = new ArrayAdapter<>(
          requireContext(),
          android.R.layout.simple_spinner_dropdown_item,
          new String[]{"Đã sử dụng", "Chưa sử dụng"}
        );
        spinnerConditionUsed.setAdapter(boolAdapterCondition);
        
        spinnerHasOrigin.setSelection(1);
        spinnerReturnPolicy.setSelection(1);
        spinnerConditionUsed.setSelection(0);
        
        spinnerType = view.findViewById(R.id.spinner_type);
        btnNext1 = view.findViewById(R.id.btn_next1);
        
        spinnerBrand = view.findViewById(R.id.spinner_brand);
        layoutCategoryPicker = view.findViewById(R.id.layout_category_picker);
        tvSelectedCategories = view.findViewById(R.id.tv_selected_categories);

        setupSpinner();
        
        productApi = RetrofitProvider.product();
        fetchMetaAndInitUi();
    }
    private void fetchMetaAndInitUi() {
        productApi.getProductMeta().enqueue(new Callback<ProductMetaResponse>() {
            @Override
            public void onResponse(Call<ProductMetaResponse> call,
                                   Response<ProductMetaResponse> response) {
                if (!isAdded()) return;
                
                if (!response.isSuccessful() || response.body() == null || !response.body().success) {
                    Toast.makeText(getContext(), "Không tải được dữ liệu meta", Toast.LENGTH_SHORT).show();
                    return;
                }
                Gson gson = new Gson();
                Log.d("ProductMetaResponse", gson.toJson(response.body()));
                
                ProductMetaResponse.Data data = response.body().data;
                if (data == null) return;
                
                // brand
                brandOptions.clear();
                if (data.brands != null) {
                    brandOptions.addAll(data.brands);
                }
                
                // category – nếu muốn chỉ cho chọn category con thì lọc parentId != null
                categoryOptions.clear();
                if (data.categories != null) {
                    for (ProductMetaResponse.Category c : data.categories) {
                        if (c.parentId != null) {  // chỉ lấy category con
                            categoryOptions.add(c);
                        }
                    }
                }
                
                setupBrandSpinnerFromMeta();
                setupCategoryPickerFromMeta();
            }
            
            @Override
            public void onFailure(Call<ProductMetaResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối meta", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupBrandSpinnerFromMeta() {
        if (brandOptions.isEmpty() || spinnerBrand == null) return;
        
        BrandSpinnerAdapter adapter = new BrandSpinnerAdapter(
          requireContext(),
          brandOptions
        );
        spinnerBrand.setAdapter(adapter);
        
        spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ProductMetaResponse.Brand b = brandOptions.get(position);
                selectedBrandId = b.id;
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBrandId = null;
            }
        });
        
        // default: chọn dòng đầu
        spinnerBrand.setSelection(0);
        selectedBrandId = brandOptions.get(0).id;
    }
    
    
    private void setupCategoryPickerFromMeta() {
        layoutCategoryPicker.setOnClickListener(v -> showCategoryMultiSelectDialog());
    }
    
    private void showCategoryMultiSelectDialog() {
        if (categoryOptions.isEmpty()) return;
        
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_category_picker);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            // cho dialog rộng ~90% màn hình, không bị tụm như hình
            android.view.WindowManager.LayoutParams lp =
              new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9f);
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }
        
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        tvTitle.setText("Chọn danh mục");
        
        androidx.recyclerview.widget.RecyclerView rv =
          dialog.findViewById(R.id.rvCategories);
        rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        
        // adapter nhận list category & list id đã chọn
        CategoryPickerAdapter adapter =
          new CategoryPickerAdapter(categoryOptions, selectedCategoryIds);
        rv.setAdapter(adapter);
        
        com.google.android.material.button.MaterialButton btnCancel =
          dialog.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnConfirm =
          dialog.findViewById(R.id.btnConfirm);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirm.setOnClickListener(v -> {
            selectedCategoryIds.clear();
            selectedCategoryIds.addAll(adapter.getSelectedIds());
            updateCategorySummary();   // cập nhật text "Danh mục" ở màn hình chính
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    
    private void updateCategorySummary() {
        if (selectedCategoryIds.isEmpty()) {
            tvSelectedCategories.setText("Chọn danh mục");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (ProductMetaResponse.Category c : categoryOptions) {
            if (selectedCategoryIds.contains(c.id)) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(c.name);
            }
        }
        tvSelectedCategories.setText(sb.toString());
    }
    
    protected void initStep2Views(View view) {
        etDescription = view.findViewById(R.id.et_description);
        btnBack2 = view.findViewById(R.id.btn_back2);
        btnNext2 = view.findViewById(R.id.btn_next2);
    }

    protected void initStep3Views(View view) {
        etSourceLink = view.findViewById(R.id.et_source_link);
        etProof = view.findViewById(R.id.et_proof);
        proofThumbnailContainer = view.findViewById(R.id.proof_thumbnail_container);
        btnBack3 = view.findViewById(R.id.btn_back3);
        btnPreview = view.findViewById(R.id.btn_preview);
    }

    protected void setupSpinner() {
        String[] types = {"Giá cố định", "Thương lượng", "Đấu giá"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                types
        );
        spinnerType.setAdapter(adapter);

        int defaultPosition = getSpinnerPosition(productType);
        spinnerType.setSelection(defaultPosition);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: productType = "fixed"; break;
                    case 1: productType = "negotiable"; break;
                    case 2: productType = "auction"; break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    protected int getSpinnerPosition(String type) {
        switch (type) {
            case "fixed": return 0;
            case "negotiable": return 1;
            case "auction": return 2;
            default: return 0;
        }
    }

    protected void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && selectedImages.size() < 10) {
                        selectedImages.add(uri);
                        updateThumbnails();
                        updateImageSlider();
                    }
                }
        );
    }

    protected void updateThumbnails() {
        thumbnailContainer.removeAllViews();
        thumbnailContainer.addView(btnAddImage);

        for (int i = 0; i < selectedImages.size(); i++) {
            Uri imageUri = selectedImages.get(i);
            View thumbnailView = getLayoutInflater().inflate(R.layout.item_thumbnail, thumbnailContainer, false);
            ImageView thumbnail = thumbnailView.findViewById(R.id.img_thumb);

            try {
                int resId = Integer.parseInt(imageUri.toString());
                thumbnail.setImageResource(resId);
            } catch (NumberFormatException e) {
                thumbnail.setImageURI(imageUri);
            }

            int position = i;
            thumbnail.setOnClickListener(v -> viewPager.setCurrentItem(position, true));

            thumbnailContainer.addView(thumbnailView);
        }
    }

    protected void updateProofThumbnails() {
        if (proofThumbnailContainer != null) {
            proofThumbnailContainer.removeAllViews();

            for (Uri imageUri : selectedImages) {
                View thumbnailView = getLayoutInflater().inflate(R.layout.item_thumbnail, proofThumbnailContainer, false);
                ImageView thumbnail = thumbnailView.findViewById(R.id.img_thumb);

                try {
                    int resId = Integer.parseInt(imageUri.toString());
                    thumbnail.setImageResource(resId);
                } catch (NumberFormatException e) {
                    thumbnail.setImageURI(imageUri);
                }
                proofThumbnailContainer.addView(thumbnailView);
            }
        }
    }


    protected void updateImageSlider() {
        List<String> imageUrisString = new ArrayList<>();
        for (Uri uri : selectedImages) {
            imageUrisString.add(uri.toString());
        }
        if (imageSliderAdapter == null) {
            imageSliderAdapter = new ImageSliderAdapter(imageUrisString);
            viewPager.setAdapter(imageSliderAdapter);
        } else {
            imageSliderAdapter.setImageUrls(imageUrisString);
        }
    }

    protected void setupStep1Listeners() {
        btnAddImage.setOnClickListener(v -> {
            if (selectedImages.size() < 10) {
                imagePickerLauncher.launch("image/*");
            }
        });

        btnNext1.setOnClickListener(v -> {
            if (validateStep1()) {
                showStep(2);
            }
        });
    }

    protected void setupStep2Listeners() {
        btnBack2.setOnClickListener(v -> showStep(1));
        btnNext2.setOnClickListener(v -> {
            if (validateStep2()) {
                showStep(3);
            }
        });
    }

    protected void setupStep3Listeners() {
        btnBack3.setOnClickListener(v -> showStep(2));
        btnPreview.setOnClickListener(v -> showPreview());
    }

    protected boolean validateStep1() {
        String name = etProductName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
            etProductName.requestFocus();
            return false;
        }
        if (price.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
            etPrice.requestFocus();
            return false;
        }
        if (selectedImages.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    protected boolean validateStep2() {
        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập mô tả sản phẩm", Toast.LENGTH_SHORT).show();
            etDescription.requestFocus();
            return false;
        }
        return true;
    }
    
    protected void showPreview() {
        Product product = collectProductData();
        
        Bundle bundle = new Bundle();
        bundle.putString("productId", "preview");
        bundle.putString("productName", product.getName());
        bundle.putDouble("price", product.getPrice());
        bundle.putInt("quantity", product.getQuantity());
        bundle.putString("description", product.getDescription());
        bundle.putString("source", product.getSource());
        bundle.putString("proof", product.getProof());
        bundle.putString("otherInfo", product.getOtherInfo());
        bundle.putLong("endTime", product.getEndTime());
        
        // --- Ảnh ---
        ArrayList<String> imageUrls = new ArrayList<>();
        for (Uri uri : selectedImages) {
            imageUrls.add(uri.toString());
        }
        bundle.putStringArrayList("imageUrls", imageUrls);
        bundle.putString("productType", productType);
        bundle.putBoolean("isPreview", true);
        
        // --- Brand & Category đã chọn ---
        bundle.putString("brandId", selectedBrandId);
        bundle.putStringArrayList(
          "categoryIds",
          new ArrayList<>(selectedCategoryIds)
        );
        
        // --- TEXT THÔ cho Preview build request ---
        bundle.putString("raw_price", etPrice.getText().toString().trim());
        
        bundle.putString("raw_newPercent",
          etConditionNew.getText().toString().trim());
        bundle.putString("raw_damagePercent",
          etConditionDamage.getText().toString().trim());
        bundle.putString("raw_warranty",
          etWarranty.getText().toString().trim());
        bundle.putBoolean("hasOrigin", hasOrigin);
        bundle.putBoolean("hasReturnPolicy", hasReturnPolicy);
        bundle.putBoolean("hasConditionUsed", hasConditionUsed);
        bundle.putString("raw_originUrl",
          etSourceLink.getText().toString().trim());
        
        Navigation.findNavController(requireView())
          .navigate(getPreviewNavigationAction(), bundle);
    }
    
    
    protected Product collectProductData() {
        Product product = new Product();
        product.setName(etProductName.getText().toString().trim());
        product.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        product.setType(getTypeLabel(productType));
        product.setStatus(productType);
        product.setQuantity(1);
        product.setDescription(etDescription.getText().toString().trim());
        product.setSource(etSourceLink.getText().toString().trim());
        product.setProof(etProof.getText().toString().trim());
        product.setOtherInfo(""); 

        if ("auction".equals(productType)) {
            long twentyFourHoursInMillis = TimeUnit.HOURS.toMillis(24);
            product.setEndTime(System.currentTimeMillis() + twentyFourHoursInMillis);
        }

        List<String> imageUrls = new ArrayList<>();
        for (Uri uri : selectedImages) {
            imageUrls.add(uri.toString());
        }
        product.setImageUrls(imageUrls);
        return product;
    }

    protected String getTypeLabel(String type) {
        switch (type) {
            case "fixed": return "Giá cố định";
            case "negotiable": return "Thương lượng";
            case "auction": return "Đấu giá";
            default: return "";
        }
    }
    
    
    protected void showSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_post_success);
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        ImageView btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView())
                    .navigate(getSuccessNavigationAction());
        });

        dialog.show();
    }
}