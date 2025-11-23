package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.data.repo.HomeRepository;
import com.example.secondchance.databinding.FragmentAuctionBinding;
import com.example.secondchance.dto.response.AuctionListResponse;
import com.example.secondchance.ui.card.ProductCard;
import com.example.secondchance.ui.home.CategoryAdapter;
import com.google.gson.Gson;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuctionFragment extends Fragment {
    
    private FragmentAuctionBinding binding;
    private AuctionViewModel vm;
    private ProductApi productApi;
    private CategoryAdapter catAdapter;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuctionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(this).get(AuctionViewModel.class);
//        productApi = RetrofitProvider.product();
        loadAuctionData(1, 10);
        
        
        RecyclerView rv = view.findViewById(R.id.rvCategories);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(new CategoryAdapter());
        catAdapter = (CategoryAdapter) rv.getAdapter();
    }
    
    private void loadAuctionData(int page, int pageSize) {
//        productApi.getAuctions(page, pageSize).enqueue(new Callback<AuctionListResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<AuctionListResponse> call,
//                                   @NonNull Response<AuctionListResponse> response) {
//                if (!isAdded()) return;
//                AuctionListResponse body = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(body);
//                Log.d("AuctionFragment", "onResponse: " + json);
//
//                if (response.isSuccessful() && body != null && body.success && body.data != null) {
//                    vm.setItems(body.data.items);
//                } else {
//                    // TODO: show empty/error state nếu cần
//                    vm.setItems(null);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AuctionListResponse> call, @NonNull Throwable t) {
//                if (!isAdded()) return;
//                // TODO: show error UI
//                vm.setItems(null);
//            }
//        });
//
        HomeRepository repo = new HomeRepository();
        repo.fetchHome(new HomeRepository.HomeCallback() {
            @Override
            public void onSuccess(HomeApi.HomeEnvelope.Data data) {
                if (!isAdded()) return;

                // ==== 2) Bind categories (như cũ) =====
                if (data != null && data.categories != null) {
                    for (HomeApi.Category c : data.categories) {
                        if (c.icon != null) c.icon = c.icon.replace('-', '_');
                    }
                    catAdapter.submit(data.categories);
                }
            }
            @Override public void onError(String message) {
                if (isAdded())
                    Toast.makeText(requireContext(), "Tải Home thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
