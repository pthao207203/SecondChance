package com.example.secondchance.ui.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.secondchance.R;

public class AddBankFragment extends Fragment {

    private ProfileViewModel viewModel;
    private EditText etName, etBankName, etAccountNumber;
    private MaterialCheckBox cbSetAsDefault;
    private AppCompatButton btnAdd;

    private boolean isEditMode = false;
    private int editPosition = -1;
    private PaymentMethodItem currentPaymentMethod = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEdit", false);
            if (isEditMode) {
                currentPaymentMethod = (PaymentMethodItem) getArguments().getSerializable("paymentMethod");
                editPosition = getArguments().getInt("position", -1);
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

        // Nếu đang ở chế độ chỉnh sửa, điền thông tin sẵn có
        if (isEditMode && currentPaymentMethod != null) {
            etName.setText(currentPaymentMethod.getAccountHolderName());
            etBankName.setText(currentPaymentMethod.getBankName());
            etAccountNumber.setText(currentPaymentMethod.getAccountNumber());
            cbSetAsDefault.setChecked(currentPaymentMethod.isDefault());
            btnAdd.setText("CẬP NHẬT");
        }

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

        showSuccessDialog();
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
        viewModel.updatePaymentMethod(editPosition, updatedPaymentMethod);

        showSuccessDialog();
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        View btnCloseSuccess = dialog.findViewById(R.id.btnCloseSuccess);
        btnCloseSuccess.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).popBackStack();
        });

        dialog.show();
    }
}