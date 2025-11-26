package com.example.secondchance.ui.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.secondchance.R;
import com.example.secondchance.data.model.BankProvider;

public class AddBankFragment extends Fragment {

    private ProfileViewModel viewModel;
    private EditText etName, etAccountNumber;
    private AutoCompleteTextView etBankName;
    private MaterialCheckBox cbSetAsDefault;
    private AppCompatButton btnAdd;

    private boolean isEditMode = false;
    private PaymentMethodItem currentPaymentMethod = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEdit", false);
            if (isEditMode) {
                currentPaymentMethod = (PaymentMethodItem) getArguments().getSerializable("paymentMethod");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        etName = view.findViewById(R.id.etName);
        etBankName = view.findViewById(R.id.etBankName);
        etAccountNumber = view.findViewById(R.id.etAccountNumber);
        cbSetAsDefault = view.findViewById(R.id.cbSetAsDefault);
        btnAdd = view.findViewById(R.id.btnAdd);

        // Setup AutoCompleteTextView for Bank Name
        // Sử dụng BankAdapter tùy chỉnh để xử lý hiển thị item cuối cùng
        BankAdapter adapter = new BankAdapter(requireContext(), BankProvider.getSupportedBanks());
        etBankName.setAdapter(adapter);
        etBankName.setThreshold(1); // Bắt đầu gợi ý từ 1 ký tự
        etBankName.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dropdown_bank)); // Bo tròn góc dưới
        etBankName.setDropDownVerticalOffset(-20); // Đẩy danh sách lên một chút để che viền dưới của ô nhập liệu, tạo cảm giác liền mạch
        etBankName.setOnClickListener(v -> etBankName.showDropDown()); // Hiện list khi click vào
        etBankName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etBankName.showDropDown();
        });


        // Nếu đang ở chế độ chỉnh sửa, điền thông tin sẵn có
        if (isEditMode && currentPaymentMethod != null) {
            etName.setText(currentPaymentMethod.getAccountHolderName());
            etBankName.setText(currentPaymentMethod.getBankName());
            etAccountNumber.setText(currentPaymentMethod.getAccountNumber());
            cbSetAsDefault.setChecked(currentPaymentMethod.isDefault());
            btnAdd.setText("CẬP NHẬT");
            
            viewModel.fetchBankDetail(currentPaymentMethod.getBankName());

            etBankName.setEnabled(false);
            etBankName.setAlpha(0.5f);
        }
        
        // Observe bank detail update
        viewModel.getBankDetail().observe(getViewLifecycleOwner(), item -> {
            if (item != null && isEditMode) {
                 etName.setText(item.getAccountHolderName());
                 etBankName.setText(item.getBankName());
                 etAccountNumber.setText(item.getAccountNumber());
                 cbSetAsDefault.setChecked(item.isDefault());
            }
        });

        // Observe kết quả từ ViewModel để hiển thị dialog thành công hoặc lỗi
        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showSuccessDialog();
                viewModel.resetOperationSuccess();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút "THÊM" hoặc "CẬP NHẬT"
        btnAdd.setOnClickListener(v -> {
            if (isEditMode) {
                updatePaymentMethodAndShowDialog();
            } else {
                addPaymentMethodAndShowDialog();
            }
        });
    }

    private void addPaymentMethodAndShowDialog() {
        String name = etName.getText().toString().trim();
        String bankName = etBankName.getText().toString().trim();
        String accountNumber = etAccountNumber.getText().toString().trim();
        boolean isDefault = cbSetAsDefault.isChecked();

        if (name.isEmpty() || bankName.isEmpty() || accountNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        
        PaymentMethodItem newPaymentMethod = new PaymentMethodItem(name, bankName, accountNumber, isDefault);
        viewModel.addPaymentMethod(newPaymentMethod);
    }

    private void updatePaymentMethodAndShowDialog() {
        String name = etName.getText().toString().trim();
        String bankName = etBankName.getText().toString().trim();
        String accountNumber = etAccountNumber.getText().toString().trim();
        boolean isDefault = cbSetAsDefault.isChecked();

        if (name.isEmpty() || bankName.isEmpty() || accountNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentMethodItem updatedPaymentMethod = new PaymentMethodItem(name, bankName, accountNumber, isDefault);
        String oldBankName = (currentPaymentMethod != null) ? currentPaymentMethod.getBankName() : "";
        viewModel.updatePaymentMethod(oldBankName, updatedPaymentMethod);
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Chọn layout dựa trên chế độ (Edit vs Add)
        if (isEditMode) {
            dialog.setContentView(R.layout.dialog_update_success);
        } else {
            dialog.setContentView(R.layout.dialog_add_success);
        }
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        View btnCloseSuccess = dialog.findViewById(R.id.btnCloseSuccess);
        btnCloseSuccess.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).popBackStack();
        });

        dialog.show();
    }
}
