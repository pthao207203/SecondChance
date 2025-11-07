// File: FillInfoSellerFragment.java
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
    private SellerViewModel sellerViewModel; // Thêm ViewModel

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel ngay từ đầu
        sellerViewModel = new ViewModelProvider(requireActivity()).get(SellerViewModel.class);
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

        ImageView checkbox = view.findViewById(R.id.checkbox);
        MaterialButton btnRegister = view.findViewById(R.id.btnRegisterSeller);

        // Xử lý checkbox
        checkbox.setOnClickListener(v -> {
            isChecked = !isChecked;
            checkbox.setImageResource(isChecked ?
                    R.drawable.ic_checkbox_checked :
                    R.drawable.ic_checkbox_unchecked);
        });

        // Click nút "Đăng ký"
        btnRegister.setOnClickListener(v -> {
            if (!isChecked) {
                Toast.makeText(requireContext(), "Vui lòng đồng ý điều khoản!", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = ((EditText) view.findViewById(R.id.etName)).getText().toString().trim();
            String cccd = ((EditText) view.findViewById(R.id.etCCCD)).getText().toString().trim();
            String shopName = ((EditText) view.findViewById(R.id.etShopName)).getText().toString().trim();
            String address = ((EditText) view.findViewById(R.id.etAddress)).getText().toString().trim();

            if (name.isEmpty() || cccd.isEmpty() || shopName.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // HIỆN DIALOG
            showWaitAcceptDialog();
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

            // ĐÁNH DẤU ĐÃ LÀ NGƯỜI BÁN THÀNH CÔNG
            sellerViewModel.setSeller(true);

            // Quay về Profile và làm sạch back stack
            NavHostFragment.findNavController(this).navigate(
                    R.id.action_fill_info_seller_to_profile,
                    null,
                    new NavOptions.Builder()
                            .setPopUpTo(R.id.navigation_profile, true) // Xóa toàn bộ stack đến Profile
                            .build()
            );
        });

        dialog.show();
    }
}
