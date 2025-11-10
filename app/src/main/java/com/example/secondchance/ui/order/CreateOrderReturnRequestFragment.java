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
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.ui.order.adapter.OrderProductAdapter;
import com.example.secondchance.data.model.OrderItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateOrderReturnRequestFragment extends Fragment
        implements OrderProductAdapter.OnSelectionChangedListener {

    private static final String TAG = "CreateReturnRequest";
    private FragmentCreateOrderReturnRequestBinding binding;
    private SharedViewModel sharedViewModel;
    private String receivedOrderId;
    private OrderProductAdapter productAdapter;
    private final List<OrderItem> productList = new ArrayList<>();

    private OrderRepository orderRepository;

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

        orderRepository = new OrderRepository();
        setupProductRecyclerView();

        loadData(receivedOrderId);

        binding.btnSendRequest.setOnClickListener(v -> {
            String reason = binding.etReason.getText().toString().trim();
            List<OrderItem> selectedProducts = productAdapter.getSelectedProducts();

            if (selectedProducts.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sản phẩm để hoàn trả.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập chi tiết nguyên nhân hoàn trả.", Toast.LENGTH_SHORT).show();
                return;
            }

            sendReturnRequest(reason, selectedProducts);
        });
    }

    private void loadData(String id) {
        if (id == null) return;

        orderRepository.getOrderDetails(id, new OrderRepository.RepoCallback<OrderDetailResponse.Data>() {
            @Override
            public void onSuccess(OrderDetailResponse.Data data) {
                if (!isAdded()) return;

                productList.clear();
                if (data.order != null && data.order.orderItems != null) {

                    for (OrderDetailResponse.OrderItem dtoItem : data.order.orderItems) {
                        OrderItem modelItem = new OrderItem();
                        modelItem.productId = dtoItem.productId;
                        modelItem.name = dtoItem.name;
                        modelItem.imageUrl = dtoItem.imageUrl;
                        modelItem.price = dtoItem.price;
                        modelItem.quantity = dtoItem.qty;
                        productList.add(modelItem);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                Toast.makeText(getContext(), "Lỗi tải danh sách sản phẩm: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendReturnRequest(String reason, List<OrderItem> selectedProducts) {

        List<String> dummyMedia = new ArrayList<>();
        dummyMedia.add("https://res.cloudinary.com/dgejserfb/image/upload/v1762277354/samsung-galaxy-s24-256gb-5g-thumb-600x600_tyye4v.jpg");


        List<OrderApi.ReturnRequestItem> requestItems = new ArrayList<>();
        for (OrderItem model : selectedProducts) {
            requestItems.add(new OrderApi.ReturnRequestItem(model.productId, model.quantity));
        }

        OrderApi.ReturnRequestBody body = new OrderApi.ReturnRequestBody(reason, requestItems, dummyMedia);

        orderRepository.createReturnRequest(receivedOrderId, body, new OrderRepository.RepoCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded()) return;


                Toast.makeText(getContext(), "Đã gửi yêu cầu hoàn trả!", Toast.LENGTH_LONG).show();
                sharedViewModel.refreshOrderLists();
                sharedViewModel.requestTabChange(4);
                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                Toast.makeText(getContext(), "Gửi yêu cầu thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}