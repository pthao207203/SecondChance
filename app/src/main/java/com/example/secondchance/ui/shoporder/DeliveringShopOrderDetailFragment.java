package com.example.secondchance.ui.shoporder;

import android.content.Context;
import android.os.Bundle;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;

import com.example.secondchance.databinding.FragmentShopDeliveringOrderDetailBinding;

import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.data.model.ShopTrackingStatus;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.OrderDetailResponse;

import com.example.secondchance.ui.shoporder.adapter.ShopTrackingStatusAdapter;
import com.example.secondchance.viewmodel.SharedViewModel;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveringShopOrderDetailFragment extends Fragment {
    private static final String TAG = "DelivShopDetail";

    private FragmentShopDeliveringOrderDetailBinding binding;

    private String receivedShopOrderId;
    private ShopOrder.DeliveryOverallStatus receivedDeliveryStatus;
    private SharedViewModel sharedViewModel;
    private OrderApi orderApi;

    private ShopOrderProductAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();

    private List<ShopTrackingStatus> trackingList = new ArrayList<>();
    private ShopTrackingStatusAdapter trackingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            receivedShopOrderId = getArguments().getString("shopOrderId");
            try {
                receivedDeliveryStatus = (ShopOrder.DeliveryOverallStatus) getArguments().getSerializable("deliveryStatus");
            } catch (Exception e) {

                receivedDeliveryStatus = ShopOrder.DeliveryOverallStatus.PACKAGED;
            }
        } else {
            Toast.makeText(getContext(), "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return null;
        }

        binding = FragmentShopDeliveringOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        orderApi = RetrofitProvider.order();

        setupProductRecyclerView(binding.rvOrderItems);
        setupTrackingRecyclerView(binding.rvTrackingStatus);
        updateStepper(binding.getRoot(), receivedDeliveryStatus);

        if (receivedShopOrderId != null) {
            loadOrderDetail(receivedShopOrderId);
        }

    }

    private void loadOrderDetail(String id) {
        orderApi.getOrderDetail(id).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                if (!isAdded()) return;

                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được chi tiết đơn", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindDataToUI(res.body().data);
            }
            @Override public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDataToUI(OrderDetailResponse.Data data) {

        productList.clear();
        if (data.order != null && data.order.orderItems != null) {
            for (OrderDetailResponse.OrderItem dtoItem : data.order.orderItems) {
                productList.add(new ShopOrderProduct(
                        "ID", dtoItem.name,
                        "Mã đơn: " + (data.order.id != null ? data.order.id.toUpperCase() : ""),
                        formatVnd(dtoItem.price), 0, dtoItem.qty, dtoItem.imageUrl
                ));
            }
        }
        if (productAdapter != null) productAdapter.notifyDataSetChanged();

        View rootView = binding.getRoot();
        bindReceiverInfo(rootView, data);

        if (data.shipment != null) {
            trackingList.clear();
            if (data.shipment.events != null) {
                for (OrderDetailResponse.Event e : data.shipment.events) {
                    trackingList.add(new ShopTrackingStatus(formatVnTime(e.eventTime), mapStatusTitle(e), false));
                }
                if (!trackingList.isEmpty()) trackingList.get(trackingList.size() - 1).setActive(true);
            }
            if (trackingAdapter != null) trackingAdapter.notifyDataSetChanged();

            updateStepperFromApi(binding.getRoot(), data.shipment.currentStatus, data.order.orderStatus);
        }
    }

    private void bindReceiverInfo(View rootView, OrderDetailResponse.Data data) {
        TextView tvName = rootView.findViewById(R.id.tvReceiverName);
        TextView tvPhone = rootView.findViewById(R.id.tvReceiverPhone);
        TextView tvAddress = rootView.findViewById(R.id.tvReceiverAddress);
        TextView tvOrderId = rootView.findViewById(R.id.tvOrderId);

        if (data.order != null) {
            if (tvOrderId != null) tvOrderId.setText(data.order.id != null ? data.order.id.toUpperCase() : "");

            if (data.order.orderShippingAddress != null) {
                var a = data.order.orderShippingAddress;
                if (tvName != null) tvName.setText(safe(a.name));
                if (tvPhone != null) tvPhone.setText(safe(a.phone));

                if (tvAddress != null) {
                    String fullAddr = safe(a.street) + ", " + safe(a.ward) + ", " + safe(a.province);
                    tvAddress.setText(fullAddr);
                }
            }
        }
    }

    private void setupProductRecyclerView(RecyclerView rv) {
        productAdapter = new ShopOrderProductAdapter(getContext(), productList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(productAdapter);
        rv.setNestedScrollingEnabled(false);
    }

    private void setupTrackingRecyclerView(RecyclerView rv) {
        trackingAdapter = new ShopTrackingStatusAdapter(getContext(), trackingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(trackingAdapter);
        rv.setNestedScrollingEnabled(false);
    }

    private void updateStepperFromApi(View view, int shipmentStatus, int orderStatus) {
        ShopOrder.DeliveryOverallStatus currentStatus = ShopOrder.DeliveryOverallStatus.PACKAGED;
        if (orderStatus >= 3) {
            currentStatus = ShopOrder.DeliveryOverallStatus.DELIVERED;
        } else if (shipmentStatus >= 5) {
            currentStatus = ShopOrder.DeliveryOverallStatus.DELIVERING;
        } else if (shipmentStatus >= 2) {
            currentStatus = ShopOrder.DeliveryOverallStatus.AT_POST_OFFICE;
        }

        updateStepper(view, currentStatus);
    }

    private void updateStepper(View stepperLayout, ShopOrder.DeliveryOverallStatus status) {
        if (stepperLayout == null || getContext() == null) return;

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

        if (step1Icon == null) return;

        int activeColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.highLight4);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int activeIconRes = R.drawable.ic_active;
        int inactiveIcon = R.drawable.ic_inactive;

        step1Icon.setBackgroundResource(inactiveIcon); step2Icon.setBackgroundResource(inactiveIcon);
        step3Icon.setBackgroundResource(inactiveIcon); step4Icon.setBackgroundResource(inactiveIcon);
        step1Line.setBackgroundColor(inactiveColor); step2Line.setBackgroundColor(inactiveColor); step3Line.setBackgroundColor(inactiveColor);
        step1Label.setTextColor(inactiveTextColor); step2Label.setTextColor(inactiveTextColor);
        step3Label.setTextColor(inactiveTextColor); step4Label.setTextColor(inactiveTextColor);

        switch (status) {
            case DELIVERED:
                step4Icon.setBackgroundResource(activeIconRes);
                step4Label.setTextColor(activeTextColor);
                step3Line.setBackgroundColor(activeColor);

            case DELIVERING:
                step3Icon.setBackgroundResource(activeIconRes);
                step3Label.setTextColor(activeTextColor);
                step2Line.setBackgroundColor(activeColor);

            case AT_POST_OFFICE:
                step2Icon.setBackgroundResource(activeIconRes);
                step2Label.setTextColor(activeTextColor);
                step1Line.setBackgroundColor(activeColor);

            case PACKAGED:
                step1Icon.setBackgroundResource(activeIconRes);
                step1Label.setTextColor(activeTextColor);
                break;
        }
    }

    private String formatVnd(long amount) {
        return java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN")).format(amount);
    }

    private String formatVnTime(String iso) {
        try {
            ZonedDateTime z = ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
            return z.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) { return iso != null ? iso : ""; }
    }

    private String mapStatusTitle(OrderDetailResponse.Event e) {
        if (e == null || e.eventCode == null) return safe(e != null ? e.description : "");
        switch (e.eventCode) {
            case 1: return "Đã lấy hàng";
            case 2: return "Đến bưu cục";
            case 3: return "Rời bưu cục";
            case 4: return "Đang giao";
            case 5: return "Giao thành công";
            default: return "Cập nhật";
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

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
            View view = LayoutInflater.from(context).inflate(R.layout.item_canceled_order, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            holder.bind(productList.get(position));
        }

        @Override
        public int getItemCount() { return productList.size(); }

        static class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvSubtitle, tvPrice;

            ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            void bind(ShopOrderProduct product) {
                tvTitle.setText(product.getTitle());
                tvPrice.setText(product.getPrice());

                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(product.getImageUrl())
                            .into(imgProduct);
                } else {
                    imgProduct.setImageResource(product.getImageRes());
                }

                if (tvSubtitle != null) tvSubtitle.setText(product.getSubtitle());
            }
        }
    }
}
