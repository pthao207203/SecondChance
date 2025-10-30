package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.lifecycle.ViewModelProvider;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.databinding.FragmentBoughtOrderBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.List;

public class BoughtFragment extends Fragment {
    
    private FragmentBoughtOrderBinding binding;
    private BoughtOrdersAdapter adapter;
    private final List<Order> dummyOrderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoughtOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // RecyclerView
        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Dữ liệu mẫu
        loadDummyData();
        
        // Adapter + listener click: gọi navigateToDetail ở Fragment cha (StatusOrderFragment)
        adapter = new BoughtOrdersAdapter(dummyOrderList, (orderId, isEvaluated) -> {
            try {
                NavController nav = Navigation.findNavController(
                  requireActivity(), R.id.nav_host_fragment_activity_main
                );
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                args.putBoolean("isEvaluated", isEvaluated);
                nav.navigate(R.id.action_orderFragment_to_boughtOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.rvDeliveringOrders.setAdapter(adapter);
        observeViewModel();
    }
    private void observeViewModel() {
        if (sharedViewModel == null) return;

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {

                Log.d("BoughtFragment", "Nhận lệnh refresh, tải lại dữ liệu...");

                loadDummyData();

                sharedViewModel.clearRefreshRequest();
            }
        });
    }
    private void loadDummyData() {
        dummyOrderList.clear();
        dummyOrderList.add(new Order("BOUGHT001", "Giỏ gỗ cắm hoa", "50.000", "x1", null, "Đã giao 17/6/2025", "Chưa đánh giá", null, null, false, null, Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("BOUGHT002", "Tranh sơn mài", "250.000", "x1", null, "Đã giao 19/6/2025", "Đã đánh giá", null, null, true,  null, Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("BOUGHT003", "Bình gốm cổ", "150.000", "x1", null, "Đã giao 18/6/2025", "Chưa đánh giá", null, null, false, null, Order.DeliveryOverallStatus.DELIVERED));
        
        if (adapter != null) adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.rvDeliveringOrders.setAdapter(null);
        }
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
    
    // Adapter

    private interface OnOrderClickListener {
        void onOrderClick(String orderId, boolean isEvaluated);
    }

    private static class BoughtOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        
        private final List<Order> items;
        private final OnOrderClickListener listener;
        
        private static final int VIEW_TYPE_NOT_EVALUATED = 1;
        private static final int VIEW_TYPE_EVALUATED     = 2;
        
        BoughtOrdersAdapter(List<Order> items, OnOrderClickListener listener) {
            this.items = items;
            this.listener = listener;
        }
        
        @Override
        public int getItemViewType(int position) {
            Order order = items.get(position);
            return order.isEvaluated() ? VIEW_TYPE_EVALUATED : VIEW_TYPE_NOT_EVALUATED;
        }
        
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_EVALUATED) {
                View v = inflater.inflate(R.layout.item_bought_evaluated, parent, false);
                return new EvaluatedViewHolder(v, listener);
            } else {
                View v = inflater.inflate(R.layout.item_bought_not_evaluated, parent, false);
                return new NotEvaluatedViewHolder(v, listener);
            }
        }
        
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Order order = items.get(position);
            if (holder instanceof EvaluatedViewHolder) {
                ((EvaluatedViewHolder) holder).bind(order);
            } else if (holder instanceof NotEvaluatedViewHolder) {
                ((NotEvaluatedViewHolder) holder).bind(order);
            }
        }
        
        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }
        
        //  ViewHolder: Chưa đánh giá
        private static class NotEvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            
            private final OnOrderClickListener listener;
            
            NotEvaluatedViewHolder(@NonNull View itemView, OnOrderClickListener listener) {
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
            
            void bind(Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatusReview.setText(order.getStatusText()); // "Chưa đánh giá"
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onOrderClick(order.getId(), order.isEvaluated());
                });
            }
        }
        
        //  ViewHolder: Đã đánh giá
        private static class EvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            
            private final OnOrderClickListener listener;
            
            EvaluatedViewHolder(@NonNull View itemView, OnOrderClickListener listener) {
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
            
            void bind(Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatusReview.setText(order.getStatusText()); // "Đã đánh giá"
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onOrderClick(order.getId(), order.isEvaluated());
                });
            }
        }
    }
}
