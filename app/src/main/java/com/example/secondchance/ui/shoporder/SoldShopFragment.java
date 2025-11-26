package com.example.secondchance.ui.shoporder;

import android.annotation.SuppressLint;
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

import com.bumptech.glide.Glide;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;
// Model API
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.repo.OrderRepository;
// Model UI
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.databinding.FragmentShopSoldOrderBinding; // Đổi tên binding
import com.example.secondchance.viewmodel.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

public class SoldShopFragment extends Fragment {

    private static final String TAG = "SoldShopFrag";
    private FragmentShopSoldOrderBinding binding;
    private SoldShopAdapter adapter;
    private final List<ShopOrder> shopOrderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;
    private String currentShopId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopSoldOrderBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderRepository = new OrderRepository();

        currentShopId = sharedViewModel.getCurrentShopId();
        if (currentShopId == null || currentShopId.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy ID Shop. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        binding.rvSoldOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SoldShopAdapter(shopOrderList, (orderId, isEvaluated) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("shopOrderId", orderId);
                args.putBoolean("isEvaluated", isEvaluated);

                nav.navigate(R.id.action_shopOrderFragment_to_soldShopOrderDetailFragment, args);
            } catch (Exception e) {
                Log.e(TAG, "Navigation error", e);
                Toast.makeText(requireContext(), "Lỗi mở chi tiết đơn đã bán.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvSoldOrders.setAdapter(adapter);

        loadData();

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                loadData();
            }
        });
    }

    private void loadData() {
        if (currentShopId == null || currentShopId.isEmpty()) return;

        orderRepository.fetchOrdersForShop(currentShopId, "2", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<OrderWrapper> data) {
                if (!isAdded()) return;

                shopOrderList.clear();
                if (data != null) {
                    for (OrderWrapper wrapper : data) {
                        if (wrapper.order != null) {
                            shopOrderList.add(convertToShopOrder(wrapper));
                        }
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (shopOrderList.isEmpty()) {
                    Log.d(TAG, "Danh sách đơn đã bán trống");
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ShopOrder convertToShopOrder(OrderWrapper wrapper) {
        List<ShopOrderProduct> products = new ArrayList<>();
        OrderItem firstItem = wrapper.order.getFirstItem();

        if (firstItem != null) {
            products.add(new ShopOrderProduct(
                    "ID",
                    firstItem.getName(),
                    "Mã đơn: " + wrapper.order.getId().substring(0, Math.min(8, wrapper.order.getId().length())),
                    wrapper.order.getPrice(),
                    0,
                    firstItem.getQuantity(),
                    firstItem.getImageUrl()
            ));
        }

        boolean evaluated = wrapper.order.isEvaluated();
        String statusText = evaluated ? "Khách đã đánh giá" : "Giao thành công";

        return new ShopOrder(
                wrapper.order.getId(),
                products,
                wrapper.order.getPrice(),
                wrapper.order.getDate(),
                statusText,
                "Khách hàng đã nhận được hàng",
                ShopOrder.ShopOrderType.SOLD,
                evaluated,
                null,
                ShopOrder.DeliveryOverallStatus.DELIVERED
        );
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvSoldOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnItemClickListener {
        void onClick(String orderId, boolean isEvaluated);
    }

    private static class SoldShopAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<ShopOrder> items;
        private final OnItemClickListener listener;

        private static final int VIEW_TYPE_NOT_EVALUATED = 1;
        private static final int VIEW_TYPE_EVALUATED = 2;

        SoldShopAdapter(List<ShopOrder> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            ShopOrder shopOrder = items.get(position);
            return shopOrder.isEvaluated() ? VIEW_TYPE_EVALUATED : VIEW_TYPE_NOT_EVALUATED;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_EVALUATED) {
                View v = inflater.inflate(R.layout.item_shop_sold_evaluated, parent, false);
                return new EvaluatedViewHolder(v, listener);
            } else {
                View v = inflater.inflate(R.layout.item_shop_sold_not_evaluated, parent, false);
                return new NotEvaluatedViewHolder(v, listener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ShopOrder order = items.get(position);
            if (holder instanceof EvaluatedViewHolder) {
                ((EvaluatedViewHolder) holder).bind(order);
            } else if (holder instanceof NotEvaluatedViewHolder) {
                ((NotEvaluatedViewHolder) holder).bind(order);
            }
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        static class NotEvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct; TextView tvTitle, tvPrice, tvSubtitleDate; View invoiceLayout;
            final OnItemClickListener listener;

            NotEvaluatedViewHolder(View v, OnItemClickListener l) {
                super(v); listener = l;
                imgProduct=v.findViewById(R.id.imgProduct); tvTitle=v.findViewById(R.id.tvTitle);
                tvPrice=v.findViewById(R.id.tvPrice); tvSubtitleDate=v.findViewById(R.id.tvSubtitleDate);
                invoiceLayout=v.findViewById(R.id.invoice_layout);
            }
            void bind(ShopOrder o) {
                tvPrice.setText(o.getTotalPrice()); tvSubtitleDate.setText(o.getDate());
                bindCommon(o, imgProduct, tvTitle, itemView, invoiceLayout, listener);
            }
        }

        static class EvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct; TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview; View invoiceLayout;
            final OnItemClickListener listener;

            EvaluatedViewHolder(View v, OnItemClickListener l) {
                super(v); listener = l;
                imgProduct=v.findViewById(R.id.imgProduct); tvTitle=v.findViewById(R.id.tvTitle);
                tvPrice=v.findViewById(R.id.tvPrice); tvSubtitleDate=v.findViewById(R.id.tvSubtitleDate);
                tvStatusReview=v.findViewById(R.id.tvStatusReview); invoiceLayout=v.findViewById(R.id.invoice_layout);
            }
            void bind(ShopOrder o) {
                tvPrice.setText(o.getTotalPrice()); tvSubtitleDate.setText(o.getDate());
                tvStatusReview.setText(o.getStatusText());
                bindCommon(o, imgProduct, tvTitle, itemView, invoiceLayout, listener);
            }
        }

        private static void bindCommon(ShopOrder o, ImageView img, TextView title, View root, View invoice, OnItemClickListener l) {
            if (o.getItems() != null && !o.getItems().isEmpty()) {
                ShopOrderProduct p = o.getItems().get(0);
                title.setText(p.getTitle());
                if (p.getImageUrl() != null) Glide.with(root.getContext()).load(p.getImageUrl()).into(img);
                else img.setImageResource(R.drawable.sample_flower);
            }
            View.OnClickListener cl = v -> { if(l!=null) l.onClick(o.getId(), o.isEvaluated()); };
            root.setOnClickListener(cl);
            if(invoice!=null) invoice.setOnClickListener(cl);
        }
    }
}
