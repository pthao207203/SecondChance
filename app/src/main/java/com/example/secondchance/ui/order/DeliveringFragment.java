package com.example.secondchance.ui.order;

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
import com.example.secondchance.databinding.FragmentDeliveringBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class DeliveringFragment extends Fragment {
    private FragmentDeliveringBinding binding;
    private DeliveringAdapter adapter;
    private final List<OrderWrapper> orderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;

    public interface DeliveringOrderNavigationListener {
        void navigateToDeliveringDetail(String orderId, @Nullable Order.DeliveryOverallStatus deliveryStatus);
    }
    private DeliveringOrderNavigationListener navigationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeliveringBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof DeliveringOrderNavigationListener) {
            navigationListener = (DeliveringOrderNavigationListener) getParentFragment();
        } else {
            Log.e("DeliveringFragment", "Parent fragment must implement DeliveringOrderNavigationListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderRepository = new OrderRepository();

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new DeliveringAdapter(orderList, (orderId, deliveryStatus) -> {

            if (navigationListener != null) {

                navigationListener.navigateToDeliveringDetail(orderId, deliveryStatus);
            } else {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy trình điều hướng.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvDeliveringOrders.setAdapter(adapter);

        loadData();

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("DeliveringFragment", "Got refresh signal! Reloading data...");
                loadData();
            }
        });
    }

    public void loadData() {

        orderRepository.fetchOrders("1", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                orderList.clear();
                orderList.addAll(data);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvDeliveringOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnOrderClick {
        void openDetail(String orderId, @Nullable Order.DeliveryOverallStatus deliveryStatus);
    }

    private static class DeliveringAdapter extends RecyclerView.Adapter<DeliveringAdapter.OrderViewHolder> {

        private final List<OrderWrapper> items;
        private final OnOrderClick listener;

        DeliveringAdapter(List<OrderWrapper> items, OnOrderClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_delivering_order, parent, false);
            return new OrderViewHolder(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class OrderViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvQuantity, tvStatus, tvDescription;
            final OnOrderClick listener;

            OrderViewHolder(@NonNull View itemView, OnOrderClick listener) {
                super(itemView);
                this.listener = listener;
                imgProduct    = itemView.findViewById(R.id.imgProduct);
                tvTitle       = itemView.findViewById(R.id.tvTitle);
                tvPrice       = itemView.findViewById(R.id.tvPrice);
                tvQuantity    = itemView.findViewById(R.id.tvQuantity);
                tvStatus      = itemView.findViewById(R.id.tvStatus);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }

            void bind(final OrderWrapper order) {

                tvTitle.setText(order.order.getTitle());
                tvPrice.setText(order.order.getPrice());
                tvQuantity.setText(order.order.getQuantity());

                Order.DeliveryOverallStatus deliveryStatus = order.order.getDeliveryStatus();

                if (deliveryStatus != null) {
                    switch (deliveryStatus) {
                        case PACKAGED:
                            tvStatus.setText("Đã đóng gói");
                            break;
                        case AT_POST_OFFICE:
                            tvStatus.setText("Đã tới bưu cục");
                            break;
                        case DELIVERING:
                            tvStatus.setText("Đang giao hàng");
                            break;
                        case DELIVERED:
                            tvStatus.setText("Đã giao");
                            break;
                        default:
                            tvStatus.setText(order.order.getStatusText());
                            break;
                    }
                } else {

                    tvStatus.setText(order.order.getStatusText());
                }

                tvDescription.setText(order.order.getDescription());

                OrderItem firstItem = order.order.getFirstItem();
                if (firstItem != null && firstItem.getImageUrl() != null) {
                    Glide.with(itemView.getContext())
                            .load(firstItem.getImageUrl())
                            .into(imgProduct);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.openDetail(order.order.getId(), order.order.getDeliveryStatus());
                });
            }
        }
    }
}