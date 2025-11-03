package com.example.secondchance.ui.product.add;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ImageSliderAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        etConditionUsed = view.findViewById(R.id.et_condition_used);
        etConditionNew = view.findViewById(R.id.et_condition_new);
        etConditionDamage = view.findViewById(R.id.et_condition_damage);
        etWarranty = view.findViewById(R.id.et_warranty);
        etReturnPolicy = view.findViewById(R.id.et_return_policy);
        etSource = view.findViewById(R.id.et_source);
        spinnerType = view.findViewById(R.id.spinner_type);
        btnNext1 = view.findViewById(R.id.btn_next1);

        setupSpinner();
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

        ArrayList<String> imageUrls = new ArrayList<>();
        for (Uri uri : selectedImages) {
            imageUrls.add(uri.toString());
        }
        bundle.putStringArrayList("imageUrls", imageUrls);
        bundle.putString("productType", productType);
        bundle.putBoolean("isPreview", true);

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

    public void publishProduct() {
        Product product = collectProductData();
        SampleProductData.addProduct(product);
        Toast.makeText(getContext(), "Đang lưu sản phẩm...", Toast.LENGTH_SHORT).show();
        showSuccessDialog();
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