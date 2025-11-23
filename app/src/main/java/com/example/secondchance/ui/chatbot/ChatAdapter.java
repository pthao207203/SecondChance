package com.example.secondchance.ui.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  
  private static final int TYPE_USER = 1;
  private static final int TYPE_BOT = 2;
  
  private List<MessageModel> messageList;
  private Markwon markwon;
  
  public ChatAdapter(List<MessageModel> messageList) {
    this.messageList = messageList;
  }
  
  // 1. Xác định tin nhắn hiện tại là của ai để chọn Layout
  @Override
  public int getItemViewType(int position) {
    if (messageList.get(position).isUser()) {
      return TYPE_USER;
    } else {
      return TYPE_BOT;
    }
  }
  
  // 2. Tạo ViewHolder dựa trên loại Layout
  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (markwon == null) {
      markwon = Markwon.create(parent.getContext());
    }
    if (viewType == TYPE_USER) {
      View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_chat_user, parent, false); // Tên file xml user của bạn
      return new UserViewHolder(view);
    } else {
      View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_chat_ai, parent, false); // Tên file xml bot của bạn
      return new BotViewHolder(view);
    }
    
  }
  
  // 3. Đổ dữ liệu vào View
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    MessageModel message = messageList.get(position);
    
    if (getItemViewType(position) == TYPE_USER) {
      UserViewHolder userHolder = (UserViewHolder) holder;
      userHolder.tvMessage.setText(message.getMessage());
      
      // Xử lý hiển thị ảnh user gửi
      if (message.getImage() != null) {
        userHolder.imgSent.setVisibility(View.VISIBLE);
        userHolder.imgSent.setImageBitmap(message.getImage());
      } else {
        userHolder.imgSent.setVisibility(View.GONE);
      }
      
    } else {
      BotViewHolder botHolder = (BotViewHolder) holder;
      botHolder.tvMessage.setVisibility(View.VISIBLE);
      markwon.setMarkdown(botHolder.tvMessage, message.getMessage());
    }
  }
  
  @Override
  public int getItemCount() {
    return messageList.size();
  }
  
  // --- View Holders ---
  
  // ViewHolder cho USER
  static class UserViewHolder extends RecyclerView.ViewHolder {
    TextView tvMessage;
    ImageView imgSent;
    
    public UserViewHolder(@NonNull View itemView) {
      super(itemView);
      tvMessage = itemView.findViewById(R.id.tvMessage);
      imgSent = itemView.findViewById(R.id.imgUserSent);
    }
  }
  
  // ViewHolder cho BOT
  static class BotViewHolder extends RecyclerView.ViewHolder {
    TextView tvMessage;
    
    public BotViewHolder(@NonNull View itemView) {
      super(itemView);
      tvMessage = itemView.findViewById(R.id.tvMessage);
    }
  }
}