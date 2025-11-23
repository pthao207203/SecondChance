package com.example.secondchance.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentMessageBinding;

public class MessageFragment extends Fragment {

    private FragmentMessageBinding binding;
    private String currentTab = "shop"; // mặc định là Shop

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(inflater, container, false);

        // Hiện luôn tab Shop + danh sách khi mở app
        updateTabUI();
        replaceFragment(new ListShopMessageFragment());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTabClicks();
    }

    private void setupTabClicks() {
        binding.tabKhach.setOnClickListener(v -> selectTabKhach());
        binding.tabShop.setOnClickListener(v -> selectTabShop());
    }

    private void selectTabKhach() {
        if (currentTab.equals("khach")) return;
        currentTab = "khach";
        updateTabUI();
        replaceFragment(new ListUserMessageFragment());
    }

    private void selectTabShop() {
        if (currentTab.equals("shop")) return;
        currentTab = "shop";
        updateTabUI();
        replaceFragment(new ListShopMessageFragment());
    }

    private void updateTabUI() {
        if (currentTab.equals("khach")) {
            binding.tabKhach.setBackgroundResource(R.color.highlight4blur);
            binding.indicatorKhach.setVisibility(View.VISIBLE);

            binding.tabShop.setBackgroundResource(R.color.whiteDay);
            binding.indicatorShop.setVisibility(View.INVISIBLE);
        } else {
            binding.tabShop.setBackgroundResource(R.color.highlight4blur);
            binding.indicatorShop.setVisibility(View.VISIBLE);

            binding.tabKhach.setBackgroundResource(R.color.whiteDay);
            binding.indicatorKhach.setVisibility(View.INVISIBLE);
        }
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)
                .setReorderingAllowed(true)
                .commit(); // ĐỔI TỪ commitNow() THÀNH commit() → SIÊU QUAN TRỌNG!!!
    }

    // HÀM NÀY SẼ ĐƯỢC GỌI TỪ ListUserMessageFragment VÀ ListShopMessageFragment
    public void openChatDetail(Message message) {
        Bundle bundle = new Bundle();
        bundle.putString("senderName", message.getSenderName());
        bundle.putString("senderAvatar", message.getAvatarUrl());
        bundle.putString("senderId", message.getSenderId() != null ? message.getSenderId() : "");

        NavHostFragment.findNavController(this)
                .navigate(R.id.navigation_detail_message, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}