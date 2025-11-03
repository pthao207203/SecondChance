// ui/auction/AuctionGoingOnFragment.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.databinding.FragmentRecyclerAuctionGoingonBinding;

import java.util.ArrayList;
import java.util.List;

public class AuctionGoingOnFragment extends Fragment {

    private FragmentRecyclerAuctionGoingonBinding binding;
    private AuctionGoingOnAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerAuctionGoingonBinding.inflate(inflater, container, false);

        setupRecyclerView();
        loadData();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        binding.recyclerViewAuctionGoingOn.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AuctionGoingOnAdapter(new ArrayList<>());
        binding.recyclerViewAuctionGoingOn.setAdapter(adapter);
    }

    private void loadData() {
        List<AuctionGoingOn> list = new ArrayList<>();

        // Dữ liệu mẫu (thay bằng API sau)
        long endTime = System.currentTimeMillis() + 2 * 60 * 60 * 1000 + 39 * 60 * 1000 + 12 * 1000; // 2h39m12s
        list.add(new AuctionGoingOn(
                "Vòng hoa hướng dương vàng",
                "₫8.500.000",
                1,
                "https://example.com/hoa1.jpg", // hoặc dùng drawable: String.valueOf(R.drawable.nhan1)
                endTime
        ));

        list.add(new AuctionGoingOn(
                "Nhẫn kim cương 1 carat",
                "₫45.000.000",
                1,
                "https://example.com/nhan.jpg",
                System.currentTimeMillis() + 5 * 60 * 1000 // 5 phút
        ));

        adapter = new AuctionGoingOnAdapter(list);
        binding.recyclerViewAuctionGoingOn.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
