package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.secondchance.databinding.FragmentAuctionBinding;

public class AuctionFragment extends Fragment {

    private FragmentAuctionBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAuctionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Có thể thêm logic load dữ liệu, filter, refresh...
        // Ví dụ: load danh sách đấu giá từ API
        loadAuctionData();
    }

    private void loadAuctionData() {
        // Gọi API hoặc load từ local
        // Sau khi có dữ liệu → truyền vào AuctionGoingOnFragment
        // (nếu cần, dùng ChildFragmentManager)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
