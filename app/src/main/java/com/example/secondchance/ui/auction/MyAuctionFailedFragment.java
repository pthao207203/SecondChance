
// ============================================
// MyAuctionFailedFragment.java
// ============================================
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerCardBinding;
import com.example.secondchance.dto.response.AuctionListResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAuctionFailedFragment extends Fragment {
    private static final String TAG = "MyAuctionFailed";
    private FragmentRecyclerCardBinding binding;
    private AuctionFailedAdapter adapter;
    private List<AuctionGoingOn> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AuctionFailedAdapter(items);
        binding.recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        ProductApi api = RetrofitProvider.product();

        api.getFailedAuctions(1, 20).enqueue(new Callback<AuctionListResponse>() {
            @Override
            public void onResponse(Call<AuctionListResponse> call, Response<AuctionListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().success) {
                    String errorMsg = "Không thể tải danh sách đấu giá thất bại";
                    if (response.code() == 401) {
                        errorMsg = "Vui lòng đăng nhập để xem đấu giá của bạn";
                    }
                    showError(errorMsg);
                    return;
                }

                items.clear();
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                for (AuctionListResponse.Item item : response.body().data.items) {
                    String productId = item.productId != null ? item.productId : item.id;

                    items.add(new AuctionGoingOn(
                            item.title,
                            currencyFormat.format(item.currentPrice),
                            item.quantity,
                            item.imageUrl,
                            System.currentTimeMillis() - 1000, // Already ended
                            productId
                    ));
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
            }

            @Override
            public void onFailure(Call<AuctionListResponse> call, Throwable t) {
                Log.e(TAG, "Load error", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}