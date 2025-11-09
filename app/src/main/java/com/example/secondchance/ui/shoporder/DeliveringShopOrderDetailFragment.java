package com.example.secondchance.ui.shoporder;

import android.content.Context; // <-- Thêm
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // <-- Thêm
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // <-- Thêm
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // <-- Thêm

// === THAY ĐỔI BINDING ===
import com.example.secondchance.databinding.FragmentShopDeliveringOrderDetailBinding;
import com.example.secondchance.databinding.FragmentShopNotYetDeliveringOrderDetailBinding;
//
import com.example.secondchance.R;
// === MODEL ĐỒNG BỘ ===
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.data.model.ShopTrackingStatus; // (Giả sử bạn dùng model này)
//
import com.example.secondchance.ui.shoporder.adapter.ShopTrackingStatusAdapter;
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.List;

public class DeliveringShopOrderDetailFragment extends Fragment {
    private static final String TAG = "DeliveringShopOrderDetail";

    // === BINDING CHO 2 LAYOUT ===
    private FragmentShopDeliveringOrderDetailBinding deliveringBinding;
    private FragmentShopNotYetDeliveringOrderDetailBinding notYetBinding;
    //

    private String receivedShopOrderId;
    private ShopOrder.DeliveryOverallStatus receivedDeliveryStatus;
    private SharedViewModel sharedViewModel; // <-- Thêm

    // === SẢN PHẨM (ĐÃ ĐỒNG BỘ MODEL) ===
    private ShopOrderProductAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();

    // === TRACKING (CHỈ DÙNG CHO 1 LAYOUT) ===
    private List<ShopTrackingStatus> trackingList = new ArrayList<>();
    private ShopTrackingStatusAdapter trackingAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Lấy arguments TRƯỚC để quyết định layout
        if (getArguments() != null) {
            receivedShopOrderId = getArguments().getString("shopOrderId");
            try {
                receivedDeliveryStatus = (ShopOrder.DeliveryOverallStatus) getArguments().getSerializable("deliveryStatus");
            } catch (Exception e) {
                Log.e(TAG, "Error getting deliveryStatus from arguments", e);
                // Mặc định là PACKAGED nếu có lỗi
                receivedDeliveryStatus = ShopOrder.DeliveryOverallStatus.PACKAGED;
            }
        } else {
            Log.e(TAG, "Arguments are null!");
            // Nếu không có arguments, không thể làm gì, quay lại
            Toast.makeText(getContext(), "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        }

        View rootView;
        // === LOGIC CHỌN LAYOUT ===
        if (receivedDeliveryStatus == ShopOrder.DeliveryOverallStatus.PACKAGED) {
            // Trạng thái "Chưa giao" -> Inflate layout "not_yet"
            notYetBinding = FragmentShopNotYetDeliveringOrderDetailBinding.inflate(inflater, container, false);
            rootView = notYetBinding.getRoot();
        } else {
            // Trạng thái "Đang giao", "Đã đến bưu cục"... -> Inflate layout "delivering"
            deliveringBinding = FragmentShopDeliveringOrderDetailBinding.inflate(inflater, container, false);
            rootView = deliveringBinding.getRoot();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Logic UI phân nhánh dựa trên layout đã inflate
        if (receivedDeliveryStatus == ShopOrder.DeliveryOverallStatus.PACKAGED && notYetBinding != null) {
            // --- CÀI ĐẶT CHO LAYOUT "CHƯA GIAO" ---
            setupProductRecyclerView(notYetBinding.rvOrderItems);
            updateStepper(notYetBinding.stepperLayout); // Dùng stepper layout này
            setupNotYetDeliveringListeners(); // Cài đặt nút "Đã giao bưu cục"

        } else if (deliveringBinding != null) {
            // --- CÀI ĐẶT CHO LAYOUT "ĐANG GIAO" ---
            setupProductRecyclerView(deliveringBinding.rvOrderItems);
            updateStepper(deliveringBinding.stepperLayout); // Dùng stepper layout này
            setupTrackingRecyclerView(deliveringBinding.rvTrackingStatus); // Layout này mới có tracking
            loadTrackingDataBasedOnStatus(receivedDeliveryStatus); // Tải data tracking
            if (trackingAdapter != null) {
                trackingAdapter.notifyDataSetChanged();
            }
        } else {
            Log.e(TAG, "Binding bị null, không thể cài đặt UI!");
            return;
        }

        // Tải dữ liệu sản phẩm (chung cho cả 2 layout)
        if (receivedShopOrderId != null) {
            loadDummyProductData(receivedShopOrderId);
            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
                Log.d(TAG, "ShopOrder product list updated for RecyclerView");
            }
        } else {
            Log.e(TAG, "ShopOrder ID is null.");
            Toast.makeText(getContext(), "Lỗi tải chi tiết đơn giao hàng của shop.", Toast.LENGTH_SHORT).show();
        }
    }

    // === CÀI ĐẶT NÚT CHO LAYOUT "CHƯA GIAO" ===
    private void setupNotYetDeliveringListeners() {
        notYetBinding.btnShipOrder.setOnClickListener(v -> {
            Log.d(TAG, "Shop đã xác nhận 'GIAO CHO BƯU CỤC' cho đơn: " + receivedShopOrderId);
            Toast.makeText(getContext(), "Đã xác nhận giao bưu cục!", Toast.LENGTH_SHORT).show();

            // TODO: Gọi API, cập nhật trạng thái đơn hàng sang AT_POST_OFFICE

            // Sau khi xong, refresh list và quay lại
            sharedViewModel.refreshOrderLists();
            // Yêu cầu tab "Đang giao" (index 1) được chọn (vì nó vẫn ở tab này)
            sharedViewModel.requestTabChange(1);
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    // === SỬA: DÙNG ShopOrderProductAdapter ===
    private void setupProductRecyclerView(RecyclerView rv) {
        productAdapter = new ShopOrderProductAdapter(getContext(), productList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(productAdapter);
        rv.setNestedScrollingEnabled(false);
        Log.d(TAG, "ShopOrder product RecyclerView setup complete.");
    }

    // === SỬA: Tải ShopOrderProduct (khớp với DeliveringShopFragment) ===
    private void loadDummyProductData(String shopOrderId) {
        productList.clear();
        if ("SHOPDELIV001".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct("P-301", "Đơn giao nhẫn kim cương", "Giá cố định", "50.000", R.drawable.ic_ring, 1));
        } else if ("SHOPDELIV002".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct("P-302", "Bình gốm sứ", "Hàng dễ vỡ", "150.000", R.drawable.sample_flower, 1));
        } else {
            productList.add(new ShopOrderProduct("P-ERR", "Lỗi tải sản phẩm", "", "0", R.drawable.sample_flower, 0));
        }
    }

    // === CÀI ĐẶT TRACKING (CHO LAYOUT "ĐANG GIAO") ===
    private void setupTrackingRecyclerView(RecyclerView rv) {
        trackingAdapter = new ShopTrackingStatusAdapter(getContext(), trackingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(trackingAdapter);
        rv.setNestedScrollingEnabled(false);
        Log.d(TAG, "Tracking RecyclerView setup complete (Reverse Layout).");
    }

    // Tải data tracking (Giữ nguyên)
    private void loadTrackingDataBasedOnStatus(ShopOrder.DeliveryOverallStatus status) {
        trackingList.clear();
        List<ShopTrackingStatus> temp = new ArrayList<>();

        if (status.ordinal() >= ShopOrder.DeliveryOverallStatus.PACKAGED.ordinal()) {
            temp.add(new ShopTrackingStatus("10:00, 25/10/2025", "Shop đã đóng gói sản phẩm", false));
        }
        if (status.ordinal() >= ShopOrder.DeliveryOverallStatus.AT_POST_OFFICE.ordinal()) {
            temp.add(new ShopTrackingStatus("15:30, 25/10/2025", "Đơn giao đã đến bưu cục", false));
        }
        if (status.ordinal() >= ShopOrder.DeliveryOverallStatus.DELIVERING.ordinal()) {
            temp.add(new ShopTrackingStatus("08:00, 26/10/2025", "Đang giao đến khách hàng", false));
        }
        if (status.ordinal() >= ShopOrder.DeliveryOverallStatus.DELIVERED.ordinal()) {
            temp.add(new ShopTrackingStatus("XX:XX, XX/XX/2025", "Khách hàng đã nhận hàng", false));
        }

        trackingList.addAll(temp);

        if (!trackingList.isEmpty()) {
            trackingList.get(trackingList.size() - 1).setActive(true);
        }
    }

    // === SỬA: Dùng View stepperLayout thay vì binding ===
    private void updateStepper(View stepperLayout) {
        if (stepperLayout == null || getContext() == null) return;

        Log.d(TAG, "Updating shop stepper for status: " + receivedDeliveryStatus);

        ImageView step1Icon = stepperLayout.findViewById(R.id.step1_icon);
        ImageView step2Icon = stepperLayout.findViewById(R.id.step2_icon);
        ImageView step3Icon = stepperLayout.findViewById(R.id.step3_icon);
        ImageView step4Icon = stepperLayout.findViewById(R.id.step4_icon);

        View step1Line = stepperLayout.findViewById(R.id.step1_line);
        View step2Line = stepperLayout.findViewById(R.id.step2_line);
        View step3Line = stepperLayout.findViewById(R.id.step3_line);

        TextView step1Label = stepperLayout.findViewById(R.id.step1_label);
        TextView step2Label = stepperLayout.findViewById(R.id.step2_label);
        TextView step3Label = stepperLayout.findViewById(R.id.step3_label);
        TextView step4Label = stepperLayout.findViewById(R.id.step4_label);

        int activeColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.highLight4);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int activeIcon = R.drawable.ic_active;
        int inactiveIcon = R.drawable.ic_inactive;

        // Reset
        step1Icon.setBackgroundResource(inactiveIcon);
        step2Icon.setBackgroundResource(inactiveIcon);
        step3Icon.setBackgroundResource(inactiveIcon);
        step4Icon.setBackgroundResource(inactiveIcon);
        step1Line.setBackgroundColor(inactiveColor);
        step2Line.setBackgroundColor(inactiveColor);
        step3Line.setBackgroundColor(inactiveColor);
        step1Label.setTextColor(inactiveTextColor);
        step2Label.setTextColor(inactiveTextColor);
        step3Label.setTextColor(inactiveTextColor);
        step4Label.setTextColor(inactiveTextColor);

        switch (receivedDeliveryStatus) {
            case DELIVERED:
                step4Icon.setBackgroundResource(activeIcon);
                step4Label.setTextColor(activeTextColor);
                step3Line.setBackgroundColor(activeColor);
            case DELIVERING:
                step3Icon.setBackgroundResource(activeIcon);
                step3Label.setTextColor(activeTextColor);
                step2Line.setBackgroundColor(activeColor);
            case AT_POST_OFFICE:
                step2Icon.setBackgroundResource(activeIcon);
                step2Label.setTextColor(activeTextColor);
                step1Line.setBackgroundColor(activeColor);
            case PACKAGED:
                step1Icon.setBackgroundResource(activeIcon);
                step1Label.setTextColor(activeTextColor);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Đặt cả hai binding về null
        deliveringBinding = null;
        notYetBinding = null;
        Log.d(TAG, "onDestroyView called");
    }

    // =================================================================
    // === ADAPTER MỚI ĐỂ HIỂN THỊ ShopOrderProduct ===
    // =================================================================
    private static class ShopOrderProductAdapter extends RecyclerView.Adapter<ShopOrderProductAdapter.ProductViewHolder> {

        private final List<ShopOrderProduct> productList;
        private final Context context;

        ShopOrderProductAdapter(Context context, List<ShopOrderProduct> productList) {
            this.context = context;
            this.productList = productList;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // **LƯU Ý:** Bạn cần tạo một file layout cho item này
            // Ví dụ: R.layout.item_shop_order_product_detail
            // Tôi sẽ tạm dùng layout cũ 'item_canceled_order' vì nó có các ID gần giống
            View view = LayoutInflater.from(context).inflate(R.layout.item_canceled_order, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            ShopOrderProduct product = productList.get(position);
            holder.bind(product);
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        static class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvSubtitle, tvPrice;

            ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitleDate); // Tạm dùng ID này
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            void bind(ShopOrderProduct product) {
                tvTitle.setText(product.getTitle());
                tvPrice.setText(product.getPrice());
                imgProduct.setImageResource(product.getImageRes());
                if (tvSubtitle != null) {
                    tvSubtitle.setText(product.getSubtitle());
                }
            }
        }
    }
}