package com.example.secondchance.ui.profile;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import com.example.secondchance.data.model.LabelProvider;
import com.example.secondchance.data.model.ProvinceProvider;
import com.example.secondchance.data.model.WardProvider;

import java.util.List;

public class AddAddressFragment extends Fragment {

    private ProfileViewModel viewModel;
    private AutoCompleteTextView etLabel, etProvince, etWard;
    private EditText etName, etPhone, etAddress;
    private CheckBox cbSetAsDefault;
    private AppCompatButton btnAdd;

    private boolean isEditMode = false;
    private int editPosition = -1;
    private AddressItem currentAddress = null;
    private boolean isWaitingForOperation = false;

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

        etLabel = view.findViewById(R.id.etLabel);
        etProvince = view.findViewById(R.id.etProvince);
        etWard = view.findViewById(R.id.etWard);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);
        cbSetAsDefault = view.findViewById(R.id.cbSetAsDefault);
        btnAdd = view.findViewById(R.id.btnAdd);

        setupDropdowns();

        if (isEditMode && currentAddress != null) {
            fillData(currentAddress);
            btnAdd.setText("CẬP NHẬT");
            
            // Fetch fresh detail
            if (currentAddress.getId() != null) {
                viewModel.fetchAddressDetail(currentAddress.getId());
            }
        }

        btnAdd.setOnClickListener(v -> {
            if (isEditMode) {
                updateAddress();
            } else {
                addAddress();
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (isWaitingForOperation && success != null) {
                isWaitingForOperation = false;
                if (success) {
                    showSuccessDialog();
                } else {
                    Toast.makeText(requireContext(), "Thao tác thất bại", Toast.LENGTH_SHORT).show();
                }
                viewModel.resetOperationSuccess();
            }
        });
        
        viewModel.getAddressDetail().observe(getViewLifecycleOwner(), item -> {
            if (isEditMode && item != null && currentAddress != null) {
                // Only update if IDs match (basic check)
                if (item.getId() != null && item.getId().equals(currentAddress.getId())) {
                    currentAddress = item;
                    fillData(item);
                }
            }
        });
    }

    private void setupDropdowns() {
        // Setup Label
        List<String> labels = LabelProvider.getLabels();
        BankAdapter labelAdapter = new BankAdapter(requireContext(), labels); // Use BankAdapter for custom UI
        etLabel.setAdapter(labelAdapter);
        etLabel.setThreshold(1000);
        etLabel.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dropdown_bank));
        etLabel.setDropDownVerticalOffset(-20);
        etLabel.setOnClickListener(v -> etLabel.showDropDown());
        etLabel.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etLabel.showDropDown();
            if (!hasFocus) {
                String input = etLabel.getText().toString();
                if (!labels.contains(input) && !input.isEmpty()) {
                    etLabel.setText("");
                    Toast.makeText(requireContext(), "Vui lòng chọn loại địa chỉ từ danh sách", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup Province
        List<String> provinces = ProvinceProvider.getProvinces();
        BankAdapter provinceAdapter = new BankAdapter(requireContext(), provinces); // Use BankAdapter
        etProvince.setAdapter(provinceAdapter);
        etProvince.setThreshold(1);
        etProvince.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dropdown_bank));
        etProvince.setDropDownVerticalOffset(-20);
        etProvince.setOnClickListener(v -> etProvince.showDropDown());
        etProvince.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etProvince.showDropDown();
             if (!hasFocus) {
                String input = etProvince.getText().toString();
                if (!provinces.contains(input) && !input.isEmpty()) {
                    etProvince.setText("");
                    Toast.makeText(requireContext(), "Vui lòng chọn Tỉnh/Thành phố từ danh sách", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update Ward when Province changes
        etProvince.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etWard.setText("");
                setupWardDropdown(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initial Ward setup (empty)
        setupWardDropdown("");
    }

    private void setupWardDropdown(String province) {
        List<String> wards = WardProvider.getWards(province);
        BankAdapter wardAdapter = new BankAdapter(requireContext(), wards); // Use BankAdapter
        etWard.setAdapter(wardAdapter);
        etWard.setThreshold(1);
        etWard.setDropDownBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dropdown_bank));
        etWard.setDropDownVerticalOffset(-20);
        etWard.setOnClickListener(v -> {
            if (etProvince.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn Tỉnh/Thành phố trước", Toast.LENGTH_SHORT).show();
            } else {
                etWard.showDropDown();
            }
        });
        etWard.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (etProvince.getText().toString().isEmpty()) {
                    etWard.clearFocus();
                    Toast.makeText(requireContext(), "Vui lòng chọn Tỉnh/Thành phố trước", Toast.LENGTH_SHORT).show();
                } else {
                    etWard.showDropDown();
                }
            }
             if (!hasFocus) {
                String input = etWard.getText().toString();
                if (!wards.contains(input) && !input.isEmpty()) {
                    etWard.setText("");
                    Toast.makeText(requireContext(), "Vui lòng chọn Phường/Xã từ danh sách", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void fillData(AddressItem item) {
        etLabel.setText(item.getLabel(), false); 
        etProvince.setText(item.getProvince(), false);
        setupWardDropdown(item.getProvince()); 
        etWard.setText(item.getWard(), false);
        
        etName.setText(item.getName());
        etPhone.setText(item.getPhone());
        if (item.getStreet() != null && !item.getStreet().isEmpty()) {
            etAddress.setText(item.getStreet());
        } else {
             etAddress.setText(item.getAddress());
        }
        cbSetAsDefault.setChecked(item.isDefault());
    }

    private void addAddress() {
        String label = etLabel.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String ward = etWard.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        boolean isDefault = cbSetAsDefault.isChecked();

        if (label.isEmpty() || province.isEmpty() || ward.isEmpty() || name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<String> labels = LabelProvider.getLabels();
        if (!labels.contains(label)) {
             Toast.makeText(requireContext(), "Loại địa chỉ không hợp lệ", Toast.LENGTH_SHORT).show();
             return;
        }
        List<String> provinces = ProvinceProvider.getProvinces();
        if (!provinces.contains(province)) {
             Toast.makeText(requireContext(), "Tỉnh/Thành phố không hợp lệ", Toast.LENGTH_SHORT).show();
             return;
        }
        List<String> wards = WardProvider.getWards(province);
        if (!wards.contains(ward)) {
             Toast.makeText(requireContext(), "Phường/Xã không hợp lệ", Toast.LENGTH_SHORT).show();
             return;
        }


        AddressItem newAddress = new AddressItem(null, name, phone, address, ward, province, "VietNam", label, isDefault, 0, 0);
        
        isWaitingForOperation = true;
        viewModel.resetOperationSuccess();
        viewModel.addAddress(newAddress);
    }

    private void updateAddress() {
        String label = etLabel.getText().toString().trim();
        String province = etProvince.getText().toString().trim();
        String ward = etWard.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        boolean isDefault = cbSetAsDefault.isChecked();

        if (label.isEmpty() || province.isEmpty() || ward.isEmpty() || name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<String> labels = LabelProvider.getLabels();
        if (!labels.contains(label)) {
             Toast.makeText(requireContext(), "Loại địa chỉ không hợp lệ", Toast.LENGTH_SHORT).show();
             return;
        }
        List<String> provinces = ProvinceProvider.getProvinces();
        if (!provinces.contains(province)) {
             Toast.makeText(requireContext(), "Tỉnh/Thành phố không hợp lệ", Toast.LENGTH_SHORT).show();
             return;
        }
        List<String> wards = WardProvider.getWards(province);
        if (!wards.contains(ward)) {
             Toast.makeText(requireContext(), "Phường/Xã không hợp lệ", Toast.LENGTH_SHORT).show();
             return;
        }

        if (currentAddress == null) return;

        currentAddress.setLabel(label);
        currentAddress.setProvince(province);
        currentAddress.setWard(ward);
        currentAddress.setName(name);
        currentAddress.setPhone(phone);
        currentAddress.setStreet(address);
        currentAddress.setDefault(isDefault);

        isWaitingForOperation = true;
        viewModel.resetOperationSuccess();
        if (currentAddress.getId() != null) {
            viewModel.updateAddress(currentAddress.getId(), currentAddress);
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID địa chỉ", Toast.LENGTH_SHORT).show();
            isWaitingForOperation = false;
        }
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Choose layout based on mode
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
        if (btnCloseSuccess != null) {
            btnCloseSuccess.setOnClickListener(v -> {
                dialog.dismiss();
                Navigation.findNavController(requireView()).popBackStack();
            });
        }

        dialog.show();
    }
}
