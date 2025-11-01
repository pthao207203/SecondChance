package com.example.secondchance.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class WalletPurchasedHistoryAdapter extends RecyclerView.Adapter<WalletPurchasedHistoryAdapter.VH> {
  public interface OnItemClickListener { void onItemClick(WalletHistoryItem item); }
  private OnItemClickListener listener;
  public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }
  private final List<WalletHistoryItem> data;
  
  public WalletPurchasedHistoryAdapter(List<WalletHistoryItem> data) {
    this.data = data;
  }
  
  @NonNull @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_wallet_purchased_received_history, parent, false);
    return new VH(v);
  }
  
  @Override public void onBindViewHolder(@NonNull VH h, int position) {
    WalletHistoryItem it = data.get(position);
    h.ivThumb.setImageResource(it.thumbRes);
    h.tvTitle.setText(it.title);
    h.tvSub.setText(it.sub);
    h.tvAmount.setText(it.price); // truyền vào chỉ số tiền "50.000"
    
    h.itemView.setOnClickListener(v -> {
      if (listener != null) listener.onItemClick(it);
    });
  }
  
  @Override public int getItemCount() { return data.size(); }
  
  static class VH extends RecyclerView.ViewHolder {
    ShapeableImageView ivThumb;
    TextView tvTitle, tvSub, tvCurrency, tvAmount;
    VH(@NonNull View itemView) {
      super(itemView);
      ivThumb = itemView.findViewById(R.id.ivThumb);
      tvTitle = itemView.findViewById(R.id.tvTitle);
      tvSub = itemView.findViewById(R.id.tvSub);
      tvCurrency = itemView.findViewById(R.id.tvCurrency);
      tvAmount = itemView.findViewById(R.id.tvAmount);
    }
  }
}
