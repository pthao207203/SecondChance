package com.example.secondchance.ui.message;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentMessageDetailBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageDetailFragment extends Fragment {

    private FragmentMessageDetailBinding binding;
    private MessageDetailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NHẬN DỮ LIỆU QUA BUNDLE – CHẠY NGAY, KHÔNG CẦN SAFE ARGS
        Bundle args = getArguments();
        String senderName = "Chat";
        String senderAvatar = null;

        if (getArguments() != null) {
            senderName = getArguments().getString("senderName", "Chat");
            senderAvatar = getArguments().getString("senderAvatar");
        }

        // HIỆN TÊN LÊN TOOLBAR (dùng TextView trong layout)
        binding.tvToolbarTitle.setText(senderName);

        // HIỆN AVATAR LÊN TOOLBAR
        if (senderAvatar != null && !senderAvatar.isEmpty()) {
            Glide.with(this)
                    .load(senderAvatar)
                    .placeholder(R.drawable.avatar1)
                    .circleCrop()
                    .into(binding.imgToolbarAvatar);
        } else {
            binding.imgToolbarAvatar.setImageResource(R.drawable.avatar1);
        }

        setupRecyclerView();
        loadFakeData();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        adapter = new MessageDetailAdapter();
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMessages.setAdapter(adapter);
    }

    private void loadFakeData() {
        List<MessageDetail> list = new ArrayList<>();
        long now = System.currentTimeMillis();

        list.add(new MessageDetail("Dạ shop ơi váy còn không ạ?", "14:00", now - 86400000L * 2, false, "https://i.imgur.com/shop.jpg"));
        list.add(new MessageDetail("Còn chị ơi", "14:02", now - 86400000L * 2, false, "https://i.imgur.com/shop.jpg"));
        list.add(new MessageDetail("Size M nha shop", "14:03", now, true, "https://i.imgur.com/me.jpg"));
        list.add(new MessageDetail("Cảm ơn shop nhiềuuu", "14:05", now, true, "https://i.imgur.com/me.jpg"));

        // ĐÃ SỬA: DÙNG HÀM MỚI
        adapter.submitMessagesWithDate(list);
        binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void sendMessage() {
        String text = binding.edtMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            MessageDetail msg = new MessageDetail(text, time, System.currentTimeMillis(), true, "https://i.imgur.com/me.jpg");

            // ĐÃ SỬA: DÙNG HÀM MỚI
            adapter.addMessageWithDate(msg);

            binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
            binding.edtMessage.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}