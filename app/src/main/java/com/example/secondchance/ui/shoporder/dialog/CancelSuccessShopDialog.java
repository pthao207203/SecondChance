package com.example.secondchance.ui.shoporder.dialog;

import android.content.DialogInterface;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.secondchance.databinding.DialogConfirmCancelSuccessfulBinding; // Đảm bảo tên này đúng

public class CancelSuccessShopDialog extends DialogFragment {

    public static final String TAG = "CancelSuccessDialogShop";
    private DialogConfirmCancelSuccessfulBinding binding;
    private OnDismissListener listener;

    public interface OnDismissListener {
        void onSuccessfulDismiss();
    }

    public CancelSuccessShopDialog(OnDismissListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogConfirmCancelSuccessfulBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCloseDialog.setOnClickListener(v -> {
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (listener != null) {
            listener.onSuccessfulDismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}