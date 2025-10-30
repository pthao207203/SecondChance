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
import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentRefundOrderBinding;
import java.util.ArrayList;
import java.util.List;

public class RefundFragment extends Fragment {
    private FragmentRefundOrderBinding binding;
    private RefundOrdersAdapter adapter;
    private List<Order> dummyOrderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView
        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        // dữ liệu mẫu (dummy data)
        loadDummyData();

        // Khởi tạo Adapter và gán listener
        adapter = new RefundOrdersAdapter(dummyOrderList);

        // Gán Adapter cho RecyclerView
        binding.rvDeliveringOrders.setAdapter(adapter);
    }

    private void loadDummyData() {
        dummyOrderList.clear();
        dummyOrderList.add(new Order("REFUND001", "Giỏ gỗ cắm hoa", "₫ 50.000", null, null, "Đã giao 17/6/2025", "Chưa xác nhận", null, null, false, Order.RefundStatus.NOT_CONFIRMED,Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("REFUND002", "Tranh sơn mài", "₫ 250.000", null, null, "Đã giao 19/6/2025", "Đã xác nhận", null, null, false, Order.RefundStatus.CONFIRMED,Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("REFUND003", "Bình gốm cổ", "₫ 150.000", null, null, "Đã giao 18/6/2025", "Đã từ chối", null, null, false, Order.RefundStatus.REJECTED,Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("REFUND004", "Nhẫn kim cương", "₫ 500.000", null, null, "Đã giao 20/6/2025", "Hoàn trả thành công", null, null, false, Order.RefundStatus.SUCCESSFUL,Order.DeliveryOverallStatus.DELIVERED));

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }

    public interface OnOrderClickListener {
        void onOrderClick(String orderId);
    }

    private class RefundOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Order> items;
        // hằng số định nghĩa view type
        private static final int VIEW_TYPE_NOT_CONFIRMED = 1;
        private static final int VIEW_TYPE_CONFIRMED = 2;
        private static final int VIEW_TYPE_REJECTED = 3;
        private static final int VIEW_TYPE_SUCCESSFUL = 4;
        RefundOrdersAdapter(List<Order> items) {
            this.items = items;
        }

        // Xác định view type
        @Override
        public int getItemViewType(int position) {
            Order order = items.get(position);
            if (order == null || order.getRefundStatus() == null) {
                return VIEW_TYPE_NOT_CONFIRMED; // Mặc định an toàn
            }
            switch (order.getRefundStatus()) {
                case NOT_CONFIRMED:
                    return VIEW_TYPE_NOT_CONFIRMED;
                case CONFIRMED:
                    return VIEW_TYPE_CONFIRMED;
                case REJECTED:
                    return VIEW_TYPE_REJECTED;
                case SUCCESSFUL:
                    return VIEW_TYPE_SUCCESSFUL;
                default:
                    return VIEW_TYPE_NOT_CONFIRMED;
            }
        }

        // Tạo ViewHolder tương ứng
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            switch (viewType) {
                case VIEW_TYPE_CONFIRMED:
                    view = inflater.inflate(R.layout.item_refund_confirmed, parent, false);
                    break;
                case VIEW_TYPE_REJECTED:
                    view = inflater.inflate(R.layout.item_refund_rejected, parent, false);
                    break;
                case VIEW_TYPE_SUCCESSFUL:
                    view = inflater.inflate(R.layout.item_refund_successful, parent, false);
                    break;
                case VIEW_TYPE_NOT_CONFIRMED:
                default:
                    view = inflater.inflate(R.layout.item_refund_not_confirmed, parent, false);
                    break;
            }
            return new RefundViewHolder(view);
        }

        // Gán dữ liệu vào ViewHolder
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Order order = items.get(position);
            ((RefundViewHolder) holder).bind(order);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // --- ViewHolder CHUNG cho cả 4 loại item ---
        class RefundViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            // LinearLayout invoiceLayout;

            RefundViewHolder(View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                imgViewInvoiceArrow = itemView.findViewById(R.id.imgViewInvoiceArrow);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatusReview = itemView.findViewById(R.id.tvStatusReview); // TextView trạng thái
                tvViewInvoiceText = itemView.findViewById(R.id.tvViewInvoiceText);
                // invoiceLayout = itemView.findViewById(R.id.invoice_layout);
            }

            void bind(Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatusReview.setText(order.getStatusText()); // Hiển thị trạng thái tương ứng

                // Xử lý click cho item
                itemView.setOnClickListener(v -> {
                    if (getParentFragment() instanceof OrderFragment) {
                        ((OrderFragment) getParentFragment()).navigateToDetail(
                                order.getId(),
                                R.id.action_orderFragment_to_refundOrderDetailFragment, // Action ID mới
                                order.getRefundStatus() // Gửi RefundStatus
                        );
                    }
                });

            }
        }
    }
}