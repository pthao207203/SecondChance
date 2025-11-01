package com.example.secondchance.ui.product.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.product.SampleProductData;
import com.example.secondchance.ui.product.Product;
import com.example.secondchance.ui.product.adapter.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment {

    private static final String ARG_TAB_TYPE = "tab_type";
    private String tabType;
    private RecyclerView recyclerView;

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
        loadProducts();
        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // The adapter is now specific to the product type via ProductViewPagerAdapter
        ProductAdapter defaultAdapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                handleDefaultNavigation(product);
            }

            @Override
            public void onViewDetailsClick(Product product) {
                handleDefaultNavigation(product);
            }
        });
        recyclerView.setAdapter(defaultAdapter);
    }

    private void loadProducts() {
        List<Product> products = getProductsByType(tabType);
        if (recyclerView.getAdapter() instanceof ProductAdapter) {
            ((ProductAdapter) recyclerView.getAdapter()).setProducts(products);
        }
    }

    private List<Product> getProductsByType(String type) {
        if ("fixed".equals(type)) {
            return SampleProductData.getFixedProducts();
        }
        return new ArrayList<>();
    }

    private void handleDefaultNavigation(Product product) {
        // This fragment is now only for 'fixed' type
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
