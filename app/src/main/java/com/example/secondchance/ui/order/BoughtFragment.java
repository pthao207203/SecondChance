package com.example.secondchance.ui.order;

import android.content.Context;
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
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.databinding.FragmentBoughtOrderBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.google.android.material.imageview.ShapeableImageView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class BoughtFragment extends Fragment {

    private FragmentBoughtOrderBinding binding;
    private BoughtOrdersAdapter adapter;
    private final List<OrderWrapper> orderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    private OrderRepository orderRepository;

    public interface BoughtOrderNavigationListener {
        void navigateToBoughtDetail(String orderId, boolean isEvaluated);
    }
    private BoughtOrderNavigationListener navigationListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoughtOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof BoughtOrderNavigationListener) {
            navigationListener = (BoughtOrderNavigationListener) getParentFragment();
        } else {

            Log.e("BoughtFragment", "Parent fragment must implement BoughtOrderNavigationListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        orderRepository = new OrderRepository(); // Khởi tạo repo

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BoughtOrdersAdapter(orderList, (orderId, isEvaluated) -> {
            if (navigationListener != null) {

                navigationListener.navigateToBoughtDetail(orderId, isEvaluated);
            } else {
                Toast.makeText(requireContext(), "Lỗi: Không tìm thấy trình điều hướng.", Toast.LENGTH_SHORT).show();
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
                Log.d("BoughtFragment", "Nhận lệnh refresh, tải lại dữ liệu...");
                loadData();
                sharedViewModel.clearRefreshRequest();
            }
        });
    }

    private void loadData() {

        orderRepository.fetchOrders("2", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
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
        if (binding != null) {
            binding.rvDeliveringOrders.setAdapter(null);
        }
        adapter = null;
        binding = null;
        super.onDestroyView();
    }


    private interface OnOrderClickListener {
        void onOrderClick(String orderId, boolean isEvaluated);
    }

    private static class BoughtOrdersAdapter extends RecyclerView.Adapter<BoughtOrdersAdapter.BoughtViewHolder> {

        private final List<OrderWrapper> items;
        private final OnOrderClickListener listener;

        BoughtOrdersAdapter(List<OrderWrapper> items, OnOrderClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public BoughtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View v = inflater.inflate(R.layout.item_bought_order, parent, false);
            return new BoughtViewHolder(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull BoughtViewHolder holder, int position) {
            OrderWrapper order = items.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        // CHỈ CÒN MỘT VIEW HOLDER
        private static class BoughtViewHolder extends RecyclerView.ViewHolder {
            ShapeableImageView imgProduct;
            ImageView imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;

            private final OnOrderClickListener listener;

            BoughtViewHolder(@NonNull View itemView, OnOrderClickListener listener) {
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

            void bind(OrderWrapper order) {
                tvTitle.setText(order.order.getTitle());
                tvPrice.setText(order.order.getPrice());
                tvSubtitleDate.setText(order.order.getDate());

                if (order.order.isEvaluated()) {

                    tvStatusReview.setText("Đã đánh giá");
                    tvStatusReview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.darkerDay));
                    tvStatusReview.setTypeface(null, Typeface.NORMAL);

                    tvStatusReview.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {

                    tvStatusReview.setText("Chưa đánh giá");
                    tvStatusReview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.normalDay));
                    tvStatusReview.setTypeface(null, Typeface.BOLD);

                    tvStatusReview.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_dot, 0, 0, 0);
                }


                OrderItem firstItem = order.order.getFirstItem();
                if (firstItem != null && firstItem.getImageUrl() != null) {
                    Glide.with(itemView.getContext())
                            .load(firstItem.getImageUrl())
                            .into(imgProduct);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onOrderClick(order.order.getId(), order.order.isEvaluated());
                });
            }
        }
    }
}
