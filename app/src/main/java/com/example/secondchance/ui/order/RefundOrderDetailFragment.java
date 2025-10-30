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
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.databinding.FragmentRefundOrderDetailBinding;
import com.example.secondchance.viewmodel.SharedViewModel;

public class RefundOrderDetailFragment extends Fragment {
    private static final String TAG = "RefundDetail";
    private FragmentRefundOrderDetailBinding binding;
    private SharedViewModel sharedViewModel;
    private String orderId;
    private Order.RefundStatus currentStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nhận arguments
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

        // Kiểm tra dữ liệu
        if (currentStatus == null || orderId == null) {
            Log.e(TAG, "Lỗi: Thiếu orderId hoặc refundStatus!");
            Toast.makeText(getContext(), "Lỗi tải dữ liệu chi tiết.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        Log.d(TAG, "Tải chi tiết cho Order ID: " + orderId + " - Trạng thái: " + currentStatus.name());

        // TODO: Tải dữ liệu chi tiết (tên SP, ảnh, lý do...) từ API/ViewModel dùng orderId

        // Quyết định layout nào sẽ được hiển thị (ẩn/hiện các nhóm layout (LinearLayout) dựa trên trạng thái)
        setupUIForStatus(currentStatus);
    }

    private void setupUIForStatus(Order.RefundStatus status) {

        binding.layoutStatusNotConfirmed.setVisibility(View.GONE);
        binding.layoutStatusConfirmed.setVisibility(View.GONE);
        binding.layoutStatusRejected.setVisibility(View.GONE);
        binding.layoutStatusSuccessful.setVisibility(View.GONE);

        // layout tương ứng
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
            args.putString("orderId", orderId); // Gửi orderId

            // NavController và điều hướng
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

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        final AlertDialog agreenotrefunddialog = builder.create();

        // Tìm các nút bên trong layout dialog
        MaterialButton btnKeepRequest = dialogView.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnConfirmCancel = dialogView.findViewById(R.id.btnKeepOrder);

        // click cho các nút trong dialog
        btnKeepRequest.setOnClickListener(v -> {
            agreenotrefunddialog.dismiss();
        });

        //"Xác nhận hủy" -> logic hủy và đóng dialog
        btnConfirmCancel.setOnClickListener(v -> {
            // TODO: Thêm logic gọi ViewModel/API để hủy đơn hàng
            // Đóng dialog xác nhận
            agreenotrefunddialog.dismiss();
            // Gọi hàm hiển thị dialog thành công
            showAgreeNotRefundSuccessfulDialog();
        });
        // Làm trong suốt nền của Dialog
        if (agreenotrefunddialog.getWindow() != null) {
            agreenotrefunddialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // Hiển thị dialog
        agreenotrefunddialog.show();
    }

    private void showAgreeNotRefundSuccessfulDialog() {
        if (getContext() == null) return;

        // Inflate layout dialog THÀNH CÔNG
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_agree_not_refund_successful, null);

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        final AlertDialog successDialog = builder.create();

        // "X" (ImageView)
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        // sự kiện click cho nút "X"
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                successDialog.dismiss();

                if (sharedViewModel != null) {
                    sharedViewModel.requestTabChange(2);
                    sharedViewModel.refreshOrderLists();
                }

                // Quay lại màn hình StatusOrderFragment
                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
            });
        }

        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // Hiển thị dialog
        successDialog.show();
    }

    private void showCancelConfirmationDialog() {
        if (getContext() == null) return;

        // Inflate (biến XML thành View) layout dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_refund_request, null);

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        MaterialButton btnKeepRequest = dialogView.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnConfirmCancel = dialogView.findViewById(R.id.btnKeepOrder);
        // "Giữ yêu cầu" -> đóng dialog
        btnKeepRequest.setOnClickListener(v -> {
            dialog.dismiss();
        });
        //"Xác nhận hủy" -> thực hiện logic hủy và đóng dialog
        btnConfirmCancel.setOnClickListener(v -> {
            // TODO: Thêm logic gọi ViewModel/API để hủy đơn hàng

            dialog.dismiss();

            showCancelSuccessfulDialog();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // Hiển thị dialog
        dialog.show();
    }

    private void showReturnConfirmationDialog() {
        if (getContext() == null) return;

        //Inflate layout dialog THÀNH CÔNG
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_return_order, null);

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        final AlertDialog returnDialog = builder.create();

        // "X" (ImageView)
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

        //Hiển thị dialog
        returnDialog.show();
    }

    private void showCancelSuccessfulDialog() {
        if (getContext() == null) return;

        // Inflate layout dialog THÀNH CÔNG
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cancel_refund_request_successful, null);

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        final AlertDialog successDialog = builder.create();

        // Tìm nút "X" (ImageView)
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        // sự kiện click cho nút "X"
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                successDialog.dismiss();

                if (sharedViewModel != null) {

                    sharedViewModel.requestTabChange(2);

                    sharedViewModel.refreshOrderLists();
                }

                // Quay lại màn hình StatusOrderFragment
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

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        final AlertDialog successDialog = builder.create();

        // "X" (ImageView)
        ImageView btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        // sự kiện click cho nút "X"
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                successDialog.dismiss();

                if (sharedViewModel != null) {
                    sharedViewModel.requestTabChange(4);
                    sharedViewModel.refreshOrderLists();
                }

                // Quay lại màn hình StatusOrderFragment
                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
            });
        }

        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // Hiển thị dialog
        successDialog.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
