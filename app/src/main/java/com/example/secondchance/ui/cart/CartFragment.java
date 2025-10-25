package com.example.secondchance.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemListener {

    private RecyclerView recyclerViewCart;
    private ImageView checkboxSelectAll;
    private TextView tvTotalPrice;
    private View btnBuyNow, layoutSelectAll;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private boolean isAllSelected = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        initViews(view);

        // Khởi tạo dữ liệu mẫu
        initSampleData();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập các sự kiện click
        setupClickListeners();

        // Cập nhật tổng giá
        updateTotalPrice();
    }

    private void initViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        checkboxSelectAll = view.findViewById(R.id.checkboxSelectAll);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnBuyNow = view.findViewById(R.id.btnBuyNow);
        layoutSelectAll = view.findViewById(R.id.layoutSelectAll);
    }

    private void initSampleData() {
        cartItems = new ArrayList<>();

        // Thêm dữ liệu mẫu
        cartItems.add(new CartItem(
                "1",
                "Áo vàng cổ điển",
                "1 mẫu, giá cố định, đổi hư hại 100%, có bảo hành, đổi trả với shop",
                50000,
                ""
        ));

        cartItems.add(new CartItem(
                "2",
                "Áo vàng cổ điển",
                "1 mẫu, giá cố định, đổi hư hại 100%, có bảo hành, đổi trả với shop",
                50000,
                ""
        ));

        cartItems.add(new CartItem(
                "3",
                "Áo vàng cổ điển",
                "1 mẫu, giá cố định, đổi hư hại 100%, có bảo hành, đổi trả với shop",
                50000,
                ""
        ));
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems, this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewCart.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Checkbox chọn tất cả
        layoutSelectAll.setOnClickListener(v -> toggleSelectAll());

        // Nút mua ngay
        btnBuyNow.setOnClickListener(v -> handleBuyNow());
    }

    private void toggleSelectAll() {
        isAllSelected = !isAllSelected;

        checkboxSelectAll.setImageResource(
                isAllSelected ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
        );

        adapter.selectAll(isAllSelected);
        updateTotalPrice();
    }

    private void handleBuyNow() {
        List<CartItem> selectedItems = adapter.getSelectedItems();

        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn sản phẩm để mua", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển sang CheckoutFragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedItems", new ArrayList<>(selectedItems));

        Navigation.findNavController(requireView())
                .navigate(R.id.action_cartFragment_to_checkoutFragment, bundle);
    }

    private void updateTotalPrice() {
        int totalPrice = 0;
        List<CartItem> selectedItems = adapter.getSelectedItems();

        for (CartItem item : selectedItems) {
            totalPrice += item.getPrice();
        }

        tvTotalPrice.setText("đ " + String.format("%,d", totalPrice));
    }

    // Implement CartAdapter.OnCartItemListener
    @Override
    public void onItemChecked(CartItem item, boolean isChecked) {
        updateTotalPrice();

        // Cập nhật trạng thái checkbox "Chọn tất cả"
        List<CartItem> selectedItems = adapter.getSelectedItems();
        isAllSelected = selectedItems.size() == cartItems.size();
        checkboxSelectAll.setImageResource(
                isAllSelected ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
        );
    }

    @Override
    public void onItemDeleted(CartItem item, int position) {
        cartItems.remove(position);
        updateTotalPrice();

        // Cập nhật trạng thái checkbox "Chọn tất cả"
        if (cartItems.isEmpty()) {
            isAllSelected = false;
            checkboxSelectAll.setImageResource(R.drawable.ic_checkbox_unchecked);
        }
    }

    @Override
    public void onViewDetail(CartItem item) {
        Toast.makeText(requireContext(), "Xem chi tiết: " + item.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Navigate to product detail fragment
        // Bundle bundle = new Bundle();
        // bundle.putString("productId", item.getId());
        // Navigation.findNavController(requireView())
        //         .navigate(R.id.action_cartFragment_to_productDetailFragment, bundle);
    }
}