package com.example.secondchance.ui.product.preview;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ImageSliderAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ProductPreviewAuctionFragment extends Fragment {

    private TextView tvProductName, tvPrice, tvType, tvDescription, tvSource, tvProof, tvOtherInfo;
    private View badgeType;
    private AppCompatButton btnPublish;
    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private TabLayout tabLayout;
    private LinearLayout btnBack;

    private String productId, productName, productType;
    private double price;
    private int quantity;
    private ArrayList<String> imageUrls;
    private boolean isEditMode;
    private long endTime;
    private String description, source, proof, otherInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            productName = getArguments().getString("productName");
            price = getArguments().getDouble("price");
            quantity = getArguments().getInt("quantity");
            productType = getArguments().getString("productType");
            imageUrls = getArguments().getStringArrayList("imageUrls");
            isEditMode = getArguments().getBoolean("isEditMode");
            description = getArguments().getString("description");
            source = getArguments().getString("source");
            proof = getArguments().getString("proof");
            otherInfo = getArguments().getString("otherInfo");
            endTime = getArguments().getLong("endTime");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_fixed_preview, container, false);

        initViews(view);
        loadPreviewData();
        applyTypeStyle();
        setupButtons();
        setupImageSlider();
        updateUiForMode();
        setupTabLayout();

        return view;
    }

    private void initViews(View view) {
        tvProductName = view.findViewById(R.id.tv_product_name);
        tvPrice = view.findViewById(R.id.tv_price);
        tvType = view.findViewById(R.id.tv_type);
        badgeType = view.findViewById(R.id.tv_type);
        btnPublish = view.findViewById(R.id.btn_publish);
        btnBack = view.findViewById(R.id.btn_back);
        viewPager = view.findViewById(R.id.imageViewPager);
        layoutIndicators = view.findViewById(R.id.layoutIndicators);
        tabLayout = view.findViewById(R.id.tab_layout);
        tvDescription = view.findViewById(R.id.contentDescription);
        tvSource = view.findViewById(R.id.contentSource);
        tvProof = view.findViewById(R.id.contentProof);
        tvOtherInfo = view.findViewById(R.id.contentOther);
    }

    private void loadPreviewData() {
        tvProductName.setText(productName);
        tvPrice.setText(String.format("đ %,.0f", price));
        tvType.setText(getTypeLabel(productType));
        tvDescription.setText(description);
        tvSource.setText(source);
        tvProof.setText(proof);
        tvOtherInfo.setText(otherInfo);
    }

    private void setupImageSlider() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(imageUrls);
            viewPager.setAdapter(adapter);
            setupIndicators(imageUrls.size());

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    setCurrentIndicator(position);
                }
            });
        }
    }

    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicators.addView(indicators[i]);
        }
        if (indicators.length > 0) {
            indicators[0].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active));
        }
    }

    private void setCurrentIndicator(int index) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = layoutIndicators.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView imageView = (ImageView) child;
                if (i == index) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active));
                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive));
                }
            }
        }
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabContent(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        updateTabContent(0);
    }

    private void updateTabContent(int position) {
        tvDescription.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        tvSource.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        tvProof.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        tvOtherInfo.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
    }

    private void applyTypeStyle() {
        if (badgeType == null || tvType == null) return;
        int backgroundColor = Color.parseColor("#FEE2E2");
        int textColor = Color.parseColor("#DC2626");
        badgeType.setBackgroundColor(backgroundColor);
        tvType.setTextColor(textColor);
    }

    private void updateUiForMode() {
        if (isEditMode) {
            btnPublish.setText("Cập nhật");
        }
    }

    private void setupButtons() {
        btnPublish.setOnClickListener(v -> {
            if (isEditMode) {
                updateProduct();
            } else {
                publishProduct();
            }
        });

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    private void publishProduct() {
        Product product = new Product();
        product.setName(productName);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setImageUrls(imageUrls);
        product.setType(getTypeLabel(productType));
        product.setStatus(productType);
        product.setDescription(description);
        product.setSource(source);
        product.setProof(proof);
        product.setOtherInfo(otherInfo);
        if ("auction".equals(productType)) {
            product.setEndTime(this.endTime);
        }
        SampleProductData.addProduct(product);
        showPostSuccessDialog();
    }

    private void updateProduct() {
        Product productToUpdate = SampleProductData.getProductById(productId);
        if (productToUpdate != null) {
            productToUpdate.setName(productName);
            productToUpdate.setPrice(price);
            productToUpdate.setQuantity(quantity);
            productToUpdate.setImageUrls(imageUrls);
            productToUpdate.setType(getTypeLabel(productType));
            productToUpdate.setStatus(productType);
            productToUpdate.setDescription(description);
            productToUpdate.setSource(source);
            productToUpdate.setProof(proof);
            productToUpdate.setOtherInfo(otherInfo);
            if ("auction".equals(productType)) {
                productToUpdate.setEndTime(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));
            }
            SampleProductData.updateProduct(productToUpdate);
        }
        showUpdateSuccessDialog();
    }


    private void showPostSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_post_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).navigate(R.id.action_previewAuction_to_productList);
        });

        dialog.show();
    }

    private void showUpdateSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_update_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView btnClose = dialog.findViewById(R.id.btnCloseSuccess);
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).navigate(R.id.action_previewAuction_to_productList);
        });

        dialog.show();
    }

    private String getTypeLabel(String type) {
        if (type == null) return "";
        switch (type) {
            case "fixed": return "Giá cố định";
            case "negotiable": return "Thương lượng";
            case "auction": return "Đấu giá";
            default: return "";
        }
    }
}