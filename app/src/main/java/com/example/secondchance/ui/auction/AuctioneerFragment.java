// ui/auction/AuctioneerFragment.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerAuctioneerBinding;
import com.example.secondchance.dto.response.AuctionListUserResponse;
import com.example.secondchance.dto.response.ProductDetailResponse;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuctioneerFragment extends Fragment {

    private FragmentRecyclerAuctioneerBinding binding;
    private AuctioneerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerAuctioneerBinding.inflate(inflater, container, false);

        setupRecyclerView();

        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
          AuctionDetailFragment.KEY_AUCTION_BIDS, this, (k, b) -> {
            Type t = new com.google.gson.reflect.TypeToken<List<AuctionListUserResponse.Bid>>(){}.getType();
            List<AuctionListUserResponse.Bid> bids =
              new Gson().fromJson(b.getString(AuctionDetailFragment.KEY_BID_LIST_JSON), t);
              if (bids != null && !bids.isEmpty()) {
                  int latestIdx = indexOfLatest(bids);
                  if (latestIdx >= 0 && latestIdx < bids.size()) {
                      bids.remove(latestIdx);
                  }
              }
            Log.d("AuctioneerFragment", "bids: " + new Gson().toJson(bids));
            
            RecyclerView rv = requireView().findViewById(R.id.recyclerViewAuctioneer);
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.setAdapter(new AuctioneerAdapter(bids));
          }
        );
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
        catch (Exception e) { return Long.MIN_VALUE; } // createdAt null/lỗi → coi như rất cũ
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
