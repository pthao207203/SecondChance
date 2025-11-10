package com.example.secondchance.ui.order;

import android.graphics.Typeface;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.databinding.FragmentRefundOrderBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefundFragment extends Fragment {

    private FragmentRefundOrderBinding binding;
    private RefundOrdersAdapter adapter;
    private final List<OrderWrapper> orderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;

    private static final String TAG = "RefundFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        orderRepository = new OrderRepository();

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RefundOrdersAdapter(orderList, (orderId, refundStatus) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                if (refundStatus != null) {
                    args.putSerializable("refundStatus", refundStatus);
                }
                nav.navigate(R.id.action_orderFragment_to_refundOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết hoàn trả.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvDeliveringOrders.setAdapter(adapter);

        loadData();
        observeViewModel();
    }

    private void observeViewModel() {
        if (sharedViewModel == null) return;
        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d(TAG, "Nhận lệnh refresh, tải lại dữ liệu...");
                loadData();
                sharedViewModel.clearRefreshRequest();
            }
        });
    }

    private void loadData() {

        orderRepository.fetchOrders("4", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                boolean needsFallback = data == null || data.isEmpty();

                if (!needsFallback) {

                    List<OrderWrapper> filtered = new ArrayList<>();
                    for (OrderWrapper o : data) {
                        if (o != null && o.order.getRefundStatus() != null) {
                            filtered.add(o);
                        }
                    }
                    if (!filtered.isEmpty()) {
                        applyOrders(filtered);
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

                Log.w(TAG, "fetchOrders(4) failed: " + message + " -> fallback to all");
                loadAllAndFilterRefunds();
            }
        });
    }

    private void loadAllAndFilterRefunds() {
        orderRepository.fetchOrders(null, new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                Map<String, OrderWrapper> byId = new HashMap<>();
                if (data != null) {
                    for (OrderWrapper o : data) {
                        if (o == null) continue;
                        if (o.order.getRefundStatus() != null) {

                            byId.put(o.order.getId(), o.order);
                        }
                    }
                }

                applyOrders(new ArrayList<>(byId.values()));
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi tải đơn hàng hoàn trả: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyOrders(List<OrderWrapper> items) {
        orderList.clear();
        if (items != null) orderList.addAll(items);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvDeliveringOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnOrderClickListener {
        void onClick(String orderId, @Nullable Order.RefundStatus refundStatus);
    }

    private static class RefundOrdersAdapter extends RecyclerView.Adapter<RefundOrdersAdapter.RefundViewHolder> {
        private final List<OrderWrapper> items;
        private final OnOrderClickListener listener;

        RefundOrdersAdapter(List<OrderWrapper> items, OnOrderClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RefundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_refund_order, parent, false);
            return new RefundViewHolder(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull RefundViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        private static class RefundViewHolder extends RecyclerView.ViewHolder {
            com.google.android.material.imageview.ShapeableImageView imgProduct;
            ImageView imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            final OnOrderClickListener listener;

            RefundViewHolder(@NonNull View itemView, OnOrderClickListener listener) {
                super(itemView);
                this.listener = listener;
                imgProduct         = itemView.findViewById(R.id.imgProduct);
                imgViewInvoiceArrow= itemView.findViewById(R.id.imgViewInvoiceArrow);
                tvTitle            = itemView.findViewById(R.id.tvTitle);
                tvPrice            = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate     = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatusReview     = itemView.findViewById(R.id.tvStatusReview);
                tvViewInvoiceText  = itemView.findViewById(R.id.tvViewInvoiceText);
            }

            void bind(final OrderWrapper order) {
                tvTitle.setText(order.order.getTitle());
                tvPrice.setText(order.order.getPrice());
                tvSubtitleDate.setText(order.order.getDate());

                Order.RefundStatus status = order.order.getRefundStatus();
                if (status == null) status = Order.RefundStatus.NOT_CONFIRMED;

                tvStatusReview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.normalDay));
                tvStatusReview.setTypeface(null, Typeface.BOLD);
                tvStatusReview.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dot, 0, 0, 0);

                switch (status) {
                    case NOT_CONFIRMED:
                        tvStatusReview.setText("Chưa xác nhận");
                        break;
                    case CONFIRMED:
                        tvStatusReview.setText("Đã xác nhận");
                        break;
                    case REJECTED:
                        tvStatusReview.setText("Đã từ chối");
                        tvStatusReview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.normalDay));
                        break;
                    case SUCCESSFUL:
                        tvStatusReview.setText("Hoàn trả thành công");
                        tvStatusReview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.darkerDay));
                        tvStatusReview.setTypeface(null, Typeface.NORMAL);
                        tvStatusReview.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        break;
                }

                OrderItem firstItem = order.order.getFirstItem();
                if (firstItem != null && firstItem.getImageUrl() != null) {
                    Glide.with(itemView.getContext())
                            .load(firstItem.getImageUrl())
                            .into(imgProduct);
                } else {
                    imgProduct.setImageResource(R.drawable.giohoa3);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onClick(order.order.getId(), order.order.getRefundStatus());
                });
            }
        }
    }
}
