package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.databinding.FragmentCancelOrderBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.List;

public class CancelFragment extends Fragment {

    private FragmentCancelOrderBinding binding;
    private CancelOrdersAdapter adapter;
    private final List<Order> dummyOrderList = new ArrayList<>();

    private SharedViewModel sharedViewModel;

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

        binding.rvCancelOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDummyData();

        adapter = new CancelOrdersAdapter(dummyOrderList, orderId -> {
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

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("CancelFragment", "Got refresh signal! Reloading data...");
                loadDummyData();
            }
        });
    }

    public void loadDummyData() {
        dummyOrderList.clear();
        dummyOrderList.add(new Order("CANCELED001", "Giỏ gỗ cắm hoa", "50.000", "x1",
                "Giá cố định", "Đã hủy 17/6/2025", "Đã hủy", null, Order.OrderType.UNCONFIRMED,
                false, null, null));
        dummyOrderList.add(new Order("CANCELED002", "Vòng tay bạc", "100.000", "x1",
                "Giá cố định", "Đã hủy 18/6/2025", "Đã hủy", null, Order.OrderType.CONFIRMED_FIXED,
                false, null, null));
        if (adapter != null) adapter.notifyDataSetChanged();
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
        
        private final List<Order> items;
        private final OnOrderClick listener;
        
        CancelOrdersAdapter(List<Order> items, OnOrderClick listener) {
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
            TextView tvTitle, tvDate, tvPrice;
            private final OnOrderClick listener;
            
            OrderViewHolder(@NonNull View itemView, OnOrderClick listener) {
                super(itemView);
                this.listener = listener;
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDate  = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
            
            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvDate.setText(order.getDate());
                tvPrice.setText(order.getPrice());
                
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.openDetail(order.getId());
                });
            }
        }
    }
}
