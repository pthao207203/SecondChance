package com.example.secondchance.ui.order;
import com.example.secondchance.data.model.Order;
import android.widget.ImageView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R; // Đảm bảo import R của bạn
import com.example.secondchance.databinding.FragmentBoughtOrderBinding; // ViewBinding

import java.util.ArrayList;
import java.util.List;

public class BoughtFragment extends Fragment {

    private FragmentBoughtOrderBinding binding;
    private BoughtOrdersAdapter adapter;
    private List<Order> dummyOrderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentBoughtOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        // dữ liệu mẫu (dummy data)
        loadDummyData();

        //Khởi tạo Adapter và gán listener
        adapter = new BoughtOrdersAdapter(dummyOrderList);

        //Gán Adapter cho RecyclerView
        binding.rvDeliveringOrders.setAdapter(adapter);
    }

    private void loadDummyData() {
        dummyOrderList.clear();
        dummyOrderList.add(new Order("BOUGHT001", "Giỏ gỗ cắm hoa", "₫ 50.000", "x1", null, "Đã giao 17/6/2025", "Chưa đánh giá", null, null, false,null, Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("BOUGHT002", "Tranh sơn mài", "₫ 250.000", "x1", null, "Đã giao 19/6/2025", "Đã đánh giá", null, null, true,null, Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("BOUGHT003", "Bình gốm cổ", "₫ 150.000", "x1", null, "Đã giao 18/6/2025", "Chưa đánh giá", null, null, false,null, Order.DeliveryOverallStatus.DELIVERED));

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // click listener
    public interface OnOrderClickListener {
        void onOrderClick(String orderId);
    }

    private class BoughtOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Order> items;

        private static final int VIEW_TYPE_NOT_EVALUATED = 1;
        private static final int VIEW_TYPE_EVALUATED = 2;

        BoughtOrdersAdapter(List<Order> items) {
            this.items = items;
        }

        // Xác định view type
        @Override
        public int getItemViewType(int position) {
            Order order = items.get(position);
            if (order.isEvaluated()) {
                return VIEW_TYPE_EVALUATED;
            } else {
                return VIEW_TYPE_NOT_EVALUATED;
            }
        }

        // Tạo ViewHolder tương ứng
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            if (viewType == VIEW_TYPE_EVALUATED) {
                view = inflater.inflate(R.layout.item_bought_evaluated, parent, false);
                return new EvaluatedViewHolder(view);
            } else { // VIEW_TYPE_NOT_EVALUATED
                view = inflater.inflate(R.layout.item_bought_not_evaluated, parent, false);
                return new NotEvaluatedViewHolder(view);
            }
        }

        // Gán dữ liệu vào ViewHolder tương ứng
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Order order = items.get(position);
            if (holder.getItemViewType() == VIEW_TYPE_EVALUATED) {
                ((EvaluatedViewHolder) holder).bind(order);
            } else { // VIEW_TYPE_NOT_EVALUATED
                ((NotEvaluatedViewHolder) holder).bind(order);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // ViewHolder cho item_bought_not_evaluated.xml
        class NotEvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            // LinearLayout invoiceLayout; // xử lý click "Xem hóa đơn"

            NotEvaluatedViewHolder(View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                imgViewInvoiceArrow = itemView.findViewById(R.id.imgViewInvoiceArrow);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatusReview = itemView.findViewById(R.id.tvStatusReview);
                tvViewInvoiceText = itemView.findViewById(R.id.tvViewInvoiceText);
                // invoiceLayout = itemView.findViewById(R.id.invoice_layout);
            }

            void bind(Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatusReview.setText(order.getStatusText()); // "Chưa đánh giá"

                // Xử lý click cho item
                itemView.setOnClickListener(v -> {
                    if (getParentFragment() instanceof OrderFragment) {

                        ((OrderFragment) getParentFragment()).navigateToDetail(
                                order.getId(),
                                R.id.action_orderFragment_to_boughtOrderDetailFragment,
                                order.isEvaluated() // Gửi trạng thái isEvaluated (false)
                        );
                    }
                });
            }
        }

        // ViewHolder cho item_bought_evaluated.xml
        class EvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            // LinearLayout invoiceLayout;

            EvaluatedViewHolder(View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                imgViewInvoiceArrow = itemView.findViewById(R.id.imgViewInvoiceArrow);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatusReview = itemView.findViewById(R.id.tvStatusReview);
                tvViewInvoiceText = itemView.findViewById(R.id.tvViewInvoiceText);
                // invoiceLayout = itemView.findViewById(R.id.invoice_layout);
            }

            void bind(Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatusReview.setText(order.getStatusText()); // "Đã đánh giá"

                // Xử lý click cho item
                itemView.setOnClickListener(v -> {
                    if (getParentFragment() instanceof OrderFragment) {

                        ((OrderFragment) getParentFragment()).navigateToDetail(
                                order.getId(),
                                R.id.action_orderFragment_to_boughtOrderDetailFragment,
                                order.isEvaluated() //trạng thái isEvaluated
                        );
                    }
                });

            }
        }
    }
}