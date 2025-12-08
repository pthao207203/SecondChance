// ui/auction/AuctioneerFragment.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentRecyclerAuctioneerBinding;
import com.example.secondchance.dto.response.AuctionListUserResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AuctioneerFragment extends Fragment {

    private FragmentRecyclerAuctioneerBinding binding;
    private AuctioneerAdapter adapter;
    private final Gson gson = new Gson();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerAuctioneerBinding.inflate(inflater, container, false);
        setupRecyclerView();
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        
        getParentFragmentManager().setFragmentResultListener(
          AuctionDetailFragment.KEY_AUCTION_BIDS, this, (k, b) -> {
            
            String jsonBids = b.getString(AuctionDetailFragment.KEY_BID_LIST_JSON);
            String jsonUser = b.getString(AuctionDetailFragment.KEY_CURRENT_USER_JSON);
            
            if (jsonBids == null) return;

            Type t = new TypeToken<List<AuctionListUserResponse.Bid>>(){}.getType();
            List<AuctionListUserResponse.Bid> allBids = gson.fromJson(jsonBids, t);
            AuctionListUserResponse.CurrentUser currentUser = null;
            
            if (jsonUser != null) {
                currentUser = gson.fromJson(jsonUser, AuctionListUserResponse.CurrentUser.class);
            }

            List<AuctionListUserResponse.Bid> otherBidders = new ArrayList<>();
            if (allBids != null && !allBids.isEmpty()) {
                // Tìm bid mới nhất để loại bỏ khỏi danh sách "người khác" (vì thường hiển thị riêng ở header)
                // Hoặc theo logic cũ là bỏ người thắng cuộc khỏi list này?
                // Theo logic thông thường: List này hiện lịch sử những người khác.
                
                int latestIdx = indexOfLatest(allBids);
                
                for (int i = 0; i < allBids.size(); i++) {
                    // Nếu muốn ẩn bid cao nhất khỏi list này (vì đã hiện ở banner chính), giữ dòng dưới:
                    if (i == latestIdx) continue; 
                    
                    AuctionListUserResponse.Bid bid = allBids.get(i);
                    
                    // --- LOGIC LỌC QUAN TRỌNG ---
                    // Nếu bid này là của TÔI, bỏ qua, không add vào list này.
                    if (isMyBid(bid, currentUser)) {
                        continue;
                    }
                    
                    otherBidders.add(bid);
                }
            }
            
            Log.d("AuctioneerFragment", "Filtered other bidders count: " + otherBidders.size());
            
            if (adapter != null) {
                adapter.updateData(otherBidders);
            } else {
                adapter = new AuctioneerAdapter(otherBidders);
                binding.recyclerViewAuctioneer.setAdapter(adapter);
            }
          }
        );
    }

    // Hàm kiểm tra xem bid có phải của user hiện tại không
    private boolean isMyBid(AuctionListUserResponse.Bid bid, AuctionListUserResponse.CurrentUser me) {
        if (me == null || me.id == null) return false;
        
        // Check ID trực tiếp trong object User
        if (bid.byUser != null && TextUtils.equals(bid.byUser.id, me.id)) return true;
        
        // Check field userId (nếu có)
        if (TextUtils.equals(bid.userId, me.id)) return true;
        
        return false;
    }

    private int indexOfLatest(List<AuctionListUserResponse.Bid> list) {
        if (list == null || list.isEmpty()) return -1;
        int best = 0;
        long bestEpoch = toEpoch(list.get(0).createdAt);
        for (int i = 1; i < list.size(); i++) {
            long t = toEpoch(list.get(i).createdAt);
            if (t > bestEpoch) {
                bestEpoch = t;
                best = i;
            }
        }
        return best;
    }
    
    private long toEpoch(String iso) {
        try { return java.time.Instant.parse(iso).toEpochMilli(); }
        catch (Exception e) { return Long.MIN_VALUE; }
    }

    private void setupRecyclerView() {
        binding.recyclerViewAuctioneer.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AuctioneerAdapter(new ArrayList<>());
        binding.recyclerViewAuctioneer.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
