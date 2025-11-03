package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentCreateOrderReturnRequestBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.ui.order.adapter.OrderProductAdapter;
import com.example.secondchance.data.model.OrderProduct;
import java.util.ArrayList;
import java.util.List;

public class CreateOrderReturnRequestFragment extends Fragment
        implements OrderProductAdapter.OnSelectionChangedListener {

    private static final String TAG = "CreateReturnRequest";
    private FragmentCreateOrderReturnRequestBinding binding;
    private SharedViewModel sharedViewModel;
    private String receivedOrderId;
    private OrderProductAdapter productAdapter;
    private final List<OrderProduct> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateOrderReturnRequestBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            Log.d(TAG, "Đang tạo yêu cầu hoàn trả cho Order ID gốc: " + receivedOrderId);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel.updateTitle("Tạo yêu cầu hoàn trả");
        setupProductRecyclerView();
        // Tải dữ liệu mẫu
        loadDummyProductData();
        productAdapter.notifyDataSetChanged();

        binding.btnSendRequest.setOnClickListener(v -> {
            String reason = binding.etReason.getText().toString().trim();
            List<OrderProduct> selectedProducts = productAdapter.getSelectedProducts();


            if (selectedProducts.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm để hoàn trả.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập chi tiết nguyên nhân hoàn trả.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Gửi Yêu Cầu với " + selectedProducts.size() + " sản phẩm. Lý do: " + reason);

            // TODO: Gọi API/Repository để tạo RefundRequest

            Toast.makeText(getContext(), "Đã gửi yêu cầu hoàn trả!", Toast.LENGTH_LONG).show();
            sharedViewModel.refreshOrderLists();
            sharedViewModel.requestTabChange(4);
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void setupProductRecyclerView() {
        productAdapter = new OrderProductAdapter(productList, this);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        binding.btnSendRequest.setEnabled(selectedCount > 0);
        Log.d(TAG, selectedCount + " item(s) selected. Button enabled: " + (selectedCount > 0));
    }

    // Tải dữ liệu mẫu
    private void loadDummyProductData() {
        productList.clear();

        productList.add(new OrderProduct("P001", "Nhẫn Kim Cương Hữu Hạn",
                "Loại 1, Hãng abc", "50.000", R.drawable.nhan1, 1));

        productList.add(new OrderProduct("P002", "Vòng Tay Vàng 24K",
                "Giỏ hoa loại 1 new 99%", "150.000", R.drawable.sample_flower, 1));

        productList.add(new OrderProduct("P003", "Nhẫn Vàng Nữa",
                "Loại 2, Test", "1.000.000", R.drawable.nhan1, 2));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}