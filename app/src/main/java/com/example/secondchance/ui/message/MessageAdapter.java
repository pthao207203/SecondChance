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
import com.example.secondchance.dto.response.MessageListResponseDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  
  private static final int VIEW_TYPE_INCOMING = 1;
  private static final int VIEW_TYPE_OUTGOING = 2;
  
  private final List<MessageListResponseDto.MessageItemDto> items;
  private final String partnerId;         // id của đối phương
  private final String partnerAvatarUrl;  // avatar đối phương
  
  public MessageAdapter(List<MessageListResponseDto.MessageItemDto> items,
                        String partnerId,
                        String partnerAvatarUrl) {
    this.items = items;
    this.partnerId = partnerId;
    this.partnerAvatarUrl = partnerAvatarUrl;
  }
  
  @Override
  public int getItemViewType(int position) {
    MessageListResponseDto.MessageItemDto msg = items.get(position);
    // senderId == partnerId → incoming (bên trái)
    if (msg.getSenderId() != null && msg.getSenderId().equals(partnerId)) {
      return VIEW_TYPE_INCOMING;
    } else {
      return VIEW_TYPE_OUTGOING;
    }
  }
  
  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                    int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    if (viewType == VIEW_TYPE_INCOMING) {
      View v = inflater.inflate(R.layout.item_message_left, parent, false);
      return new IncomingVH(v);
    } else {
      View v = inflater.inflate(R.layout.item_message_right, parent, false);
      return new OutgoingVH(v);
    }
  }
  
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                               int position) {
    MessageListResponseDto.MessageItemDto msg = items.get(position);
    String time = formatTime(msg.getSentAt());
    String type = msg.getContentType() != null ? msg.getContentType() : "text";
    
    if (holder.getItemViewType() == VIEW_TYPE_INCOMING) {
      IncomingVH in = (IncomingVH) holder;
      bindCommon(in.tvTime, in.tvContent, in.imgMessage, type, msg.getContent());
      in.tvTime.setText(time);
      
      // avatar đối phương
      Glide.with(in.itemView.getContext())
        .load(partnerAvatarUrl)
        .placeholder(R.drawable.avatar1)
        .error(R.drawable.avatar1)
        .into(in.imgAvatar);
      
    } else {
      OutgoingVH out = (OutgoingVH) holder;
      bindCommon(out.tvTime, out.tvContent, out.imgMessage, type, msg.getContent());
      out.tvTime.setText(time);
      
      // avatar của mình (nếu chưa có thì tạm để placeholder)
      Glide.with(out.itemView.getContext())
        .load(getSelfAvatarUrl())
        .placeholder(R.drawable.avatar1)
        .error(R.drawable.avatar1)
        .into(out.imgAvatar);
    }
  }
  
  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size();
  }
  
  // --- ViewHolders ---
  
  static class IncomingVH extends RecyclerView.ViewHolder {
    TextView tvContent, tvTime;
    ImageView imgMessage, imgAvatar;
    
    IncomingVH(@NonNull View itemView) {
      super(itemView);
      tvContent = itemView.findViewById(R.id.tvcontent);
      tvTime = itemView.findViewById(R.id.tvtime);
      imgMessage = itemView.findViewById(R.id.imgMessage);
      imgAvatar = itemView.findViewById(R.id.imgAvatar);
    }
  }
  
  static class OutgoingVH extends RecyclerView.ViewHolder {
    TextView tvContent, tvTime;
    ImageView imgMessage, imgAvatar;
    
    OutgoingVH(@NonNull View itemView) {
      super(itemView);
      tvContent = itemView.findViewById(R.id.tvcontent);
      tvTime = itemView.findViewById(R.id.tvtime);
      imgMessage = itemView.findViewById(R.id.imgMessage);
      imgAvatar = itemView.findViewById(R.id.imgAvatar);
    }
  }
  
  // --- Helper: bind text / image ---
  
  private void bindCommon(TextView tvTime,
                          TextView tvContent,
                          ImageView imgMessage,
                          String contentType,
                          String content) {
    if ("image".equalsIgnoreCase(contentType)) {
      // hiển thị ảnh
      tvContent.setVisibility(View.GONE);
      imgMessage.setVisibility(View.VISIBLE);
      
      Glide.with(imgMessage.getContext())
        .load(content)     // content = URL ảnh
        .placeholder(R.drawable.image_01) // icon ảnh của bạn
        .error(R.drawable.image_01)
        .into(imgMessage);
    } else {
      // hiển thị text
      imgMessage.setVisibility(View.GONE);
      tvContent.setVisibility(View.VISIBLE);
      tvContent.setText(content != null ? content : "");
    }
  }
  
  private String getSelfAvatarUrl() {
    // TODO: nếu app có avatar user hiện tại thì trả về ở đây
    return null;
  }
  
  private String formatTime(String isoString) {
    if (isoString == null) return "";
    try {
      SimpleDateFormat serverFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
      serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date d = serverFormat.parse(isoString);
      if (d == null) return "";
      SimpleDateFormat outFormat =
        new SimpleDateFormat("HH:mm", Locale.getDefault());
      outFormat.setTimeZone(TimeZone.getDefault());
      return outFormat.format(d);
    } catch (ParseException e) {
      return "";
    }
  }
}
