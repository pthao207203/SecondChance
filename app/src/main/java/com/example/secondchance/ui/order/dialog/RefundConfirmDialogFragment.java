package com.example.secondchance.ui.order.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.secondchance.R;
import com.google.android.material.button.MaterialButton;

public class RefundConfirmDialogFragment extends DialogFragment {

    public interface RefundConfirmListener {
        void onRefundConfirmed();
        void onRefundCancelled();
    }
    private RefundConfirmListener listener;

    public RefundConfirmDialogFragment() {
    }

    public void setListener(RefundConfirmListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_refund_confirm, container, false);

        MaterialButton btnConfirmCancel = view.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnKeepOrder = view.findViewById(R.id.btnKeepOrder);

        // Nút HỦY
        btnConfirmCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRefundCancelled();
            }
            dismiss();
        });

        btnKeepOrder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRefundConfirmed();
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
