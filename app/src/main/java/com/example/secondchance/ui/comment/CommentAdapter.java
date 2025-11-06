// ui/comment/CommentAdapter.java
package com.example.secondchance.ui.comment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
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
            binding.setHasReply(false);

            // Load avatar
            Glide.with(itemView.getContext())
                    .load(comment.getAvatarUrl())
                    .placeholder(R.drawable.avatar1)
                    .circleCrop()
                    .into(binding.imageViewAvatar);

            // Load 3 ảnh media
            var media = comment.getMedia();
            var images = new android.widget.ImageView[]{
                    binding.imageReview1,
                    binding.imageReview2,
                    binding.imageReview3
            };

            for (int i = 0; i < images.length; i++) {
                if (i < media.size() && media.get(i) != null) {
                    images[i].setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                            .load(media.get(i))
                            .placeholder(R.drawable.giohoa1)
                            .into(images[i]);
                } else {
                    images[i].setVisibility(View.GONE);
                }
            }

            binding.executePendingBindings();
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<Comment> {
        @Override
        public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.getContent().equals(newItem.getContent());
        }
    }
}
