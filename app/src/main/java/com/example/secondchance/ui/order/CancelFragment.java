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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.databinding.FragmentCancelOrderBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class CancelFragment extends Fragment {

    private FragmentCancelOrderBinding binding;
    private CancelOrdersAdapter adapter;
    private final List<OrderWrapper> orderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    private OrderRepository orderRepository;

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

        binding.rvCancelOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CancelOrdersAdapter(orderList, orderId -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                nav.navigate(R.id.action_orderFragment_to_canceledOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng đã hủy.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvCancelOrders.setAdapter(adapter);

        loadData();

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("CancelFragment", "Got refresh signal! Reloading data...");
                loadData();
            }
        });
    }

    public void loadData() {

        orderRepository.fetchOrders("3", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
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
        if (binding != null) binding.rvCancelOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnOrderClick {
        void openDetail(String orderId);
    }

    private static class CancelOrdersAdapter extends RecyclerView.Adapter<CancelOrdersAdapter.OrderViewHolder> {

        private final List<OrderWrapper> items;
        private final OnOrderClick listener;

        CancelOrdersAdapter(List<OrderWrapper> items, OnOrderClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_canceled_order, parent, false);
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
                tvDate  = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            void bind(final OrderWrapper order) {
                tvTitle.setText(order.order.getTitle());
                tvDate.setText(order.order.getDate());
                tvPrice.setText(order.order.getPrice());

                OrderItem firstItem = order.order.getFirstItem();
                if (firstItem != null && firstItem.getImageUrl() != null) {
                    Glide.with(itemView.getContext())
                            .load(firstItem.getImageUrl())
                            .into(imgProduct);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.openDetail(order.order.getId());
                });
            }
        }
    }
}