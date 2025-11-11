package com.example.secondchance.ui.profile;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.example.secondchance.R;
import com.google.android.material.button.MaterialButton;

public class FillInfoSellerFragment extends Fragment {

    private boolean isChecked = false;

    private RegisterSellerViewModel registerViewModel;

    private ImageView checkbox;
    private MaterialButton btnRegisterSeller;
    private EditText etName, etCCCD, etShopName;
    private EditText etPickupContactName, etPickupContactPhone, etPickupStreet;
    private EditText etPickupProvince, etPickupCity, etPickupPostalCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerViewModel = new ViewModelProvider(this).get(RegisterSellerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fill_info_seller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupClickListeners();
        observeViewModel();
    }

    private void initViews(@NonNull View view) {
        checkbox = view.findViewById(R.id.checkbox);
        btnRegisterSeller = view.findViewById(R.id.btnRegisterSeller);

        etName = view.findViewById(R.id.etName);
        etCCCD = view.findViewById(R.id.etCCCD);
        etShopName = view.findViewById(R.id.etShopName);

        etPickupContactName = view.findViewById(R.id.etPickupContactName);
        etPickupContactPhone = view.findViewById(R.id.etPickupContactPhone);
        etPickupStreet = view.findViewById(R.id.etPickupStreet);
        etPickupProvince = view.findViewById(R.id.etPickupProvince);
        etPickupCity = view.findViewById(R.id.etPickupCity);
        etPickupPostalCode = view.findViewById(R.id.etPickupPostalCode);
    }

    private void setupClickListeners() {
        // Xử lý checkbox
        checkbox.setOnClickListener(v -> {
            isChecked = !isChecked;
            checkbox.setImageResource(isChecked ?
                    R.drawable.ic_checkbox_checked :
                    R.drawable.ic_checkbox_unchecked);
        });

        // Click nút "Đăng ký"
        btnRegisterSeller.setOnClickListener(v -> {
            if (!isChecked) {
                Toast.makeText(requireContext(), "Vui lòng đồng ý điều khoản!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi hàm xử lý đăng ký
            attemptRegistration();
        });
    }

    private void attemptRegistration() {

        String name = etName.getText().toString().trim();
        String cccd = etCCCD.getText().toString().trim();
        String shopName = etShopName.getText().toString().trim();
        String contactName = etPickupContactName.getText().toString().trim();
        String contactPhone = etPickupContactPhone.getText().toString().trim();
        String street = etPickupStreet.getText().toString().trim();
        String province = etPickupProvince.getText().toString().trim();
        String city = etPickupCity.getText().toString().trim();
        String postalCode = etPickupPostalCode.getText().toString().trim();

        if (name.isEmpty() || cccd.isEmpty() || shopName.isEmpty() || contactName.isEmpty() ||
                contactPhone.isEmpty() || street.isEmpty() || province.isEmpty() || city.isEmpty() || postalCode.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ tất cả thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi ViewModel để gửi API
        registerViewModel.submitRegistration(
                name, cccd, shopName,
                contactName, contactPhone, street,
                province, city, postalCode
        );
    }

    private void observeViewModel() {
        // Lắng nghe trạng thái loading
        registerViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            btnRegisterSeller.setEnabled(!isLoading);
            btnRegisterSeller.setText(isLoading ? "Đang xử lý..." : "ĐĂNG KÝ");
        });

        // Lắng nghe lỗi
        registerViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Lắng nghe đăng ký thành công
        registerViewModel.registrationSuccess.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                // API đã báo thành công, hiện dialog
                showWaitAcceptDialog();
                // Báo cho ViewModel đã xử lý xong
                registerViewModel.onRegistrationHandled();
            }
        });
    }

    private void showWaitAcceptDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_wait_accept);
        dialog.setCancelable(false);

        ImageView btnClose = dialog.findViewById(R.id.btnCloseDialog);
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();

            NavHostFragment.findNavController(this).navigate(
                    R.id.action_fill_info_seller_to_profile,
                    null,
                    new NavOptions.Builder()
                            .setPopUpTo(R.id.navigation_profile, true)
                            .build()
            );
        });

        dialog.show();
    }
}