package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerAuctionGoingonBinding;
import com.example.secondchance.dto.response.AuctionListResponse;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuctionGoingOnFragment extends Fragment {
    
    private FragmentRecyclerAuctionGoingonBinding binding;
    private AuctionGoingOnAdapter adapter;
    private ProductApi productApi;
    private final NumberFormat vndFmt = NumberFormat.getInstance(new Locale("vi","VN"));
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerAuctionGoingonBinding.inflate(inflater, container, false);
        setupRecyclerView();
        productApi = RetrofitProvider.product();
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Gọi API khi view sẵn sàng
        fetchAuctions(1, 10);
    }
    
    private void setupRecyclerView() {
        binding.recyclerViewAuctionGoingOn.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AuctionGoingOnAdapter(new ArrayList<>());
        binding.recyclerViewAuctionGoingOn.setAdapter(adapter);
    }
    
    private void fetchAuctions(int page, int pageSize) {
        productApi.getAuctions(page, pageSize).enqueue(new Callback<AuctionListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuctionListResponse> call,
                                   @NonNull Response<AuctionListResponse> response) {
                if (!isAdded()) return;
                AuctionListResponse body = response.body();
                if (response.isSuccessful() && body != null && body.success && body.data != null) {
                    List<AuctionGoingOn> mapped = mapToUi(body.data.items);
                    Gson gson = new Gson();
                    String json = gson.toJson(mapped);
                    Log.d("AuctionGoingOnFragment", "onResponse: " + json);
                    
                    adapter = new AuctionGoingOnAdapter(mapped);
                    binding.recyclerViewAuctionGoingOn.setAdapter(adapter);
                } else {
                    // TODO: show empty state
                    adapter = new AuctionGoingOnAdapter(new ArrayList<>());
                    binding.recyclerViewAuctionGoingOn.setAdapter(adapter);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<AuctionListResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                // TODO: show error UI/toast
            }
        });
    }
    
    private List<AuctionGoingOn> mapToUi(List<AuctionListResponse.Item> items) {
        List<AuctionGoingOn> out = new ArrayList<>();
        if (items == null) return out;
        
        for (AuctionListResponse.Item it : items) {
            long endMillis = 0L;
            try {
                // "2025-11-11T08:00:00.000Z" -> millis UTC
                endMillis = ZonedDateTime.parse(it.endsAt).toInstant().toEpochMilli();
            } catch (Exception ignore) {}
            
            String priceDisplay = "₫" + vndFmt.format(it.currentPrice);
            
            out.add(new AuctionGoingOn(
              it.title,
              priceDisplay,
              it.quantity,
              it.imageUrl,   // adapter của bạn đang .load(url)
              endMillis,
              it.productId
            ));
        }
        return out;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
