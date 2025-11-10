package com.example.secondchance.ui.order;

import android.widget.ImageView;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.databinding.FragmentRefundOrderDetailBinding;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RefundOrderDetailFragment extends Fragment {
    private static final String TAG = "RefundDetail";
    private FragmentRefundOrderDetailBinding binding;
    private SharedViewModel sharedViewModel;
    private String orderId;
    private Order.RefundStatus currentStatus;

    private OrderRepository orderRepository;
    private OrderItemAdapter productAdapter;
    private List<OrderItem> productList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            Serializable s = getArguments().getSerializable("refundStatus");
            if (s instanceof Order.RefundStatus) {
                currentStatus = (Order.RefundStatus) s;
            } else {
                currentStatus = null;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderDetailBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel.updateTitle("Chi tiết Hoàn trả");

        orderRepository = new OrderRepository();

        productAdapter = new OrderItemAdapter(requireContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "Thiếu orderId", Toast.LENGTH_LONG).show();
            return;
        }

        loadData(orderId);
    }

    private void loadData(String id) {
        // (hiển thị loading nếu cần)
        orderRepository.getOrderDetails(id, new OrderRepository.RepoCallback<OrderDetailResponse.Data>() {
            @Override
            public void onSuccess(OrderDetailResponse.Data data) {
                if (!isAdded()) return;
                bindOrder(data);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi tải chi tiết: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOrder(OrderDetailResponse.Data data) {
        productList.clear();
        if (data.order != null && data.order.orderItems != null) {
            for (OrderDetailResponse.OrderItem dtoItem : data.order.orderItems) {
                OrderItem modelItem = new OrderItem();
                modelItem.name = dtoItem.name;
                modelItem.imageUrl = dtoItem.imageUrl;
                modelItem.price = dtoItem.price;
                modelItem.quantity = dtoItem.qty;
                productList.add(modelItem);
            }
        }
        productAdapter.notifyDataSetChanged();

        if (data.order != null && data.order.returnRequest != null) {
            String reason = data.order.returnRequest.description;
            if (reason == null || reason.isEmpty()) reason = "Không có lý do.";

            binding.tvReason.setText(reason);
            binding.tvReason2.setText(reason);

            if (data.order.returnRequest.media != null && !data.order.returnRequest.media.isEmpty()) {
                String chosen = null;
                for (String u : data.order.returnRequest.media) {
                    if (u == null) continue;
                    String low = u.toLowerCase();
                    if (low.endsWith(".jpg") || low.endsWith(".jpeg") || low.endsWith(".png")) {
                        chosen = u;
                        break;
                    }
                }
                if (chosen == null) chosen = data.order.returnRequest.media.get(0);
                Glide.with(this).load(chosen).into(binding.imgEvidence1);
            }
        }

        if (currentStatus == null) {
            currentStatus = extractStatusFromData(data);
        }

        if (currentStatus == null) currentStatus = Order.RefundStatus.NOT_CONFIRMED;
        setupUIForStatus(currentStatus);
    }

    private Order.RefundStatus extractStatusFromData(OrderDetailResponse.Data data) {
        try {
            if (data.order == null || data.order.returnRequest == null) return null;

            Object raw = null;
            try {
                raw = data.order.returnRequest.status; // nếu có
            } catch (Throwable ignored) {}
            if (raw == null) return null;

            // Nếu là String
            if (raw instanceof String) {
                String s = ((String) raw).toUpperCase();
                switch (s) {
                    case "NOT_CONFIRMED": case "PENDING": case "0": return Order.RefundStatus.NOT_CONFIRMED;
                    case "CONFIRMED": case "1": return Order.RefundStatus.CONFIRMED;
                    case "REJECTED": case "2": return Order.RefundStatus.REJECTED;
                    case "SUCCESSFUL": case "COMPLETED": case "3": return Order.RefundStatus.SUCCESSFUL;
                    default: return null;
                }
            }

            // Nếu là Number
            if (raw instanceof Number) {
                int v = ((Number) raw).intValue();
                switch (v) {
                    case 0: return Order.RefundStatus.NOT_CONFIRMED;
                    case 1: return Order.RefundStatus.CONFIRMED;
                    case 2: return Order.RefundStatus.REJECTED;
                    case 3: return Order.RefundStatus.SUCCESSFUL;
                    default: return null;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Không map được refund status từ API: " + e.getMessage());
        }
        return null;
    }

    private void setupUIForStatus(Order.RefundStatus status) {
        binding.layoutStatusNotConfirmed.setVisibility(View.GONE);
        binding.layoutStatusConfirmed.setVisibility(View.GONE);
        binding.layoutStatusRejected.setVisibility(View.GONE);
        binding.layoutStatusSuccessful.setVisibility(View.GONE);

        switch (status) {
            case NOT_CONFIRMED:
                binding.layoutStatusNotConfirmed.setVisibility(View.VISIBLE);
                binding.layoutCommonReason.setVisibility(View.GONE);
                binding.btnCancelRequest.setOnClickListener(v -> showCancelConfirmationDialog());
                break;

            case CONFIRMED:
                binding.layoutStatusConfirmed.setVisibility(View.VISIBLE);
                binding.btnReturnToPostOffice.setOnClickListener(v -> showReturnConfirmationDialog());
                break;

            case REJECTED:
                binding.layoutStatusRejected.setVisibility(View.VISIBLE);
                binding.btnAgree.setOnClickListener(v -> showAgreeNotRefundDialog());
                binding.btnResendRequest.setOnClickListener(v -> navigateToEditRefundScreen());
                break;

            case SUCCESSFUL:
                binding.layoutStatusSuccessful.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void navigateToEditRefundScreen() {
        try {
            int navigationActionId = R.id.action_refundOrderDetail_to_createRefundFragment;
            Bundle args = new Bundle();
            args.putString("orderId", orderId);
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(navigationActionId, args);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi điều hướng đến màn hình Gửi lại", e);
            Toast.makeText(getContext(), "Không thể mở màn hình chỉnh sửa.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAgreeNotRefundDialog() {
        if (getContext() == null) return;
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_agree_not_refund, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        final AlertDialog agreenotrefunddialog = builder.create();

        MaterialButton btnKeepRequest = dialogView.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnConfirmCancel = dialogView.findViewById(R.id.btnKeepOrder);

        btnKeepRequest.setOnClickListener(v -> agreenotrefunddialog.dismiss());
        btnConfirmCancel.setOnClickListener(v -> {
            agreenotrefunddialog.dismiss();
            showAgreeNotRefundSuccessfulDialog();
        });
        if (agreenotrefunddialog.getWindow() != null) {
            agreenotrefunddialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        agreenotrefunddialog.show();
    }

    private void showAgreeNotRefundSuccessfulDialog() {
        if (getContext() == null) return;
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_agree_not_refund_successful, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        final AlertDialog successDialog = builder.create();

        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                successDialog.dismiss();
                if (sharedViewModel != null) {
                    sharedViewModel.requestTabChange(2);
                    sharedViewModel.refreshOrderLists();
                }
                if (getView() != null) Navigation.findNavController(getView()).popBackStack();
            });
        }
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        successDialog.show();
    }

    private void showCancelConfirmationDialog() {
        if (getContext() == null) return;
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_refund_request, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        MaterialButton btnKeepRequest = dialogView.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnConfirmCancel = dialogView.findViewById(R.id.btnKeepOrder);

        btnKeepRequest.setOnClickListener(v -> dialog.dismiss());
        btnConfirmCancel.setOnClickListener(v -> {
            dialog.dismiss();
            showCancelSuccessfulDialog();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();
    }

    private void showReturnConfirmationDialog() {
        if (getContext() == null) return;
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_return_order, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        final AlertDialog returnDialog = builder.create();

        MaterialButton btnKeepRequest = dialogView.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnConfirmCancel = dialogView.findViewById(R.id.btnKeepOrder);

        btnKeepRequest.setOnClickListener(v -> returnDialog.dismiss());
        btnConfirmCancel.setOnClickListener(v -> {
            returnDialog.dismiss();
            showReturnSuccessfulDialog();
        });

        if (returnDialog.getWindow() != null) {
            returnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        returnDialog.show();
    }

    private void showCancelSuccessfulDialog() {
        if (getContext() == null) return;
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_refund_request_successful, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        final AlertDialog successDialog = builder.create();

        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                successDialog.dismiss();
                if (sharedViewModel != null) {
                    sharedViewModel.requestTabChange(2);
                    sharedViewModel.refreshOrderLists();
                }
                if (getView() != null) Navigation.findNavController(getView()).popBackStack();
            });
        }
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        successDialog.show();
    }

    private void showReturnSuccessfulDialog(){
        if (getContext() == null) return;
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_return_order_successful, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        final AlertDialog successDialog = builder.create();

        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                successDialog.dismiss();
                if (sharedViewModel != null) {
                    sharedViewModel.requestTabChange(4);
                    sharedViewModel.refreshOrderLists();
                }
                if (getView() != null) Navigation.findNavController(getView()).popBackStack();
            });
        }
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        successDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
