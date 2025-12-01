package com.example.secondchance.ui.product.search;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.secondchance.R;

public class ProductFilterDialogFragment extends DialogFragment {
  
  // ==================== CALLBACK VỀ MÀN CHA ====================
  public interface OnFilterApplyListener {
    void onFilterApplied(
      @Nullable String pickupCity,
      @Nullable Integer minRate,
      @Nullable Integer productNewPercent,
      @Nullable Integer minPrice,
      @Nullable Integer maxPrice,
      @Nullable Integer priceType
    );
  }
  
  private OnFilterApplyListener applyListener;
  
  public void setOnFilterApplyListener(OnFilterApplyListener l) {
    this.applyListener = l;
  }
  
  // ==================== ENUM TAB ====================
  private enum TabType {
    ADDRESS,
    RATING,
    STATUS,
    PRICE,
    PRICE_TYPE
  }
  
  // ==================== STATE FILTER ĐÃ CHỌN ====================
  // city
  @Nullable private String selectedCity = null;
  // rating tối thiểu (3,4,5 sao)
  @Nullable private Integer selectedMinRate = null;
  // phần trăm mới: 100, 90, 80
  @Nullable private Integer selectedProductNewPercent = null;
  // khoảng giá
  @Nullable private Integer selectedMinPrice = null;
  @Nullable private Integer selectedMaxPrice = null;
  // loại giá: 1=fixed, 2=negotiation, 3=auction
  @Nullable private Integer selectedPriceType = null;
  
  // ==================== VIEW ====================
  private TextView tvTitle;
  private GridLayout gridOptions;
  
  private TextView tvFilterAddress;
  private TextView tvFilterRating;
  private TextView tvFilterStatus;
  private TextView tvFilterPrice;
  private TextView tvFilterTypePrice;
  private ImageView ivCheckbox;
  
  private boolean selectAll = false;
  
  // ==================== KEYS ARG (nếu muốn truyền state vào) ====================
  private static final String ARG_CITY        = "arg_city";
  private static final String ARG_MIN_RATE    = "arg_minRate";
  private static final String ARG_NEW_PERCENT = "arg_productNewPercent";
  private static final String ARG_MIN_PRICE   = "arg_minPrice";
  private static final String ARG_MAX_PRICE   = "arg_maxPrice";
  private static final String ARG_PRICE_TYPE  = "arg_priceType";
  
  public static ProductFilterDialogFragment newInstance(
    @Nullable String city,
    @Nullable Integer minRate,
    @Nullable Integer productNewPercent,
    @Nullable Integer minPrice,
    @Nullable Integer maxPrice,
    @Nullable Integer priceType
  ) {
    ProductFilterDialogFragment f = new ProductFilterDialogFragment();
    Bundle b = new Bundle();
    if (city != null) b.putString(ARG_CITY, city);
    if (minRate != null) b.putInt(ARG_MIN_RATE, minRate);
    if (productNewPercent != null) b.putInt(ARG_NEW_PERCENT, productNewPercent);
    if (minPrice != null) b.putInt(ARG_MIN_PRICE, minPrice);
    if (maxPrice != null) b.putInt(ARG_MAX_PRICE, maxPrice);
    if (priceType != null) b.putInt(ARG_PRICE_TYPE, priceType);
    f.setArguments(b);
    return f;
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_TITLE, R.style.BottomSheetDialogTheme);
    
    // đọc state đã có từ arguments nếu có
    Bundle args = getArguments();
    if (args != null) {
      if (args.containsKey(ARG_CITY)) {
        selectedCity = args.getString(ARG_CITY);
      }
      if (args.containsKey(ARG_MIN_RATE)) {
        selectedMinRate = args.getInt(ARG_MIN_RATE);
      }
      if (args.containsKey(ARG_NEW_PERCENT)) {
        selectedProductNewPercent = args.getInt(ARG_NEW_PERCENT);
      }
      if (args.containsKey(ARG_MIN_PRICE)) {
        selectedMinPrice = args.getInt(ARG_MIN_PRICE);
      }
      if (args.containsKey(ARG_MAX_PRICE)) {
        selectedMaxPrice = args.getInt(ARG_MAX_PRICE);
      }
      if (args.containsKey(ARG_PRICE_TYPE)) {
        selectedPriceType = args.getInt(ARG_PRICE_TYPE);
      }
    }
  }
  
  @Nullable
  @Override
  public View onCreateView(
    @NonNull LayoutInflater inflater,
    @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState
  ) {
    return inflater.inflate(R.layout.dialog_filter_search, container, false);
  }
  
  @Override
  public void onViewCreated(
    @NonNull View view,
    @Nullable Bundle savedInstanceState
  ) {
    super.onViewCreated(view, savedInstanceState);
    
    // ----- view bên trái -----
    tvTitle = view.findViewById(R.id.tvTitle);
    gridOptions = view.findViewById(R.id.gridAddress1);
    
    // ----- tab bên phải -----
    tvFilterAddress   = view.findViewById(R.id.tvFilterAddress);
    tvFilterRating    = view.findViewById(R.id.tvFilterRating);
    tvFilterStatus    = view.findViewById(R.id.tvFilterStatus);
    tvFilterPrice     = view.findViewById(R.id.tvFilterPrice);
    tvFilterTypePrice = view.findViewById(R.id.tvFilterTypePrice);
    
    ImageView ivFilterIcon = view.findViewById(R.id.ivFilterIcon);
    ivCheckbox = view.findViewById(R.id.checkbox);
    Button btnApply = view.findViewById(R.id.btnApply);
    
    // ----- Chọn tất cả = clear hết filter -----
    ivCheckbox.setOnClickListener(v -> {
      selectAll = !selectAll;
      if (selectAll) {
        ivCheckbox.setImageResource(R.drawable.ic_checkbox_checked);
        clearAllFilterState();
        // reload tab hiện tại (mặc định address)
        showAddressOptions();
      } else {
        ivCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked);
      }
    });
    
    // ----- click tab bên phải -----
    tvFilterAddress.setOnClickListener(v -> {
      setSelectedTab(tvFilterAddress);
      showAddressOptions();
    });
    
    tvFilterRating.setOnClickListener(v -> {
      setSelectedTab(tvFilterRating);
      showRatingOptions();
    });
    
    tvFilterStatus.setOnClickListener(v -> {
      setSelectedTab(tvFilterStatus);
      showStatusOptions();
    });
    
    tvFilterPrice.setOnClickListener(v -> {
      setSelectedTab(tvFilterPrice);
      showPriceOptions();
    });
    
    tvFilterTypePrice.setOnClickListener(v -> {
      setSelectedTab(tvFilterTypePrice);
      showPriceTypeOptions();
    });
    
    // Tab mặc định
    setSelectedTab(tvFilterAddress);
    showAddressOptions();
    
    // ----- nút áp dụng -----
    btnApply.setOnClickListener(v -> {
      // Nếu tích "chọn tất cả" → clear filter, để null hết
      if (selectAll) {
        clearAllFilterState();
      }
      
      if (applyListener != null) {
        applyListener.onFilterApplied(
          selectedCity,
          selectedMinRate,
          selectedProductNewPercent,
          selectedMinPrice,
          selectedMaxPrice,
          selectedPriceType
        );
      }
      dismiss();
    });
  }
  
  @Override
  public void onStart() {
    super.onStart();
    Dialog dialog = getDialog();
    if (dialog != null) {
      Window window = dialog.getWindow();
      if (window != null) {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity()
          .getWindowManager()
          .getDefaultDisplay()
          .getMetrics(metrics);
        
        int screenHeight = metrics.heightPixels;
        int desiredHeight = (int) (screenHeight * 0.8f);
        
        window.setLayout(
          ViewGroup.LayoutParams.MATCH_PARENT,
          desiredHeight
        );
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        
        WindowManager.LayoutParams params = window.getAttributes();
        params.dimAmount = 0.4f;
        window.setAttributes(params);
      }
    }
  }
  
  // ==================== TAB HIGHLIGHT (BÊN PHẢI) ====================
  
  private void setSelectedTab(TextView selected) {
    resetTab(tvFilterAddress);
    resetTab(tvFilterRating);
    resetTab(tvFilterStatus);
    resetTab(tvFilterPrice);
    resetTab(tvFilterTypePrice);
    
    if (selected != null) {
      selected.setBackgroundResource(R.drawable.bg_filter_menu_selected);
      selected.setTextColor(requireContext().getColor(R.color.darkerDay));
      selected.setTypeface(selected.getTypeface(), android.graphics.Typeface.BOLD);
    }
  }
  
  private void resetTab(TextView tv) {
    if (tv == null) return;
    tv.setBackground(null);
    tv.setTextColor(requireContext().getColor(R.color.darkerDay));
    tv.setTypeface(null, android.graphics.Typeface.NORMAL);
  }
  
  // ==================== CÁC DATA GIẢ CHO TỪNG TAB ====================
  
  private void showAddressOptions() {
    tvTitle.setText("Lọc theo địa chỉ");
    
    String[] options = new String[]{
      "Tp.HCM",
      "Hà Nội",
      "Đà Nẵng",
      "Cần Thơ",
      "Hải Phòng",
      "Bình Dương",
      "Đồng Nai",
      "Huế"
    };
    
    populateGridWithOptions(options, TabType.ADDRESS);
  }
  
  private void showRatingOptions() {
    tvTitle.setText("Lọc theo đánh giá");
    
    String[] options = new String[]{
      "Từ 5★",
      "Từ 4★ trở lên",
      "Từ 3★ trở lên"
    };
    
    populateGridWithOptions(options, TabType.RATING);
  }
  
  private void showStatusOptions() {
    tvTitle.setText("Lọc theo trạng thái sản phẩm");
    
    String[] options = new String[]{
      "Mới 100%",
      "Mới 90%",
      "Mới 80%"
    };
    
    populateGridWithOptions(options, TabType.STATUS);
  }
  
  private void showPriceOptions() {
    tvTitle.setText("Lọc theo khoảng giá");
    
    String[] options = new String[]{
      "Dưới 500.000đ",
      "500.000đ - 1.000.000đ",
      "1.000.000đ - 3.000.000đ",
      "3.000.000đ - 5.000.000đ",
      "Trên 5.000.000đ"
    };
    
    populateGridWithOptions(options, TabType.PRICE);
  }
  
  private void showPriceTypeOptions() {
    tvTitle.setText("Lọc theo loại giá");
    
    String[] options = new String[]{
      "Giá cố định",
      "Thương lượng",
      "Đấu giá"
    };
    
    populateGridWithOptions(options, TabType.PRICE_TYPE);
  }
  
  // ==================== GRID CHIP BÊN TRÁI ====================
  
  private void populateGridWithOptions(String[] options, TabType tab) {
    if (gridOptions == null || getContext() == null) return;
    
    gridOptions.removeAllViews();
    
    int margin8 = dpToPx(8);
    int height44 = dpToPx(44);
    
    for (String label : options) {
      TextView tv = new TextView(requireContext());
      tv.setText(label);
      tv.setGravity(Gravity.CENTER);
      tv.setTextSize(14);
      
      GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
      lp.width = 0;
      lp.height = height44;
      lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
      lp.setMargins(0, 0, margin8, margin8);
      tv.setLayoutParams(lp);
      
      boolean selected = isOptionSelected(tab, label);
      applyChipStyle(tv, selected);
      
      tv.setOnClickListener(v -> onOptionClicked(tab, label, tv));
      
      gridOptions.addView(tv);
    }
  }
  
  private void onOptionClicked(TabType tab, String label, TextView view) {
    boolean wasSelected = isOptionSelected(tab, label);
    
    if (wasSelected) {
      // 👉 Đang được chọn, nhấn lần nữa = BỎ CHỌN
      clearSelectionForTab(tab);
      
      int childCount = gridOptions.getChildCount();
      for (int i = 0; i < childCount; i++) {
        View child = gridOptions.getChildAt(i);
        if (child instanceof TextView) {
          applyChipStyle((TextView) child, false);
        }
      }
      return;
    }
    
    // 👉 Đang không được chọn: chọn chip này (1 chip/tab)
    clearSelectionForTab(tab);
    
    int childCount = gridOptions.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = gridOptions.getChildAt(i);
      if (child instanceof TextView) {
        applyChipStyle((TextView) child, false);
      }
    }
    applyChipStyle(view, true);
    
    // cập nhật state
    switch (tab) {
      case ADDRESS:
        selectedCity = label;
        break;
      
      case RATING:
        if (label.contains("5"))      selectedMinRate = 5;
        else if (label.contains("4")) selectedMinRate = 4;
        else if (label.contains("3")) selectedMinRate = 3;
        break;
      
      case STATUS:
        if (label.contains("100"))      selectedProductNewPercent = 100;
        else if (label.contains("90"))  selectedProductNewPercent = 90;
        else if (label.contains("80"))  selectedProductNewPercent = 80;
        break;
      
      case PRICE:
        // ánh xạ label -> min/max price
        if (label.startsWith("Dưới")) {
          selectedMinPrice = null;
          selectedMaxPrice = 500_000;
        } else if (label.startsWith("500.000")) {
          selectedMinPrice = 500_000;
          selectedMaxPrice = 1_000_000;
        } else if (label.startsWith("1.000.000")) {
          selectedMinPrice = 1_000_000;
          selectedMaxPrice = 3_000_000;
        } else if (label.startsWith("3.000.000")) {
          selectedMinPrice = 3_000_000;
          selectedMaxPrice = 5_000_000;
        } else if (label.startsWith("Trên")) {
          selectedMinPrice = 5_000_000;
          selectedMaxPrice = null;
        }
        break;
      
      case PRICE_TYPE:
        if (label.contains("cố định"))           selectedPriceType = 1;
        else if (label.contains("Thương lượng")) selectedPriceType = 2;
        else if (label.contains("Đấu giá"))      selectedPriceType = 3;
        break;
    }
  }
  
  private boolean isOptionSelected(TabType tab, String label) {
    switch (tab) {
      case ADDRESS:
        return label.equals(selectedCity);
      
      case RATING:
        if (selectedMinRate == null) return false;
        if (label.contains("5")) return selectedMinRate == 5;
        if (label.contains("4")) return selectedMinRate == 4;
        if (label.contains("3")) return selectedMinRate == 3;
        return false;
      
      case STATUS:
        if (selectedProductNewPercent == null) return false;
        if (label.contains("100")) return selectedProductNewPercent == 100;
        if (label.contains("90"))  return selectedProductNewPercent == 90;
        if (label.contains("80"))  return selectedProductNewPercent == 80;
        return false;
      
      case PRICE:
        if (selectedMinPrice == null && selectedMaxPrice == null) return false;
        
        if (label.startsWith("Dưới")) {
          return selectedMinPrice == null && Integer.valueOf(500_000).equals(selectedMaxPrice);
        } else if (label.startsWith("500.000")) {
          return Integer.valueOf(500_000).equals(selectedMinPrice)
            && Integer.valueOf(1_000_000).equals(selectedMaxPrice);
        } else if (label.startsWith("1.000.000")) {
          return Integer.valueOf(1_000_000).equals(selectedMinPrice)
            && Integer.valueOf(3_000_000).equals(selectedMaxPrice);
        } else if (label.startsWith("3.000.000")) {
          return Integer.valueOf(3_000_000).equals(selectedMinPrice)
            && Integer.valueOf(5_000_000).equals(selectedMaxPrice);
        } else if (label.startsWith("Trên")) {
          return Integer.valueOf(5_000_000).equals(selectedMinPrice)
            && selectedMaxPrice == null;
        }
        return false;
      
      case PRICE_TYPE:
        if (selectedPriceType == null) return false;
        if (label.contains("cố định"))           return selectedPriceType == 1;
        if (label.contains("Thương lượng"))      return selectedPriceType == 2;
        if (label.contains("Đấu giá"))           return selectedPriceType == 3;
        return false;
    }
    return false;
  }
  
  private void clearSelectionForTab(TabType tab) {
    switch (tab) {
      case ADDRESS:
        selectedCity = null;
        break;
      case RATING:
        selectedMinRate = null;
        break;
      case STATUS:
        selectedProductNewPercent = null;
        break;
      case PRICE:
        selectedMinPrice = null;
        selectedMaxPrice = null;
        break;
      case PRICE_TYPE:
        selectedPriceType = null;
        break;
    }
  }
  
  private void clearAllFilterState() {
    selectedCity = null;
    selectedMinRate = null;
    selectedProductNewPercent = null;
    selectedMinPrice = null;
    selectedMaxPrice = null;
    selectedPriceType = null;
  }
  
  private void applyChipStyle(TextView tv, boolean selected) {
    if (selected) {
      tv.setBackgroundResource(R.drawable.bg_filter_chip_selected);
      tv.setTextColor(requireContext().getColor(R.color.darkerDay));
    } else {
      tv.setBackgroundResource(R.drawable.bg_filter_chip);
      tv.setTextColor(requireContext().getColor(R.color.darkerDay));
    }
  }
  
  private int dpToPx(int dp) {
    float density = requireContext().getResources().getDisplayMetrics().density;
    return Math.round(dp * density);
  }
}
