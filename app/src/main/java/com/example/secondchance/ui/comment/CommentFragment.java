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
import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerCommentBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class CommentFragment extends Fragment {

    private FragmentRecyclerCommentBinding binding;
    private CommentAdapter adapter;
    private MeApi meApi;
    private String sellerId = "68f72a0b51284f307a9cbad9"; // Default

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerCommentBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            sellerId = getArguments().getString("sellerId", sellerId);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        meApi = RetrofitProvider.getRetrofit().create(MeApi.class);
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

        Call<MeApi.GetShopCommentsResponse> call = meApi.getShopComments(sellerId);
        call.enqueue(new Callback<MeApi.GetShopCommentsResponse>() {
            @Override
            public void onResponse(Call<MeApi.GetShopCommentsResponse> call, Response<MeApi.GetShopCommentsResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<Comment> comments = response.body().getComments();
                    if (comments != null && !comments.isEmpty()) {
                        adapter.submitList(new ArrayList<>(comments));
                        binding.textEmpty.setVisibility(View.GONE);
                    } else {
                        showEmpty();
                    }
                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<MeApi.GetShopCommentsResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                t.printStackTrace();
                showEmpty();
            }
        });
    }

    private void showEmpty() {
        adapter.submitList(new ArrayList<>());
        binding.textEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}