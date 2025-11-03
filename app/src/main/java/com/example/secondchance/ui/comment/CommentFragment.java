// ui/comment/CommentFragment.java
package com.example.secondchance.ui.comment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.databinding.FragmentCommentBinding;

import java.util.ArrayList;
import java.util.List;

public class CommentFragment extends Fragment {

    private FragmentCommentBinding binding;
    private CommentAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadSampleData();
    }

    private void setupRecyclerView() {
        adapter = new CommentAdapter();
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewComments.setAdapter(adapter);
        binding.recyclerViewComments.setHasFixedSize(true);
    }

    private void loadSampleData() {
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment(
                "Fish can Fly",
                "18/02/2025",
                "Bé cá 7 màu, bằng kính, loại dày, số lượng 1.",
                "Shop tư vấn nhiệt tình, đóng gói cẩn thận, lúc về đến nhà check đúng sự thật. Cảm ơn shop, ship nhanh tại mình đang cần gấp hệ hệ",
                "4.9",
                "Shop xin lỗi vì những sự cố như thế này, mong bạn có thể ib lại để shop hỗ trợ tư vấn ạ"
        ));

        comments.add(new Comment(
                "Người dùng 2",
                "17/02/2025",
                "Sản phẩm A",
                "Hàng đẹp, giao nhanh, sẽ ủng hộ tiếp!",
                "5.0",
                null
        ));

        adapter.submitList(comments);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
