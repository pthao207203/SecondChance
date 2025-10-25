package com.example.secondchance.ui.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.secondchance.R;

public class AddAddressFragment extends Fragment {

    private ProfileViewModel viewModel;
    private EditText etName, etPhone, etAddress;
    private CheckBox cbSetAsDefault;
    private AppCompatButton btnAdd;

    private boolean isEditMode = false;
    private int editPosition = -1;
    private AddressItem currentAddress = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEdit", false);
            if (isEditMode) {
                currentAddress = (AddressItem) getArguments().getSerializable("address");
                editPosition = getArguments().getInt("position", -1);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        cbSetAsDefault = view.findViewById(R.id.cbSetAsDefault);
        btnAdd = view.findViewById(R.id.btnAdd);

        if (isEditMode && currentAddress != null) {
            etName.setText(currentAddress.getName());
            etPhone.setText(currentAddress.getPhone());
            etAddress.setText(currentAddress.getAddress());
            cbSetAsDefault.setChecked(currentAddress.isDefault());
            btnAdd.setText("CẬP NHẬT");
        }

        btnAdd.setOnClickListener(v -> {
            if (isEditMode) {
                updateAddressAndShowDialog();
            } else {
                addAddressAndShowDialog();
            }
        });
    }

    private void addAddressAndShowDialog() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        boolean isDefault = cbSetAsDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        AddressItem newAddress = new AddressItem(name, phone, address, isDefault);
        viewModel.addAddress(newAddress);

        showSuccessDialog();
    }

    private void updateAddressAndShowDialog() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        boolean isDefault = cbSetAsDefault.isChecked();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        AddressItem updatedAddress = new AddressItem(name, phone, address, isDefault);
        viewModel.updateAddress(editPosition, updatedAddress);

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