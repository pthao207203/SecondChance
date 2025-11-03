package com.example.secondchance.ui.card;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentRecyclerCardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class CardListFragment extends Fragment implements CardListAdapter.OnItemClickListener {

    private FragmentRecyclerCardBinding binding;
    private CardListAdapter adapter;
    private CardListViewModel viewModel;
    private Map<Integer, Integer> columnHeights = new HashMap<>(); // Theo dõi chiều cao của các cột

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recycler_card, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("CardFragment", "🔥 Fragment onViewCreated");

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CardListViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Load sample data
        loadSampleData();

        // Observe data
        observeData();
    }

    private void setupRecyclerView() {
        Log.d("CardFragment", "⚙️ Setting up RecyclerView");
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setNestedScrollingEnabled(true); // Ngăn RecyclerView cuộn riêng
        adapter = new CardListAdapter(requireContext(), new ArrayList<>(), this);
        binding.recyclerView.setAdapter(adapter);
    }
    private void loadSampleData() {
        List<ProductCard> products = new ArrayList<>();

        Log.d("CardFragment", "=== LOADING SAMPLE DATA ===");

        // Hiện tại (10:40 PM +07, 22/10/2025)
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.OCTOBER, 22, 22, 40); // 22/10/2025 22:40
        Date now = cal.getTime();

        // Product 1: AUCTION - 1 giờ trước (tên dài 2-3 dòng)
        cal.add(Calendar.HOUR_OF_DAY, -1);
        Date postTime1 = cal.getTime();
        ProductCard auctionProduct = new ProductCard(
                1, R.drawable.binhhoa, "Bình hoa sứ trắng cao cấp với hoa văn dát vàng tinh xảo",
                "Hoa văn dát vàng handmade", 1, 4.2f, "1.200.000", ProductCard.ProductType.AUCTION, postTime1, 100);
        auctionProduct.setTimeRemaining("01:00:00");
        products.add(auctionProduct);
        Log.d("CardFragment", "✅ ADDED AUCTION: " + auctionProduct.getTitle() + " | ImageRes: " + auctionProduct.getImageRes() + " | PostTime: " + postTime1);

        // Product 2: NEGOTIATION - 2 ngày trước (tên ngắn 1 dòng)
        cal.set(2025, Calendar.OCTOBER, 22, 22, 40);
        cal.add(Calendar.DAY_OF_MONTH, -2);
        Date postTime2 = cal.getTime();
        ProductCard negotiationProduct = new ProductCard(
                2, R.drawable.nhan1, "Nhẫn bạc 925",
                "Giỏ gỗ New 99,9%", 2, 4.0f, "300.000", ProductCard.ProductType.NEGOTIATION, postTime2, 150);
        products.add(negotiationProduct);
        Log.d("CardFragment", "✅ ADDED NEGOTIATION: " + negotiationProduct.getTitle() + " | ImageRes: " + negotiationProduct.getImageRes() + " | PostTime: " + postTime2);

        // Product 3: FIXED - 5 ngày trước (tên dài 2-3 dòng)
        cal.set(2025, Calendar.OCTOBER, 22, 22, 40);
        cal.add(Calendar.DAY_OF_MONTH, -5);
        Date postTime3 = cal.getTime();
        ProductCard fixedProduct = new ProductCard(
                3, R.drawable.nhan1, "Nhẫn kim cương cao cấp với thiết kế độc quyền",
                "Giỏ gỗ New 99,9%", 1, 4.5f, "500.000", ProductCard.ProductType.FIXED, postTime3, 120);
        products.add(fixedProduct);
        Log.d("CardFragment", "✅ ADDED FIXED: " + fixedProduct.getTitle() + " | ImageRes: " + fixedProduct.getImageRes() + " | PostTime: " + postTime3);

        // Product 4: NEGOTIATION - 1 ngày trước (tên dài 2-3 dòng)
        cal.set(2025, Calendar.OCTOBER, 22, 22, 40);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date postTime4 = cal.getTime();
        ProductCard negotiationProduct2 = new ProductCard(
                4, R.drawable.nhan1, "Vòng cổ vàng 18K đính đá quý cao cấp",
                "Thiết kế sang trọng và bền đẹp", 2, 4.3f, "2.000.000", ProductCard.ProductType.NEGOTIATION, postTime4, 150);
        products.add(negotiationProduct2);
        Log.d("CardFragment", "✅ ADDED NEGOTIATION: " + negotiationProduct2.getTitle() + " | ImageRes: " + negotiationProduct2.getImageRes() + " | PostTime: " + postTime4);

        // Product 5: AUCTION - 3 giờ trước (tên ngắn 1 dòng)
        cal.set(2025, Calendar.OCTOBER, 22, 22, 40);
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Date postTime5 = cal.getTime();
        ProductCard auctionProduct2 = new ProductCard(
                5, R.drawable.binhhoa, "Bình hoa nhỏ",
                "Thiết kế đơn giản", 1, 4.0f, "800.000", ProductCard.ProductType.AUCTION, postTime5, 100);
        auctionProduct2.setTimeRemaining("02:30:00");
        products.add(auctionProduct2);
        Log.d("CardFragment", "✅ ADDED AUCTION: " + auctionProduct2.getTitle() + " | ImageRes: " + auctionProduct2.getImageRes() + " | PostTime: " + postTime5);

        // Product 6: FIXED - 7 ngày trước (tên ngắn 1 dòng)
        cal.set(2025, Calendar.OCTOBER, 22, 22, 40);
        cal.add(Calendar.DAY_OF_MONTH, -22);
        Date postTime6 = cal.getTime();
        ProductCard fixedProduct2 = new ProductCard(
                6, R.drawable.nhan1, "Bút bi cao cấp",
                "Chất liệu thép không gỉ", 1, 4.1f, "150.000", ProductCard.ProductType.FIXED, postTime6, 120);
        products.add(fixedProduct2);
        Log.d("CardFragment", "✅ ADDED FIXED: " + fixedProduct2.getTitle() + " | ImageRes: " + fixedProduct2.getImageRes() + " | PostTime: " + postTime6);

        // Sắp xếp và cập nhật ViewModel
        products = sortAndDistributeByColumnHeight(products);
        viewModel.setProducts(products);
        Log.d("CardFragment", "📊 TOTAL PRODUCTS: " + products.size());
        Log.d("CardFragment", "=== SAMPLE DATA LOADED ===");
    }

    private List<ProductCard> sortAndDistributeByColumnHeight(List<ProductCard> products) {
        Collections.sort(products, (p1, p2) -> p2.getPostTime().compareTo(p1.getPostTime()));
        List<ProductCard> sortedList = new ArrayList<>();
        columnHeights.clear();

        for (ProductCard product : products) {
            int shortestColumn = findShortestColumn();
            sortedList.add(product);
            // Sử dụng chiều cao ảnh làm giá trị khởi tạo
            int imageHeight = getImageHeightForProductType(product.getProductType());
            columnHeights.put(shortestColumn, columnHeights.getOrDefault(shortestColumn, 0) + imageHeight);
        }
        return sortedList;
    }

    private int findShortestColumn() {
        int column0Height = columnHeights.getOrDefault(0, 0);
        int column1Height = columnHeights.getOrDefault(1, 0);
        return column0Height <= column1Height ? 0 : 1;
    }

    private int getImageHeightForProductType(ProductCard.ProductType type) {
        switch (type) {
            case FIXED:
                return 129; // dp
            case AUCTION:
                return 129; // dp
            case NEGOTIATION:
                return 129; // dp
            default:
                return 129; // Default
        }
    }

    private void observeData() {
        Log.d("CardFragment", "👀 Starting to observe ViewModel data");
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            Log.d("CardFragment", "🔄 OBSERVED PRODUCTS: " + (products != null ? products.size() : 0));
            if (products != null) {
                Log.d("CardFragment", "📋 Products received: ");
                for (int i = 0; i < products.size(); i++) {
                    ProductCard p = products.get(i);
                    Log.d("CardFragment", "  [" + i + "] " + p.getTitle() + " | Type: " + p.getProductType());
                }
                adapter.updateData(products);
            } else {
                Log.e("CardFragment", "❌ Products is NULL!");
            }
        });
    }

    @Override
    public void onItemClick(ProductCard product) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", product); // Truyền toàn bộ object

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_home_navigation_detail_product, bundle);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
