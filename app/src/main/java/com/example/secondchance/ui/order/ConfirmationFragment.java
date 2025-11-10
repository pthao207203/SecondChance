package com.example.secondchance.ui.order;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.databinding.FragmentConfirmationBinding;
import com.example.secondchance.ui.order.dialog.ConfirmCancelDialog;
import com.example.secondchance.ui.order.dialog.CancelSuccessDialog;
import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

public class ConfirmationFragment extends Fragment implements ConfirmCancelDialog.OnCancelConfirmationListener,CancelSuccessDialog.OnDismissListener {

    private FragmentConfirmationBinding binding;
    private ConfirmationAdapter adapter;
    private final List<OrderWrapper> orderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;
    private OrderRepository orderRepository;

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

        orderRepository = new OrderRepository();

        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        //loadDummyData();
        loadData();

        adapter = new ConfirmationAdapter(
                orderList,
                (orderId, orderType) -> {
                    try {
                        NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                        Bundle args = new Bundle();
                        args.putString("orderId", orderId);
                        if (orderType != null) {
                            args.putSerializable("orderType", orderType);
                        }
                        nav.navigate(R.id.action_orderFragment_to_confirmOrderDetailFragment, args);
                    } catch (Exception e) {
                        Log.e("ConfirmationFragment", "Navigate to ConfirmDetail failed", e);
                        Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
                    }
                },
                orderId -> {
                    ConfirmCancelDialog dialog = new ConfirmCancelDialog(orderId, this);
                    dialog.show(getParentFragmentManager(), ConfirmCancelDialog.TAG);
                }
        );
        binding.rvOrders.setAdapter(adapter);
        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("ConfirmationFragment", "Got refresh signal! Reloading data...");

                loadData();
            }
        });
    }

    @Override
    public void onCancelConfirmed(String orderId) {
        Log.d("ConfirmationFragment", "Order " + orderId + " confirmed for cancellation.");

        orderRepository.cancelOrder(orderId, new OrderRepository.RepoCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded()) return;

                CancelSuccessDialog successDialog = new CancelSuccessDialog(ConfirmationFragment.this);
                successDialog.show(getParentFragmentManager(), CancelSuccessDialog.TAG);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                Toast.makeText(getContext(), "Hủy đơn thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onSuccessfulDismiss() {
        Log.d("ConfirmationFragment", "Success dialog dismissed. Refreshing lists.");

        sharedViewModel.refreshOrderLists();
        sharedViewModel.requestTabChange(3);
    }


    public void loadData() {

        orderRepository.fetchOrders("0", new OrderRepository.RepoCallback<List<OrderWrapper>>() {
            @SuppressLint("NotifyDataSetChanged")
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
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    public interface OnItemClickListener {
        void onItemClick(String orderId, Order.OrderType orderType);
    }
    public interface OnCancelClickListener {
        void onCancelClick(String orderId);
    }

    private static class ConfirmationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<OrderWrapper> items;
        private final OnItemClickListener itemClickListener;
        private final OnCancelClickListener cancelClickListener;

        private static final int VIEW_TYPE_UNCONFIRMED = 1;
        private static final int VIEW_TYPE_CONFIRMED_FIXED = 2;
        private static final int VIEW_TYPE_CONFIRMED_AUCTION = 3;

        ConfirmationAdapter(List<OrderWrapper> items, OnItemClickListener itemClickListener, OnCancelClickListener cancelClickListener) {
            this.items = items;
            this.itemClickListener = itemClickListener;
            this.cancelClickListener = cancelClickListener;
        }

        @Override
        public int getItemViewType(int position) {
            OrderWrapper order = items.get(position);
            Gson gson = new Gson();
            Log.d("ConfirmationFragment", gson.toJson(order.shipment));
            if (order != null && order.order.status == 3) return VIEW_TYPE_CONFIRMED_AUCTION;
            if (order != null && order.shipment != null) return VIEW_TYPE_CONFIRMED_FIXED;
            return VIEW_TYPE_UNCONFIRMED;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            switch (viewType) {
                case VIEW_TYPE_UNCONFIRMED:
                    view = inflater.inflate(R.layout.item_unconfirm_order, parent, false);
                    return new UnconfirmedViewHolder(view, itemClickListener, cancelClickListener);
                case VIEW_TYPE_CONFIRMED_FIXED:
                    view = inflater.inflate(R.layout.item_confirm_fixed_order, parent, false);
                    return new ConfirmedFixedViewHolder(view, itemClickListener);
                case VIEW_TYPE_CONFIRMED_AUCTION:
                    view = inflater.inflate(R.layout.item_confirm_auction_order, parent, false);
                    return new ConfirmedAuctionViewHolder(view, itemClickListener);
                default:
                    view = inflater.inflate(R.layout.item_unconfirm_order, parent, false);
                    return new UnconfirmedViewHolder(view, itemClickListener, cancelClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            OrderWrapper order = items.get(position);
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_UNCONFIRMED:
                    Log.d("ConfirmationFragment", "VIEW_TYPE_UNCONFIRMED");
                    ((UnconfirmedViewHolder) holder).bind(order.order);
                    break;
                case VIEW_TYPE_CONFIRMED_FIXED:
                    Log.d("ConfirmationFragment", "VIEW_TYPE_CONFIRMED_FIXED");
                    ((ConfirmedFixedViewHolder) holder).bind(order.order);
                    break;
                case VIEW_TYPE_CONFIRMED_AUCTION:
                    Log.d("ConfirmationFragment", "VIEW_TYPE_CONFIRMED_AUCTION");
                    ((ConfirmedAuctionViewHolder) holder).bind(order.order);
                    break;
            }
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        private static class UnconfirmedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgDot;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus;
            Button btnCancel;

            private final OnItemClickListener itemClickListener;
            private final OnCancelClickListener cancelClickListener;

            UnconfirmedViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener, OnCancelClickListener cancelClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;
                this.cancelClickListener = cancelClickListener;

                imgProduct     = itemView.findViewById(R.id.imgProduct);
                imgDot         = itemView.findViewById(R.id.imgDot);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                btnCancel      = itemView.findViewById(R.id.btnCancel);
            }

            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvSubtitleFixed.setText(order.getSubtitle());
                tvSubtitleDate.setText(order.getDate());

                OrderItem firstItem = order.getFirstItem();
                if (firstItem != null && firstItem.getImageUrl() != null) {
                    Glide.with(itemView.getContext())
                            .load(firstItem.getImageUrl())
                            .into(imgProduct);
                }

                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
                btnCancel.setOnClickListener(v -> {
                    if (cancelClickListener != null) cancelClickListener.onCancelClick(order.getId());
                });
            }
        }

        private static class ConfirmedFixedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus, tvAuctionInfo;
            private final OnItemClickListener itemClickListener;

            ConfirmedFixedViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;

                imgProduct     = itemView.findViewById(R.id.imgProduct);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvAuctionInfo  = itemView.findViewById(R.id.tvConfirmInfo);
            }

            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvSubtitleFixed.setText(order.getSubtitle());
                tvSubtitleDate.setText(order.getDate());
                
                OrderItem firstItem = order.getFirstItem();
                if (firstItem != null && firstItem.getImageUrl() != null) {
                    Glide.with(itemView.getContext())
                      .load(firstItem.getImageUrl())
                      .into(imgProduct);
                }
                
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
            }
        }

        private static class ConfirmedAuctionViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvAuctionInfo;
            private final OnItemClickListener itemClickListener;

            ConfirmedAuctionViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;

                imgProduct     = itemView.findViewById(R.id.imgProduct);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvAuctionInfo  = itemView.findViewById(R.id.tvAuctionInfo);
            }

            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvSubtitleFixed.setText(order.getSubtitle());
                tvSubtitleDate.setText(order.getDate());
                
                OrderItem firstItem = order.getFirstItem();
                if (firstItem != null && firstItem.getImageUrl() != null) {
                    Glide.with(itemView.getContext())
                      .load(firstItem.getImageUrl())
                      .into(imgProduct);
                }

                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
            }
        }
    }
}
