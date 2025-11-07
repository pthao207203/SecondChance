// ui/comment/CommentFragment.java
package com.example.secondchance.ui.comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.data.repo.CommentRepository;
import com.example.secondchance.databinding.FragmentRecyclerCommentBinding;
import java.util.ArrayList;
import java.util.List;

public class CommentFragment extends Fragment {

    private FragmentRecyclerCommentBinding binding;
    private CommentAdapter adapter;
    private CommentRepository repository;
    private String sellerId = "68f72a0b51284f307a9cbad9"; // Thay bằng ID thật hoặc truyền từ ngoài vào

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerCommentBinding.inflate(inflater, container, false);

        // Lấy sellerId từ arguments (nếu có)
        if (getArguments() != null) {
            sellerId = getArguments().getString("sellerId", sellerId);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        repository = new CommentRepository();
        loadComments();
    }

    private void setupRecyclerView() {
        adapter = new CommentAdapter();
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewComments.setAdapter(adapter);
        binding.recyclerViewComments.setHasFixedSize(true);
    }

    private void loadComments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textEmpty.setVisibility(View.GONE);

        // ĐÃ SỬA LỖI Ở ĐÂY: xóa từ "save" thừa
        MutableLiveData<List<Comment>> liveData = new MutableLiveData<>();

        repository.getSellerComments(sellerId, liveData);

        liveData.observe(getViewLifecycleOwner(), comments -> {
            binding.progressBar.setVisibility(View.GONE);

            if (comments != null && !comments.isEmpty()) {
                adapter.submitList(new ArrayList<>(comments));
                binding.textEmpty.setVisibility(View.GONE);
            } else {
                adapter.submitList(new ArrayList<>());
                binding.textEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
