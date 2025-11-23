package com.example.secondchance.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.databinding.FragmentListUserMessageBinding;

import java.util.ArrayList;
import java.util.List;

public class ListUserMessageFragment extends Fragment {

    private FragmentListUserMessageBinding binding;
    private UserMessageAdapter adapter;
    private List<Message> userMessageList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListUserMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadFakeData();
    }

    private void setupRecyclerView() {
        adapter = new UserMessageAdapter(userMessageList, message -> {
            MessageFragment parent = (MessageFragment) getParentFragment();
            if (parent != null) {
                parent.openChatDetail(message);
            }
        });

        binding.rvUserMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUserMessages.setAdapter(adapter);
    }

    private void loadFakeData() {
        userMessageList.clear();

        long now = System.currentTimeMillis();
        userMessageList.add(new Message(
                "u1", "user123", "Chị Lan Anh",
                "Shop ơi váy này còn size S không ạ?", now - 30*1000,
                "https://i.imgur.com/user1.jpg", false, true
        ));

        userMessageList.add(new Message(
                "u2", "user456", "Em Minh Thư",
                "Dạ em chốt đơn rồi ạ", now - 5*60*60*1000,
                "https://i.imgur.com/user2.jpg", false, false
        ));

        adapter.updateData(userMessageList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}