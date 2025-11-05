package com.example.secondchance.ui.order;

import android.widget.ImageView;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
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
import com.example.secondchance.data.model.OrderProduct;
import com.example.secondchance.databinding.FragmentRefundOrderDetailBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.ui.order.adapter.RefundOrderItemAdapter;
import java.util.ArrayList;
import java.util.List;

public class RefundOrderDetailFragment extends Fragment {
    private static final String TAG = "RefundDetail";
    private FragmentRefundOrderDetailBinding binding;
    private SharedViewModel sharedViewModel;
    private String orderId;
    private Order.RefundStatus currentStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            currentStatus = (Order.RefundStatus) getArguments().getSerializable("refundStatus");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderDetailBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel.updateTitle("Chi tiết Hoàn trả");

        if (currentStatus == null || orderId == null) {
            Log.e(TAG, "Lỗi: Thiếu orderId hoặc refundStatus!");
            Toast.makeText(getContext(), "Lỗi tải dữ liệu chi tiết.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        Log.d(TAG, "Tải chi tiết cho Order ID: " + orderId + " - Trạng thái: " + currentStatus.name());

        // TODO: Tải dữ liệu chi tiết (tên SP, ảnh, lý do...) từ API/ViewModel dùng orderId

        List<OrderProduct> refundItems = getDummyProducts();

        if (getContext() != null) {

            RefundOrderItemAdapter adapter = new RefundOrderItemAdapter(getContext(), refundItems);


            binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));

            binding.rvOrderItems.setAdapter(adapter);
        }

        setupUIForStatus(currentStatus);
    }

    private List<OrderProduct> getDummyProducts() {
        List<OrderProduct> products = new ArrayList<>();

        products.add(new OrderProduct(
                "P001",
                "Giỏ gỗ cắm hoa",
                "Mẫu: Trắng, Số lượng: 1",
                "500.000",
                R.drawable.sample_flower,
                1));
        products.add(new OrderProduct(
                "P002",
                "Chậu cây mini",
                "Phân loại: Xanh lá, Số lượng: 2",
                "150.000",
                R.drawable.sample_flower,
                2));
        return products;
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
                binding.btnCancelRequest.setOnClickListener(v -> {
                    showCancelConfirmationDialog();
                });
                break;

            case CONFIRMED:
                binding.layoutStatusConfirmed.setVisibility(View.VISIBLE);
                binding.btnReturnToPostOffice.setOnClickListener(v -> {
                    showReturnConfirmationDialog();
                });
                break;

            case REJECTED:
                binding.layoutStatusRejected.setVisibility(View.VISIBLE);
                binding.btnAgree.setOnClickListener(v -> {
                    showAgreeNotRefundDialog();
                });
                binding.btnResendRequest.setOnClickListener(v -> {
                    navigateToEditRefundScreen();
                });

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

        btnKeepRequest.setOnClickListener(v -> {
            agreenotrefunddialog.dismiss();
        });

        btnConfirmCancel.setOnClickListener(v -> {
            // TODO: Thêm logic gọi ViewModel/API để hủy đơn hàng
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

                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
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

        btnKeepRequest.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnConfirmCancel.setOnClickListener(v -> {
            // TODO: Thêm logic gọi ViewModel/API để hủy đơn hàng

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

        btnKeepRequest.setOnClickListener(v -> {
            returnDialog.dismiss();
        });

        btnConfirmCancel.setOnClickListener(v -> {
            // TODO: Thêm logic gọi ViewModel/API để hủy đơn hàng

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

                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
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

                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
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
