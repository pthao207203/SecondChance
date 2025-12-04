// ============================================
// MyAuctionBiddingFragment.java
// ============================================
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.data.model.UserProfileResponse;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerCardBinding;
import com.example.secondchance.dto.response.AuctionListResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAuctionBiddingFragment extends Fragment {
    private static final String TAG = "MyAuctionBidding";
    private FragmentRecyclerCardBinding binding;
    private AuctionGoingOnAdapter adapter;
    private List<AuctionGoingOn> items = new ArrayList<>();
    
    // Lưu cache thông tin current user
    private String currentUserId;
    private String currentUserName;
    private String currentUserAvatar;

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

        adapter = new AuctionGoingOnAdapter(items);
        binding.recyclerView.setAdapter(adapter);

        // Đầu tiên load profile để lấy ID chính xác, sau đó mới load danh sách đấu giá
        loadUserProfileAndThenAuctions();
    }

    private void loadUserProfileAndThenAuctions() {
        RetrofitProvider.me().getUserProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    currentUserId = response.body().getData().getId();
                    currentUserName = response.body().getData().getName();
                    currentUserAvatar = response.body().getData().getAvatar();
                    Log.d(TAG, "Current User ID loaded: " + currentUserId);
                } else {
                    Log.w(TAG, "Could not load user profile, logic counting bids might be inaccurate.");
                }
                // Dù thành công hay thất bại, vẫn tiếp tục load danh sách đấu giá
                loadData();
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load user profile", t);
                loadData();
            }
        });
    }

    private void loadData() {
        ProductApi api = RetrofitProvider.product();

        api.getParticipatedAuctions(1, 20).enqueue(new Callback<AuctionListResponse>() {
            @Override
            public void onResponse(Call<AuctionListResponse> call, Response<AuctionListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().success) {
                    String errorMsg = "Không thể tải danh sách đấu giá";
                    if (response.code() == 401) {
                        errorMsg = "Vui lòng đăng nhập để xem đấu giá của bạn";
                    } else if (response.code() == 400) {
                        errorMsg = "Vui lòng thêm địa chỉ giao hàng trước";
                    }
                    showError(errorMsg);
                    return;
                }

                items.clear();
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                
                // Nếu API trả về currentUser trong envelope, ưu tiên dùng nó (nếu API đó chính xác)
                // Nhưng ở đây ta đã có currentUserId từ API /me/profile nên sẽ tin tưởng nó hơn hoặc dùng kết hợp.
                if (response.body().data.currentUser != null && currentUserId == null) {
                    currentUserId = response.body().data.currentUser.id;
                }

                for (AuctionListResponse.Item item : response.body().data.items) {
                    // Call API lấy chi tiết từng sản phẩm để lấy bidHistory đầy đủ
                    // Vì API list participanted thường không trả về full bidHistory của tất cả user để đếm
                    // Tuy nhiên, nếu API participanted đã trả về bidHistory của current user thì tốt.
                    // Theo log bạn gửi: GET .../participated trả về items[] có myBidAmount, nhưng KHÔNG thấy bidHistory.
                    // GET .../auctions/{id} MỚI trả về bidHistory đầy đủ.
                    
                    // VẬY GIẢI PHÁP LÀ:
                    // Cách 1: Gọi API detail cho từng item (chậm, spam request).
                    // Cách 2: Chấp nhận hiển thị 1 nếu API list không có count.
                    // Nhưng bạn muốn đếm. Nếu API list KHÔNG trả về bidHistory, client KHÔNG THỂ đếm được trừ khi gọi detail.
                    
                    // Theo log bạn gửi ở trên: 
                    // API /participated trả về item chỉ có: id, title, imageUrl, quantity, currentPrice, currency, endsAt, condition, featured, myBidAmount, isLeading.
                    // API /auctions/{id} trả về bidHistory.
                    
                    // Nên để đếm đúng, tôi sẽ phải fetch detail cho từng item trong list này.
                    // Điều này không tối ưu về hiệu năng mạng, nhưng sẽ giải quyết yêu cầu của bạn.
                    
                    // Tạo item tạm với count = 1 (hoặc 0), sau đó fetch detail để update.
                    
                    try {
                        Date endDate = dateFormat.parse(item.endsAt);
                        long endTimeMillis = endDate != null ? endDate.getTime() : System.currentTimeMillis();
                        String productId = item.productId != null ? item.productId : item.id;
                        
                        long bidAmount = item.myBidAmount != null ? item.myBidAmount : 0;
                        String lastBidStr = currencyFormat.format(bidAmount);
                        
                        // Mặc định 1 nếu có bidAmount
                        int initialCount = (bidAmount > 0) ? 1 : 0;

                        AuctionGoingOn auctionItem = new AuctionGoingOn(
                                item.title,
                                currencyFormat.format(item.currentPrice),
                                item.quantity,
                                item.imageUrl,
                                endTimeMillis,
                                productId,
                                initialCount,
                                lastBidStr
                        );
                        items.add(auctionItem);
                        
                        // Gọi async lấy detail để đếm số lần bid chính xác
                        fetchAuctionDetailAndCountBids(api, productId, auctionItem);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing auction item", e);
                    }
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

    private void fetchAuctionDetailAndCountBids(ProductApi api, String auctionId, AuctionGoingOn itemToUpdate) {
        api.getAuctionById(auctionId).enqueue(new Callback<ProductApi.AuctionDetailEnvelope>() {
            @Override
            public void onResponse(Call<ProductApi.AuctionDetailEnvelope> call, Response<ProductApi.AuctionDetailEnvelope> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    ProductApi.AuctionDetailEnvelope.AuctionDetail detail = response.body().data;
                    if (detail != null && detail.bidHistory != null && currentUserId != null) {
                        int count = 0;
                        for (ProductApi.AuctionDetailEnvelope.BidHistory bid : detail.bidHistory) {
                            // Check userId directly or inside byUser object
                            String bidUserId = bid.userId;
                            if (bidUserId == null && bid.byUser != null) {
                                bidUserId = bid.byUser.id;
                            }
                            
                            if (TextUtils.equals(bidUserId, currentUserId)) {
                                count++;
                            }
                        }
                        
                        // Update item và notify adapter
                        if (count > 0) {
                            itemToUpdate.setUserBidCount(count);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductApi.AuctionDetailEnvelope> call, Throwable t) {
                // Ignore fail, keep default count
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
