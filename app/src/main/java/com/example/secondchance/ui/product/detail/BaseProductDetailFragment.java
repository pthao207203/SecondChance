package com.example.secondchance.ui.product.detail;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.AdminProductDetailResponse;
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ImageSliderAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseProductDetailFragment extends Fragment {
    
    // Phần header
    protected TextView tvProductName, tvPrice, tvQuantity;
    protected TextView negotiableIndicator;
    
    // Nội dung tabs
    protected LinearLayout llDescription, llSource, llProof, llOtherInfo;
    
    // Chips “Thông tin chung”
    protected TextView tvConditionNote, tvNewPercent, tvDamagePercent,
      tvOriginStatus, tvWarranty, tvReturnPolicy;
    
    // Seller & giao hàng
    protected TextView tvDeliveryTime, tvShipperName, tvDistance, tvShipperAddress, tvVerified;
    protected ImageView ivAvatar;
    
    // Slider
    protected ViewPager2 viewPager;
    protected LinearLayout layoutIndicators;
    protected TabLayout tabLayout;
    
    // Data
    protected String productId, productType, productName;
    protected ArrayList<String> imageUrls = new ArrayList<>();
    protected float price;
    protected int quantity;
    protected Product currentProduct;
    private AdminProductDetailResponse.AdminProduct detailData;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
        productType = getProductType(); // fixed / negotiable / auction
        productName = getArguments().getString("productName");
        
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        
        View view = inflater.inflate(getLayoutResource(), container, false);
        initViews(view);
        
        // ⭐ Gọi API lấy dữ liệu thật
        fetchProductDetail();
        
        return view;
    }
    
    /** =============================
     *   GỌI API /admin/products/{id}
     *  ============================= */
    private void fetchProductDetail() {
        if (TextUtils.isEmpty(productId)) {
            Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ProductApi api = RetrofitProvider.product();
        
        api.getAdminProductById(productId)
          .enqueue(new Callback<AdminProductDetailResponse>() {
              @Override
              public void onResponse(@NonNull Call<AdminProductDetailResponse> call,
                                     @NonNull Response<AdminProductDetailResponse> response) {
                  if (!isAdded()) return;
                  
                  if (response.isSuccessful()
                    && response.body() != null
                    && response.body().success
                    && response.body().data != null) {
                      
                      detailData = response.body().data;
                      
                      // Map cơ bản sang Product (để tái sử dụng model UI cũ)
                      currentProduct = new Product();
                      currentProduct.setId(detailData.id);
                      currentProduct.setName(detailData.name);
                      currentProduct.setDescription(detailData.description);
                      currentProduct.setPrice(detailData.price);
                      currentProduct.setQuantity(detailData.quantity);
                      
                      if (detailData.media != null) {
                          currentProduct.setImageUrls(new ArrayList<>(detailData.media));
                      } else {
                          currentProduct.setImageUrls(new ArrayList<>());
                      }
                      
                      price = (float) detailData.price;
                      quantity = detailData.quantity;
                      imageUrls = new ArrayList<>(currentProduct.getImageUrls());
                      
                      // ⭐ BIND UI
                      loadProductData();      // tên, giá, số lượng, mô tả
                      bindExtraInfo();        // chips, source, proof, seller, giao hàng
                      setupNegotiableIndicator(); // style chip “Thương lượng / Giá cố định / Đấu giá”
                      setupImageSlider();     // slider + indicators
                      setupTabLayout();       // tabs mô tả / nguồn / minh chứng / khác
                      
                  } else {
                      Toast.makeText(getContext(),
                        "Lỗi server: " + response.code(),
                        Toast.LENGTH_SHORT).show();
                  }
              }
              
              @Override
              public void onFailure(@NonNull Call<AdminProductDetailResponse> call,
                                    @NonNull Throwable t) {
                  if (!isAdded()) return;
                  Toast.makeText(getContext(),
                    "Lỗi kết nối: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
                  t.printStackTrace();
              }
          });
    }
    
    /** Bind phần “thông tin chung”, source, proof, seller… */
    private void bindExtraInfo() {
        if (detailData == null) return;
        
        // ===== Chips THÔNG TIN CHUNG =====
        if (tvConditionNote != null) {
            String note = !TextUtils.isEmpty(detailData.conditionNote)
              ? detailData.conditionNote
              : "Không rõ tình trạng";
            tvConditionNote.setText(note);
        }
        
        if (tvNewPercent != null) {
            Integer p = detailData.newPercent;
            tvNewPercent.setText(p != null
              ? String.format(Locale.getDefault(), "Mới %d%%", p)
              : "Mới (chưa rõ)");
        }
        
        if (tvDamagePercent != null) {
            Integer d = detailData.damagePercent;
            tvDamagePercent.setText(d != null
              ? String.format(Locale.getDefault(), "Hỏng %d%%", d)
              : "Hỏng (chưa rõ)");
        }
        
        if (tvOriginStatus != null) {
            Boolean hasOrigin = detailData.hasOrigin;
            tvOriginStatus.setText(Boolean.TRUE.equals(hasOrigin)
              ? "Nguồn uy tín"
              : "Nguồn chưa xác thực");
        }
        
        if (tvWarranty != null) {
            Integer m = detailData.warrantyMonths;
            if (m != null && m > 0) {
                tvWarranty.setText("Bảo hành " + m + " tháng");
            } else {
                tvWarranty.setText("Không bảo hành");
            }
        }
        
        if (tvReturnPolicy != null) {
            Boolean canReturn = detailData.returnPolicy;
            tvReturnPolicy.setText(Boolean.TRUE.equals(canReturn)
              ? "Có đổi trả"
              : "Không đổi trả");
        }
        
        // ===== Tabs: Nguồn & Minh chứng & Khác =====
        addText(llSource, detailData.originUrl != null ? detailData.originUrl : "Chưa có nguồn gốc");
        
        if (llProof != null) llProof.removeAllViews();
        if (detailData.originProof != null && detailData.originProof.images != null) {
            for (String img : detailData.originProof.images) {
                addText(llProof, img);
            }
        } else {
            addText(llProof, "Chưa có minh chứng");
        }
        
        addText(llOtherInfo, "Không có dữ liệu khác");
        
        
        // ===== Seller / giao hàng =====
        if (detailData.seller != null) {
            if (tvShipperName != null) {
                String name = !TextUtils.isEmpty(detailData.seller.shopName)
                  ? detailData.seller.shopName
                  : detailData.seller.userName;
                tvShipperName.setText(name);
            }
            
            if (ivAvatar != null && !TextUtils.isEmpty(detailData.seller.userAvatar)) {
                Glide.with(this)
                  .load(detailData.seller.userAvatar)
                  .placeholder(R.drawable.avatar1)
                  .error(R.drawable.avatar1)
                  .into(ivAvatar);
            }
        }
        
        // Tạm thời hard-code giống màn cũ, sau này BE trả thì sửa
        if (tvDeliveryTime != null) {
            tvDeliveryTime.setText("Khoảng 3 ngày kể từ ngày đặt");
        }
        if (tvShipperAddress != null) {
            tvShipperAddress.setText("Sản phẩm được giao từ Quận 1, TP. HCM, Việt Nam.");
        }
        if (tvDistance != null) {
            tvDistance.setText("Cách bạn 13km");
        }
        
        if (tvVerified != null) {
            tvVerified.setText(Boolean.TRUE.equals(detailData.hasOrigin)
              ? "Đã xác thực"
              : "Chưa xác thực");
        }
    }
    
    protected int getLayoutResource() {
        return R.layout.fragment_product_detail_fixed;
    }
    
    /** fixed / negotiable / auction */
    protected abstract String getProductType();
    
    // ================== INIT VIEW ==================
    
    protected void initViews(View view) {
        tvProductName = view.findViewById(R.id.textViewTitle);
        tvPrice = view.findViewById(R.id.price_value);
        tvQuantity = view.findViewById(R.id.product_quantity);
        negotiableIndicator = view.findViewById(R.id.tv_negotiable_indicator);
        
        viewPager = view.findViewById(R.id.viewPagerImages);
        layoutIndicators = view.findViewById(R.id.dots_indicator_container);
        
        llDescription = view.findViewById(R.id.contentDescription);
        llSource      = view.findViewById(R.id.contentSource);
        llProof = view.findViewById(R.id.contentEvidence);
        llOtherInfo   = view.findViewById(R.id.contentOther);
        
        
        tvConditionNote = view.findViewById(R.id.tvConditionNote);
        tvNewPercent = view.findViewById(R.id.tvNewPercent);
        tvDamagePercent = view.findViewById(R.id.tvDamagePercent);
        tvOriginStatus = view.findViewById(R.id.tvSource);
        tvWarranty = view.findViewById(R.id.tvWarranty);
        tvReturnPolicy = view.findViewById(R.id.tvReturn);
        
        tvDeliveryTime = view.findViewById(R.id.tvDeliveryTime);
        tvShipperName = view.findViewById(R.id.tvShipperName);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvShipperAddress = view.findViewById(R.id.tvShipperAddress);
        tvVerified = view.findViewById(R.id.tvVerified);
        ivAvatar = view.findViewById(R.id.ivAvatar);
    }
    private void addText(LinearLayout parent, String text) {
        if (parent == null || text == null) return;
        
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkerDay));
        
        parent.addView(tv);
    }
    
    
    
    
    /** ================== BIND HEADER ================== */
    
    protected void loadProductData() {
        if (currentProduct == null) return;
        
        tvProductName.setText(currentProduct.getName());
        
        // Format giá giống màn cũ (1.000.000)
        try {
            tvPrice.setText(String.format(Locale.getDefault(), "%,.0f", currentProduct.getPrice()));
        } catch (Exception e) {
            tvPrice.setText(String.valueOf(currentProduct.getPrice()));
        }
        
        tvQuantity.setText("Số lượng: " + currentProduct.getQuantity());
        
        llDescription.removeAllViews();
        addText(llDescription, currentProduct.getDescription());
        
        // Nếu BE có label priceType, hiển thị luôn trên chip
        if (negotiableIndicator != null && detailData != null
          && !TextUtils.isEmpty(detailData.priceTypeLabel)) {
            negotiableIndicator.setText(detailData.priceTypeLabel);
        }
    }
    
    /** ================== SLIDER ẢNH ================== */
    private void setupImageSlider() {
        if (viewPager == null) return;
        
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        
        ImageSliderAdapter adapter = new ImageSliderAdapter(imageUrls);
        viewPager.setAdapter(adapter);
        
        setupIndicators(imageUrls.size());
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setCurrentIndicator(position);
            }
        });
    }
    
    private void setupIndicators(int count) {
        layoutIndicators.removeAllViews();
        if (count <= 0) return;
        
        for (int i = 0; i < count; i++) {
            ImageView img = new ImageView(getContext());
            img.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.indicator_inactive));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(8, 0, 8, 0);
            img.setLayoutParams(lp);
            layoutIndicators.addView(img);
        }
        
        setCurrentIndicator(0);
    }
    
    private void setCurrentIndicator(int index) {
        int child = layoutIndicators.getChildCount();
        for (int i = 0; i < child; i++) {
            ImageView img = (ImageView) layoutIndicators.getChildAt(i);
            img.setImageDrawable(ContextCompat.getDrawable(requireContext(),
              i == index ? R.drawable.indicator_active : R.drawable.indicator_inactive));
        }
    }
    
    /** ================== PRICE TYPE CHIP ================== */
    private void setupNegotiableIndicator() {
        if (negotiableIndicator == null) return;
        
        // Mặc định màu xám
        int bgRes = R.drawable.bg_gray_solid;
        
        if ("negotiable".equals(productType)) {
            bgRes = R.drawable.bg_indicator_negotiable_active;
        }
        // Nếu sau này có fragment Auction, bạn có thể thêm style riêng ở đây
        
        negotiableIndicator.setBackgroundResource(bgRes);
    }
    
    /** ================== TABS ================== */
    private void setupTabLayout() {
        if (tabLayout == null) return;
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                updateTabContent(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        updateTabContent(0);
    }
    
    private void updateTabContent(int position) {
        if (llDescription != null)
            llDescription.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        if (llSource != null)
            llSource.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        if (llProof != null)
            llProof.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        if (llOtherInfo != null)
            llOtherInfo.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
    }
}
