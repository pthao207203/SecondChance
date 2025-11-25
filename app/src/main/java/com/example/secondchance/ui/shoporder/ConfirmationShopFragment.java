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
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.repo.OrderRepository;

import com.example.secondchance.databinding.FragmentConfirmationBinding;
import com.example.secondchance.ui.shoporder.dialog.ConfirmCancelShopDialog;
import com.example.secondchance.ui.shoporder.dialog.CancelSuccessShopDialog;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationShopFragment extends Fragment
        implements ConfirmCancelShopDialog.OnCancelConfirmationListener,
        CancelSuccessShopDialog.OnDismissListener {

    private static final String TAG = "ConfirmShopFrag";
    private FragmentConfirmationBinding binding;
    private ConfirmationShopAdapter adapter;
    private final List<ShopOrder> orderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;
    private String currentShopId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmationBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderRepository = new OrderRepository();

        currentShopId = sharedViewModel.getCurrentShopId();
        Log.w(TAG, "Shop ID received from ViewModel: " + currentShopId);
        if (currentShopId == null || currentShopId.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy ID Shop. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ConfirmationShopAdapter(
                orderList,
                (orderId, orderType) -> {
                    try {
                        NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                        Bundle args = new Bundle();
                        args.putString("shopOrderId", orderId);
                        if (orderType != null) {
                            args.putSerializable("orderType", orderType);
                        }
                        nav.navigate(R.id.action_shopOrderFragment_to_confirmShopOrderDetailFragment, args);
                    } catch (Exception e) {
                        Log.e(TAG, "Navigate to ConfirmDetail failed", e);
                        Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        binding.rvOrders.setAdapter(adapter);

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

        orderRepository.fetchOrdersForShop(currentShopId, "0", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                orderList.clear();
                if (data != null) {
                    for (OrderWrapper wrapper : data) {
                        if (wrapper.order != null && wrapper.order.status == 0) {
                            ShopOrder shopOrder = convertToShopOrder(wrapper);
                            orderList.add(shopOrder);
                        }
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (orderList.isEmpty()) {
//                    binding.tvNoOrders.setVisibility(View.VISIBLE);
                    binding.rvOrders.setVisibility(View.GONE);
                    Log.d(TAG, "Danh sách đơn hàng rỗng");
                } else {
//                    binding.tvNoOrders.setVisibility(View.GONE);
                    binding.rvOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Log.e(TAG, "Error fetching orders: " + message);
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ShopOrder convertToShopOrder(OrderWrapper wrapper) {
        if (wrapper == null || wrapper.order == null) return null;

        List<ShopOrderProduct> productList = new ArrayList<>();

        OrderItem firstItem = wrapper.order.getFirstItem();
        if (firstItem != null) {
            productList.add(new ShopOrderProduct(
                    "ITEM_ID",
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
                productList,
                wrapper.order.getPrice(),
                wrapper.order.getDate(),
                "Chờ xác nhận",
                null,
                ShopOrder.ShopOrderType.UNCONFIRMED,
                false,
                null,
                null
        );
    }

    @Override
    public void onCancelConfirmed(String orderId) { }
    @Override
    public void onSuccessfulDismiss() {
        sharedViewModel.refreshOrderLists();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    public interface OnItemClickListener {
        void onItemClick(String orderId, ShopOrder.ShopOrderType orderType);
    }

    private static class ConfirmationShopAdapter extends RecyclerView.Adapter<ConfirmationShopAdapter.UnconfirmedViewHolder> {

        private final List<ShopOrder> items;
        private final OnItemClickListener itemClickListener;

        ConfirmationShopAdapter(List<ShopOrder> items, OnItemClickListener itemClickListener) {
            this.items = items;
            this.itemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public UnconfirmedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_shop_unconfirm_order, parent, false);
            return new UnconfirmedViewHolder(view, itemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull UnconfirmedViewHolder holder, int position) {
            ShopOrder order = items.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        private static class UnconfirmedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus;
            private final OnItemClickListener itemClickListener;

            UnconfirmedViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;

                imgProduct     = itemView.findViewById(R.id.imgProduct);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatus       = itemView.findViewById(R.id.tvStatusReview);
            }

            void bind(final ShopOrder order) {
                tvPrice.setText(order.getTotalPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatus.setText(order.getStatusText());

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = order.getItems().get(0);

                    tvTitle.setText(firstProduct.getTitle());
                    tvQuantity.setText("x" + firstProduct.getQuantity());
                    tvSubtitleFixed.setText(firstProduct.getSubtitle());

                    if (firstProduct.getImageUrl() != null && !firstProduct.getImageUrl().isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(firstProduct.getImageUrl())
                                .into(imgProduct);
                    } else {
                        imgProduct.setImageResource(firstProduct.getImageRes() != 0 ? firstProduct.getImageRes() : R.drawable.sample_flower);
                    }

                } else {
                    tvTitle.setText("Đơn hàng rỗng");
                    tvQuantity.setText("x0");
                }

                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
            }
        }
    }
}
