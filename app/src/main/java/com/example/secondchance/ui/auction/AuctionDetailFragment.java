// com/example/secondchance/ui/auction/AuctionDetailFragment.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.model.UserProfileResponse;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentAuctionDetailBinding;
import com.example.secondchance.dto.request.BidRequest;
import com.example.secondchance.dto.response.AuctionListUserResponse;
import com.example.secondchance.util.Prefs;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuctionDetailFragment extends Fragment {
    
    private FragmentAuctionDetailBinding binding;
    private HomeApi homeApi;
    private CountDownTimer countdown;
    
    private final Gson gson = new Gson();
    
    // Keys cho Fragment Result API
    public static final String KEY_AUCTION_HEADER = "auction_product";
    public static final String KEY_AUCTION_BIDS = "auction_bid";
    public static final String KEY_AUCTION_JSON = "auction_json";
    public static final String KEY_CURRENT_USER_JSON = "current_user_json";
    public static final String KEY_BID_LIST_JSON = "bid_list_json";
    private String auctionId;
    private EditText etBidAmount;
    private MaterialCardView cardPlaceBid;
    private long currentUserBalance = 0;
    private String cachedCurrentUserId;
    private String cachedUserAvatar;

    private static final java.time.format.DateTimeFormatter VN_DATETIME =
      java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss, dd/MM/yyyy")
        .withLocale(new Locale("vi", "VN"));
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuctionDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        homeApi = RetrofitProvider.home();
        
        // Fetch user profile first to get avatar and ID
        fetchUserProfileForAvatar();
        
        loadData();
        
        // Use binding for these lookups
        etBidAmount = binding.etBidAmount;
        cardPlaceBid = binding.cardPlaceBid;
        
        cardPlaceBid.setOnClickListener(view -> {
            long maxCanBid = currentMaxCanBid; // Use stored maxCanBid
            placeBidWithValidation(maxCanBid);
        });
    }
    
    private void fetchUserProfileForAvatar() {
        RetrofitProvider.me().getUserProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (response.body().getData() != null) {
                         cachedCurrentUserId = response.body().getData().getId();
                         cachedUserAvatar = response.body().getData().getAvatar();
                         currentUserBalance = response.body().getData().getWalletBalance();
                         
                         if (binding != null && cachedUserAvatar != null) {
                             Glide.with(AuctionDetailFragment.this)
                                  .load(cachedUserAvatar)
                                  .placeholder(R.drawable.avatar1)
                                  .circleCrop()
                                  .into(binding.userAvatar);
                         }
                    }
                }
            }
            @Override public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });
    }

    private long currentMaxCanBid = 0;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countdown != null) countdown.cancel();
        binding = null;
    }
    
    private void loadData() {
        String productIdArg = getArguments() != null ? getArguments().getString("productId") : null;
        if (TextUtils.isEmpty(productIdArg)) {
            Toast.makeText(requireContext(), "Thiếu productId", Toast.LENGTH_SHORT).show();
            return;
        }
        
        homeApi.getProductAuctionUser(productIdArg).enqueue(new Callback<AuctionListUserResponse>() {
            @Override public void onResponse(@NonNull Call<AuctionListUserResponse> call,
                                             @NonNull Response<AuctionListUserResponse> res) {
                if (!res.isSuccessful() || res.body() == null || !res.body().success || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được dữ liệu đấu giá", Toast.LENGTH_SHORT).show();
                    return;
                }
                auctionId = res.body().data.id;
                AuctionListUserResponse.Data data = res.body().data;
                
                if (data.currentUser != null) {
                    currentUserBalance = data.currentUser.balance;
                    cachedCurrentUserId = data.currentUser.id;
                    cachedUserAvatar = data.currentUser.avatar;
                    
                    // Important: Update currentUserBalance from API if available
                    if (data.currentUser.balance > 0) {
                        currentUserBalance = data.currentUser.balance;
                    }
                } else {
                    // If currentUser is missing, fetch profile
                    fetchUserProfile(data);
                    // Continue with what we have for now
                }

                Log.d("AuctionDetail", "API data: " + gson.toJson(data));

                boolean isEnded = checkIsEnded(data);
                bindOwnViews(data, isEnded);
                dispatchToChildren(data, isEnded);
            }

            @Override public void onFailure(@NonNull Call<AuctionListUserResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                Log.e("AuctionDetail", "loadData onFailure", t);
            }
        });
    }

    private void fetchUserProfile(AuctionListUserResponse.Data data) {
        RetrofitProvider.me().getUserProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (response.body().getData() != null) {
                         cachedCurrentUserId = response.body().getData().getId();
                         cachedUserAvatar = response.body().getData().getAvatar();
                         // Update balance if available in UserData
                         currentUserBalance = response.body().getData().getWalletBalance();

                         // Refresh Views that depend on this
                         if (binding != null) {
                             boolean isEnded = checkIsEnded(data);
                             // Re-bind views and Re-dispatch to update filters with correct ID
                             bindOwnViews(data, isEnded);
                             dispatchToChildren(data, isEnded);
                             
                             // Set avatar specifically here as well
                             if (cachedUserAvatar != null) {
                                 Glide.with(AuctionDetailFragment.this)
                                      .load(cachedUserAvatar)
                                      .placeholder(R.drawable.avatar1)
                                      .circleCrop()
                                      .into(binding.userAvatar);
                             }
                         }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e("AuctionDetail", "Failed to fetch user profile", t);
            }
        });
    }

    private boolean checkIsEnded(AuctionListUserResponse.Data data) {
        boolean isEnded = false;
        if (data.endsAt != null) {
            try {
                Instant endInstant = Instant.parse(data.endsAt);
                isEnded = Instant.now().isAfter(endInstant);
            } catch (Exception e) {
                Log.e("AuctionDetail", "Error parsing endsAt", e);
            }
        }
        if ("success".equalsIgnoreCase(data.finalState) || "failed".equalsIgnoreCase(data.finalState) || "cancelled".equalsIgnoreCase(data.finalState)) {
            isEnded = true;
        }
        return isEnded;
    }

    private void bindOwnViews(AuctionListUserResponse.Data data, boolean isEnded) {
        AuctionListUserResponse.CurrentUser me = data.currentUser;
        String currentUserId = getCurrentUserId();

        // ----- Giá hiện tại (card trên cùng của layout này) -----
        binding.currentPrice.setText(formatVnd(data.currentPrice));

        // "date"
        String dateToShow = null;
        AuctionListUserResponse.Bid latest = getLatestBid(data.bidHistory);
        if (latest != null && !TextUtils.isEmpty(latest.createdAt)) {
            dateToShow = formatIsoToVN(latest.createdAt);
        } else if (!TextUtils.isEmpty(data.endsAt)) {
            dateToShow = formatIsoToVN(data.endsAt);
        }
        binding.date.setText(safe(dateToShow));

        // Tên người dùng
        if (latest != null && latest.byUser != null) {
            binding.userName.setText(safe(latest.byUser.name));
        } else {
            binding.userName.setText("");
        }

        // Ký hiệu tiền tệ + số tiền gần nhất
        binding.priceSymbol.setText(data.currency != null && data.currency.equalsIgnoreCase("VND") ? "₫" : safe(data.currency));
        long lastAmount = latest != null ? latest.amount : data.currentPrice;
        binding.price.setText(formatPlain(lastAmount));

        // Mức tăng so với lượt gần nhất
        long inc = calcIncrease(data.bidHistory, latest);
        binding.priceIncrease.setText(inc > 0 ? " (+" + formatPlain(inc) + ")" : "");

        // Avatar: hiện avatar bidder mới nhất
        String avatarUrl = null;
        if (latest != null && latest.byUser != null) {
             avatarUrl = latest.byUser.avatar;
        } else if (me != null) {
             avatarUrl = me.avatar;
        } else {
             avatarUrl = cachedUserAvatar;
        }
        
        // Nếu latest bid là của current user, thử tìm avatar xịn trong bid history của current user
        // Vì đôi khi latest.byUser.avatar bị null hoặc lỗi, nhưng các bid cũ hơn lại có avatar
        if (latest != null && isMyBid(latest, currentUserId)) {
             // Nếu latest.byUser.avatar null hoặc empty, thử lấy từ cachedUserAvatar
             if (TextUtils.isEmpty(avatarUrl)) {
                 avatarUrl = cachedUserAvatar;
             }
             // Nếu vẫn null, thử tìm trong history các bid khác của user này
             if (TextUtils.isEmpty(avatarUrl) && data.bidHistory != null) {
                 for (AuctionListUserResponse.Bid b : data.bidHistory) {
                     if (isMyBid(b, currentUserId) && b.byUser != null && !TextUtils.isEmpty(b.byUser.avatar)) {
                         avatarUrl = b.byUser.avatar;
                         break;
                     }
                 }
             }
        }
        
        if (avatarUrl != null) {
             Glide.with(this).load(avatarUrl).placeholder(R.drawable.avatar1).circleCrop().into(binding.avatar);
        } else {
             binding.avatar.setImageResource(R.drawable.avatar1);
        }

        // Người dùng hiện tại (Bottom Info) - Avatar input
        String myAvatarUrl = cachedUserAvatar;
        if (me != null && me.avatar != null) {
            myAvatarUrl = me.avatar;
        }
        
        // --- NEW: Tìm avatar từ bidHistory nếu trùng khớp currentUserId ---
        if (data.bidHistory != null && currentUserId != null) {
            for (AuctionListUserResponse.Bid bid : data.bidHistory) {
                if (isMyBid(bid, currentUserId)) {
                    if (bid.byUser != null && !TextUtils.isEmpty(bid.byUser.avatar)) {
                        myAvatarUrl = bid.byUser.avatar;
                        break; // Tìm thấy avatar của mình trong lịch sử đấu giá
                    }
                }
            }
        }

        if (myAvatarUrl != null) {
            Glide.with(this).load(myAvatarUrl).placeholder(R.drawable.avatar1).circleCrop().into(binding.userAvatar);
        } else {
             binding.userAvatar.setImageResource(R.drawable.avatar1);
        }

        // ========== CALCULATE MY BID AMOUNT ==========
        long myLastBidAmount = 0;

        // Priority 1: Use API value
        if (me != null && me.myBidAmount != null && me.myBidAmount > 0) {
            myLastBidAmount = me.myBidAmount;
        } else if (data.bidHistory != null && currentUserId != null) {
            // Priority 2: Calculate from local bidHistory
            List<AuctionListUserResponse.Bid> myBids = new ArrayList<>();
            for (AuctionListUserResponse.Bid bid : data.bidHistory) {
                if (isMyBid(bid, currentUserId)) {
                    myBids.add(bid);
                }
            }
            if (!myBids.isEmpty()) {
                Collections.sort(myBids, (o1, o2) -> Long.compare(o2.amount, o1.amount));
                myLastBidAmount = myBids.get(0).amount;
            }
        }

        // ========== CHECK IF USER IS LEADING ==========
        boolean isLeading = false;
        // Fix: isLeading is primitive boolean, cannot check for null.
        if (me != null) {
             isLeading = me.isLeading;
        } else {
             isLeading = isCurrentlyLeading(data, currentUserId);
        }

        // ========== CALCULATE MAX CAN BID ==========
        long maxCanBid = currentUserBalance;
        if (me != null && me.maxCanBid != null) {
             maxCanBid = me.maxCanBid;
        } else if (isLeading && myLastBidAmount > 0) {
            maxCanBid = currentUserBalance + myLastBidAmount;
        }
        
        currentMaxCanBid = maxCanBid; // Store for click listener

        // ========== BIND BOTTOM INFO ==========
        binding.myBidAmount.setText(formatPlain(myLastBidAmount));
        binding.userBalance.setText(formatPlain(currentUserBalance));
        binding.userCan.setText(formatPlain(maxCanBid));

        // ========== SETUP PLACE BID BUTTON ==========
        // Listener is set in onViewCreated

        // =================== LOGIC FOR BIDS DISPLAY ===================
        List<AuctionListUserResponse.Bid> myBids = new ArrayList<>();
        if (data.bidHistory != null) {
            for (AuctionListUserResponse.Bid bid : data.bidHistory) {
                if (isMyBid(bid, currentUserId)) {
                    myBids.add(bid);
                }
            }
        }

        // Setup Visibility based on state
        if (isEnded) {
            // State: Ended/Success/Failed
            binding.layoutPlaceBid.setVisibility(View.GONE);

            // Show My Bids History if exists
            if (!myBids.isEmpty()) {
                binding.layoutMyBidsHistory.setVisibility(View.VISIBLE);
                Collections.sort(myBids, (o1, o2) -> o2.createdAt.compareTo(o1.createdAt));
                MyBidsAdapter adapter = new MyBidsAdapter(myBids, data.bidHistory);
                binding.rvMyBids.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.rvMyBids.setAdapter(adapter);
            } else {
                binding.layoutMyBidsHistory.setVisibility(View.GONE);
            }
        } else {
            // State: Ongoing
            binding.layoutPlaceBid.setVisibility(View.VISIBLE);
            binding.layoutMyBidsHistory.setVisibility(View.GONE);
        }
    }

    /**
     * Check if current user is leading (has highest bid)
     */
    private boolean isCurrentlyLeading(AuctionListUserResponse.Data data, String currentUserId) {
        if (data.bidHistory == null || data.bidHistory.isEmpty() || currentUserId == null) {
            return false;
        }

        // Find highest bid
        AuctionListUserResponse.Bid highestBid = Collections.max(
                data.bidHistory,
                Comparator.comparingLong(b -> b.amount)
        );

        return isMyBid(highestBid, currentUserId);
    }

    /**
     * Place bid with proper validation against maxCanBid
     */
    private void placeBidWithValidation(long maxCanBid) {
        String raw = etBidAmount.getText() != null ? etBidAmount.getText().toString().trim() : "";
        if (raw.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove formatting
        String digitsOnly = raw.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(digitsOnly);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Số tiền quá lớn hoặc không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(requireContext(), "Số tiền phải > 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDATE AGAINST MAX CAN BID (not just balance)
        // Use the passed maxCanBid (which should be currentMaxCanBid updated in bindOwnViews)
        if (maxCanBid > 0 && amount > maxCanBid) {
            Toast.makeText(requireContext(),
                    "Số tiền không được vượt quá " + formatPlain(maxCanBid) + " VND",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Send API request
        BidRequest body = new BidRequest(amount);
        cardPlaceBid.setEnabled(false);

        homeApi.placeBid(auctionId, body).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> resp) {
                cardPlaceBid.setEnabled(true);
                if (resp.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã đặt giá " + formatPlain(amount) + " VND", Toast.LENGTH_SHORT).show();
                    etBidAmount.setText("");
                    loadData(); // Reload to get updated data
                } else {
                    // Parse error message if possible
                    String msg = "Đặt giá thất bại: " + resp.code();
                    try {
                         if (resp.errorBody() != null) {
                             // Try to read error body
                             // msg += " " + resp.errorBody().string(); 
                             // Don't block thread
                         }
                    } catch (Exception e) {}
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cardPlaceBid.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetch user profile when currentUser is null in API response
     */
    private void fetchUserProfileAndBind(AuctionListUserResponse.Data data) {
        RetrofitProvider.me().getUserProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (response.body().getData() != null) {
                        cachedCurrentUserId = response.body().getData().getId();
                        cachedUserAvatar = response.body().getData().getAvatar();
                        currentUserBalance = response.body().getData().getWalletBalance();

                        // NOW bind UI with complete data
                        if (binding != null) {
                            boolean isEnded = checkIsEnded(data);
                            bindOwnViews(data, isEnded);
                            dispatchToChildren(data, isEnded);
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải thông tin user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e("AuctionDetail", "Failed to fetch user profile", t);
                Toast.makeText(requireContext(), "Lỗi kết nối khi tải user", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getCurrentUserId() {
        if (cachedCurrentUserId != null) return cachedCurrentUserId;
        return getUserIdFromToken();
    }
    
    private boolean isMyBid(AuctionListUserResponse.Bid bid, String currentUserId) {
        if (currentUserId == null || currentUserId.isEmpty() || bid == null) return false;
        if (bid.byUser != null && currentUserId.equals(bid.byUser.id)) return true;
        return currentUserId.equals(bid.userId);
    }

    private void dispatchToChildren(AuctionListUserResponse.Data data, boolean isEnded) {
        // Pass auction header data
        Bundle headerBundle = new Bundle();
        headerBundle.putString(KEY_AUCTION_JSON, gson.toJson(data));
        getChildFragmentManager().setFragmentResult(KEY_AUCTION_HEADER, headerBundle);
        
        String currentUserId = getCurrentUserId();
        
        List<AuctionListUserResponse.Bid> bidsToSend = new ArrayList<>();
        
        if (data.bidHistory != null) {
            if (isEnded) {
                 // If Ended (Success/Failed), fragment shows only OTHERS' bids
                 for (AuctionListUserResponse.Bid bid : data.bidHistory) {
                     if (!isMyBid(bid, currentUserId)) {
                         bidsToSend.add(bid);
                     }
                 }
            } else {
                 // If Ongoing, fragment shows ALL bids (me + others)
                 bidsToSend.addAll(data.bidHistory);
            }
        }

        Bundle bidsBundle = new Bundle();
        bidsBundle.putString(KEY_BID_LIST_JSON, gson.toJson(bidsToSend));
        bidsBundle.putString(KEY_CURRENT_USER_JSON, gson.toJson(data.currentUser));
        getChildFragmentManager().setFragmentResult(KEY_AUCTION_BIDS, bidsBundle);
    }
    
    private String getUserIdFromToken() {
        try {
            String token = Prefs.getToken(requireContext());
            if (TextUtils.isEmpty(token)) return null;
            
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject json = new JSONObject(payload);
            
            if (json.has("id")) return json.getString("id");
            if (json.has("_id")) return json.getString("_id");
            if (json.has("sub")) return json.getString("sub");
            if (json.has("userId")) return json.getString("userId");
            
        } catch (Exception e) {
            Log.e("AuctionDetail", "Error parsing token for user ID", e);
        }
        return null;
    }
    
    // === Helpers ===
    
    @Nullable
    private AuctionListUserResponse.Bid getLatestBid(@Nullable List<AuctionListUserResponse.Bid> list) {
        if (list == null || list.isEmpty()) return null;
        return Collections.max(list, (o1, o2) -> o1.createdAt.compareTo(o2.createdAt));
    }
    
    private long calcIncrease(@Nullable List<AuctionListUserResponse.Bid> list, AuctionListUserResponse.Bid current) {
        if (current == null || list == null || list.size() < 2) return 0;
        
        AuctionListUserResponse.Bid previous = list.stream()
            .filter(b -> b != current && b.createdAt.compareTo(current.createdAt) < 0)
            .max((o1, o2) -> o1.createdAt.compareTo(o2.createdAt))
            .orElse(null);
            
        if (previous == null) return 0;
        return Math.max(0, current.amount - previous.amount);
    }
    
    private String formatIsoToVN(String iso) {
        try {
            return java.time.Instant.parse(iso)
              .atZone(java.time.ZoneId.systemDefault())
              .format(VN_DATETIME);
        } catch (Exception e) {
            return iso; // fallback if parse lỗi
        }
    }
    
    private String safe(String s) { return s == null ? "" : s; }
    
    private String formatVnd(long amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }
    
    private String formatPlain(long amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }
}
