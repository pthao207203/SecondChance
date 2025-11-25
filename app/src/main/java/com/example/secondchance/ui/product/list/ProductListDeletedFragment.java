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
import com.example.secondchance.ui.product.adapter.ProductDeletedAdapter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListDeletedFragment extends Fragment implements ProductDeletedAdapter.OnProductClickListener {
    
    private static final String ARG_TAB_TYPE = "tab_type";
    private String tabType;
    private RecyclerView recyclerView;
    private ProductDeletedAdapter adapter;
    
    public static ProductListDeletedFragment newInstance(String tabType) {
        ProductListDeletedFragment fragment = new ProductListDeletedFragment();
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
        adapter = new ProductDeletedAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    /** ⭐ Load ALL deleted products */
    private void loadProductsFromApi() {
        
        if (!"deleted".equals(tabType)) {
            adapter.setProducts(new ArrayList<>());
            return;
        }
        
        ProductApi api = RetrofitProvider.product();
        
        // priceType = null, showDeleted = true
        Call<AdminProductListResponse> call =
          api.getAdminProducts(
            null,   // tất cả priceType
            true,   // ⭐ show deleted items
            1,
            50
          );
        
        call.enqueue(new Callback<AdminProductListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminProductListResponse> call,
                                   @NonNull Response<AdminProductListResponse> response) {
                
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    AdminProductListResponse body = response.body();
                    
                    if (body.success && body.data != null && body.data.items != null) {
                        adapter.setProducts(mapDeletedProducts(body.data.items));
                    } else {
                        Toast.makeText(getContext(), "Không có sản phẩm đã xoá", Toast.LENGTH_SHORT).show();
                    }
                    
                } else {
                    Toast.makeText(getContext(),
                      "Lỗi server " + response.code(),
                      Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<AdminProductListResponse> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                t.printStackTrace();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    
    /** ⭐ Convert AdminProduct → Product */
    private List<Product> mapDeletedProducts(List<AdminProductListResponse.AdminProduct> items) {
        List<Product> result = new ArrayList<>();
        
        for (AdminProductListResponse.AdminProduct item : items) {
            Product p = new Product();
            
            p.setId(item.id);
            p.setName(item.name);
            p.setPrice(item.price);
            p.setQuantity(item.quantity);
            p.setPostedDate(item.createdAt);
            
            // Thumbnail
            List<String> urls = new ArrayList<>();
            if (item.thumbnail != null && !item.thumbnail.isEmpty()) {
                urls.add(item.thumbnail);
            }
            p.setImageUrls(urls);
            
            // ⭐ Map priceType → internal type
            String type = "";
            if (item.priceType != null) {
                switch (item.priceType) {
                    case "Giá cố định":
                        type = "fixed";
                        break;
                    case "Thương lượng":
                        type = "negotiable";
                        break;
                    case "Đấu giá":
                        type = "auction";
                        break;
                }
            }
            p.setType(type);
            
            // ⭐ Nếu có auctionEndsAt thì set
            if (item.auctionEndsAt != null) {
                try {
                    long endMillis = Instant.parse(item.auctionEndsAt).toEpochMilli();
                    p.setEndTime(endMillis);
                } catch (Exception ignore) {
                    p.setEndTime(0);
                }
            }
            
            result.add(p);
        }
        
        return result;
    }
    
    @Override
    public void onProductClick(Product product) {
        Bundle bundle = new Bundle();
        bundle.putString("productId", product.getId());
        bundle.putString("productName", product.getName());
        bundle.putFloat("price", (float) product.getPrice());
        bundle.putInt("quantity", product.getQuantity());
        bundle.putStringArrayList("imageUrls", new ArrayList<>(product.getImageUrls()));
        
        Navigation.findNavController(requireView())
          .navigate(R.id.action_productList_to_productDetailDeleted, bundle);
    }
}
