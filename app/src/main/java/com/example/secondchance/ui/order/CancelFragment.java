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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentCancelOrderBinding;
import com.example.secondchance.data.model.Order;
import java.util.ArrayList;
import java.util.List;

public class CancelFragment extends Fragment {
    private FragmentCancelOrderBinding binding;
    private CancelOrdersAdapter adapter;
    private List<Order> dummyOrderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCancelOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDummyData();
        adapter = new CancelOrdersAdapter(dummyOrderList);
        binding.rvDeliveringOrders.setAdapter(adapter);
    }


    private void loadDummyData() {
        dummyOrderList.clear();
        dummyOrderList.add(new Order("CANCELED001", "Giỏ gỗ cắm hoa", "₫ 50.000", "x1", "Giá cố định", "Đã hủy 17/6/2025", "Đã hủy", null, Order.OrderType.UNCONFIRMED, false, null, null)); // Ví dụ
        dummyOrderList.add(new Order("CANCELED002", "Vòng tay bạc", "₫ 100.000", "x1", "Giá cố định", "Đã hủy 18/6/2025", "Đã hủy", null, Order.OrderType.CONFIRMED_FIXED, false, null, null)); // Ví dụ
        dummyOrderList.add(new Order("CANCELED003", "Tranh sơn dầu", "₫ 300.000", "x1", "Đấu giá", "Đã hủy 19/6/2025", "Đã hủy", null, Order.OrderType.CONFIRMED_AUCTION, false, null, null)); // Ví dụ

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class CancelOrdersAdapter extends RecyclerView.Adapter<CancelOrdersAdapter.OrderViewHolder> {
        private List<Order> items;
        public CancelOrdersAdapter(List<Order> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_canceled_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = items.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {

            // TODO: Khai báo các TextView/ImageView thật sự có trong item_canceled_order.xml
            TextView tvTitle;
            TextView tvDate;
            TextView tvPrice;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: Ánh xạ các View thật sự từ item_canceled_order.xml
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            public void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvDate.setText(order.getDate());
                tvPrice.setText(order.getPrice());

                // Gán listener
                itemView.setOnClickListener(v -> {

                    if (getParentFragment() instanceof OrderFragment) {
                        ((OrderFragment) getParentFragment()).navigateToDetail(
                                order.getId(),
                                R.id.action_orderFragment_to_canceledOrderDetailFragment, // Action ID mới
                                null // Không cần gửi Type
                        );
                    }
                });
            }
        }
    }
}