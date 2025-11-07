package com.example.secondchance.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class WalletHistoryAdapter extends RecyclerView.Adapter<WalletHistoryAdapter.VH> {
  
  private final List<WalletTopupHistory> data;
  private final NumberFormat vnd = NumberFormat.getInstance(new Locale("vi", "VN"));
  
  public WalletHistoryAdapter(List<WalletTopupHistory> data) {
    this.data = data;
  }
  
  public void setData(List<WalletTopupHistory> newData) {
    this.data.clear();
    if (newData != null) this.data.addAll(newData);
    notifyDataSetChanged();
  }
  
  @NonNull @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_wallet_history, parent, false);
    return new VH(v);
  }
  
  @Override
  public void onBindViewHolder(@NonNull VH h, int pos) {
    WalletTopupHistory it = data.get(pos);
    h.tvTitle.setText(it.title);
    h.tvDate.setText(it.date);
    h.tvAmount.setText(vnd.format(it.amount));
  }
  
  @Override public int getItemCount() { return data.size(); }
  
  static class VH extends RecyclerView.ViewHolder {
    TextView tvTitle, tvDate, tvAmount;
    VH(@NonNull View itemView) {
      super(itemView);
      tvTitle  = itemView.findViewById(R.id.tvTitle);
      tvDate   = itemView.findViewById(R.id.tvDate);
      tvAmount = itemView.findViewById(R.id.tvAmount);
    }
  }
}