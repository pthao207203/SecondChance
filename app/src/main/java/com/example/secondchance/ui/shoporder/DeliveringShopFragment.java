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
import com.example.secondchance.databinding.FragmentShopDeliveringBinding;

import java.util.ArrayList;
import java.util.List;

public class DeliveringShopFragment extends Fragment {

    private static final String TAG = "DeliveringShopFrag";
    private FragmentShopDeliveringBinding binding;
    private DeliveringShopAdapter adapter;
    private final List<ShopOrder> shopOrderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;
    private String currentShopId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopDeliveringBinding.inflate(inflater, container, false);
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

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new DeliveringShopAdapter(shopOrderList, (orderId, deliveryStatus) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("shopOrderId", orderId);
                if (deliveryStatus != null) {
                    args.putSerializable("deliveryStatus", deliveryStatus);
                }
                nav.navigate(R.id.action_shopOrderFragment_to_deliveringShopOrderDetailFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error", e);
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvDeliveringOrders.setAdapter(adapter);

        loadData();

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                loadData();
                sharedViewModel.clearRefreshRequest();
            }
        });
    }

    private void loadData() {
        if (currentShopId == null || currentShopId.isEmpty()) return;

        orderRepository.fetchOrdersForShop(currentShopId, "1", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                shopOrderList.clear();
                if (data != null) {
                    for (OrderWrapper wrapper : data) {

                        if (wrapper.order != null) {
                            ShopOrder shopOrder = convertToShopOrder(wrapper);
                            shopOrderList.add(shopOrder);
                        }
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (shopOrderList.isEmpty()) {
                    Log.d(TAG, "Danh sách đơn hàng trống");
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
        com.example.secondchance.data.model.Order.DeliveryOverallStatus apiStatus = wrapper.order.getDeliveryStatus();
        ShopOrder.DeliveryOverallStatus status = mapToShopStatus(apiStatus);

        String statusText;
        String description;

        switch (status) {
            case PACKAGED:
                statusText = "Bạn chưa giao đơn hàng cho bưu cục";
                description = "Đã xác nhận, đang đóng gói";
                break;
            case AT_POST_OFFICE:
                statusText = "Đã đến bưu cục";
                description = "Đơn hàng đang ở bưu cục";
                break;
            case DELIVERING:
                statusText = "Đang giao hàng";
                description = "Shipper đang giao tới khách";
                break;
            case DELIVERED:
                statusText = "Giao hàng thành công";
                description = "Khách đã nhận được hàng";
                break;
            default:
                statusText = wrapper.order.getStatusText();
                description = "Đang xử lý";
        }

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
                statusText,
                description,
                ShopOrder.ShopOrderType.DELIVERING,
                false,
                null,
                status
        );
    }

    private ShopOrder.DeliveryOverallStatus mapToShopStatus(com.example.secondchance.data.model.Order.DeliveryOverallStatus apiStatus) {
        if (apiStatus == null) return ShopOrder.DeliveryOverallStatus.PACKAGED;

        try {
            return ShopOrder.DeliveryOverallStatus.valueOf(apiStatus.name());
        } catch (IllegalArgumentException e) {
            return ShopOrder.DeliveryOverallStatus.PACKAGED;
        }
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvDeliveringOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnShopOrderClick {
        void openDetail(String shopOrderId, @Nullable ShopOrder.DeliveryOverallStatus deliveryStatus);
    }

    private static class DeliveringShopAdapter extends RecyclerView.Adapter<DeliveringShopAdapter.ShopOrderViewHolder> {

        private final List<ShopOrder> items;
        private final OnShopOrderClick listener;

        private static final int VIEW_TYPE_NOT_YET_DELIVERING = 1;
        private static final int VIEW_TYPE_DELIVERING = 2;

        DeliveringShopAdapter(List<ShopOrder> items, OnShopOrderClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            ShopOrder order = items.get(position);
            if (order.getDeliveryStatus() == ShopOrder.DeliveryOverallStatus.PACKAGED) {
                return VIEW_TYPE_NOT_YET_DELIVERING;
            } else {
                return VIEW_TYPE_DELIVERING;
            }
        }

        @NonNull
        @Override
        public ShopOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v;
            if (viewType == VIEW_TYPE_NOT_YET_DELIVERING) {
                v = inflater.inflate(R.layout.item_shop_not_yet_delivering_order, parent, false);
            } else {
                v = inflater.inflate(R.layout.item_shop_delivering_order, parent, false);
            }
            return new ShopOrderViewHolder(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopOrderViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class ShopOrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvQuantity, tvStatus, tvDescription;
            ImageView imgProduct;
            final OnShopOrderClick listener;

            ShopOrderViewHolder(@NonNull View itemView, OnShopOrderClick listener) {
                super(itemView);
                this.listener = listener;
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }

            void bind(final ShopOrder shopOrder) {
                tvStatus.setText(shopOrder.getStatusText());
                tvDescription.setText(shopOrder.getDescription());
                tvPrice.setText(shopOrder.getTotalPrice());

                if (shopOrder.getItems() != null && !shopOrder.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = shopOrder.getItems().get(0);

                    tvTitle.setText(firstProduct.getTitle());
                    tvQuantity.setText("Số lượng: " + firstProduct.getQuantity());

                    if (firstProduct.getImageUrl() != null && !firstProduct.getImageUrl().isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(firstProduct.getImageUrl())
                                .into(imgProduct);
                    } else {
                        imgProduct.setImageResource(firstProduct.getImageRes() != 0 ? firstProduct.getImageRes() : R.drawable.sample_flower);
                    }

                } else {
                    tvTitle.setText("Đơn hàng lỗi");
                    tvQuantity.setText("Số lượng: 0");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null)
                        listener.openDetail(shopOrder.getId(), shopOrder.getDeliveryStatus());
                });
            }
        }
    }
}
