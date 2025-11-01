package com.example.secondchance.ui.product.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ImageSliderAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public abstract class BaseProductDetailFragment extends Fragment {

    protected TextView tvProductName, tvPrice, tvQuantity;
    protected TextView tvDescription, tvSource, tvProof, tvOtherInfo;
    protected TextView negotiableIndicator;
    protected ViewPager2 viewPager;
    protected LinearLayout layoutIndicators;
    protected TabLayout tabLayout;

    protected String productId, productName, productType;
    protected ArrayList<String> imageUrls;
    protected float price;
    protected int quantity;
    protected Product currentProduct;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId", "");
            currentProduct = SampleProductData.getProductById(productId);

            if (currentProduct != null) {
                productName = currentProduct.getName();
                price = (float) currentProduct.getPrice();
                quantity = currentProduct.getQuantity();
                imageUrls = new ArrayList<>(currentProduct.getImageUrls());
                productType = getProductType();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResource(), container, false);

        initViews(view);
        if (currentProduct != null) {
            loadProductData();
            setupNegotiableIndicator();
            setupImageSlider();
            setupTabLayout();
        }

        return view;
    }

    protected int getLayoutResource() {
        return R.layout.fragment_product_detail_fixed;
    }

    protected abstract String getProductType();

    protected void initViews(View view) {
        tvProductName = view.findViewById(R.id.tv_product_name);
        tvPrice = view.findViewById(R.id.tv_price);
        tvQuantity = view.findViewById(R.id.tv_quantity);
        negotiableIndicator = view.findViewById(R.id.tv_negotiable_indicator);
        viewPager = view.findViewById(R.id.imageViewPager);
        layoutIndicators = view.findViewById(R.id.layoutIndicators);
        tabLayout = view.findViewById(R.id.tab_layout);
        tvDescription = view.findViewById(R.id.contentDescription);
        tvSource = view.findViewById(R.id.contentSource);
        tvProof = view.findViewById(R.id.contentProof);
        tvOtherInfo = view.findViewById(R.id.contentOther);
    }

    protected void loadProductData() {
        if (tvProductName != null) tvProductName.setText(productName);
        if (tvPrice != null) tvPrice.setText(String.format("%,.0f", price));
        if (tvQuantity != null) tvQuantity.setText("Số lượng: " + quantity);
        if (negotiableIndicator != null) negotiableIndicator.setText("Thương lượng");

        if (currentProduct == null) return;
        if (tvDescription != null) tvDescription.setText(currentProduct.getDescription());
        if (tvSource != null) tvSource.setText(currentProduct.getSource());
        if (tvProof != null) tvProof.setText(currentProduct.getProof());
        if (tvOtherInfo != null) tvOtherInfo.setText(currentProduct.getOtherInfo());
    }

    private void setupImageSlider() {
        if (viewPager != null && imageUrls != null && !imageUrls.isEmpty()) {
            ImageSliderAdapter adapter = new ImageSliderAdapter(imageUrls);
            viewPager.setAdapter(adapter);

            if (layoutIndicators != null) {
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
    }

    private void setupIndicators(int count) {
        if (getContext() == null || layoutIndicators == null) return;
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
            View child = layoutIndicators.getChildAt(0);
            if (child instanceof ImageView) {
                ((ImageView) child).setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active));
            }
        }
    }

    private void setCurrentIndicator(int index) {
        if (layoutIndicators == null) return;
        for (int i = 0; i < layoutIndicators.getChildCount(); i++) {
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

    private void setupNegotiableIndicator() {
        if (negotiableIndicator == null) return;
        if ("negotiable".equals(productType)) {
            negotiableIndicator.setBackgroundResource(R.drawable.bg_indicator_negotiable_active);
        } else {
            negotiableIndicator.setBackgroundResource(R.drawable.bg_gray_solid);
        }
    }

    private void setupTabLayout() {
        if (tabLayout == null) return;
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
        if (tvDescription != null) tvDescription.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        if (tvSource != null) tvSource.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        if (tvProof != null) tvProof.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        if (tvOtherInfo != null) tvOtherInfo.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
    }

    protected String getTypeLabel(String type) {
        if (type == null) return "";
        switch (type) {
            case "fixed": return "Giá cố định";
            case "negotiable": return "Thương lượng";
            case "auction": return "Đấu giá";
            case "deleted": return "Đã xóa";
            default: return "";
        }
    }
}