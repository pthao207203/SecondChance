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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListNegotiableFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    
    private static final String ARG_TAB_TYPE = "tab_type";
    private String tabType;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    
    public static ProductListNegotiableFragment newInstance(String tabType) {
        ProductListNegotiableFragment fragment = new ProductListNegotiableFragment();
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
        adapter = new ProductAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    /** Gọi API /admin/products?priceType=2 và bind vào adapter */
    private void loadProductsFromApi() {
        
        if (!"negotiable".equals(tabType)) {
            adapter.setProducts(new ArrayList<>());
            return;
        }
        
        ProductApi api = RetrofitProvider.product();
        
        Call<AdminProductListResponse> call = api.getAdminProducts(
          2,      // ⭐ priceType=2 → hàng thương lượng
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
                        List<Product> uiProducts = mapNegotiableProducts(body.data.items);
                        adapter.setProducts(uiProducts);
                    } else {
                        Toast.makeText(getContext(), "Không lấy được danh sách sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                    
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
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
    
    /** Map list AdminProduct → Product UI model */
    private List<Product> mapNegotiableProducts(List<AdminProductListResponse.AdminProduct> items) {
        List<Product> result = new ArrayList<>();
        
        for (AdminProductListResponse.AdminProduct item : items) {
            Product p = new Product();
            
            p.setId(item.id);
            p.setName(item.name);
            p.setPrice(item.price);
            p.setQuantity(item.quantity);
            p.setPostedDate(item.createdAt);
            
            // Ảnh → list 1 phần tử
            List<String> urls = new ArrayList<>();
            if (item.thumbnail != null && !item.thumbnail.isEmpty()) {
                urls.add(item.thumbnail);
            }
            p.setImageUrls(urls);
            
            // ⭐ type cho adapter
            p.setType("negotiable");
            
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
          .navigate(R.id.action_productList_to_productDetailNegotiable, bundle);
    }
    
    @Override
    public void onViewDetailsClick(Product product) {
        onProductClick(product);
    }
}
