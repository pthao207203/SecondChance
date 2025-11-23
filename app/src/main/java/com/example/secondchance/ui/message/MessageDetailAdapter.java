package com.example.secondchance.ui.message;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemDateHeaderBinding;
import com.example.secondchance.databinding.ItemMessageLeftBinding;
import com.example.secondchance.databinding.ItemMessageRightBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageDetailAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {

    private static final int TYPE_DATE = 0;
    private static final int TYPE_MESSAGE_LEFT = 1;
    private static final int TYPE_MESSAGE_RIGHT = 2;

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK = new DiffUtil.ItemCallback<Object>() {
        @Override
        public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof String && newItem instanceof String) {
                return oldItem.equals(newItem);
            }
            if (oldItem instanceof MessageDetail && newItem instanceof MessageDetail) {
                MessageDetail oldMsg = (MessageDetail) oldItem;
                MessageDetail newMsg = (MessageDetail) newItem;
                // So sánh theo timestamp + nội dung (hoặc thêm id nếu có)
                return oldMsg.getTimestamp() == newMsg.getTimestamp()
                        && TextUtils.equals(oldMsg.getContent(), newMsg.getContent());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof String && newItem instanceof String) {
                return oldItem.equals(newItem);
            }
            if (oldItem instanceof MessageDetail && newItem instanceof MessageDetail) {
                MessageDetail oldMsg = (MessageDetail) oldItem;
                MessageDetail newMsg = (MessageDetail) newItem;
                return oldMsg.getContent().equals(newMsg.getContent())
                        && oldMsg.getTime().equals(newMsg.getTime())
                        && oldMsg.isMine() == newMsg.isMine()
                        && TextUtils.equals(oldMsg.getAvatarUrl(), newMsg.getAvatarUrl());
            }
            return false;
        }
    };

    public MessageDetailAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof String) return TYPE_DATE;
        return ((MessageDetail) item).isMine() ? TYPE_MESSAGE_RIGHT : TYPE_MESSAGE_LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE) {
            ItemDateHeaderBinding binding = ItemDateHeaderBinding.inflate(inflater, parent, false);
            return new DateViewHolder(binding);
        } else if (viewType == TYPE_MESSAGE_RIGHT) {
            ItemMessageRightBinding binding = ItemMessageRightBinding.inflate(inflater, parent, false);
            return new RightViewHolder(binding);
        } else {
            ItemMessageLeftBinding binding = ItemMessageLeftBinding.inflate(inflater, parent, false);
            return new LeftViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = getItem(position);

        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).bind((String) item);
        } else {
            MessageDetail msg = (MessageDetail) item;
            boolean showAvatarAndTime = isLastMessageOfSender(position);
            if (holder instanceof LeftViewHolder) {
                ((LeftViewHolder) holder).bind(msg, showAvatarAndTime);
            } else if (holder instanceof RightViewHolder) {
                ((RightViewHolder) holder).bind(msg, showAvatarAndTime);
            }
        }
    }

    // Kiểm tra tin này có phải tin cuối của người gửi không
    private boolean isLastMessageOfSender(int position) {
        if (position == getItemCount() - 1) return true;
        Object current = getItem(position);
        Object next = getItem(position + 1);
        if (!(current instanceof MessageDetail) || !(next instanceof MessageDetail)) return true;
        return ((MessageDetail) current).isMine() != ((MessageDetail) next).isMine();
    }

    // SUBMIT LIST CÓ NGÀY TỰ ĐỘNG
    public void submitMessagesWithDate(List<MessageDetail> messages) {
        java.util.List<Object> newList = new java.util.ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());

        long lastDate = -1;
        for (MessageDetail msg : messages) {
            long currentDate = getStartOfDay(msg.getTimestamp());
            if (currentDate != lastDate) {
                String dateStr = dateFormat.format(new Date(msg.getTimestamp()));
                newList.add(dateStr);
                lastDate = currentDate;
            }
            newList.add(msg);
        }
        submitList(newList);
    }

    // ADD TIN MỚI CÓ NGÀY TỰ ĐỘNG
    public void addMessageWithDate(MessageDetail message) {
        java.util.List<Object> currentList = new java.util.ArrayList<>(getCurrentList());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());

        if (currentList.isEmpty()) {
            currentList.add(dateFormat.format(new Date(message.getTimestamp())));
            currentList.add(message);
        } else {
            Object lastItem = currentList.get(currentList.size() - 1);
            if (lastItem instanceof MessageDetail) {
                long lastTime = ((MessageDetail) lastItem).getTimestamp();
                if (getStartOfDay(lastTime) != getStartOfDay(message.getTimestamp())) {
                    currentList.add(dateFormat.format(new Date(message.getTimestamp())));
                }
            }
            currentList.add(message);
        }
        submitList(currentList);
    }

    private long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    // VIEW HOLDER CHO NGÀY
    static class DateViewHolder extends RecyclerView.ViewHolder {
        ItemDateHeaderBinding binding;
        DateViewHolder(ItemDateHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(String date) {
            binding.tvDate.setText(date);
        }
    }

    // LEFT & RIGHT VIEW HOLDER
    static class LeftViewHolder extends RecyclerView.ViewHolder {
        ItemMessageLeftBinding binding;
        LeftViewHolder(ItemMessageLeftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(MessageDetail msg, boolean show) {
            binding.tvcontent.setText(msg.getContent());
            binding.tvtime.setText(msg.getTime());
            binding.tvtime.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.imgAvatar.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                Glide.with(itemView.getContext())
                        .load(msg.getAvatarUrl())
                        .placeholder(R.drawable.avatar1)
                        .circleCrop()
                        .into(binding.imgAvatar);
            }
        }
    }

    static class RightViewHolder extends RecyclerView.ViewHolder {
        ItemMessageRightBinding binding;
        RightViewHolder(ItemMessageRightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(MessageDetail msg, boolean show) {
            binding.tvcontent.setText(msg.getContent());
            binding.tvtime.setText(msg.getTime());
            binding.tvtime.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.imgAvatar.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                Glide.with(itemView.getContext())
                        .load(msg.getAvatarUrl())
                        .placeholder(R.drawable.avatar2)
                        .circleCrop()
                        .into(binding.imgAvatar);
            }
        }
    }
}