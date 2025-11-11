package com.example.secondchance.ui.cart;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.repo.CartRepository;
import com.example.secondchance.data.remote.CartApi;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemListener {

    private RecyclerView recyclerViewCart;
    private ImageView checkboxSelectAll;
    private TextView tvTotalPrice;
    private AppCompatButton btnBuyNow;
    private View layoutSelectAll;
    private CartAdapter adapter;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartData();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        checkboxSelectAll = view.findViewById(R.id.checkboxSelectAll);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnBuyNow = view.findViewById(R.id.btnBuyNow);
        layoutSelectAll = view.findViewById(R.id.layoutSelectAll);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCart.setAdapter(adapter);
    }

    private void setupClickListeners() {
        layoutSelectAll.setOnClickListener(v -> toggleSelectAll());
        btnBuyNow.setOnClickListener(v -> handleBuyNow());
    }

    private void loadCartData() {
        List<CartApi.CartItem> cachedItems = CartRepository.getInstance().getCachedCart();
        adapter.updateItems(cachedItems);
        updateUIState(cachedItems);
        fetchCartFromServer();
    }

    private void fetchCartFromServer() {
        if (isLoading) return;
        setLoadingState(true);

        CartRepository.getInstance().fetchCart(new CartRepository.CartCallback() {
            @Override
            public void onSuccess(List<CartApi.CartItem> items) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    adapter.updateItems(items);
                    updateUIState(items);
                    setLoadingState(false);
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    setLoadingState(false);
                });
            }
        });
    }

    private void updateUIState(List<CartApi.CartItem> items) {
        if (items.isEmpty()) {
            layoutSelectAll.setVisibility(View.GONE);
        } else {
            layoutSelectAll.setVisibility(View.VISIBLE);
        }
        updateTotalPrice();
        checkboxSelectAll.setImageResource(
                adapter.areAllItemsSelected() ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
        );
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        btnBuyNow.setEnabled(!loading);
    }

    private void toggleSelectAll() {
        boolean shouldSelectAll = !adapter.areAllItemsSelected();
        adapter.selectAll(shouldSelectAll);
        updateUIState(adapter.getItems());
    }

    private void handleBuyNow() {
        List<CartApi.CartItem> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm để mua", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<CartApi.CartItem> itemsToCheckout = new ArrayList<>(selectedItems);

        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedItems", itemsToCheckout);

        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_cartFragment_to_checkoutFragment, bundle);
        } catch(Exception e) {
            Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalPrice() {
        long totalPrice = 0;
        for (CartApi.CartItem item : adapter.getSelectedItems()) {
            totalPrice += item.getTotalPrice();
        }
        String formattedPrice = String.format("%,d", totalPrice).replace(",", ".");
        tvTotalPrice.setText(formattedPrice);
    }

    @Override
    public void onItemChecked(CartApi.CartItem item, boolean isChecked) {
        updateUIState(adapter.getItems());
    }

    @Override
    public void onViewDetail(CartApi.CartItem item) {
        Bundle bundle = new Bundle();
        bundle.putString("productId", item.productId);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_cartFragment_to_detailProductFragment, bundle);
    }

    @Override
    public void onItemDeleted(CartApi.CartItem item, int position) {
        if (!isAdded() || getContext() == null) return;

        final Dialog confirmDialog = new Dialog(getContext());
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmDialog.setContentView(R.layout.dialog_confirm_delete);
        if (confirmDialog.getWindow() != null) {
            confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnConfirm = confirmDialog.findViewById(R.id.btnConfirmDelete);
        Button btnCancel = confirmDialog.findViewById(R.id.btnCancelDelete);

        btnConfirm.setOnClickListener(v -> {
            confirmDialog.dismiss();
            deleteItem(item);
        });

        btnCancel.setOnClickListener(v -> confirmDialog.dismiss());

        confirmDialog.show();
    }

    private void deleteItem(CartApi.CartItem item) {
        if (isLoading) return;
        setLoadingState(true);

        // SỬA: Dùng lại item.productId theo yêu cầu
        CartRepository.getInstance().removeFromCart(item.productId, new CartRepository.CartCallback() {
            @Override
            public void onSuccess(List<CartApi.CartItem> items) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    showDeleteSuccessDialog();
                    adapter.updateItems(items);
                    updateUIState(items);
                    setLoadingState(false);
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    setLoadingState(false);
                });
            }
        });
    }

    private void showDeleteSuccessDialog() {
        if (!isAdded() || getContext() == null) return;

        final Dialog successDialog = new Dialog(getContext());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.dialog_delete_success);
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView btnClose = successDialog.findViewById(R.id.btnCloseSuccess);
        btnClose.setOnClickListener(v -> successDialog.dismiss());

        successDialog.show();
    }
}
