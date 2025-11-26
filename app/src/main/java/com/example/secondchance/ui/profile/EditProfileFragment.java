package com.example.secondchance.ui.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import java.io.IOException;

public class EditProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvDefaultAddress, tvDefaultBank;
    private ImageView ivAvatar, btnEditAvatar;
    private EditText etName, etPhone, etEmail;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().getContentResolver(), selectedImageUri);
                        ivAvatar.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDefaultAddress = view.findViewById(R.id.tvDefaultAddress);
        tvDefaultBank = view.findViewById(R.id.tvDefaultBank);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);

        // Bind existing data
        viewModel.getName().observe(getViewLifecycleOwner(), name -> { if(name != null) etName.setText(name); });
        viewModel.getPhone().observe(getViewLifecycleOwner(), phone -> { if(phone != null) etPhone.setText(phone); });
        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> { if(email != null) etEmail.setText(email); });

        viewModel.getAddressList().observe(getViewLifecycleOwner(), addressList -> {
            AddressItem defaultAddress = viewModel.getDefaultAddress();
            if (defaultAddress != null) {
                tvDefaultAddress.setText(defaultAddress.getAddress());
            } else {
                tvDefaultAddress.setText("Chưa có địa chỉ");
            }
        });

        viewModel.getPaymentMethodList().observe(getViewLifecycleOwner(), paymentMethods -> {
            PaymentMethodItem defaultPayment = viewModel.getDefaultPaymentMethod();
            if (defaultPayment != null) {
                tvDefaultBank.setText(defaultPayment.getDisplayName());
            } else {
                tvDefaultBank.setText("Chưa có ngân hàng");
            }
        });

        // Observe operation success
        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success) {
                    showUpdateSuccessDialog();
                }
                viewModel.resetOperationSuccess();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                // Clear error message in VM if needed, or just show toast
            }
        });

        View.OnClickListener selectImageListener = v -> openImagePicker();
        ivAvatar.setOnClickListener(selectImageListener);
        btnEditAvatar.setOnClickListener(selectImageListener);

        view.findViewById(R.id.btnEditAddress).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_editProfile_to_addressList)
        );

        view.findViewById(R.id.btnEditBank).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_editProfile_to_paymentMethod)
        );

        view.findViewById(R.id.btnUpdate).setOnClickListener(v -> updateProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming avatar update is handled separately or not implemented in this API call as per requirements
        if (selectedImageUri != null) {
            viewModel.setAvatarUri(selectedImageUri);
        }

        viewModel.updateUserProfile(name, phone, email);
    }

    private void showUpdateSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_success);
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