// ui/negotiation/NegotiationRequestFragment.java
package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.databinding.FragmentRecyclerNegotiationRequestBinding;
import java.util.ArrayList;
import java.util.List;

public class NegotiationRequestFragment extends Fragment {

    private FragmentRecyclerNegotiationRequestBinding binding;
    private NegotiationRequestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerNegotiationRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadSampleData();
    }

    private void setupRecyclerView() {
        adapter = new NegotiationRequestAdapter();
        binding.recyclerViewRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewRequests.setHasFixedSize(true);
        binding.recyclerViewRequests.setAdapter(adapter);
    }

    private void loadSampleData() {
        List<NegotiationRequest> list = new ArrayList<>();

        // Item 1
        list.add(new NegotiationRequest());

        // Item 2
        NegotiationRequest item2 = new NegotiationRequest();
        item2.setUserName("Người bán A");
        item2.setDate("19/02/2025");
        item2.setNegotiationText("Thương lượng lần 2");
        item2.setProductTitle("Bình hoa sứ cao cấp");
        item2.setPrice("₫ 120.000");
        item2.setQuantity("2");
        item2.setCreatedDate("18/06/2025");
        item2.setHasReply(true);
        list.add(item2);

        // Item 3
        NegotiationRequest item3 = new NegotiationRequest();
        item3.setUserName("Shop XYZ");
        item3.setDate("20/02/2025");
        item3.setNegotiationText("Thương lượng lần 1");
        item3.setProductTitle("Chậu cây mini");
        item3.setPrice("₫ 35.000");
        item3.setQuantity("3");
        item3.setCreatedDate("19/06/2025");
        item3.setHasReply(false);
        list.add(item3);

        adapter.submitList(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
