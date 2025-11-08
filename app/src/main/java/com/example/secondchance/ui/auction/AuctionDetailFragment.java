// com/example/secondchance/ui/auction/AuctionDetailFragment.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentAuctionDetailBinding;
import com.example.secondchance.dto.request.BidRequest;
import com.example.secondchance.dto.response.AuctionListUserResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
    
    private static final java.time.format.DateTimeFormatter VN_DATETIME =
      java.time.format.DateTimeFormatter.ofPattern("HH:ss, dd/MM/yyyy")
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
        loadData();
        
        etBidAmount = v.findViewById(R.id.etBidAmount);
        cardPlaceBid = v.findViewById(R.id.cardPlaceBid);
        
        cardPlaceBid.setOnClickListener(view -> {
            String raw = etBidAmount.getText() != null ? etBidAmount.getText().toString().trim() : "";
            if (raw.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }
            // Nếu có format có dấu phẩy/chấm, loại bỏ:
            String digitsOnly = raw.replaceAll("[^0-9]", "");
            if (digitsOnly.isEmpty()) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            long amount;
            try { amount = Long.parseLong(digitsOnly); }
            catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số tiền quá lớn hoặc không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Số tiền phải > 0", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Gửi API
            BidRequest body = new BidRequest(amount);
            cardPlaceBid.setEnabled(false);
            
            homeApi.placeBid(auctionId, body).enqueue(new retrofit2.Callback<Void>() {
                @Override public void onResponse(Call<Void> call, retrofit2.Response<Void> resp) {
                    cardPlaceBid.setEnabled(true);
                    if (resp.isSuccessful()) {
                        Toast.makeText(requireContext(), "Đã đặt giá " + amount + " VND", Toast.LENGTH_SHORT).show();
                        
                        loadData();
                    } else {
                        Toast.makeText(requireContext(), "Đặt giá thất bại: " + resp.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {
                    cardPlaceBid.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
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
                Log.d("AuctionDetail", "API data: " + gson.toJson(data));
                
                // 1) Gán vào view của chính file này (những view đang có id)
                bindOwnViews(data);
                
                // 2) Phát dữ liệu xuống Fragment con (những phần không render ở đây)
                dispatchToChildren(data);
            }
            
            @Override public void onFailure(@NonNull Call<AuctionListUserResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                Log.e("AuctionDetail", "loadData onFailure", t);
            }
        });
    }
    
    private void bindOwnViews(AuctionListUserResponse.Data data) {
        AuctionListUserResponse.CurrentUser me = data.currentUser;
        
        // ----- Giá hiện tại (card trên cùng của layout này) -----
        binding.currentPrice.setText(formatVnd(data.currentPrice)); // ₫x.xxx.xxx
      
      
        // “date”: mình hiển thị mốc gần nhất: nếu có bid gần nhất -> dùng createdAt, không thì dùng endsAt
        String dateToShow = null;
        AuctionListUserResponse.Bid latest = getLatestBid(data.bidHistory);
        if (latest != null && !TextUtils.isEmpty(latest.createdAt)) {
            dateToShow = formatIsoToVN(latest.createdAt);
        } else if (!TextUtils.isEmpty(data.endsAt)) {
            dateToShow = formatIsoToVN(data.endsAt);
        }
        binding.date.setText(safe(dateToShow));
        
        // Tên người dùng
        binding.userName.setText(safe(latest.byUser.name));
      
        // Ký hiệu tiền tệ + số tiền gần nhất
        binding.priceSymbol.setText(data.currency != null && data.currency.equalsIgnoreCase("VND") ? "₫" : safe(data.currency));
        long lastAmount = latest != null ? latest.amount : data.currentPrice;
        binding.price.setText(formatPlain(lastAmount)); // “1.000.000”
      
        // Mức tăng so với lượt gần nhất
        long inc = calcIncrease(data.bidHistory, latest);
        binding.priceIncrease.setText(inc > 0 ? " (+" + formatPlain(inc) + ")" : "");
      
        // Avatar: hiện avatar bidder mới nhất; nếu không có, hiện avatar currentUser
        String avatarUrl = (latest != null && latest.byUser != null) ? latest.byUser.avatar
            : (me != null ? me.avatar : null);
        Glide.with(this)
            .load(avatarUrl)
            .circleCrop()
            .into(binding.avatar);
      
        // Người dùng hiện tại
        Glide.with(this)
          .load(data.currentUser.avatar)
          .circleCrop()
          .into(binding.userAvatar);
        long balance = data.currentUser.balance;
        long myBidAmount = data.currentUser.myBidAmount;
        
        long available = balance - myBidAmount;
        binding.userBalance.setText(String.valueOf(available));
        binding.myBidAmount.setText(String.valueOf(data.currentUser.myBidAmount));
        binding.userCan.setText(String.valueOf(data.currentUser.balance));
    }
    
    private void dispatchToChildren(AuctionListUserResponse.Data data) {
        Bundle headerBundle = new Bundle();
        headerBundle.putString(KEY_AUCTION_JSON, gson.toJson(data));
        Bundle bidsBundle = new Bundle();
        bidsBundle.putString(KEY_BID_LIST_JSON, gson.toJson(data.bidHistory));
        
        // Gửi xuống tất cả fragment con qua ChildFragmentManager
        // Con sẽ lắng nghe bằng getParentFragmentManager()
        getChildFragmentManager().setFragmentResult(KEY_AUCTION_HEADER, headerBundle);
        getChildFragmentManager().setFragmentResult(KEY_AUCTION_BIDS, bidsBundle);
    }
    
    // === Helpers ===
    
    @Nullable
    private AuctionListUserResponse.Bid getLatestBid(@Nullable List<AuctionListUserResponse.Bid> list) {
        if (list == null || list.isEmpty()) return null;
        // Giả sử list đã sort theo thời gian tăng dần từ backend; nếu không, có thể duyệt tìm max createdAt
        return list.get(0).createdAt.compareTo(list.get(list.size()-1).createdAt) <= 0
          ? list.get(list.size()-1)
          : list.get(0);
    }
    
    private long calcIncrease(@Nullable List<AuctionListUserResponse.Bid> list, AuctionListUserResponse.Bid current) {
        if (list == null || list.size() < 2) return 0;
        // Tìm bid trước đó
        AuctionListUserResponse.Bid prev = null;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == current) continue;
            prev = list.get(i);
            break;
        }
        if (current == null || prev == null) return 0;
        return Math.max(0, current.amount - prev.amount);
    }
    
    private String formatIsoToVN(String iso) {
        try {
            return java.time.Instant.parse(iso)
              .atZone(java.time.ZoneId.systemDefault())
              .format(VN_DATETIME);
        } catch (Exception e) {
            return iso; // fallback nếu parse lỗi
        }
    }
    
    private String safe(String s) { return s == null ? "" : s; }
    
    private String formatVnd(long amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }
    
    private String formatPlain(long amount) {
        // plain dạng 1.000.000 (không ký hiệu ₫ vì view bên cạnh đã có)
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }
}
