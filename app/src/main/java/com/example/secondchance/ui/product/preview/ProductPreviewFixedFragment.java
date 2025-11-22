package com.example.secondchance.ui.product.preview;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.request.ProductCreateRequest;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ImageSliderAdapter;
import com.example.secondchance.util.CloudinaryUploader;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProductPreviewFixedFragment extends Fragment {
    private Dialog loadingDialog;
    private TextView tvProductName, tvPrice, tvType;
    private View contentDescriptionView, contentSourceView, contentProofView, contentOtherView;
    private View badgeType;
    private AppCompatButton btnPublish;
    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private TabLayout tabLayout;
    private LinearLayout btnBack;
    private ArrayList<String> selectedImages = new ArrayList<>();
    private String productId, productName, productType;
    private double price;
    private int quantity;
    private ArrayList<String> categoryIds;
    private String brandId;
    private ArrayList<String> imageUrls;
    private boolean isEditMode;
    private long endTime;
    private String description, source, proof, otherInfo;
    private String rawPriceStr;
    private String rawConditionNote;
    private String rawNewPercent;
    private String rawDamagePercent;
    private String rawWarranty;
    private String rawReturnPolicy;
    private String rawOriginDescription;
    private String rawOriginUrl;
    private boolean hasOrigin;
    private boolean hasReturnPolicy;
    private boolean hasConditionUsed;
    
    protected String getCloudinaryFolder() {
        return "NT118/products";
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId   = getArguments().getString("productId");
            productName = getArguments().getString("productName");
            price       = getArguments().getDouble("price");
            quantity    = getArguments().getInt("quantity");
            productType = getArguments().getString("productType");
            imageUrls   = getArguments().getStringArrayList("imageUrls");
            isEditMode  = getArguments().getBoolean("isEditMode");
            description = getArguments().getString("description");
            source      = getArguments().getString("source");
            proof       = getArguments().getString("proof");
            otherInfo   = getArguments().getString("otherInfo");
            endTime     = getArguments().getLong("endTime");
            
            if (imageUrls != null) {
                selectedImages.clear();
                selectedImages.addAll(imageUrls);
            }
            
            brandId     = getArguments().getString("brandId");
            categoryIds = getArguments().getStringArrayList("categoryIds");
            if (categoryIds == null) categoryIds = new ArrayList<>();
            
            hasOrigin        = getArguments().getBoolean("hasOrigin", false);
            hasReturnPolicy  = getArguments().getBoolean("hasReturnPolicy", false);
            hasConditionUsed  = getArguments().getBoolean("hasConditionUsed", false);
            rawPriceStr          = getArguments().getString("raw_price");
            rawNewPercent        = getArguments().getString("raw_newPercent");
            rawDamagePercent     = getArguments().getString("raw_damagePercent");
            rawWarranty          = getArguments().getString("raw_warranty");
            rawOriginUrl         = getArguments().getString("raw_originUrl");
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
        
        // badgeType hiện vẫn dùng chung view của tv_type (nếu sau này bọc trong CardView thì đổi id riêng cũng được)
        badgeType = view.findViewById(R.id.tv_type);
        
        btnPublish = view.findViewById(R.id.btn_publish);
        btnBack = view.findViewById(R.id.btn_back);
        viewPager = view.findViewById(R.id.imageViewPager);
        layoutIndicators = view.findViewById(R.id.layoutIndicators);
        
        tabLayout = view.findViewById(R.id.tab_layout); // có thể null nếu layout mới bỏ TabLayout
        
        // Các content bây giờ có thể là LinearLayout, nên dùng View
        contentDescriptionView = view.findViewById(R.id.contentDescription);
        contentSourceView = view.findViewById(R.id.contentSource);
        contentProofView = view.findViewById(R.id.contentProof);
        contentOtherView = view.findViewById(R.id.contentOther);
        
        MaterialCardView tabDesc     = view.findViewById(R.id.tabDescription);
        MaterialCardView tabSource   = view.findViewById(R.id.tabSource);
        MaterialCardView tabEvidence = view.findViewById(R.id.tabEvidence);
        MaterialCardView tabOther    = view.findViewById(R.id.tabOther);
        
        LinearLayout contentDesc     = view.findViewById(R.id.contentDescription);
        LinearLayout contentSource   = view.findViewById(R.id.contentSource);
        LinearLayout contentEvidence = view.findViewById(R.id.contentEvidence);
        LinearLayout contentOther    = view.findViewById(R.id.contentOther);
        
        View indDesc     = view.findViewById(R.id.indicatorDescription);
        View indSource   = view.findViewById(R.id.indicatorSource);
        View indEvidence = view.findViewById(R.id.indicatorEvidence);
        View indOther    = view.findViewById(R.id.indicatorOther);

        // Gom mảng
        MaterialCardView[] tabs   = { tabDesc, tabSource, tabEvidence, tabOther };
        LinearLayout[] contents   = { contentDesc, contentSource, contentEvidence, contentOther };
        View[] indicators         = { indDesc, indSource, indEvidence, indOther };
        // Màu
        final int colorSelected      = requireContext().getColor(R.color.highlight4blur);
        final int colorUnselected    = requireContext().getColor(R.color.whiteDay);
        final int indicatorSelected  = requireContext().getColor(R.color.highLight5);
        final int indicatorUnselected= requireContext().getColor(R.color.whiteDay);

        // Listener
        tabDesc.setOnClickListener(v -> selectTab(0, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));
        tabSource.setOnClickListener(v -> selectTab(1, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));
        tabEvidence.setOnClickListener(v -> selectTab(2, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));
        tabOther.setOnClickListener(v -> selectTab(3, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));

        // Chọn mặc định là tab mô tả
        selectTab(0, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected);
    }
    private void selectTab(
      int index,
      MaterialCardView[] tabs,
      LinearLayout[] contents,
      View[] indicators,
      int colorSelected,
      int colorUnselected,
      int indicatorSelected,
      int indicatorUnselected
    ) {
        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == index);
            
            // Tab có thể null nếu id không tồn tại trong layout
            MaterialCardView tab = tabs[i];
            if (tab != null) {
                tab.setCardBackgroundColor(active ? colorSelected : colorUnselected);
            }
            
            // Indicator cũng vậy
            View indicator = indicators[i];
            if (indicator != null) {
                indicator.setBackgroundColor(active ? indicatorSelected : indicatorUnselected);
            }
            
            // ✅ CHỖ GÂY CRASH: phải check null trước khi setVisibility
            LinearLayout content = contents[i];
            if (content != null) {
                content.setVisibility(active ? View.VISIBLE : View.GONE);
            }
        }
    }
    
    private void loadPreviewData() {
        if (tvProductName != null) {
            tvProductName.setText(productName);
        }
        if (tvPrice != null) {
            tvPrice.setText(String.format("đ %,.0f", price));
        }
        if (tvType != null) {
            tvType.setText(getTypeLabel(productType));
        }
        
        // set text vào TextView con bên trong (hoặc chính nó nếu là TextView)
        setContentText(contentDescriptionView, description);
        setContentText(contentSourceView, source);
        setContentText(contentProofView, proof);
        setContentText(contentOtherView, otherInfo);
    }
    
    /**
     * Helper: gán text cho view content.
     * - Nếu view là TextView -> setText
     * - Nếu view là ViewGroup -> tìm TextView con đầu tiên và setText
     */
    private void setContentText(@Nullable View root, @Nullable String text) {
        if (root == null || text == null) return;
        
        if (root instanceof TextView) {
            ((TextView) root).setText(text);
            return;
        }
        
        if (root instanceof ViewGroup) {
            TextView tv = findFirstTextView((ViewGroup) root);
            if (tv != null) {
                tv.setText(text);
            }
        }
    }
    
    private TextView findFirstTextView(ViewGroup parent) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                return (TextView) child;
            } else if (child instanceof ViewGroup) {
                TextView nested = findFirstTextView((ViewGroup) child);
                if (nested != null) return nested;
            }
        }
        return null;
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
        // Nếu layout mới bỏ TabLayout, thì bỏ qua phần này
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
        // Tab mặc định = 0 (Mô tả)
        updateTabContent(0);
    }
    
    private void updateTabContent(int position) {
        if (contentDescriptionView != null) {
            contentDescriptionView.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        }
        if (contentSourceView != null) {
            contentSourceView.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        }
        if (contentProofView != null) {
            contentProofView.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        }
        if (contentOtherView != null) {
            contentOtherView.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
        }
    }
    
    private void applyTypeStyle() {
        if (badgeType == null || tvType == null) return;
        int backgroundColor, textColor;
        switch (productType) {
            case "fixed":
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.lightDay);
                textColor = ContextCompat.getColor(requireContext(), R.color.darkerDay);
                break;
            case "negotiable":
                backgroundColor = Color.parseColor("#FFF9E6");
                textColor = Color.parseColor("#F59E0B");
                break;
            case "auction":
                backgroundColor = Color.parseColor("#FEE2E2");
                textColor = Color.parseColor("#DC2626");
                break;
            default:
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.lightDay);
                textColor = ContextCompat.getColor(requireContext(), R.color.darkerDay);
                break;
        }
        badgeType.setBackgroundColor(backgroundColor);
        tvType.setTextColor(textColor);
    }
    
    private void updateUiForMode() {
        if (isEditMode && btnPublish != null) {
            btnPublish.setText("Cập nhật");
        }
    }
    
    private void setupButtons() {
        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> {
                if (isEditMode) {
                    updateProduct();
                } else {
                    publishProduct();
                }
            });
        }
        
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
    }
    
    private void publishProduct() {
        // 1) Validate nhanh trước khi gọi API
        if (productName == null || productName.trim().isEmpty()
          || description == null || description.trim().isEmpty()
          || price <= 0) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin trước khi đăng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedImages == null || selectedImages.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoadingDialog();
        new Thread(() -> {
            try {
                // 1. Convert List<String> -> List<Uri>
                List<Uri> uriList = new ArrayList<>();
                for (String s : imageUrls) {
                    if (s != null && !s.isEmpty()) {
                        uriList.add(Uri.parse(s));
                    }
                }
                
                // 2. Upload ảnh lên Cloudinary
                List<String> mediaUrls = CloudinaryUploader.uploadImages(
                  requireContext(),
                  uriList,                       // ✅ giờ là List<Uri>
                  getCloudinaryFolder(),         // ví dụ: "NT118/products"
                  RetrofitProvider.cloudinary()
                );
                
                if (mediaUrls == null || mediaUrls.isEmpty()) {
                    hideLoadingDialog();
                    requireActivity().runOnUiThread(() ->
                      Toast.makeText(getContext(), "Upload ảnh thất bại", Toast.LENGTH_LONG).show()
                    );
                    return;
                }
                
                // 3) Build body ProductCreateRequest
                ProductCreateRequest req = buildProductCreateRequest(mediaUrls);
                
                // 4) Gọi API /admin/products
                retrofit2.Response<BasicResponse> res =
                  RetrofitProvider.product().createProduct(req).execute();
                
                if (res.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        hideLoadingDialog();
                        showPostSuccessDialog();
                    });
                } else {
                    hideLoadingDialog();
                    String err = res.errorBody() != null ? res.errorBody().string() : "Unknown error";
                    requireActivity().runOnUiThread(() ->
                      Toast.makeText(getContext(), "Đăng thất bại: " + err, Toast.LENGTH_LONG).show()
                    );
                    Log.e("TAG", "publishProduct: " + err);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                hideLoadingDialog();
                requireActivity().runOnUiThread(() ->
                  Toast.makeText(getContext(), "Lỗi đăng sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                Log.e("TAG", "publishProduct: " + e.getMessage());
            }
        }).start();
    }
    protected ProductCreateRequest buildProductCreateRequest(List<String> mediaUrls) {
        ProductCreateRequest req = new ProductCreateRequest();
        
        // ----- Text thô đã được truyền từ màn add -----
        String name        = safe(productName);
        String desc        = safe(description);
        
        String priceStr    = safe(rawPriceStr);         // Ví dụ "1113"
        String conditionNoteStr = safe(rawConditionNote); // "Chưa sử dụng"
        
        String newPercentStr    = safe(rawNewPercent)
          .replace("%", "")
          .trim();
        String damagePercentStr = safe(rawDamagePercent)
          .replace("%", "")
          .trim();
        String warrantyStr      = safe(rawWarranty)
          .replace("tháng", "")
          .replace(" ", "")
          .trim();
        String returnPolicyStr  = safe(rawReturnPolicy);
        
        String originDescription = safe(rawOriginDescription);
        String originUrl         = safe(rawOriginUrl);
        
        // ----- Field cơ bản -----
        req.productName        = name;
        req.productDescription = desc;
        req.productPrice       = priceStr.isEmpty() ? 0L : Long.parseLong(priceStr);
        req.productUsageTime   = 10;           // theo ví dụ BE
        req.productMedia       = mediaUrls;
        
        // map price type
        int priceType;
        switch (productType) {
            case "fixed":
                priceType = 1; break;
            case "negotiable":
                priceType = 2; break;
            case "auction":
            default:
                priceType = 3; break;
        }
        req.productPriceType = priceType;
        
        // quantity
        req.productQuantity = (quantity > 0) ? quantity : 1;
        
        // Brand & Category (BE hiện lưu 1 category chính)
        req.productBrand = brandId;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            req.productCategory = Collections.singletonList(categoryIds.get(0));
        }
        req.productNewPercent     = parseIntOrNull(newPercentStr);
        req.productDamagePercent  = parseIntOrNull(damagePercentStr);
        req.productWarrantyMonths = parseIntOrNull(warrantyStr);
        
        // có text => coi như có chính sách đổi trả
        req.productReturnPolicy = hasReturnPolicy;
        req.productHasOrigin = hasOrigin;
        req.productConditionNote = hasConditionUsed;
        
        ProductCreateRequest.OriginLink origin = new ProductCreateRequest.OriginLink();
        origin.description = originDescription;
        origin.url         = originUrl;
        req.productOriginLink = origin;
        
        // ----- Auction block nếu là đấu giá -----
        if ("auction".equals(productType)) {
            ProductCreateRequest.ProductAuction auction =
              new ProductCreateRequest.ProductAuction();
            
            long price = priceStr.isEmpty() ? 0L : Long.parseLong(priceStr);
            auction.startingPrice = price;
            
            long now     = System.currentTimeMillis();
            long oneWeek = java.util.concurrent.TimeUnit.DAYS.toMillis(7);
            Instant start = Instant.ofEpochMilli(now);
            Instant end   = Instant.ofEpochMilli(now + oneWeek);
            
            auction.startsAt = start.toString();
            auction.endsAt   = end.toString();
            auction.condition = "used";
            auction.featured  = false;
            
            req.productAution = auction;
        }
        
        // productShopId: BE tự lấy từ token nên không set
        
        return req;
    }
    
    private Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private String safe(String s) {
        return s == null ? "" : s;
    }
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(requireContext());
            loadingDialog.setContentView(R.layout.dialog_loading);
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(
                  new ColorDrawable(Color.TRANSPARENT)
                );
            }
            loadingDialog.setCancelable(false); // không cho bấm ra ngoài để hủy
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }
    
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
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
            Navigation.findNavController(requireView()).navigate(R.id.action_previewFixed_to_productList);
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
            Navigation.findNavController(requireView()).navigate(R.id.action_previewFixed_to_productList);
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
