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
import com.example.secondchance.databinding.FragmentRefundOrderBinding;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RefundShopFragment extends Fragment {

    private static final String TAG = "RefundShopFrag";
    private FragmentRefundOrderBinding binding;
    private RefundShopOrdersAdapter adapter;
    private final List<ShopOrder> shopOrderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;
    private String currentShopId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderBinding.inflate(inflater, container, false);
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

        adapter = new RefundShopOrdersAdapter(shopOrderList, (orderId, refundStatus) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                if (refundStatus != null) {
                    args.putSerializable("refundStatus", refundStatus);
                }
                nav.navigate(R.id.action_shopOrderFragment_to_refundShopOrderDetailFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigate failed", e);
                Toast.makeText(requireContext(), "Không thể mở chi tiết hoàn trả.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvDeliveringOrders.setAdapter(adapter);

        loadData();

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                loadData();
            }
        });
    }

    private void loadData() {
        if (currentShopId == null || currentShopId.isEmpty()) return; // Safety check

        orderRepository.fetchOrdersForShop(currentShopId, "4", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                boolean needsFallback = data == null || data.isEmpty();

                if (!needsFallback) {
                    List<ShopOrder> filtered = new ArrayList<>();
                    for (OrderWrapper o : data) {
                        if (o != null && o.order != null && o.order.getRefundStatus() != null) {
                            filtered.add(convertToShopOrder(o));
                        }
                    }
                    if (!filtered.isEmpty()) {
                        updateList(filtered);
                        return;
                    } else {
                        needsFallback = true;
                    }
                }

                if (needsFallback) {
                    loadAllAndFilterRefunds();
                }
            }

            @Override
            public void onError(String message) {
                loadAllAndFilterRefunds();
            }
        });
    }

    private void loadAllAndFilterRefunds() {

        orderRepository.fetchOrders(null, new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                List<ShopOrder> filteredList = new ArrayList<>();
                if (data != null) {
                    for (OrderWrapper o : data) {
                        if (o != null && o.order != null && o.order.getRefundStatus() != null) {
                            filteredList.add(convertToShopOrder(o));
                        }
                    }
                }
                updateList(filteredList);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi tải đơn hàng hoàn trả: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateList(List<ShopOrder> newList) {
        shopOrderList.clear();
        shopOrderList.addAll(newList);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private ShopOrder convertToShopOrder(OrderWrapper wrapper) {
        com.example.secondchance.data.model.Order.RefundStatus apiRefund = wrapper.order.getRefundStatus();
        ShopOrder.RefundStatus shopRefundStatus = mapToShopRefundStatus(apiRefund);

        String statusText;
        switch (shopRefundStatus) {
            case CONFIRMED:  statusText = "Bạn đã chấp nhận yêu cầu"; break;
            case DELIVERING: statusText = "Đang vận chuyển về"; break;
            case SUCCESSFUL: statusText = "Đã nhận lại hàng"; break;
            case REJECTED:   statusText = "Bạn đã từ chối"; break;
            case NOT_CONFIRMED:
            default:         statusText = "Chưa xác nhận"; break;
        }

        String displayDate = formatRefundDate(wrapper.order.createdAt, shopRefundStatus);

        List<ShopOrderProduct> products = new ArrayList<>();
        OrderItem firstItem = wrapper.order.getFirstItem();
        if (firstItem != null) {
            products.add(new ShopOrderProduct(
                    "ID",
                    firstItem.getName(),
                    "Mã đơn: " + (wrapper.order.getId() != null ? wrapper.order.getId() : ""),
                    formatVnd(wrapper.order.totalAmount),
                    0,
                    firstItem.getQuantity(),
                    firstItem.getImageUrl()
            ));
        }

        return new ShopOrder(
                wrapper.order.getId(),
                products,
                formatVnd(wrapper.order.totalAmount),
                displayDate,
                statusText,
                "Hoàn trả",
                ShopOrder.ShopOrderType.REFUND,
                false,
                shopRefundStatus,
                null
        );
    }

    private String formatRefundDate(String isoDate, ShopOrder.RefundStatus status) {
        if (isoDate == null || isoDate.isEmpty()) return "";

        String datePart = "";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inFormat.parse(isoDate);

            SimpleDateFormat outFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            datePart = outFormat.format(date);
        } catch (Exception e) {
            datePart = "";
        }

        if (status == ShopOrder.RefundStatus.SUCCESSFUL) {
            return "Hoàn trả thành công " + datePart;
        } else {
            return "Đã tạo yêu cầu " + datePart;
        }
    }

    private ShopOrder.RefundStatus mapToShopRefundStatus(com.example.secondchance.data.model.Order.RefundStatus apiStatus) {
        if (apiStatus == null) return ShopOrder.RefundStatus.NOT_CONFIRMED;
        try {
            return ShopOrder.RefundStatus.valueOf(apiStatus.name());
        } catch (Exception e) {
            return ShopOrder.RefundStatus.NOT_CONFIRMED;
        }
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvDeliveringOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnShopOrderClickListener {
        void onClick(String orderId, @Nullable ShopOrder.RefundStatus refundStatus);
    }

    private static class RefundShopOrdersAdapter extends RecyclerView.Adapter<RefundShopOrdersAdapter.RefundViewHolder> {

        private final List<ShopOrder> items;
        private final OnShopOrderClickListener listener;

        RefundShopOrdersAdapter(List<ShopOrder> items, OnShopOrderClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RefundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_shop_refund_order, parent, false);
            return new RefundViewHolder(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull RefundViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class RefundViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview;
            View invoiceLayout;
            final OnShopOrderClickListener listener;

            RefundViewHolder(@NonNull View itemView, OnShopOrderClickListener listener) {
                super(itemView);
                this.listener = listener;
                imgProduct         = itemView.findViewById(R.id.imgProduct);
                tvTitle            = itemView.findViewById(R.id.tvTitle);
                tvPrice            = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate     = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatusReview     = itemView.findViewById(R.id.tvStatusReview);
                invoiceLayout      = itemView.findViewById(R.id.invoice_layout);
            }

            void bind(final ShopOrder order) {
                if (tvSubtitleDate != null) tvSubtitleDate.setText(order.getDate());

                if (tvStatusReview != null) tvStatusReview.setText(order.getStatusText());

                if (tvPrice != null) tvPrice.setText(order.getTotalPrice());

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = order.getItems().get(0);
                    if (tvTitle != null) tvTitle.setText(firstProduct.getTitle());

                    if (firstProduct.getImageUrl() != null && !firstProduct.getImageUrl().isEmpty()) {
                        Glide.with(itemView.getContext()).load(firstProduct.getImageUrl()).into(imgProduct);
                    } else {
                        if (imgProduct != null) imgProduct.setImageResource(R.drawable.sample_flower);
                    }
                }

                View.OnClickListener clickListener = v -> {
                    if (listener != null) listener.onClick(order.getId(), order.getRefundStatus());
                };

                itemView.setOnClickListener(clickListener);
                if (invoiceLayout != null) invoiceLayout.setOnClickListener(clickListener);
            }
        }
    }
}
