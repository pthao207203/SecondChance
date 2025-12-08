// ui/negotiation/NegotiationRequestAdapter.java
package com.example.secondchance.ui.negotiation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemNegotiationRequestCardBinding;

public class NegotiationRequestAdapter
  extends ListAdapter<NegotiationRequest, NegotiationRequestAdapter.ViewHolder> {
    
    public interface OnChatClickListener {
        void onChatClick(NegotiationRequest item);
    }
    
    private OnChatClickListener chatClickListener;
    
    public void setOnChatClickListener(OnChatClickListener listener) {
        this.chatClickListener = listener;
    }
    
    public NegotiationRequestAdapter() {
        super(new DiffCallback());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNegotiationRequestCardBinding binding = ItemNegotiationRequestCardBinding.inflate(
          LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), chatClickListener);
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNegotiationRequestCardBinding binding;
        
        ViewHolder(ItemNegotiationRequestCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(NegotiationRequest request, OnChatClickListener chatClickListener) {
            // Nếu sau này bạn dùng @{} trong XML thì 2 dòng này vẫn dùng được
            binding.setRequest(request);
            binding.setHasReply(request.isHasReply());
            
            // ========== GÁN RÕ RÀNG TỪNG FIELD VÀO VIEW ==========
            
            // Header: tên user + ngày
            binding.productName.setText(request.getUserName());   // "Fish can Fly"
            binding.productDate.setText(request.getDate());       // "18/02/2025"
            
            // Dòng "Thương lượng lần 1"
            binding.negotiationAmount.setText(request.getNegotiationText());
            
            // Thông tin sản phẩm
            binding.tvTitle.setText(request.getProductTitle());   // tên sản phẩm
            binding.tvPrice.setText(request.getPrice());          // "₫ 50.000"
            // nếu muốn format "x1"
            binding.tvQuantity.setText("x" + request.getQuantity());
            
            // "Giá cố định" – nếu bạn cố định text này:
            binding.tvSubtitleFixed.setText("Giá cố định");
            // hoặc nếu BE trả về text khác:
            // binding.tvSubtitleFixed.setText(request.getFixedPriceText());
            
            // Ngày tạo: "Đã tạo ngày: dd/MM/yyyy"
            binding.tvSubtitleDate.setText("Đã tạo ngày: " + request.getCreatedDate());
            
            // ========== IMAGE BINDING ==========
            
            // Avatar user
            Glide.with(binding.getRoot().getContext())
              .load(request.getUserAvatarUrl())
              .placeholder(R.drawable.avatar1)
              .error(R.drawable.avatar1)
              .into(binding.imgUserAvatar);
            
            // Ảnh sản phẩm
            Glide.with(binding.getRoot().getContext())
              .load(request.getProductImageUrl())
              .placeholder(R.drawable.giohoa1)
              .error(R.drawable.giohoa1)
              .into(binding.imgProduct);
            
            // Click "Nhắn tin" → notify ra ngoài fragment
            binding.btnChat.setOnClickListener(v -> {
                if (chatClickListener != null) {
                    chatClickListener.onChatClick(request);
                }
            });
            
            binding.executePendingBindings();
        }
    }
    
    static class DiffCallback extends DiffUtil.ItemCallback<NegotiationRequest> {
        @Override
        public boolean areItemsTheSame(@NonNull NegotiationRequest oldItem,
                                       @NonNull NegotiationRequest newItem) {
            // nếu sau này có id, nên dùng id ở đây
            return oldItem == newItem;
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull NegotiationRequest oldItem,
                                          @NonNull NegotiationRequest newItem) {
            return oldItem.getUserName().equals(newItem.getUserName()) &&
              oldItem.getDate().equals(newItem.getDate()) &&
              oldItem.getProductTitle().equals(newItem.getProductTitle()) &&
              oldItem.isHasReply() == newItem.isHasReply();
        }
    }
}
