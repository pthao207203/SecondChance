// ui/negotiation/NegotiationAcceptedFragment.java
package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.databinding.FragmentRecyclerNegotiationAcceptedBinding;
import java.util.ArrayList;
import java.util.List;

public class NegotiationAcceptedFragment extends Fragment {

    private FragmentRecyclerNegotiationAcceptedBinding binding;
    private NegotiationAcceptedAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerNegotiationAcceptedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadSampleData();
    }

    private void setupRecyclerView() {
        adapter = new NegotiationAcceptedAdapter();
        binding.recyclerViewAccepted.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewAccepted.setHasFixedSize(true);
        binding.recyclerViewAccepted.setAdapter(adapter);
    }

    private void loadSampleData() {
        List<NegotiationAccepted> list = new ArrayList<>();

        // Item 1: Chưa thanh toán
        NegotiationAccepted item1 = new NegotiationAccepted();
        list.add(item1);

        // Item 2: Đã thanh toán
        NegotiationAccepted item2 = new NegotiationAccepted();
        item2.setPaid(true);
        list.add(item2);

        // Item 3: Chưa thanh toán
        NegotiationAccepted item3 = new NegotiationAccepted();
        item3.setPaid(false);
        list.add(item3);

        adapter.submitList(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
