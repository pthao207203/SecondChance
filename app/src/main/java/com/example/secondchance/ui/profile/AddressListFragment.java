package com.example.secondchance.ui.profile;

import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;

public class AddressListFragment extends Fragment {

    private ProfileViewModel viewModel;
    private RecyclerView rvAddressList;
    private AddressListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel chung của Activity
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo RecyclerView
        rvAddressList = view.findViewById(R.id.rvAddressList);
        rvAddressList.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo Adapter
        adapter = new AddressListAdapter();
        rvAddressList.setAdapter(adapter);

        // Xử lý click vào item
        adapter.setOnAddressClickListener(addressItem -> {
            // Khi click vào địa chỉ, mở fragment chỉnh sửa
            int position = viewModel.getAddressList().getValue().indexOf(addressItem);

            Bundle bundle = new Bundle();
            bundle.putSerializable("address", addressItem);
            bundle.putInt("position", position);
            bundle.putBoolean("isEdit", true);

            Navigation.findNavController(view)
                    .navigate(R.id.action_addressList_to_addAddress, bundle);
        });

        // Observe LiveData từ ViewModel
        viewModel.getAddressList().observe(getViewLifecycleOwner(), addressList -> {
            adapter.submitList(addressList);
        });

        // Nút thêm địa chỉ mới
        view.findViewById(R.id.btnAddNewAddress).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isEdit", false);
            Navigation.findNavController(v)
                    .navigate(R.id.action_addressList_to_addAddress, bundle);
        });
    }
}