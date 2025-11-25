package com.example.secondchance.ui.shoporder;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;

import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.data.model.ShopTrackingStatus;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.databinding.FragmentShopRefundConfirmedDetailBinding;
import com.example.secondchance.databinding.FragmentShopRefundDeliveringDetailBinding;
import com.example.secondchance.databinding.FragmentShopRefundNotConfirmedDetailBinding;
import com.example.secondchance.databinding.FragmentShopRefundSuccessfulDetailBinding;
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

public class RefundShopOrderDetailFragment extends Fragment {

    private ViewBinding binding;
    private SharedViewModel sharedViewModel;
    private OrderApi orderApi;

    private String receivedOrderId;
    private ShopOrder.RefundStatus receivedRefundStatus;

    private ShopOrderProductDetailAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();

    private final List<ShopTrackingStatus> trackingList = new ArrayList<>();
    private ShopTrackingStatusAdapter trackingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            try {
                receivedRefundStatus = (ShopOrder.RefundStatus) getArguments().getSerializable("refundStatus");
            } catch (Exception e) {
                receivedRefundStatus = ShopOrder.RefundStatus.NOT_CONFIRMED;
            }
        } else {
            NavHostFragment.findNavController(this).popBackStack();
            return null;
        }

        switch (receivedRefundStatus) {
            case CONFIRMED:
                binding = FragmentShopRefundConfirmedDetailBinding.inflate(inflater, container, false);
                break;
            case DELIVERING:
                binding = FragmentShopRefundDeliveringDetailBinding.inflate(inflater, container, false);
                break;
            case SUCCESSFUL:
                binding = FragmentShopRefundSuccessfulDetailBinding.inflate(inflater, container, false);
                break;
            case NOT_CONFIRMED:
            default:
                binding = FragmentShopRefundNotConfirmedDetailBinding.inflate(inflater, container, false);
                break;
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        orderApi = RetrofitProvider.order();

        setupButtonListeners();
        setupRecyclerViews(view);

        if (receivedOrderId != null) {
            loadOrderDetail(receivedOrderId);
        }
    }

    private void setupRecyclerViews(View view) {
        RecyclerView rvItems = view.findViewById(R.id.rvOrderItems);
        if (rvItems != null) {
            productAdapter = new ShopOrderProductDetailAdapter(getContext(), productList);
            rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
            rvItems.setAdapter(productAdapter);
            rvItems.setNestedScrollingEnabled(false);
        }

        RecyclerView rvTracking = view.findViewById(R.id.rvTrackingStatus);
        if (rvTracking != null) {
            trackingAdapter = new ShopTrackingStatusAdapter(getContext(), trackingList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setReverseLayout(true);
            layoutManager.setStackFromEnd(true);
            rvTracking.setLayoutManager(layoutManager);
            rvTracking.setAdapter(trackingAdapter);
            rvTracking.setNestedScrollingEnabled(false);
        }
    }

    private void loadOrderDetail(String id) {
        orderApi.getOrderDetail(id).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                if (!isAdded()) return;
                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindDataToUI(res.body().data);
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindDataToUI(OrderDetailResponse.Data data) {
        if (data.order != null && data.order.orderItems != null) {
            productList.clear();
            for (OrderDetailResponse.OrderItem dto : data.order.orderItems) {
                productList.add(new ShopOrderProduct(
                        "ID",
                        dto.name,
                        "Mã đơn: " + (data.order.id != null ? data.order.id.toUpperCase() : ""),
                        formatVnd(dto.price),
                        0,
                        dto.qty,
                        dto.imageUrl
                ));
            }

            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            } else if (!productList.isEmpty()) {
                bindStaticProductData(binding.getRoot(), productList.get(0));
            }
        }

        bindReturnReason(binding.getRoot(), data);
        bindEvidenceImages(binding.getRoot(), data);

        if (receivedRefundStatus == ShopOrder.RefundStatus.DELIVERING && data.shipment != null) {
            trackingList.clear();
            if (data.shipment.events != null) {
                for (OrderDetailResponse.Event e : data.shipment.events) {
                    trackingList.add(new ShopTrackingStatus(
                            formatVnTime(e.eventTime),
                            mapStatusTitle(e),
                            false
                    ));
                }
                if (!trackingList.isEmpty()) {
                    trackingList.get(trackingList.size() - 1).setActive(true);
                }
                if (trackingAdapter != null) trackingAdapter.notifyDataSetChanged();
            }
            updateStepperUI(binding.getRoot(), data.shipment.currentStatus);
        }
    }

    private void bindEvidenceImages(View view, OrderDetailResponse.Data data) {
        ImageView img1 = view.findViewById(R.id.imgEvidence1);
        ImageView img2 = view.findViewById(R.id.imgEvidence2);
        ImageView img3 = view.findViewById(R.id.imgEvidence3);

        if (img1 == null) return;

        img1.setVisibility(View.GONE);
        img2.setVisibility(View.GONE);
        img3.setVisibility(View.GONE);

        if (data.order != null && data.order.returnRequest != null && data.order.returnRequest.media != null) {
            List<String> mediaList = data.order.returnRequest.media;

            if (mediaList.size() > 0) {
                img1.setVisibility(View.VISIBLE);
                Glide.with(this).load(mediaList.get(0)).into(img1);
            }

            if (mediaList.size() > 1) {
                img2.setVisibility(View.VISIBLE);
                Glide.with(this).load(mediaList.get(1)).into(img2);
            }

            if (mediaList.size() > 2) {
                img3.setVisibility(View.VISIBLE);
                Glide.with(this).load(mediaList.get(2)).into(img3);
            }
        }
    }

    private void bindStaticProductData(View view, ShopOrderProduct product) {
        try {
            ImageView img = view.findViewById(R.id.imgProduct);
            TextView title = view.findViewById(R.id.tvTitle);
            TextView price = view.findViewById(R.id.tvPrice);
            TextView qty = view.findViewById(R.id.tvQuantity);
            TextView desc = view.findViewById(R.id.tvDescription);

            if (title != null) title.setText(product.getTitle());
            if (price != null) price.setText(product.getPrice());
            if (qty != null) qty.setText("Số lượng: " + product.getQuantity());
            if (desc != null) desc.setText(product.getSubtitle());

            if (img != null) {
                if (product.getImageUrl() != null) {
                    Glide.with(this).load(product.getImageUrl()).into(img);
                } else {
                    img.setImageResource(R.drawable.sample_flower);
                }
            }
        } catch (Exception e) {
        }
    }

    private void bindReturnReason(View view, OrderDetailResponse.Data data) {
        if (data.order == null || data.order.returnRequest == null) return;
        String reason = data.order.returnRequest.description;

        TextView tvReason = view.findViewById(R.id.tvReason);
        if (tvReason != null) {
            tvReason.setText(reason);
        } else {
            EditText etReason = view.findViewById(R.id.etReason);
            if (etReason != null) {
                etReason.setText(reason);
                etReason.setFocusable(false);
                etReason.setClickable(false);
            }
        }
    }

    private void updateStepperUI(View view, int shipmentStatus) {
        if (view == null || getContext() == null) return;

        ImageView step1 = view.findViewById(R.id.step1_icon);
        ImageView step2 = view.findViewById(R.id.step2_icon);
        ImageView step3 = view.findViewById(R.id.step3_icon);
        ImageView step4 = view.findViewById(R.id.step4_icon);

        View line1 = view.findViewById(R.id.step1_line);
        View line2 = view.findViewById(R.id.step2_line);
        View line3 = view.findViewById(R.id.step3_line);

        TextView label1 = view.findViewById(R.id.step1_label);
        TextView label2 = view.findViewById(R.id.step2_label);
        TextView label3 = view.findViewById(R.id.step3_label);
        TextView label4 = view.findViewById(R.id.step4_label);

        if (step1 == null) return;

        int activeColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.highLight4);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int activeIcon = R.drawable.ic_active;
        int inactiveIcon = R.drawable.ic_inactive;

        step1.setBackgroundResource(inactiveIcon);
        step2.setBackgroundResource(inactiveIcon);
        step3.setBackgroundResource(inactiveIcon);
        step4.setBackgroundResource(inactiveIcon);
        line1.setBackgroundColor(inactiveColor);
        line2.setBackgroundColor(inactiveColor);
        line3.setBackgroundColor(inactiveColor);
        label1.setTextColor(inactiveTextColor);
        label2.setTextColor(inactiveTextColor);
        label3.setTextColor(inactiveTextColor);
        label4.setTextColor(inactiveTextColor);

        if (shipmentStatus >= 5) {
            step4.setBackgroundResource(activeIcon);
            label4.setTextColor(activeTextColor);
            line3.setBackgroundColor(activeColor);
        }
        if (shipmentStatus >= 4) {
            step3.setBackgroundResource(activeIcon);
            label3.setTextColor(activeTextColor);
            line2.setBackgroundColor(activeColor);
        }
        if (shipmentStatus >= 2) {
            step2.setBackgroundResource(activeIcon);
            label2.setTextColor(activeTextColor);
            line1.setBackgroundColor(activeColor);
        }
        if (shipmentStatus >= 1) {
            step1.setBackgroundResource(activeIcon);
            label1.setTextColor(activeTextColor);
        }
    }

    private void setupButtonListeners() {
        if (binding instanceof FragmentShopRefundConfirmedDetailBinding) {
            FragmentShopRefundConfirmedDetailBinding b = (FragmentShopRefundConfirmedDetailBinding) binding;
            b.btnAccept.setOnClickListener(v -> handleShopAction("ACCEPT_CONFIRM"));
//            b.btnResendRequest.setOnClickListener(v -> handleShopAction("COMPLAIN"));
        } else if (binding instanceof FragmentShopRefundDeliveringDetailBinding) {
            FragmentShopRefundDeliveringDetailBinding b = (FragmentShopRefundDeliveringDetailBinding) binding;
            b.btnReceiveOrder.setOnClickListener(v -> handleShopAction("CONFIRM_RECEIVED"));
        } else if (binding instanceof FragmentShopRefundNotConfirmedDetailBinding) {
            FragmentShopRefundNotConfirmedDetailBinding b = (FragmentShopRefundNotConfirmedDetailBinding) binding;
            b.btnRejectRequest.setOnClickListener(v -> handleShopAction("REJECT_REQUEST"));
            b.btnAcceptRequest.setOnClickListener(v -> handleShopAction("ACCEPT_REQUEST"));
        }
    }

    private void handleShopAction(String action) {
        String message = "";
        switch (action) {
            case "ACCEPT_REQUEST":
                message = "Đã chấp nhận yêu cầu hoàn trả";
                break;
            case "REJECT_REQUEST":
                message = "Đã từ chối yêu cầu";
                break;
            case "ACCEPT_CONFIRM":
                message = "Đã đồng ý hoàn tiền";
                break;
            case "COMPLAIN":
                message = "Đã gửi khiếu nại";
                break;
            case "CONFIRM_RECEIVED":
                message = "Đã nhận được hàng hoàn";
                break;
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        sharedViewModel.refreshOrderLists();
        sharedViewModel.requestTabChange(4);
        NavHostFragment.findNavController(this).popBackStack();
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }

    private String formatVnTime(String iso) {
        try {
            ZonedDateTime z = ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
            return z.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return "";
        }
    }

    private String mapStatusTitle(OrderDetailResponse.Event e) {
        return (e != null && e.description != null) ? e.description : "Cập nhật";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class ShopOrderProductDetailAdapter extends RecyclerView.Adapter<ShopOrderProductDetailAdapter.ProductViewHolder> {
        private final List<ShopOrderProduct> productList;
        private final Context context;

        ShopOrderProductDetailAdapter(Context context, List<ShopOrderProduct> productList) {
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
                tvSubtitle = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            void bind(ShopOrderProduct product) {
                tvTitle.setText(product.getTitle());
                tvPrice.setText(product.getPrice());
                if (product.getImageUrl() != null) {
                    Glide.with(itemView.getContext()).load(product.getImageUrl()).into(imgProduct);
                } else {
                    imgProduct.setImageResource(product.getImageRes());
                }
                if (tvSubtitle != null) tvSubtitle.setText(product.getSubtitle());
            }
        }
    }
}
