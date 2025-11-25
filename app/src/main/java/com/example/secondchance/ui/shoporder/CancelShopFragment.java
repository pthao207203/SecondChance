package com.example.secondchance.ui.shoporder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.databinding.FragmentCancelOrderBinding;

import java.util.ArrayList;
import java.util.List;

public class CancelShopFragment extends Fragment {

    private static final String TAG = "CancelShopFrag";
    private FragmentCancelOrderBinding binding;
    private CancelOrdersAdapter adapter;
    private final List<ShopOrder> shopOrderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;
    private String currentShopId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCancelOrderBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderRepository = new OrderRepository();

        currentShopId = sharedViewModel.getCurrentShopId();
        if (currentShopId == null || currentShopId.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy ID Shop. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        binding.rvCancelOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CancelOrdersAdapter(shopOrderList, orderId -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("shopOrderId", orderId);
                nav.navigate(R.id.action_shopOrderFragment_to_canceledShopOrderDetailFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error", e);
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng đã hủy.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvCancelOrders.setAdapter(adapter);

        loadData();

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d(TAG, "Got refresh signal! Reloading data...");
                loadData();
            }
        });
    }

    private void loadData() {
        if (currentShopId == null || currentShopId.isEmpty()) return;

        orderRepository.fetchOrdersForShop(currentShopId, "3", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                shopOrderList.clear();
                if (data != null) {
                    for (OrderWrapper wrapper : data) {
                        if (wrapper.order != null && wrapper.order.status == 3) {
                            shopOrderList.add(convertToShopOrder(wrapper));
                        }
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (shopOrderList.isEmpty()) {
                    Log.d(TAG, "Danh sách đơn hủy trống");
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ShopOrder convertToShopOrder(OrderWrapper wrapper) {
        List<ShopOrderProduct> products = new ArrayList<>();
        OrderItem firstItem = wrapper.order.getFirstItem();

        if (firstItem != null) {
            products.add(new ShopOrderProduct(
                    "ID",
                    firstItem.getName(),
                    "Mã đơn: " + wrapper.order.getId().substring(0, Math.min(8, wrapper.order.getId().length())),
                    wrapper.order.getPrice(),
                    0,
                    firstItem.getQuantity(),
                    firstItem.getImageUrl()
            ));
        }

        return new ShopOrder(
                wrapper.order.getId(),
                products,
                wrapper.order.getPrice(),
                wrapper.order.getDate(),
                "Đã hủy",
                "Đơn hàng đã bị hủy",
                ShopOrder.ShopOrderType.CANCELED,
                false,
                null,
                null
        );
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvCancelOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnOrderClick {
        void openDetail(String orderId);
    }

    private static class CancelOrdersAdapter extends RecyclerView.Adapter<CancelOrdersAdapter.OrderViewHolder> {

        private final List<ShopOrder> items;
        private final OnOrderClick listener;

        CancelOrdersAdapter(List<ShopOrder> items, OnOrderClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shop_canceled_order, parent, false);
            return new OrderViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class OrderViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvDate, tvPrice;
            private final OnOrderClick listener;

            OrderViewHolder(@NonNull View itemView, OnOrderClick listener) {
                super(itemView);
                this.listener = listener;

                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            void bind(final ShopOrder order) {
                tvDate.setText("Ngày hủy: " + order.getDate());
                tvPrice.setText(order.getTotalPrice());

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = order.getItems().get(0);
                    tvTitle.setText(firstProduct.getTitle());

                    if (firstProduct.getImageUrl() != null && !firstProduct.getImageUrl().isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(firstProduct.getImageUrl())
                                .into(imgProduct);
                    } else {
                        imgProduct.setImageResource(R.drawable.sample_flower);
                    }
                } else {
                    tvTitle.setText("Đơn hàng lỗi");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.openDetail(order.getId());
                });
            }
        }
    }
}
