// ui/comment/CommentAdapter.java
package com.example.secondchance.ui.comment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.databinding.ItemCommentBinding;

public class CommentAdapter extends ListAdapter<Comment, CommentAdapter.ViewHolder> {

    public CommentAdapter() {
        super(new DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        ViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Comment comment) {
            binding.setComment(comment);
            binding.setHasReply(comment.hasReply());
            binding.executePendingBindings();
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<Comment> {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.hasReply() == newItem.hasReply() &&
                    oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getContent().equals(newItem.getContent());
        }
    }
}
