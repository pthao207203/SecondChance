package com.example.secondchance.ui.product;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.ui.product.adapter.AuctionBidAdapter;

import java.util.ArrayList;
import java.util.List;

public class AuctionSessionFragment extends Fragment {

    private static final String ARG_PRODUCT_ID = "productId";
    private static final String ARG_PRODUCT_NAME = "productName";
    private static final String ARG_CURRENT_PRICE = "currentPrice";
    private static final String ARG_QUANTITY = "quantity";
    private static final String ARG_END_TIME = "endTime"; // Timestamp kết thúc đấu giá

    private String productId;
    private String productName;
    private double currentPrice;
    private int quantity;
    private long endTime;

    // Countdown
    private TextView tvHours, tvMinutes, tvSeconds;
    private Handler countdownHandler;
    private Runnable countdownRunnable;

    // Views
    private ImageView imgAuctionProduct;
    private TextView tvProductName, tvQuantity, tvCurrentPrice, tvHighestBidAmount;
    private LinearLayout layoutHighestBid;
    private RecyclerView recyclerBidHistory;
    private LinearLayout layoutEmpty;
    private AuctionBidAdapter adapter;

    public static AuctionSessionFragment newInstance(String productId, String productName,
                                                     double currentPrice, int quantity, long endTime) {
        AuctionSessionFragment fragment = new AuctionSessionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        args.putString(ARG_PRODUCT_NAME, productName);
        args.putDouble(ARG_CURRENT_PRICE, currentPrice);
        args.putInt(ARG_QUANTITY, quantity);
        args.putLong(ARG_END_TIME, endTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
            productName = getArguments().getString(ARG_PRODUCT_NAME);
            currentPrice = getArguments().getDouble(ARG_CURRENT_PRICE);
            quantity = getArguments().getInt(ARG_QUANTITY);
            endTime = getArguments().getLong(ARG_END_TIME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auction_session, container, false);

        initViews(view);
        setupRecyclerView();
        loadProductData();
        loadBidHistory();
        startCountdown();

        return view;
    }

    private void initViews(View view) {
        // Countdown
        tvHours = view.findViewById(R.id.tv_hours);
        tvMinutes = view.findViewById(R.id.tv_minutes);
        tvSeconds = view.findViewById(R.id.tv_seconds);

        // Product info
        imgAuctionProduct = view.findViewById(R.id.img_auction_product);
        tvProductName = view.findViewById(R.id.tv_product_name);
        tvQuantity = view.findViewById(R.id.tv_quantity);
        tvCurrentPrice = view.findViewById(R.id.tv_current_price);
        layoutHighestBid = view.findViewById(R.id.tv_highest_bid);
        tvHighestBidAmount = view.findViewById(R.id.tv_highest_bid_amount);

        // RecyclerView
        recyclerBidHistory = view.findViewById(R.id.recycler_bid_history);
        layoutEmpty = view.findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        adapter = new AuctionBidAdapter();
        recyclerBidHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBidHistory.setAdapter(adapter);
    }

    private void loadProductData() {
        tvProductName.setText(productName);
        tvQuantity.setText("x" + quantity);
        tvCurrentPrice.setText(String.format("đ%,.0f", currentPrice));
        tvHighestBidAmount.setText(String.format("đ %,.0f", currentPrice));

        // TODO: Load image with Glide/Picasso
        // Glide.with(this).load(imageUrl).into(imgAuctionProduct);
    }

    private void loadBidHistory() {
        // Mock data - Replace with API call
        List<AuctionBid> bidList = getMockBidHistory();
        adapter.setBidList(bidList);

        // Show/hide empty state
        updateEmptyState(bidList.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerBidHistory.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerBidHistory.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private List<AuctionBid> getMockBidHistory() {
        List<AuctionBid> bidList = new ArrayList<>();

        // Highest bid first
        bidList.add(new AuctionBid(
                "user1", "Cá biệt bày", "",
                2, 8500000, 25000, "10:05:59, 06/08/2024"
        ));

        // Previous bids
        for (int i = 1; i <= 8; i++) {
            bidList.add(new AuctionBid(
                    "user" + i, "Cá biệt bày", "",
                    1, 1000000, 25000, "10:05:59, 06/08/2024"
            ));
        }

        return bidList;
    }

    private void startCountdown() {
        countdownHandler = new Handler(Looper.getMainLooper());
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                countdownHandler.postDelayed(this, 1000); // Update every second
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void updateCountdown() {
        long currentTime = System.currentTimeMillis();
        long remainingTime = endTime - currentTime;

        if (remainingTime <= 0) {
            // Auction ended
            tvHours.setText("00");
            tvMinutes.setText("00");
            tvSeconds.setText("00");
            stopCountdown();
            return;
        }

        long hours = (remainingTime / (1000 * 60 * 60)) % 24;
        long minutes = (remainingTime / (1000 * 60)) % 60;
        long seconds = (remainingTime / 1000) % 60;

        tvHours.setText(String.format("%02d", hours));
        tvMinutes.setText(String.format("%02d", minutes));
        tvSeconds.setText(String.format("%02d", seconds));
    }

    private void stopCountdown() {
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCountdown();
    }
}