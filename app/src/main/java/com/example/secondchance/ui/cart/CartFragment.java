package com.example.secondchance.ui.cart;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.repo.CartRepository;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.ui.card.ProductCard;
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
        // Truyền 'this' (CartFragment) làm listener cho Adapter
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

        // Cập nhật trạng thái nút Select All
        boolean allSelected = items.size() > 0 && adapter.areAllItemsSelected();
        checkboxSelectAll.setImageResource(
                allSelected ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
        );

        // Cập nhật tổng tiền ban đầu (nếu có item đã được chọn từ trước)
        onTotalChange(adapter.calculateTotal());
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        btnBuyNow.setEnabled(!loading);
    }

    private void toggleSelectAll() {
        boolean shouldSelectAll = !adapter.areAllItemsSelected();
        adapter.selectAll(shouldSelectAll);

        checkboxSelectAll.setImageResource(
                shouldSelectAll ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
        );
    }

    private void handleBuyNow() {
        ArrayList<CartApi.CartItem> selectedItems = (ArrayList<CartApi.CartItem>) adapter.getSelectedItems();

        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm để mua", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đóng gói dữ liệu vào Bundle
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedItems", selectedItems);

        // Chuyển hướng sang CheckoutFragment
        try {
            // Đảm bảo ID action này có trong nav_graph
            Navigation.findNavController(requireView()).navigate(R.id.navigation_checkout, bundle);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==================================================================
    // 🔥 IMPLEMENT CÁC HÀM CỦA INTERFACE OnCartItemListener
    // ==================================================================

    @Override
    public void onItemChecked(CartApi.CartItem item, boolean isChecked) {
        // Khi một item thay đổi trạng thái check, cập nhật UI Select All
        boolean allSelected = adapter.getItemCount() > 0 && adapter.areAllItemsSelected();
        checkboxSelectAll.setImageResource(
                allSelected ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
        );
    }

    @Override
    public void onTotalChange(long total) {
        // Cập nhật TextView tổng tiền
        String formattedPrice = String.format("%,d", total).replace(",", ".");
        tvTotalPrice.setText(formattedPrice);
    }

    @Override
    public void onViewDetail(CartApi.CartItem item) {
        ProductCard productCard = new ProductCard(
                item.productId,
                item.getImageUrl(),
                item.getName(),
                item.getDescription(),
                item.qty,
                0,
                String.valueOf(item.price),
                ProductCard.ProductType.FIXED,
                null, 0
        );

        Bundle bundle = new Bundle();
        bundle.putSerializable("product", productCard);

        try {
            Navigation.findNavController(requireView()).navigate(R.id.action_cartFragment_to_detailProductFragment, bundle);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemDeleted(CartApi.CartItem item, int position) {
        if (isLoading) return;
        setLoadingState(true);

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
        if (!isAdded()) return;
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_success);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().dimAmount = 0.6f;
        }

        ImageView btnClose = dialog.findViewById(R.id.btnCloseSuccess);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
