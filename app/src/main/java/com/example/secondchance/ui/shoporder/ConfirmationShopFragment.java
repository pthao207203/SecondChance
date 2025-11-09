package com.example.secondchance.ui.shoporder;

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

import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;
// MODEL MỚI ĐANG SỬ DỤNG
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
//
import com.example.secondchance.databinding.FragmentConfirmationBinding;
import com.example.secondchance.ui.shoporder.dialog.ConfirmCancelShopDialog;
import com.example.secondchance.ui.shoporder.dialog.CancelSuccessShopDialog;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationShopFragment extends Fragment
        implements ConfirmCancelShopDialog.OnCancelConfirmationListener,
        CancelSuccessShopDialog.OnDismissListener {

    private FragmentConfirmationBinding binding;
    private ConfirmationShopAdapter adapter;
    // ĐÃ THAY ĐỔI: Dùng model ShopOrder
    private final List<ShopOrder> orderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmationBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        loadDummyData(); // Sẽ tải dữ liệu ShopOrder mới

        adapter = new ConfirmationShopAdapter(
                orderList,
                (orderId, orderType) -> {
                    // Khi shop click vào item, chuyển đến trang chi tiết để xác nhận
                    try {
                        NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                        Bundle args = new Bundle();
                        args.putString("orderId", orderId);
                        if (orderType != null) {
                            args.putSerializable("orderType", orderType);
                        }
                        nav.navigate(R.id.action_shopOrderFragment_to_confirmShopOrderDetailFragment, args);
                    } catch (Exception e) {
                        Log.e("ConfirmationShopFragment", "Navigate to ConfirmDetail failed", e);
                        Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
                    }
                },
                orderId -> {
                    // Logic này có thể được gọi từ một nút khác (ví dụ: trong trang chi tiết)
                    ConfirmCancelShopDialog dialog = new ConfirmCancelShopDialog(orderId, this);
                    dialog.show(getParentFragmentManager(), ConfirmCancelShopDialog.TAG);
                }
        );
        binding.rvOrders.setAdapter(adapter);

        // Lắng nghe tín hiệu refresh
        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("ConfirmationShopFragment", "Got refresh signal! Reloading data...");
                loadDummyData();
            }
        });
    }

    @Override
    public void onCancelConfirmed(String orderId) {
        Log.d("ConfirmationShopFragment", "Order " + orderId + " confirmed for cancellation.");
        // TODO: GỌI API HỦY ĐƠN
        CancelSuccessShopDialog successDialog = new CancelSuccessShopDialog(this);
        successDialog.show(getParentFragmentManager(), CancelSuccessShopDialog.TAG);
    }

    @Override
    public void onSuccessfulDismiss() {
        Log.d("ConfirmationShopFragment", "Success dialog dismissed. Refreshing lists.");
        sharedViewModel.refreshOrderLists();
        sharedViewModel.requestTabChange(3); // Chuyển sang tab Hủy (index 3)
    }

    /**
     * Giả lập việc tải danh sách các đơn hàng "Chưa xác nhận" từ server
     * (Đúng theo logic bạn mô tả: khách đặt, shop thấy danh sách này)
     */
    public void loadDummyData() {
        orderList.clear();

        // 1. Tạo danh sách sản phẩm cho đơn hàng (Vì 1 đơn có thể có nhiều SP)
        List<ShopOrderProduct> productsInOrder1 = new ArrayList<>();
        // (Bạn nên dùng R.drawable thực tế nếu có)
        productsInOrder1.add(new ShopOrderProduct(
                "P102", // ID sản phẩm
                "Giỏ gỗ cắm hoa", // Tên sản phẩm
                "Loại nhỏ, đan tre", // Subtitle/mô tả
                "50.000", // Giá
                R.drawable.sample_flower, // Ảnh
                1 // Số lượng
        ));

        // 2. Tạo đơn hàng (ShopOrder) chứa danh sách sản phẩm đó
        orderList.add(new ShopOrder(
                "S-ORD-001",    // ID đơn hàng
                productsInOrder1, // Danh sách sản phẩm
                "50.000",       // Tổng tiền
                "17/06/2025",   // Ngày đặt
                "Chưa xác nhận", // Trạng thái text
                null,           // Mô tả (ví dụ: lý do hủy)
                ShopOrder.ShopOrderType.UNCONFIRMED, // <--- LOẠI "CHƯA XÁC NHẬN"
                false, // chưa đánh giá
                null,  // chưa refund
                null   // chưa giao
        ));

        // (Bạn có thể thêm đơn hàng thứ 2 tại đây nếu muốn)

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    // === INTERFACE LISTENER ĐÃ CẬP NHẬT ===
    public interface OnItemClickListener {
        void onItemClick(String orderId, ShopOrder.ShopOrderType orderType);
    }
    public interface OnCancelClickListener {
        void onCancelClick(String orderId);
    }

    // === ADAPTER ĐÃ CẬP NHẬT ===
    private static class ConfirmationShopAdapter extends RecyclerView.Adapter<ConfirmationShopAdapter.UnconfirmedViewHolder> {

        private final List<ShopOrder> items; // <--- Đã đổi sang ShopOrder
        private final OnItemClickListener itemClickListener;
        // private final OnCancelClickListener cancelClickListener; // Không dùng

        ConfirmationShopAdapter(List<ShopOrder> items, OnItemClickListener itemClickListener, OnCancelClickListener cancelClickListener) {
            this.items = items;
            this.itemClickListener = itemClickListener;
            // cancelClickListener không còn được dùng trong ViewHolder
        }

        @NonNull
        @Override
        public UnconfirmedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_shop_unconfirm_order, parent, false);
            return new UnconfirmedViewHolder(view, itemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull UnconfirmedViewHolder holder, int position) {
            ShopOrder order = items.get(position); // <--- Đã đổi sang ShopOrder
            holder.bind(order);
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        // === VIEWHOLDER ĐÃ SỬA LỖI VÀ CẬP NHẬT HOÀN TOÀN ===
        private static class UnconfirmedViewHolder extends RecyclerView.ViewHolder {
            // Khai báo biến (khớp với XML)
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus;

            private final OnItemClickListener itemClickListener;

            // Constructor (khớp với XML, đã sửa lỗi NPE)
            UnconfirmedViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;

                // Ánh xạ View (khớp với XML)
                imgProduct     = itemView.findViewById(R.id.imgProduct);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatus       = itemView.findViewById(R.id.tvStatusReview); // <--- ID đã sửa
            }

            // HÀM BIND (khớp với logic model ShopOrder mới)
            void bind(final ShopOrder order) {

                // Gán dữ liệu chung của ĐƠN HÀNG
                tvPrice.setText(order.getTotalPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatus.setText(order.getStatusText());

                // Vì 1 đơn hàng có thể có nhiều sản phẩm,
                // chúng ta hiển thị tóm tắt thông tin của SẢN PHẨM ĐẦU TIÊN
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = order.getItems().get(0);

                    tvTitle.setText(firstProduct.getTitle());
                    tvQuantity.setText("x" + firstProduct.getQuantity());
                    tvSubtitleFixed.setText(firstProduct.getSubtitle());
                    imgProduct.setImageResource(firstProduct.getImageRes()); // Tải ảnh
                } else {
                    // Trường hợp dự phòng nếu đơn hàng không có sản phẩm
                    tvTitle.setText("Đơn hàng rỗng");
                    tvQuantity.setText("x0");
                    tvSubtitleFixed.setText("Mã đơn: " + order.getId());
                }

                // Gán listener cho toàn bộ item
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
            }
        }
    }
}