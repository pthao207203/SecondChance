// ui/auction/AuctioneerFragment.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.databinding.FragmentRecyclerAuctioneerBinding;

import java.util.ArrayList;
import java.util.List;

public class AuctioneerFragment extends Fragment {

    private FragmentRecyclerAuctioneerBinding binding;
    private AuctioneerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerAuctioneerBinding.inflate(inflater, container, false);

        setupRecyclerView();
        loadSampleData();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        binding.recyclerViewAuctioneer.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AuctioneerAdapter(new ArrayList<>());
        binding.recyclerViewAuctioneer.setAdapter(adapter);
    }

    private void loadSampleData() {
        List<Auctioneer> list = new ArrayList<>();

        list.add(new Auctioneer(
                "Fish can Fly",
                "10:05:59, 06/08/2024",
                "1.000.000",
                "(+25.000)",
                "Trả giá lần 1",
                com.example.secondchance.R.drawable.avatar2
        ));

        list.add(new Auctioneer(
                "Dragon King",
                "10:06:15, 06/08/2024",
                "1.050.000",
                "(+50.000)",
                "Trả giá lần 2",
                com.example.secondchance.R.drawable.avatar1
        ));

        list.add(new Auctioneer(
                "Phoenix Rise",
                "10:07:02, 06/08/2024",
                "1.200.000",
                "(+150.000)",
                "Trả giá lần 3",
                com.example.secondchance.R.drawable.avatar1
        ));

        adapter = new AuctioneerAdapter(list);
        binding.recyclerViewAuctioneer.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
