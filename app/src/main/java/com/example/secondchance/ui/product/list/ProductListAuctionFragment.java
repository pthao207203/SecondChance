package com.example.secondchance.ui.product.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.AdminProductListResponse;
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ProductAuctionAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListAuctionFragment extends Fragment implements ProductAuctionAdapter.OnProductClickListener {
    
    private static final String ARG_TAB_TYPE = "tab_type";
    private String tabType;
    private RecyclerView recyclerView;
    private ProductAuctionAdapter adapter;
    
    public static ProductListAuctionFragment newInstance(String tabType) {
        ProductListAuctionFragment fragment = new ProductListAuctionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getString(ARG_TAB_TYPE);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.fragment_product_fixed_list, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_products);
        setupRecyclerView();
        loadProductsFromApi();
        
        return view;
    }
    
    private void setupRecyclerView() {
        adapter = new ProductAuctionAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    /** Gọi API /admin/products?priceType=3 và bind vào adapter */
    private void loadProductsFromApi() {
        if (!"auction".equals(tabType)) {
            adapter.setProducts(new ArrayList<>());
            return;
        }
        
        ProductApi api = RetrofitProvider.product();
        
        Call<AdminProductListResponse> call = api.getAdminProducts(
          3,      // ⭐ priceType = 3 → sản phẩm đấu giá
          false,  // showDeleted
          1,      // page
          10      // pageSize
        );
        
        call.enqueue(new Callback<AdminProductListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminProductListResponse> call,
                                   @NonNull Response<AdminProductListResponse> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    AdminProductListResponse body = response.body();
                    if (body.success && body.data != null && body.data.items != null) {
                        List<Product> uiProducts = mapAuctionProducts(body.data.items);
                        adapter.setProducts(uiProducts);
                    } else {
                        Toast.makeText(getContext(),
                          "Không lấy được danh sách sản phẩm đấu giá",
                          Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(),
                      "Lỗi server: " + response.code(),
                      Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<AdminProductListResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                t.printStackTrace();
                Toast.makeText(getContext(),
                  "Lỗi kết nối: " + t.getMessage(),
                  Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /** Map từ AdminProduct (API) → Product UI model */
    private List<Product> mapAuctionProducts(List<AdminProductListResponse.AdminProduct> items) {
        List<Product> result = new ArrayList<>();
        
        for (AdminProductListResponse.AdminProduct item : items) {
            Product p = new Product();
            
            p.setId(item.id);
            p.setName(item.name);
            p.setPrice(item.price);
            p.setQuantity(item.quantity);
            p.setPostedDate(item.createdAt);
            
            // Ảnh → list 1 phần tử
            List<String> imageUrls = new ArrayList<>();
            if (item.thumbnail != null && !item.thumbnail.isEmpty()) {
                imageUrls.add(item.thumbnail);
            }
            p.setImageUrls(imageUrls);
            
            // ⭐ type cho adapter
            p.setType("auction");
            
            // ⭐ Convert thời gian hết hạn (ISO → milliseconds)
            try {
                if (item.auctionEndsAt != null && !item.auctionEndsAt.isEmpty()) {
                    long endTimeMillis =
                      java.time.Instant.parse(item.auctionEndsAt).toEpochMilli();
                    p.setEndTime(endTimeMillis);
                }
            } catch (Exception e) {
                e.printStackTrace();
                p.setEndTime(0); // fallback
            }
            
            result.add(p);
        }
        
        return result;
    }
    
    
    @Override
    public void onProductClick(Product product) {
        if (product == null || getView() == null) return;
        
        Bundle bundle = new Bundle();
        bundle.putString("productId", product.getId());
        bundle.putString("productName", product.getName());
        bundle.putString("productType", "auction");
        
        Navigation.findNavController(requireView())
          .navigate(R.id.action_productList_to_productDetailAuction, bundle);
    }
    
    private boolean isAuctionExpired(Product product) {
        // Nếu chưa map endTime từ API, nhiều khả năng getEndTime() = 0 → luôn expired.
        // Sau này khi backend trả endTime thì map lại ở mapAuctionProducts().
        return product.getEndTime() < System.currentTimeMillis();
    }
}
