package com.example.secondchance.ui.order.dialog;

import android.view.ViewGroup;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.secondchance.databinding.DialogConfirmCancelBinding;

public class ConfirmCancelDialog extends DialogFragment {

    public static final String TAG = "ConfirmCancelDialog";
    private DialogConfirmCancelBinding binding;
    private OnCancelConfirmationListener listener;
    private String orderId;

    public interface OnCancelConfirmationListener {
        void onCancelConfirmed(String orderId);
    }

    public ConfirmCancelDialog(String orderId, OnCancelConfirmationListener listener) {
        this.orderId = orderId;
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
        binding = DialogConfirmCancelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnKeepOrder.setOnClickListener(v -> {
            dismiss();
        });

        binding.btnConfirmCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelConfirmed(orderId);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}