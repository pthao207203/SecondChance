package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import java.util.ArrayList;
import java.util.List;

public class NegotiationCompletedFragment extends Fragment {

    private RecyclerView recyclerView;
    private NegotiationCompletedAdapter adapter;
    private List<NegotiationCompleted> completedList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_negotiation_completed, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewNegotiationCompleted);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        completedList = new ArrayList<>();

        // 🔹 Ví dụ dữ liệu 1
        completedList.add(new NegotiationCompleted(
                "Fish can Fly", "18/02/2025", "Thương lượng lần 1",
                "Giỏ gỗ cắm hoa", "₫ 50.000", "x1",
                "Giá cố định", "Đã tạo ngày: 17/06/2025",
                "Đơn hàng đã được người dùng thanh toán. Xem hóa đơn trong Lịch sử đơn hàng"
        ));

        // 🔹 Ví dụ dữ liệu 2
        completedList.add(new NegotiationCompleted(
                "Flower Planet", "19/02/2025", "Thương lượng lần 2",
                "Bó hoa hướng dương", "₫ 120.000", "x2",
                "Giá cố định", "Đã tạo ngày: 20/06/2025",
                "Đơn hàng đã được thanh toán và chuyển sang mục Lịch sử đơn hàng"
        ));

        adapter = new NegotiationCompletedAdapter(completedList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
