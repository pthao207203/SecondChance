package com.example.secondchance.ui.product.list;

import android.os.Bundle;
import android.util.Log;
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
import com.example.secondchance.ui.product.adapter.ProductAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListFragment extends Fragment {
    
    private static final String ARG_TAB_TYPE = "tab_type";
    private String tabType;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    
    public static ProductListFragment newInstance(String tabType) {
        ProductListFragment fragment = new ProductListFragment();
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                handleDefaultNavigation(product);
            }
            
            @Override
            public void onViewDetailsClick(Product product) {
                handleDefaultNavigation(product);
            }
        });
        recyclerView.setAdapter(adapter);
    }
    
    /** Gọi API /admin/products?priceType=1 và bind vào adapter */
    private void loadProductsFromApi() {
        // Fragment này dùng cho tab fixed → lấy priceType = 1
        if (!"fixed".equals(tabType)) {
            adapter.setProducts(new ArrayList<>());
            return;
        }
        
        ProductApi api = RetrofitProvider.product();
        
        Call<AdminProductListResponse> call =
          api.getAdminProducts(
            1,
            false,
            1,   // page
            10   // pageSize
          );
        
        call.enqueue(new Callback<AdminProductListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminProductListResponse> call,
                                   @NonNull Response<AdminProductListResponse> response) {
                if (!isAdded()) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    AdminProductListResponse body = response.body();
                    Gson gson = new Gson();
                    Log.d("ProductListFragment", "onResponse: " + gson.toJson(body));
                    if (body.success && body.data != null && body.data.items != null) {
                        List<Product> uiProducts = mapAdminProductsToUi(body.data.items);
                        adapter.setProducts(uiProducts);
                    } else {
                        Toast.makeText(getContext(),
                          "Không lấy được danh sách sản phẩm",
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
    
    /** Map từ AdminProduct (API) → ui.product.Product (dùng trong adapter) */
    private List<Product> mapAdminProductsToUi(List<AdminProductListResponse.AdminProduct> items) {
        List<Product> result = new ArrayList<>();
        for (AdminProductListResponse.AdminProduct item : items) {
            Product p = new Product();
            
            // ⚠️ Đảm bảo class Product của bạn có các setter tương ứng
            p.setId(item.id);
            p.setName(item.name);
            p.setPrice(item.price);
            p.setQuantity(item.quantity);
            
            // Thumbnail → list 1 phần tử cho imageUrls
            List<String> imageUrls = new ArrayList<>();
            if (item.thumbnail != null && !item.thumbnail.isEmpty()) {
                imageUrls.add(item.thumbnail);
            }
            p.setImageUrls(imageUrls);
            
            result.add(p);
        }
        return result;
    }
    
    private void handleDefaultNavigation(Product product) {
        navigateToDetail(product, R.id.action_productList_to_productDetailFixed);
    }
    
    private void navigateToDetail(Product product, int actionId) {
        Bundle bundle = new Bundle();
        bundle.putString("productId", product.getId());
        bundle.putString("productName", product.getName());
        bundle.putFloat("price", (float) product.getPrice());
        bundle.putInt("quantity", product.getQuantity());
        bundle.putStringArrayList("imageUrls", new ArrayList<>(product.getImageUrls()));
        
        try {
            Navigation.findNavController(requireView()).navigate(actionId, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
