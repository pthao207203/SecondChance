package com.example.secondchance.ui.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;

public class PaymentMethodFragment extends Fragment {

    private ProfileViewModel viewModel;
    private RecyclerView rvPaymentMethods;
    private PaymentMethodAdapter adapter;
    private boolean isDeleting = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo RecyclerView
        rvPaymentMethods = view.findViewById(R.id.rvPaymentMethods);
        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo Adapter
        adapter = new PaymentMethodAdapter();
        rvPaymentMethods.setAdapter(adapter);

        // Xử lý click vào item
        adapter.setOnPaymentMethodClickListener(paymentMethod -> {
            int position = viewModel.getPaymentMethodList().getValue().indexOf(paymentMethod);

            Bundle bundle = new Bundle();
            bundle.putSerializable("paymentMethod", paymentMethod);
            bundle.putInt("position", position);
            bundle.putBoolean("isEdit", true);

            Navigation.findNavController(view)
                    .navigate(R.id.action_paymentMethod_to_addBank, bundle);
        });

        // Xử lý sự kiện xóa
        adapter.setOnPaymentMethodDeleteListener(this::showConfirmDeleteDialog);

        // Observe LiveData
        viewModel.getPaymentMethodList().observe(getViewLifecycleOwner(), paymentMethods -> {
            adapter.submitList(paymentMethods);
        });

        // Observe Operation Success for Deletion
        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                if (isDeleting) {
                    showDeleteSuccessDialog();
                    isDeleting = false;
                    viewModel.resetOperationSuccess();
                }
            }
        });

        // Nút thêm ngân hàng mới
        view.findViewById(R.id.btnAddNewPaymentMethod).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isEdit", false);
            Navigation.findNavController(v)
                    .navigate(R.id.action_paymentMethod_to_addBank, bundle);
        });
        
        // Load lại dữ liệu khi vào màn hình này, để đảm bảo danh sách ngân hàng luôn mới
        viewModel.fetchUserProfile();
    }

    private void showConfirmDeleteDialog(PaymentMethodItem item) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_delete_payment_method);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.findViewById(R.id.btnConfirmDeletePaymentMethod).setOnClickListener(v -> {
            isDeleting = true;
            viewModel.removePaymentMethod(item.getBankName());
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnCancelDeletePaymentMethod).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_success);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.findViewById(R.id.btnCloseSuccess).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}
