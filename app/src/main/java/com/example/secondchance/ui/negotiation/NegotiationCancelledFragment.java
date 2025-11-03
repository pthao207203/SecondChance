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

public class NegotiationCancelledFragment extends Fragment {

    private RecyclerView recyclerView;
    private NegotiationCancelledAdapter adapter;
    private List<NegotiationCancelled> cancelledList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_negotiation_cancelled, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewNegotiationCancelled);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cancelledList = new ArrayList<>();

        // 🧠 Trường hợp 1: Bị từ chối
        cancelledList.add(new NegotiationCancelled(
                "Fish can Fly", "18/02/2025", "Thương lượng lần 1",
                "Giỏ gỗ cắm hoa", "₫ 50.000", "x1", "Giá cố định", "Đã tạo ngày: 17/06/2025",
                "Cá biết bay", "18/02/2025",
                "Cảm ơn bạn đã ra giá, nhưng shop thấy giá bạn đưa ra không phù hợp, mong bạn thông cảm và có thể ra giá khác.",
                false
        ));

        // 🧠 Trường hợp 2: Quá hạn
        cancelledList.add(new NegotiationCancelled(
                "Fish can Fly", "19/02/2025", "Thương lượng lần 2",
                "Giỏ gỗ cắm hoa mini", "₫ 70.000", "x2", "Giá cố định", "Đã tạo ngày: 20/06/2025",
                "Cá biết bay", "20/06/2025",
                "", true
        ));

        adapter = new NegotiationCancelledAdapter(cancelledList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
