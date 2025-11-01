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
import com.example.secondchance.ui.product.adapter.ProductDeletedAdapter;

import java.util.ArrayList;
import java.util.List;

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
        loadProducts();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ProductDeletedAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        List<Product> products = getProductsByType(tabType);
        adapter.setProducts(products);
    }

    private List<Product> getProductsByType(String type) {
        if ("deleted".equals(type)) {
            return SampleProductData.getDeletedProducts();
        }
        return new ArrayList<>();
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
