package com.example.secondchance.ui.product.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.ProductListResponse;
import com.example.secondchance.ui.card.CardListFragment;
import com.example.secondchance.ui.card.ProductCard;
import com.example.secondchance.ui.home.CategoryAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuctionFilterFragment extends Fragment {
  
  private CategoryAdapter catAdapter;
  
  // ========================= FILTER STATE =========================
  private String searchName;
  private String pickupCity;
  private Integer minRate;
  private Integer status;
  private Integer minPrice;
  private Integer maxPrice;
  private Integer priceType;
  
  // 🔥 state category đang được chọn
  private String currentCategoryId;
  
  // ========================= KEYS =========================
  private static final String KEY_PICKUP_CITY = "pickupCity";
  private static final String KEY_MIN_RATE    = "minRate";
  private static final String KEY_STATUS      = "status";
  private static final String KEY_MIN_PRICE   = "minPrice";
  private static final String KEY_MAX_PRICE   = "maxPrice";
  private static final String KEY_PRICE_TYPE  = "priceType";
  private static final String KEY_CATEGORY_ID = "categoryId";
  private static final String KEY_SEARCH_NAME = "searchName";
  
  @Nullable
  @Override
  public View onCreateView(
    @NonNull LayoutInflater inflater,
    @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState
  ) {
    return inflater.inflate(R.layout.fragment_search_result, container, false);
  }
  
  @Override
  public void onViewCreated(
    @NonNull View view,
    @Nullable Bundle savedInstanceState
  ) {
    super.onViewCreated(view, savedInstanceState);
    
    // ===== lấy filter từ Bundle như bạn đã có =====
    Bundle args = getArguments();
    if (args != null) {
      searchName        = args.getString(KEY_SEARCH_NAME);
      pickupCity        = args.getString(KEY_PICKUP_CITY);
      currentCategoryId = args.getString(KEY_CATEGORY_ID);
      
      if (args.containsKey(KEY_MIN_RATE))   minRate   = args.getInt(KEY_MIN_RATE);
      if (args.containsKey(KEY_STATUS))     status    = args.getInt(KEY_STATUS);
      if (args.containsKey(KEY_MIN_PRICE))  minPrice  = args.getInt(KEY_MIN_PRICE);
      if (args.containsKey(KEY_MAX_PRICE))  maxPrice  = args.getInt(KEY_MAX_PRICE);
      if (args.containsKey(KEY_PRICE_TYPE)) priceType = args.getInt(KEY_PRICE_TYPE);
    }
    
    // ===== Categories =====
    RecyclerView rvCat = view.findViewById(R.id.rvCategories);
    rvCat.setLayoutManager(
      new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    );
    catAdapter = new CategoryAdapter();
    rvCat.setAdapter(catAdapter);
    
    catAdapter.setOnCategoryClickListener(category -> {
      // lấy id đang được chọn (có thể = null nếu user click lần 2 để huỷ chọn)
      String selectedId = catAdapter.getSelectedCategoryId();
      currentCategoryId = selectedId;   // currentCategoryId là biến bạn đang dùng để call API
      
      // gọi lại API với các điều kiện khác giữ nguyên, chỉ categoryId thay đổi (hoặc null)
      fetchProductsWithFilter();
    });
    
    
    // ===== Ô search local trong fragment =====
    EditText etSearchLocal = view.findViewById(R.id.search_bar_local);
    ImageView ivSearchLocal = view.findViewById(R.id.icon_search_local);
    
    // Gán text ban đầu
    if (searchName != null && !searchName.isEmpty()) {
      etSearchLocal.setText(searchName);
      etSearchLocal.setSelection(searchName.length());
    }
    
    ivSearchLocal.setOnClickListener(v -> {
      String keyword = etSearchLocal.getText().toString().trim();
      if (keyword.isEmpty()) {
        Toast.makeText(requireContext(), "Vui lòng nhập từ khoá", Toast.LENGTH_SHORT).show();
        return;
      }
      searchName = keyword;          // cập nhật filter
      fetchProductsWithFilter();     // gọi lại API với từ khoá mới
    });
    
    View btnFilter = view.findViewById(R.id.btnFilter);
    btnFilter.setOnClickListener(v -> {
      // truyền state hiện tại vào dialog
      ProductFilterDialogFragment dialog =
        ProductFilterDialogFragment.newInstance(
          pickupCity,
          minRate,
          status,
          minPrice,
          maxPrice,
          priceType
        );
      
      dialog.setOnFilterApplyListener((city, rating, productNewPercent, priceMin, priceMax, type) -> {
        // cập nhật state của fragment
        pickupCity = city;
        minRate    = rating;
        status     = productNewPercent;
        minPrice   = priceMin;
        maxPrice   = priceMax;
        priceType  = type;
        
        // gọi lại API /api/products với toàn bộ filter hiện tại
        fetchProductsWithFilter();
      });
      
      dialog.show(getChildFragmentManager(), "ProductFilterDialog");
    });
    // ===== Gọi API lần đầu =====
    fetchProductsWithFilter();
  }
  
  // ========================= CALL API FILTERED PRODUCTS =========================
  private void fetchProductsWithFilter() {
    
    ProductApi api = RetrofitProvider.product();
    
    Call<ProductListResponse> call = api.getProducts(
      1,
      20,
      emptyToNull(searchName),
      emptyToNull(pickupCity),
      minRate,
      status,
      minPrice,
      maxPrice,
      priceType,
      emptyToNull(currentCategoryId)
    );
    
    call.enqueue(new Callback<ProductListResponse>() {
      @Override
      public void onResponse(
        @NonNull Call<ProductListResponse> call,
        @NonNull Response<ProductListResponse> response
      ) {
        if (!isAdded()) return;
        
        ProductListResponse body = response.body();
        
        if (!response.isSuccessful() || body == null || !body.success || body.data == null) {
          String errorBody = null;
          
          try {
            if (response.errorBody() != null) {
              errorBody = response.errorBody().string();
            }
          } catch (Exception e) {
            Log.e("AuctionFilterFragment", "Cannot parse errorBody: " + e.getMessage());
          }
          
          Log.e("AuctionFilterFragment",
            "Response error -> code=" + response.code()
              + ", url=" + (response.raw() != null ? response.raw().request().url() : "null")
              + ", errorBody=" + errorBody
          );
          
          Toast.makeText(requireContext(),
            "Không tải được sản phẩm", Toast.LENGTH_SHORT).show();
          return;
        }
        
        // ========================= 1. BIND CATEGORY LIST =========================
        if (body.data.categories != null) {
          
          List<HomeApi.Category> mappedCats = new ArrayList<>();
          
          for (ProductListResponse.Category c : body.data.categories) {
            HomeApi.Category hc = new HomeApi.Category();
            hc.id = c.id;
            hc.name = c.name;
            hc.icon = c.icon != null ? c.icon.replace('-', '_') : null;
            mappedCats.add(hc);
          }
          
          catAdapter.submit(mappedCats);
        }
        
        // ========================= 2. MAP PRODUCTS =========================
        ArrayList<ProductCard> cards = mapItemsToProductCards(body.data.items);
        
        // ========================= 3. UPDATE CARDLISTFRAGMENT =========================
        Fragment existing =
          getChildFragmentManager().findFragmentById(R.id.cardListFragmentContainer);
        
        if (existing == null) {
          CardListFragment frag = CardListFragment.newInstance(cards);
          getChildFragmentManager().beginTransaction()
            .replace(R.id.cardListFragmentContainer, frag, "CardListFragment")
            .commit();
        } else if (existing instanceof CardListFragment) {
          ((CardListFragment) existing).setExternalData(cards);
        }
      }
      
      @Override
      public void onFailure(
        @NonNull Call<ProductListResponse> call,
        @NonNull Throwable t
      ) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(),
          "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }
  
  private String emptyToNull(String s) {
    return TextUtils.isEmpty(s) ? null : s;
  }
  
  // ========================= MAP DTO → ProductCard =========================
  private ArrayList<ProductCard> mapItemsToProductCards(List<ProductListResponse.Item> items) {
    
    ArrayList<ProductCard> out = new ArrayList<>();
    if (items == null) return out;
    
    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    
    for (ProductListResponse.Item it : items) {
      ProductCard pc = new ProductCard();
      pc.setId(it.id);
      pc.setTitle(it.name);
      pc.setDescription("");
      pc.setQuantity(it.quantity);
      pc.setImageUrl(it.thumbnail);
      
      // type sản phẩm
      ProductCard.ProductType type;
      if (it.priceType == 3) type = ProductCard.ProductType.AUCTION;
      else if (it.priceType == 2) type = ProductCard.ProductType.NEGOTIATION;
      else type = ProductCard.ProductType.FIXED;
      
      pc.setProductType(type);
      
      // format giá
      String priceStr = it.price > 0
        ? nf.format(it.price).replace(',', '.')
        : "0";
      
      pc.setPrice(priceStr);
      
      out.add(pc);
    }
    
    return out;
  }
}
