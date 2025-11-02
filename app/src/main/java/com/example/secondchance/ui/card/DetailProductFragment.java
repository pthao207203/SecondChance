package com.example.secondchance.ui.card;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentDetailProductBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.widget.TextView;
import android.widget.EditText;

import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;

public class DetailProductFragment extends Fragment {

    private FragmentDetailProductBinding binding;

    // === BIẾN TAB + NỘI DUNG ===
    private MaterialCardView tabDescription, tabSource, tabEvidence, tabOther;
    private LinearLayout contentDescription, contentSource, contentEvidence, contentOther;

    // === BIẾN MÀU ===
    private static final int COLOR_SELECTED = R.color.highlight4blur; // Xanh
    private static final int COLOR_UNSELECTED = R.color.whiteDay;     // Xám
    // === BIẾN MÀU THANH NGANG ===
    private static final int INDICATOR_SELECTED = R.color.highLight5;   // Xanh đậm
    private static final int INDICATOR_UNSELECTED = R.color.whiteDay; // Trắng
    // === BIẾN MỚI: THANH CHỈ THỊ ===
    private View indicatorDescription, indicatorSource, indicatorEvidence, indicatorOther;

    private ViewPager2 viewPagerImages;
    private ImageSliderAdapter sliderAdapter;
    private List<Integer> imageList;
    private MaterialCardView dot1, dot2, dot3;
    private MaterialCardView[] dots; // Mảng để quản lý các nút

    // Biến cho auto-scroll
    private Handler sliderHandler;
    private Runnable sliderRunnable;
    private static final long SLIDE_DELAY_MS = 2000; // 2 giây

    // Màu sắc (Lấy từ code cũ của bạn, bạn có thể cần điều chỉnh)
    private int colorActive;
    private int colorInactive;
    private static final long CLICK_DELAY_MS = 5000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo màu
        colorActive = getResources().getColor(R.color.darkerDay); // Màu sáng
        colorInactive = getResources().getColor(R.color.lighterDay); // Màu mờ (hoặc R.color.normalDay)

        // === KHỞI TẠO SLIDER ===
        // 1. Ánh xạ View (đã đúng)
        viewPagerImages = view.findViewById(R.id.viewPagerImages);
        dot1 = view.findViewById(R.id.dot_1);
        dot2 = view.findViewById(R.id.dot_2);
        dot3 = view.findViewById(R.id.dot_3);
        dots = new MaterialCardView[]{dot1, dot2, dot3};

        // 2. Chuẩn bị danh sách ảnh (đã đúng)
        imageList = new ArrayList<>();
        imageList.add(R.drawable.giohoa1);
        imageList.add(R.drawable.giohoa2);
        imageList.add(R.drawable.giohoa3);

        // 3. Set Adapter (đã đúng)
        sliderAdapter = new ImageSliderAdapter(imageList);
        viewPagerImages.setAdapter(sliderAdapter);

        // Đặt vị trí bắt đầu ở giữa để cuộn 2 chiều (đã đúng)
        // Cập nhật dots cho vị trí ban đầu
        viewPagerImages.setCurrentItem(Integer.MAX_VALUE / 2, false);
        updateDots(viewPagerImages.getCurrentItem() % imageList.size()); // <--- Gọi lần đầu tiên để update trạng thái dots

        // === BỔ SUNG: THÊM ONCLICK LISTENER CHO CÁC DOTS ===
        for (int i = 0; i < dots.length; i++) {
            final int dotIndex = i; // Cần final cho lambda
            dots[i].setOnClickListener(v -> {
                // Dừng auto-scroll ngay lập tức
                sliderHandler.removeCallbacks(sliderRunnable);

                // Chuyển ViewPager đến ảnh tương ứng
                int currentItem = viewPagerImages.getCurrentItem();
                int currentActualPosition = currentItem % imageList.size();
                int targetItem = currentItem + (dotIndex - currentActualPosition); // Tính vị trí mới dựa trên vị trí hiện tại
                viewPagerImages.setCurrentItem(targetItem, true); // Cuộn mượt mà

                // Sau 5 giây (CLICK_DELAY_MS) thì tiếp tục auto-scroll
                sliderHandler.postDelayed(sliderRunnable, CLICK_DELAY_MS);
            });
        }

        // 4. Lắng nghe sự kiện chuyển trang để cập nhật nút tròn (Đã đúng)
        viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int actualPosition = position % imageList.size();
                updateDots(actualPosition);

                // Khi người dùng tự lướt, reset timer auto-scroll
                // Nếu đang trong chu kỳ click (chờ 5s), không reset
                if (sliderHandler != null) {
                    sliderHandler.removeCallbacks(sliderRunnable);
                    sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
                }
            }
        });

        // 5. Cài đặt auto-scroll (đã đúng)
        setupAutoSlider();


        // === NHẬN PRODUCT ===
        ProductCard product = (ProductCard) getArguments().getSerializable("product");
        if (product == null) {
            Log.e("DetailFragment", "Product is NULL!");
            Toast.makeText(requireContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }
        showProduct(product);

        // === AVATAR SHOP ===
        binding.shopAvatar.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("shopName", "Cá Biết Bay");
            bundle.putString("shopPhone", "0333 333 333");
            bundle.putString("shopAddress", "Thôn Cá, xã Biết, quận Bay, thành phố Fish Fly");
            bundle.putString("shopEmail", "Cabietbay@gmail.com");
            bundle.putInt("shopAvatar", R.drawable.avatar1);
            Navigation.findNavController(v).navigate(R.id.action_detail_product_to_shop_home, bundle);


        });

        // === NÚT MUA / ĐẤU GIÁ ===
        binding.cardBuyNow.setOnClickListener(v -> {
            if (product.getProductType() == ProductCard.ProductType.AUCTION) {
                Navigation.findNavController(v).navigate(R.id.navigation_rule_auction);
            } else {
                Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        // === NÚT THƯƠNG LƯỢNG ===
        binding.cardNegotiation.setOnClickListener(v -> {
            if (product.getProductType() == ProductCard.ProductType.NEGOTIATION) {
                showNegotiationDialog(product);
            }
        });

        // === TAB + NỘI DUNG ===
        tabDescription = view.findViewById(R.id.tabDescription);
        tabSource = view.findViewById(R.id.tabSource);
        tabEvidence = view.findViewById(R.id.tabEvidence);
        tabOther = view.findViewById(R.id.tabOther);

        contentDescription = view.findViewById(R.id.contentDescription);
        contentSource = view.findViewById(R.id.contentSource);
        contentEvidence = view.findViewById(R.id.contentEvidence);
        contentOther = view.findViewById(R.id.contentOther);

        // === THANH CHỈ THỊ ===
        indicatorDescription = view.findViewById(R.id.indicatorDescription);
        indicatorSource = view.findViewById(R.id.indicatorSource);
        indicatorEvidence = view.findViewById(R.id.indicatorEvidence);
        indicatorOther = view.findViewById(R.id.indicatorOther);

        // === MẶC ĐỊNH: MÔ TẢ ===
        selectTab(tabDescription, contentDescription, indicatorDescription);

        // === CLICK LISTENER (CHỈ GỌI 1 LẦN) ===
        tabDescription.setOnClickListener(v -> selectTab(tabDescription, contentDescription, indicatorDescription));
        tabSource.setOnClickListener(v -> selectTab(tabSource, contentSource, indicatorSource));
        tabEvidence.setOnClickListener(v -> selectTab(tabEvidence, contentEvidence, indicatorEvidence));
        tabOther.setOnClickListener(v -> selectTab(tabOther, contentOther, indicatorOther));
    }
    private void updateDots(int currentPosition) {
        for (int i = 0; i < dots.length; i++) {
            if (i == currentPosition) {
                dots[i].setCardBackgroundColor(colorActive);
            } else {
                dots[i].setCardBackgroundColor(colorInactive);
            }
        }
    }

    /**
     * Thiết lập logic tự động trượt
     */
    private void setupAutoSlider() {
        sliderHandler = new Handler(Looper.getMainLooper());
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                // Tăng item lên 1
                viewPagerImages.setCurrentItem(viewPagerImages.getCurrentItem() + 1);
                // Lặp lại
                sliderHandler.postDelayed(this, SLIDE_DELAY_MS);
            }
        };
    }

    // === QUẢN LÝ VÒNG ĐỜI (LIFECYCLE) ===
    // Rất quan trọng để tránh crash

    @Override
    public void onResume() {
        super.onResume();
        // Bắt đầu trượt khi Fragment hiển thị
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY_MS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Dừng trượt khi Fragment bị tạm dừng
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }

    // === HÀM CHỌN TAB ===
    private void selectTab(MaterialCardView selectedTab, LinearLayout contentToShow, View selectedIndicator) {
        // Reset tab
        tabDescription.setCardBackgroundColor(getResources().getColor(COLOR_UNSELECTED));
        tabSource.setCardBackgroundColor(getResources().getColor(COLOR_UNSELECTED));
        tabEvidence.setCardBackgroundColor(getResources().getColor(COLOR_UNSELECTED));
        tabOther.setCardBackgroundColor(getResources().getColor(COLOR_UNSELECTED));

        // Reset thanh ngang → trắng
        indicatorDescription.setBackgroundColor(getResources().getColor(INDICATOR_UNSELECTED));
        indicatorSource.setBackgroundColor(getResources().getColor(INDICATOR_UNSELECTED));
        indicatorEvidence.setBackgroundColor(getResources().getColor(INDICATOR_UNSELECTED));
        indicatorOther.setBackgroundColor(getResources().getColor(INDICATOR_UNSELECTED));

        // Ẩn nội dung
        contentDescription.setVisibility(View.GONE);
        contentSource.setVisibility(View.GONE);
        contentEvidence.setVisibility(View.GONE);
        contentOther.setVisibility(View.GONE);

        // Hiển thị tab + thanh ngang
        selectedTab.setCardBackgroundColor(getResources().getColor(COLOR_SELECTED));
        selectedIndicator.setBackgroundColor(getResources().getColor(INDICATOR_SELECTED));
        contentToShow.setVisibility(View.VISIBLE);
    }

    // === DIALOG THƯƠNG LƯỢNG ===
    private void showNegotiationDialog(ProductCard product) {
        Dialog inputDialog = new Dialog(requireContext());
        inputDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputDialog.setContentView(R.layout.dialog_input_negotiation);
        inputDialog.setCancelable(true);

        TextView tvOriginalPrice = inputDialog.findViewById(R.id.tvOriginalPrice);
        EditText etPrice = inputDialog.findViewById(R.id.etNegotiationPrice);
        EditText etReason = inputDialog.findViewById(R.id.etReason);
        MaterialButton btnSend = inputDialog.findViewById(R.id.btnRegisterSeller);
        ImageView btnClose = inputDialog.findViewById(R.id.btnCloseSuccess);

        String priceStr = product.getPrice().replace("đ", "").replace(".", "").trim();
        double originalPrice = Double.parseDouble(priceStr);
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

        btnNextTime.setOnClickListener(v -> successDialog.dismiss());
        btnSeeNow.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Chuyển đến Quản lý thương lượng...", Toast.LENGTH_SHORT).show();
            successDialog.dismiss();
        });

        successDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showProduct(ProductCard product) {
        binding.shopAvatar.setImageResource(product.getImageRes());
        binding.textViewTitle.setText(product.getTitle());
        binding.priceValue.setText(product.getPrice());
        binding.priceCurrency.setText("đ");
        binding.productQuantity.setText("Số lượng: " + product.getQuantity());
        binding.productDescription.setText(product.getDescription());

        int bgColor = product.getProductType() == ProductCard.ProductType.NEGOTIATION
                ? R.color.highlight3blur : R.color.grayDay;
        binding.cardNegotiation.setCardBackgroundColor(requireContext().getColor(bgColor));

        if (product.getProductType() == ProductCard.ProductType.AUCTION) {
            binding.textBuyNow.setText("ĐẤU GIÁ");
            binding.cardBuyNow.setCardBackgroundColor(requireContext().getColor(R.color.normalDay));
        } else {
            binding.textBuyNow.setText("MUA NGAY");
            binding.cardBuyNow.setCardBackgroundColor(requireContext().getColor(R.color.normalDay));
        }

        if (product.getProductType() == ProductCard.ProductType.AUCTION) {
            binding.iconcart.setImageResource(R.drawable.timer);
            binding.textAddToCart.setText(product.getTimeRemaining());
            binding.textAddToCart.setGravity(Gravity.CENTER);
            binding.cardAddToCart.setCardBackgroundColor(requireContext().getColor(R.color.highlight4blur));
        } else {
            binding.iconcart.setImageResource(R.drawable.shopping_cart_02);
            binding.textAddToCart.setText("Thêm vào giỏ hàng");
            binding.textAddToCart.setGravity(Gravity.CENTER);
            binding.cardAddToCart.setCardBackgroundColor(requireContext().getColor(R.color.highlight4blur));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // Dừng trượt khi Fragment bị hủy
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }
}
