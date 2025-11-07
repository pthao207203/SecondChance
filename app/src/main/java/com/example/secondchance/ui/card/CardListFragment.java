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
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.repo.HomeRepository;
import com.example.secondchance.databinding.FragmentRecyclerCardBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CardListFragment extends Fragment implements CardListAdapter.OnItemClickListener {
    
    private static final String ARG_PRODUCTS = "arg_products"; // 🔑 KEY
    
    // newInstance để truyền data ngay lúc tạo fragment
    public static CardListFragment newInstance(ArrayList<ProductCard> items) {
        CardListFragment f = new CardListFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_PRODUCTS, items); // ProductCard phải implements Serializable
        f.setArguments(b);
        return f;
    }
    
    private FragmentRecyclerCardBinding binding;
    private CardListAdapter adapter;
    private CardListViewModel viewModel;
    private Map<Integer, Integer> columnHeights = new HashMap<>();
    
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
        viewModel = new ViewModelProvider(this).get(CardListViewModel.class);
        
        setupRecyclerView();
        
        // 1) ƯU TIÊN dùng dữ liệu được truyền qua arguments
        ArrayList<ProductCard> passed = null;
        Bundle args = getArguments();
        if (args != null) {
            Serializable s = args.getSerializable(ARG_PRODUCTS);
            if (s instanceof ArrayList) {
                //noinspection unchecked
                passed = (ArrayList<ProductCard>) s;
            }
        }
        
        if (passed != null && !passed.isEmpty()) {
            Log.d("CardFragment", "✅ Using passed data: " + passed.size());
            viewModel.setProducts(passed);
        } else {
            Log.d("CardFragment", "ℹ️ No passed data -> fallback to API");
            loadFromApi(); // GIỮ HÀNH VI CŨ KHI KHÔNG TRUYỀN GÌ
        }
        
        observeData();
    }
    
    private void setupRecyclerView() {
        Log.d("CardFragment", "⚙️ Setting up RecyclerView");
        StaggeredGridLayoutManager layoutManager =
          new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.recyclerView.setLayoutManager(layoutManager);
        
        // QUAN TRỌNG: false để ScrollView cuộn mượt, RecyclerView không tranh cuộn
        binding.recyclerView.setNestedScrollingEnabled(false);
        
        adapter = new CardListAdapter(requireContext(), new ArrayList<>(), this);
        binding.recyclerView.setAdapter(adapter);
    }
    
    // Cho phép các màn khác (hoặc Home khi fragment đã tồn tại) bơm lại data
    public void setExternalData(List<ProductCard> items) {
        if (!isAdded()) return;
        if (items == null) items = new ArrayList<>();
        viewModel.setProducts(items);
    }
    
    private void loadFromApi() {
        HomeRepository repo = new HomeRepository();
        repo.fetchHome(new HomeRepository.HomeCallback() {
            @Override public void onSuccess(HomeApi.HomeEnvelope.Data data) {
                if (!isAdded()) return;
                List<HomeApi.SuggestionItem> src = (data!=null && data.suggestions!=null) ? data.suggestions.items : null;
                List<ProductCard> mapped = mapSuggestionsToCards(src);
                viewModel.setProducts(mapped);
            }
            @Override public void onError(String message) {
                if (isAdded())
                    Toast.makeText(requireContext(), "Tải danh sách thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private List<ProductCard> mapSuggestionsToCards(List<HomeApi.SuggestionItem> items) {
        List<ProductCard> out = new ArrayList<>();
        if (items == null) return out;
        
        for (HomeApi.SuggestionItem it : items) {
            ProductCard.ProductType type = decideType(it);
            
            ProductCard pc = new ProductCard();
            pc.setTitle(it.title);
            pc.setDescription(it.conditionLabel != null ? it.conditionLabel : "");
            pc.setQuantity(it.quantity);
            pc.setProductType(type);
            pc.setImageUrl(it.imageUrl);
            
            String priceText = it.currentPrice > 0 ? formatVnd(it.currentPrice) : "—";
            pc.setPrice(priceText);
            
            if (type == ProductCard.ProductType.AUCTION) {
                pc.setTimeRemaining(formatEndsIn(Math.max(0, it.endsInSec)));
            }
            out.add(pc);
        }
        return out;
    }
    
    private ProductCard.ProductType decideType(HomeApi.SuggestionItem it) {
        if (it.endsInSec > 0) return ProductCard.ProductType.AUCTION;
        String label = it.conditionLabel != null ? it.conditionLabel.toLowerCase(Locale.ROOT) : "";
        if (label.contains("negotiation") || label.contains("offer") || label.contains("deal")
          || label.contains("bargain") || label.contains("thương lượng")) {
            return ProductCard.ProductType.NEGOTIATION;
        }
        return ProductCard.ProductType.FIXED;
    }
    
    private String formatEndsIn(long sec) {
        sec = Math.max(0, sec);
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }
    
    private String formatVnd(long v) {
        return String.format("%,d", v).replace(',', '.');
    }
    
    private List<ProductCard> sortAndDistributeByColumnHeight(List<ProductCard> products) {
        Collections.sort(products, (p1, p2) -> p2.getPostTime().compareTo(p1.getPostTime()));
        List<ProductCard> sortedList = new ArrayList<>();
        columnHeights.clear();
        for (ProductCard product : products) {
            int shortestColumn = findShortestColumn();
            sortedList.add(product);
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
            case AUCTION:
            case NEGOTIATION:
            default:
                return 129;
        }
    }
    
    private void observeData() {
        Log.d("CardFragment", "👀 Starting to observe ViewModel data");
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            Log.d("CardFragment", "🔄 OBSERVED PRODUCTS: " + (products != null ? products.size() : 0));
            if (products != null) {
                adapter.updateData(products);
            } else {
                Log.e("CardFragment", "❌ Products is NULL!");
            }
        });
    }
    
    @Override
    public void onItemClick(ProductCard product) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", product);
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_home_navigation_detail_product, bundle);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
