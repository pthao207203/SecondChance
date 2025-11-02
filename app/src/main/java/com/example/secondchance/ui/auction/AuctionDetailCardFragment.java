package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil; // QUAN TRỌNG: Dùng cái này
import androidx.fragment.app.Fragment;
import com.example.secondchance.R; // Đảm bảo bạn import R

// 1. Import class Binding của bạn (từ tên file item_auction_goingon_card.xml)
import com.example.secondchance.databinding.ItemAuctionGoingonCardBinding;

public class AuctionDetailCardFragment extends Fragment { // 2. Tên class MỚI

    private ItemAuctionGoingonCardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 3. Dùng DataBindingUtil.inflate vì file XML của bạn có thẻ <layout>
        binding = DataBindingUtil.inflate(inflater, R.layout.item_auction_goingon_card, container, false);

        // 4. (Tùy chọn) Gán dữ liệu (DataBinding)
        // Ví dụ: Lấy ID sản phẩm từ argument, gọi ViewModel, rồi gán
        // AuctionGoingOn data = viewModel.getAuctionDetails(auctionId);
        // binding.setRequest(data);
        // binding.setHasReply(false);

        // 5. Trả về root của layout đã binding
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}
