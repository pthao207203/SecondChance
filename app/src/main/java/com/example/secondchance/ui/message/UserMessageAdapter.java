package com.example.secondchance.ui.message;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemUserMessageBinding;
import com.example.secondchance.ui.message.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserMessageAdapter extends RecyclerView.Adapter<UserMessageAdapter.ViewHolder> {

    private List<Message> messageList;
    private final OnItemClickListener listener;

    public UserMessageAdapter(List<Message> messageList, OnItemClickListener listener) {
        this.messageList = messageList != null ? messageList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserMessageBinding binding = ItemUserMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserMessageBinding binding;

        ViewHolder(ItemUserMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(messageList.get(pos));
                }
            });
        }

        void bind(Message msg) {
            binding.tvName.setText(msg.getSenderName());
            binding.tvLastMessage.setText(msg.getLastMessage());
            binding.tvdate.setText(formatDate(msg.getTimestamp()));

            // Hiển thị chấm chưa đọc
            binding.tvRead.setVisibility(msg.isUnread() ? View.VISIBLE : View.INVISIBLE);

            // Đậm tin nhắn cuối nếu chưa đọc
            if (msg.isUnread()) {
                binding.tvLastMessage.setTypeface(null, Typeface.BOLD);
            } else {
                binding.tvLastMessage.setTypeface(null, Typeface.NORMAL);
            }

            // Load avatar
            Glide.with(itemView.getContext())
                    .load(msg.getAvatarUrl())
                    .placeholder(R.drawable.avatar1)
                    .circleCrop()
                    .into(binding.imgAvatar);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Message message);
    }

    public void updateData(List<Message> newList) {
        this.messageList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
