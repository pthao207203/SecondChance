package com.example.secondchance.ui.card;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.data.repo.CartRepository;
import com.example.secondchance.databinding.FragmentDetailProductBinding;
import com.example.secondchance.dto.response.ProductDetailResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailProductFragment extends Fragment {
    
    private FragmentDetailProductBinding binding;
    
    // Slider
    private ViewPager2 viewPagerImages;
    private ImageUrlSliderAdapter urlAdapter;
    
    // Dots động
    private LinearLayout dotsContainer;
    private final List<MaterialCardView> dots = new ArrayList<>();
    private int colorActive;
    private int colorInactive;
    
    // Auto scroll
    private Handler sliderHandler;
    private Runnable sliderRunnable;
    private static final long SLIDE_DELAY_MS = 2500;
    
    // API
    private HomeApi homeApi;
    private CountDownTimer auctionTimer;

    // Thêm các biến cần thiết
    private String currentProductId;
    private boolean isAddingToCart = false;

    // PageChangeCallback để unregister khi destroy
    private final ViewPager2.OnPageChangeCallback pageCallback = new ViewPager2.OnPageChangeCallback() {
        @Override public void onPageSelected(int position) {
            super.onPageSelected(position);
            int count = (urlAdapter != null) ? urlAdapter.getItemCount() : 0;
            if (count > 0) updateDots(position % count);
            
            // reset auto-scroll
            if (sliderHandler != null && sliderRunnable != null) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
            }
        }
    };
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nút hành động cơ bản
        binding.textBuyNow.setText("MUA NGAY");
        binding.cardBuyNow.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.normalDay));
        binding.iconcart.setImageResource(R.drawable.shopping_cart_02);
        binding.textAddToCart.setText("Thêm vào giỏ hàng");
        binding.cardAddToCart.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.highlight4blur));
        binding.cardNegotiation.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grayDay));
        
        // Màu dot
        colorActive   = ContextCompat.getColor(requireContext(), R.color.darkerDay);
        colorInactive = ContextCompat.getColor(requireContext(), R.color.lighterDay);
        
        // Ánh xạ qua binding (TRÁNH findViewById null)
        viewPagerImages = binding.viewPagerImages;
        dotsContainer   = binding.dotsIndicatorContainer;
        
        // Adapter cho slider
        urlAdapter = new ImageUrlSliderAdapter();
        viewPagerImages.setAdapter(urlAdapter);
        viewPagerImages.unregisterOnPageChangeCallback(pageCallback);
        viewPagerImages.registerOnPageChangeCallback(pageCallback);
        
        // Auto slider
        setupAutoSlider();
        
        // API
        homeApi = RetrofitProvider.home();
        String productIdArg = getArguments() != null ? getArguments().getString("productId") : null;
        if (productIdArg == null || productIdArg.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }
        this.currentProductId = productIdArg; // Lưu ID sản phẩm hiện tại
        fetchAndShowProduct(productIdArg);
        
        // Gán listener cho nút "Thêm vào giỏ hàng"
        binding.cardAddToCart.setOnClickListener(v -> addToCart());

        // Avatar seller (giữ ví dụ đơn giản)
        binding.shopAvatar.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("shopName", "Cá Biết Bay");
            Navigation.findNavController(v).navigate(R.id.action_detail_product_to_shop_home, b);
        });
        
        // 1) Lấy view
        MaterialCardView tabDesc = binding.tabDescription;
        MaterialCardView tabSource = binding.tabSource;
        MaterialCardView tabEvidence = binding.tabEvidence;
        MaterialCardView tabOther = binding.tabOther;
        
        LinearLayout contentDesc = binding.contentDescription;
        LinearLayout contentSource = binding.contentSource;
        LinearLayout contentEvidence = binding.contentEvidence;
        LinearLayout contentOther = binding.contentOther;
        
        View indDesc = binding.indicatorDescription;
        View indSource = binding.indicatorSource;
        View indEvidence = binding.indicatorEvidence;
        View indOther = binding.indicatorOther;

// 2) Gom theo mảng cho tiện set
        MaterialCardView[] tabs = new MaterialCardView[] { tabDesc, tabSource, tabEvidence, tabOther };
        LinearLayout[] contents = new LinearLayout[] { contentDesc, contentSource, contentEvidence, contentOther };
        View[] indicators = new View[] { indDesc, indSource, indEvidence, indOther };

// 3) Hàm áp style chọn/không chọn
        final int colorSelected = requireContext().getColor(R.color.highlight4blur);
        final int colorUnselected = requireContext().getColor(R.color.whiteDay);
        final int indicatorSelected = requireContext().getColor(R.color.highLight5);
        final int indicatorUnselected = requireContext().getColor(R.color.whiteDay);
        
        Runnable selectDefault = () -> selectTab(0, tabs, contents, indicators,
          colorSelected, colorUnselected, indicatorSelected, indicatorUnselected);

// 4) Click listeners
        tabDesc.setOnClickListener(v -> selectTab(0, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));
        tabSource.setOnClickListener(v -> selectTab(1, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));
        tabEvidence.setOnClickListener(v -> selectTab(2, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));
        tabOther.setOnClickListener(v -> selectTab(3, tabs, contents, indicators, colorSelected, colorUnselected, indicatorSelected, indicatorUnselected));

// 5) Chọn mặc định: Mô tả
        selectDefault.run();
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
            tabs[i].setCardBackgroundColor(active ? colorSelected : colorUnselected);
            indicators[i].setBackgroundColor(active ? indicatorSelected : indicatorUnselected);
            contents[i].setVisibility(active ? View.VISIBLE : View.GONE);
        }
    }
    
    /** Auto scroll setup */
    private void setupAutoSlider() {
        sliderHandler = new Handler(Looper.getMainLooper());
        sliderRunnable = () -> {
            if (viewPagerImages == null) return;
            int next = viewPagerImages.getCurrentItem() + 1;
            viewPagerImages.setCurrentItem(next, true);
            sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
        };
    }
    
    @Override public void onResume() {
        super.onResume();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
        }
    }
    
    @Override public void onPause() {
        super.onPause();
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }
    
    @Override public void onDestroyView() {
        super.onDestroyView();
        stopAuctionCountdown();
        if (viewPagerImages != null) viewPagerImages.unregisterOnPageChangeCallback(pageCallback);
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
        binding = null;
    }

    // =================== LOGIC THÊM VÀO GIỎ HÀNG ===================
    private void addToCart() {
        if (isAddingToCart) return;
        if (TextUtils.isEmpty(currentProductId)) {
            Toast.makeText(getContext(), "Lỗi: Mã sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        isAddingToCart = true;
        binding.cardAddToCart.setEnabled(false);

        CartRepository.getInstance().addToCart(currentProductId, 1, new CartRepository.CartCallback() {
            @Override
            public void onSuccess(List<CartApi.CartItem> items) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    isAddingToCart = false;
                    showAddToCartSuccessDialog(); // HIỂN THỊ DIALOG
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    isAddingToCart = false;
                    binding.cardAddToCart.setEnabled(true);
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showAddToCartSuccessDialog() {
        if (!isAdded()) return;
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_to_cart_success);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().dimAmount = 0.6f;
        }

        ImageView btnClose = dialog.findViewById(R.id.btnCloseSuccess);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // =================== API ===================
    
    private void fetchAndShowProduct(String productId) {
        showLoading(true);
        homeApi.getProductDetail(productId).enqueue(new Callback<ProductDetailResponse>() {
            @Override public void onResponse(@NonNull Call<ProductDetailResponse> call,
                                             @NonNull Response<ProductDetailResponse> res) {
                showLoading(false);
                if (!res.isSuccessful() || res.body() == null || !res.body().success || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
                    return;
                }
                Gson gson = new Gson();
                String json = gson.toJson(res.body().data);
                Log.d("DetailProductFragment", "onResponse: " + json);
                bindUi(res.body().data);
            }
            
            @Override public void onFailure(@NonNull Call<ProductDetailResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    private void bindUi(ProductDetailResponse.Data p) {
        if (!isAdded() || binding == null) return;
        
        // Texts
        binding.textViewTitle.setText(safe(p.name));
        binding.productQuantity.setText("Số lượng: " + p.quantity);
        binding.priceCurrency.setText(p.currency != null && p.currency.equalsIgnoreCase("VND") ? "đ" : safe(p.currency));
        binding.priceValue.setText(formatVnd(p.price));
        binding.ratingValue.setText(String.valueOf(p.seller.userRate));
        switch (p.priceType) {
            case 1:
                // 1 = Giá cố định
                // Giữ nguyên như mặc định
//                Log.d("DetailProductFragment", "bindUi: Giá cố định");
                break;
            
            case 2:
                // 2 = Thương lượng nổi bật: “khung vàng chữ đen”
                //   - Chip thương lượng: viền vàng, nền trắng, chữ đen
                binding.cardNegotiation.setCardBackgroundColor(
                  ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.highlight3blur))
                );
                // Text bên trong chip thương lượng:
                binding.tvNegotiation.setTextColor(
                  ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.darkerDay))
                );
//                Log.d("DetailProductFragment", "bindUi: Thương lượng nổi bật");
                break;
            
            case 3:
                // 3 = Đấu giá
                //   - Chip thương lượng: giữ như hiện tại (mặc định ở trên)
                //   - Nút “MUA NGAY” thành “ĐẤU GIÁ”
                startAuctionCountdown(p.auctionEndsAt);
                binding.textBuyNow.setText("ĐẤU GIÁ");
//                Log.d("DetailProductFragment", "bindUi: Đấu giá");
                break;
            
            default:
                // Không xác định -> giữ mặc định
                break;
        }
        
        binding.contentDescription.removeAllViews();
        binding.contentSource.removeAllViews();
        binding.contentEvidence.removeAllViews();
        binding.contentOther.removeAllViews();
        
        // Description
        if (!TextUtils.isEmpty(p.description)) {
            addMutedText(binding.contentDescription, p.description);
        }

        // Source
        if (p.hasOrigin) {
            addMutedText(binding.contentSource, safe(p.originLink.description));
        } else {
            addMutedText(binding.contentSource, "Chưa có nguồn gốc xác minh.");
        }

        // Evidence
        if (p.originLink != null && p.originLink.url != null && !p.originLink.url.isEmpty()) {
            addThumbRow(binding.contentEvidence, p.originLink.url);
        } else {
            addMutedText(binding.contentEvidence, "Chưa có ảnh minh chứng.");
        }

        // Other
        addMutedText(binding.contentOther, "Không có dữ liệu");
        
        Gson gson = new Gson();
        String json = gson.toJson(p);
        Log.d("DetailProductFragment", "bindUi: " + json);
        // Seller avatar
        if (p.seller != null) {
            Glide.with(binding.shopAvatar)
              .load(p.seller.userAvatar)
              .placeholder(R.drawable.avatar1)
              .error(R.drawable.avatar1)
              .into(binding.shopAvatar);
            
            String sellerName = p.seller.shopName ;
            binding.tvSellerName.setText(sellerName != null ? sellerName : "Người bán");
            
            binding.sellerInfoContainer.setVisibility(View.VISIBLE);
            binding.customerReviewCard.setVisibility(View.VISIBLE);
            
            // 2) Avatar shop
            Glide.with(this)
              .load(p.seller.firstComment.byUser.avatar)
              .placeholder(R.drawable.avatar1)
              .error(R.drawable.avatar1)
              .into(binding.ivCustomerAvatar);
            
            // 3) Tên hiển thị: ưu tiên seller.shopName, fallback byUser.name
            String displayName = p.seller.firstComment.byUser.name;
            binding.tvCustomerName.setText(!TextUtils.isEmpty(displayName) ? displayName : "Người mua");
            
            // 4) Ngày comment (nếu có createdAt trong firstComment)
            String dateText = "";
            if (p.seller.firstComment != null && !TextUtils.isEmpty(p.seller.firstComment.createdAt)) {
                dateText = formatDateVN(p.seller.firstComment.createdAt);
            } else if (!TextUtils.isEmpty(p.createdAt)) {
                dateText = formatDateVN(p.createdAt);
            }
            binding.tvCommentDate.setText(!TextUtils.isEmpty(dateText) ? dateText : "");
            
            // 5) Subtitle tuỳ chọn (có thể dùng conditionNote)
//            binding.tvCustomerSubtitle.setText(safe(p.conditionNote));
            
            // 6) Rating
            double rating = 0.0;
            if (p.seller.firstComment != null) {
                // Nếu BE trả rate dạng 1..5 thì dùng trực tiếp; nếu 0..50 (×10) thì chia 10.0
                rating = p.seller.firstComment.rate;
                if (rating > 5) rating = rating / 10.0;
            }
            binding.tvCustomerRating.setText(rating > 0 ? String.format(java.util.Locale.US, "%.1f", rating) : "—");
            
            // 7) Nội dung review
            String reviewText = (p.seller.firstComment != null) ? p.seller.firstComment.description : null;
            binding.tvReviewText.setText(!TextUtils.isEmpty(reviewText) ? reviewText : "Chưa có nhận xét.");
            
            // 8) Thumbnails: clear cũ rồi thêm mới
            binding.llReviewThumbs.removeAllViews();
            if (p.seller.firstComment != null && p.seller.firstComment.media != null && !p.seller.firstComment.media.isEmpty()) {
                addThumbRow(binding.llReviewThumbs, p.seller.firstComment.media);
            }
        } else {
            binding.customerReviewCard.setVisibility(View.GONE);
        }
        
        // Ảnh slider từ BE
        List<String> media = (p.media != null) ? p.media : new ArrayList<>();
        urlAdapter.setData(media);
        
        // COMMON INFO (Thông tin chung)
        setChip(binding.mcvConditionNote, binding.tvConditionNote, (p.conditionNote));                     // "Chưa sử dụng"
        setChip(binding.mcvNewPercent,   binding.tvNewPercent,   (p.newPercent != null ? "Mới " + pct(p.newPercent) : null)); // "Mới 99%"
        setChip(binding.mcvDamagePercent,binding.tvDamagePercent,(p.damagePercent != null ? "Hỏng " + pct(p.damagePercent) : null)); // "Hỏng 0%"

        setChip(binding.mcvSource, binding.tvSource, null);

        // Bảo hành
        setChip(binding.mcvWarranty, binding.tvWarranty,
          p.warrantyMonths != null ? ("Bảo hành " + months(p.warrantyMonths)) : null);

        // Đổi trả (BE chưa trả field → ẩn nếu không có)
        setChip(binding.mcvReturn, binding.tvReturn, null);
        
        // Dựng dot theo số ảnh
        buildDots(media.size());
        updateDots(0); // an toàn vì đã build dots
        viewPagerImages.setCurrentItem(0, false);

        // === NÚT ĐẤU GIÁ ===
        binding.cardBuyNow.setOnClickListener(v -> {
            if (p.priceType == 3) {
                Bundle bundle = new Bundle();
                bundle.putString("productId", p.id);
                Navigation.findNavController(v).navigate(R.id.navigation_rule_auction, bundle);
            } else {
                // TODO: Chuyển sang trang mua ngay
                Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        // === NÚT THƯƠNG LƯỢNG ===
        binding.cardNegotiation.setOnClickListener(v -> {
            if (p.priceType == 2) {
                showNegotiationDialog(p);
            }
        });
    }
    private void showNegotiationDialog(ProductDetailResponse.Data product) {
        Dialog inputDialog = new Dialog(requireContext());
        inputDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputDialog.setContentView(R.layout.dialog_input_negotiation);
        inputDialog.setCancelable(true);

        TextView tvOriginalPrice = inputDialog.findViewById(R.id.tvOriginalPrice);
        EditText etPrice = inputDialog.findViewById(R.id.etNegotiationPrice);
        EditText etReason = inputDialog.findViewById(R.id.etReason);
        MaterialButton btnSend = inputDialog.findViewById(R.id.btnRegisterSeller);
        ImageView btnClose = inputDialog.findViewById(R.id.btnCloseSuccess);

        double originalPrice = Double.parseDouble(String.valueOf(product.price));
        tvOriginalPrice.setText(String.format("%,.0f", originalPrice));
        int suggested = (int) (originalPrice * 0.8);
        etPrice.setText(String.valueOf(suggested));
        etPrice.setSelection(etPrice.getText().length());

        etPrice.requestFocus();
        inputDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btnClose.setOnClickListener(v -> inputDialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String inputPrice = etPrice.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            if (inputPrice.isEmpty() || reason.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ!", Toast.LENGTH_SHORT).show();
                return;
            }

            double offerPrice;
            try {
                offerPrice = Double.parseDouble(inputPrice);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (offerPrice >= originalPrice) {
                Toast.makeText(requireContext(), "Giá phải nhỏ hơn giá gốc!", Toast.LENGTH_SHORT).show();
                return;
            }

            inputDialog.dismiss();
            showSuccessDialog();
        });

        inputDialog.show();
    }
    private void showSuccessDialog() {
        Dialog successDialog = new Dialog(requireContext());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.dialog_negotiation_send);
        successDialog.setCancelable(false);

        MaterialButton btnNextTime = successDialog.findViewById(R.id.btnNextTime);
        MaterialButton btnSeeNow = successDialog.findViewById(R.id.btnSeeNow);

        btnSeeNow.setOnClickListener(v -> {
            successDialog.dismiss();
            NavController navController = NavHostFragment.findNavController(DetailProductFragment.this);
            NavOptions navOptions = new NavOptions.Builder()
              .setLaunchSingleTop(true)
              .setRestoreState(true)
              .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
              .build();

            navController.navigate(R.id.navigation_negotiation, null, navOptions);
        });

        successDialog.show();
    }
    private void startAuctionCountdown(String auctionEndsAtIso) {
        long target = parseIso8601ToMillis(auctionEndsAtIso);
        Log.d("DetailProductFragment", "startAuctionCountdown: " + auctionEndsAtIso);
        if (target <= 0) {
            binding.textAddToCart.setText("—");
            return;
        }
        long remaining = target - System.currentTimeMillis();
        if (remaining <= 0) {
            binding.textAddToCart.setText("Đã kết thúc");
            return;
        }
        
        stopAuctionCountdown();
        auctionTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long ms) {
                binding.iconcart.setVisibility(View.GONE);
                binding.textAddToCart.setVisibility(View.GONE);
                binding.countdownTimer.setVisibility(View.VISIBLE);
                setHMS(binding.hoursText, binding.minutesText, binding.secondsText, ms);
            }
            
            @Override
            public void onFinish() {
                // Hết giờ -> quay về text thường
                binding.countdownTimer.setVisibility(View.GONE);
                binding.textAddToCart.setVisibility(View.VISIBLE);
                binding.textAddToCart.setText("Đã kết thúc");
            }
        }.start();
    }
    
    private void stopAuctionCountdown() {
        if (auctionTimer != null) {
            auctionTimer.cancel();
            auctionTimer = null;
        }
    }
    public static long parseIso8601ToMillis(String iso8601) {
        if (iso8601 == null) return -1L;
        try {
            java.text.SimpleDateFormat sdf =
              new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US);
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdf.parse(iso8601).getTime();
        } catch (Exception e1) {
            try {
                java.text.SimpleDateFormat sdf2 =
                  new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US);
                sdf2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return sdf2.parse(iso8601).getTime();
            } catch (Exception e2) {
                e2.printStackTrace();
                return -1L;
            }
        }
    }
    
    /** Cập nhật 3 ô H-M-S; totalHours có thể > 24 (gộp cả ngày cho gọn) */
    public static void setHMS(android.widget.TextView h, android.widget.TextView m, android.widget.TextView s, long remainingMs) {
        long totalSeconds = Math.max(0, remainingMs / 1000);
        long hours = totalSeconds / 3600;           // gộp cả ngày vào giờ
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        h.setText(String.format(java.util.Locale.getDefault(), "%02d", hours));
        m.setText(String.format(java.util.Locale.getDefault(), "%02d", minutes));
        s.setText(String.format(java.util.Locale.getDefault(), "%02d", seconds));
    }
    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }
    
    /** Thêm 1 dòng chữ "nhạt" (màu xám nhẹ) vào parent */
    private void addMutedText(@NonNull LinearLayout parent, @NonNull String text) {
        if (!isAdded() || parent == null) return;
        
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(requireContext().getColor(R.color.darkerDay)); // màu nhạt
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, dp(4), 0, dp(4));
        tv.setLayoutParams(lp);
        
        parent.addView(tv);
    }
    
    /**
     * Thêm một hàng thumbnail ảnh cuộn ngang vào parent.
     * - Mỗi ảnh 80dp, bo góc nhẹ bằng Glide RoundedCorners
     * - Khoảng cách 8dp giữa các ảnh
     * - Hiển thị placeholder khi load
     */
    private void addThumbRow(@NonNull LinearLayout parent, @Nullable List<String> urls) {
        if (!isAdded() || parent == null || urls == null || urls.isEmpty()) return;
        
        // Scroll ngang chứa 1 hàng ảnh
        HorizontalScrollView hsv = new HorizontalScrollView(requireContext());
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams hsvLp = new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hsvLp.setMargins(0, dp(6), 0, dp(6));
        hsv.setLayoutParams(hsvLp);
        
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        int size = dp(80);
        int radius = dp(8);
        int space = dp(8);
        
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            ImageView iv = new ImageView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (i > 0) lp.setMargins(space, 0, 0, 0);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setAdjustViewBounds(true);
            
            Glide.with(this)
              .load(url)
              .transform(new com.bumptech.glide.load.resource.bitmap.CenterCrop(),
                new com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
              .into(iv);
            
            // (Tuỳ chọn) click để xem lớn — bạn có thể mở dialog/photoview ở đây
            // iv.setOnClickListener(v -> openImagePreview(url));
            
            row.addView(iv);
        }
        
        hsv.addView(row);
        parent.addView(hsv);
    }
    private void setChip(MaterialCardView card, TextView tv, String textOrNull) {
        if (card == null || tv == null) return;
        if (textOrNull == null || textOrNull.trim().isEmpty() || "—".equals(textOrNull)) {
            card.setVisibility(View.GONE);
        } else {
            card.setVisibility(View.VISIBLE);
            tv.setText(textOrNull);
        }
    }
    
    private String pct(Integer v) { return v == null ? null : (v + "%"); }
    private String months(Integer v){ return v == null ? null : (v + " tháng"); }
    
    
    // =================== DOTS ===================
    
    /** Tạo lại toàn bộ dot theo count ảnh */
    private void buildDots(int count) {
        if (dotsContainer == null) return;
        dotsContainer.removeAllViews();
        dots.clear();
        
        if (count <= 0) return;
        
        int size = dp(10);
        int margin = dp(3);
        
        for (int i = 0; i < count; i++) {
            MaterialCardView dot = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(lp);
            dot.setCardBackgroundColor(colorInactive);
            dot.setRadius(dp(5));
            dot.setClickable(true);
            final int index = i;
            dot.setOnClickListener(v -> {
                // khi bấm dot: ngừng auto, nhảy trang, rồi chạy lại auto
                if (sliderHandler != null && sliderRunnable != null) {
                    sliderHandler.removeCallbacks(sliderRunnable);
                }
                viewPagerImages.setCurrentItem(index, true);
                if (sliderHandler != null && sliderRunnable != null) {
                    sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
                }
            });
            
            dotsContainer.addView(dot);
            dots.add(dot);
        }
    }
    
    /** Tô màu dot hiện tại – an toàn khi rỗng/out-of-range */
    private void updateDots(int currentPosition) {
        if (dots.isEmpty()) return;
        if (currentPosition < 0 || currentPosition >= dots.size()) currentPosition = 0;
        
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).setCardBackgroundColor(i == currentPosition ? colorActive : colorInactive);
        }
    }
    
    // =================== Utils ===================
    private String formatDateVN(String iso8601) {
        try {
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(iso8601);
            java.time.LocalDate d = odt.toLocalDate();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return d.format(fmt);
        } catch (Exception e) {
            return "";
        }
    }
    private String safe(String s) { return s == null ? "" : s; }
    
    private String formatVnd(long amount) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi","VN"));
        return nf.format(amount);
    }
    
    private void showLoading(boolean show) {
        if (binding == null) return;
        binding.getRoot().setAlpha(show ? 0.6f : 1f);
    }
}
