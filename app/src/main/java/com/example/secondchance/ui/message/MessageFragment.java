package com.example.secondchance.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.MessageApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.ConversationListResponseDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends Fragment {
    
    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private final List<ConversationListResponseDto.ConversationItemDto> data = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // layout list tin nhắn của bạn, ví dụ fragment_message.xml
        return inflater.inflate(R.layout.fragment_message, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvConversations = view.findViewById(R.id.rvConversations);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // ❗️QUAN TRỌNG: gán vào field adapter, KHÔNG tạo biến local cùng tên
        adapter = new ConversationAdapter(data, item -> {
            String partnerId = item.getUser().getId();
            String partnerName = item.getUser().getUserName();
            String partnerAvatar = item.getUser().getUserAvatar();
            
            Bundle args = new Bundle();
            args.putString("partnerId", partnerId);
            args.putString("partnerName", partnerName);
            args.putString("partnerAvatar", partnerAvatar);
            
            NavController navController =
              NavHostFragment.findNavController(this);
            
            navController.navigate(
              R.id.action_messageFragment_to_messageDetailFragment,
              args
            );
        });
        
        rvConversations.setAdapter(adapter);
        
        // GỌI API SAU KHI đã set adapter
        fetchConversations();
    }
    
    private void fetchConversations() {
        MessageApi api = RetrofitProvider.message();
        api.getConversations().enqueue(new Callback<ConversationListResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<ConversationListResponseDto> call,
                                   @NonNull Response<ConversationListResponseDto> response) {
                
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lỗi " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                
                ConversationListResponseDto body = response.body();
                if (body == null || !body.isSuccess()
                  || body.getData() == null
                  || body.getData().getItems() == null) {
                    Toast.makeText(getContext(), "Không có cuộc hội thoại", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Phòng trường hợp cực đoan adapter vẫn null
                if (adapter == null) return;
                
                data.clear();
                data.addAll(body.getData().getItems());
                adapter.notifyDataSetChanged();
            }
            
            @Override
            public void onFailure(@NonNull Call<ConversationListResponseDto> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(), "Không tải được danh sách hội thoại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
