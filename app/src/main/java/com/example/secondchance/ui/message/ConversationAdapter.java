package com.example.secondchance.ui.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.dto.response.ConversationListResponseDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationVH> {
  
  public interface OnConversationClickListener {
    void onConversationClick(ConversationListResponseDto.ConversationItemDto item);
  }
  
  private List<ConversationListResponseDto.ConversationItemDto> items;
  private final OnConversationClickListener listener;
  
  public ConversationAdapter(List<ConversationListResponseDto.ConversationItemDto> items,
                             OnConversationClickListener listener) {
    this.items = items;
    this.listener = listener;
  }
  
  public void setItems(List<ConversationListResponseDto.ConversationItemDto> newItems) {
    this.items = newItems;
    notifyDataSetChanged();
  }
  
  @NonNull
  @Override
  public ConversationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_user_message, parent, false);
    return new ConversationVH(v);
  }
  
  @Override
  public void onBindViewHolder(@NonNull ConversationVH holder, int position) {
    ConversationListResponseDto.ConversationItemDto item = items.get(position);
    
    holder.tvName.setText(item.getUser().getUserName());
    holder.tvLastMessage.setText(item.getLastContent());
    
    // Format lastSentAt -> dd/MM/yyyy
    holder.tvDate.setText(formatDate(item.getLastSentAt()));
    
    // Avatar
    Glide.with(holder.itemView.getContext())
      .load(item.getUser().getUserAvatar())
      .placeholder(R.drawable.avatar1)
      .error(R.drawable.avatar1)
      .into(holder.imgAvatar);
    
    // Unread badge: nếu unreadCount > 0 thì hiện chấm, ngược lại ẩn
    if (item.getUnreadCount() > 0) {
      holder.tvRead.setVisibility(View.VISIBLE);
    } else {
      holder.tvRead.setVisibility(View.INVISIBLE);
    }
    
    holder.itemView.setOnClickListener(v -> {
      if (listener != null) listener.onConversationClick(item);
    });
  }
  
  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size();
  }
  
  static class ConversationVH extends RecyclerView.ViewHolder {
    ImageView imgAvatar;
    TextView tvName;
    TextView tvDate;
    TextView tvLastMessage;
    ImageView tvRead;
    
    ConversationVH(@NonNull View itemView) {
      super(itemView);
      imgAvatar = itemView.findViewById(R.id.imgAvatar);
      tvName = itemView.findViewById(R.id.tvName);
      tvDate = itemView.findViewById(R.id.tvdate);
      tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
      tvRead = itemView.findViewById(R.id.tvRead);
    }
  }
  
  private String formatDate(String isoString) {
    if (isoString == null) return "";
    try {
      // Parse ISO 8601
      SimpleDateFormat serverFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
      serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date d = serverFormat.parse(isoString);
      if (d == null) return "";
      // Format theo local
      SimpleDateFormat outFormat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
      outFormat.setTimeZone(TimeZone.getDefault());
      return outFormat.format(d);
    } catch (ParseException e) {
      return "";
    }
  }
}
