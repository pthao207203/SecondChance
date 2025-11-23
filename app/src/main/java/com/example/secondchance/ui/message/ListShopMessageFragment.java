package com.example.secondchance.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.databinding.FragmentListShopMessageBinding;

import java.util.ArrayList;
import java.util.List;

public class ListShopMessageFragment extends Fragment {

    private FragmentListShopMessageBinding binding;
    private ShopMessageAdapter adapter;
    private List<Message> shopMessageList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListShopMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadFakeData(); // sau này thay bằng load từ Firebase
    }

    private void setupRecyclerView() {
        adapter = new ShopMessageAdapter(shopMessageList, message -> {
            // ĐOẠN NÀY LÀ LINH HỒN – BẮT BUỘC PHẢI CÓ!!!
            MessageFragment parent = (MessageFragment) getParentFragment();
            if (parent != null) {
                parent.openChatDetail(message);
            }
        });

        binding.rvShopMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvShopMessages.setAdapter(adapter);
    }

    private void loadFakeData() {
        shopMessageList.clear();

        long now = System.currentTimeMillis();
        shopMessageList.add(new Message(
                "s1", "shop123", "Shop Thời Trang Xinh Xinh",
                "Cảm ơn chị đã ủng hộ shop nha", now - 60*1000,
                "https://i.imgur.com/shop1.jpg", true, true
        ));

        shopMessageList.add(new Message(
                "s2", "shop456", "MyphamChinhHang VN",
                "Dạ size M còn 3 cái thôi chị ơi", now - 2*60*60*1000,
                "https://i.imgur.com/shop2.jpg", true, false
        ));

        shopMessageList.add(new Message(
                "s3", "shop789", "Đồ Ăn Vặt Ngon",
                "Hàng mới về chị vào xem nha", now - 24*60*60*1000,
                "https://i.imgur.com/shop3.jpg", true, false
        ));

        adapter.updateData(shopMessageList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}