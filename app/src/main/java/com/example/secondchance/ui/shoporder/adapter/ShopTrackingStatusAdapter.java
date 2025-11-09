package com.example.secondchance.ui.shoporder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.model.ShopTrackingStatus;
import java.util.List;

public class ShopTrackingStatusAdapter extends RecyclerView.Adapter<ShopTrackingStatusAdapter.TrackingViewHolder> {

    private Context context;
    private List<ShopTrackingStatus> statusList;

    public ShopTrackingStatusAdapter(Context context, List<ShopTrackingStatus> statusList) {
        this.context = context;
        this.statusList = statusList;
    }

    @NonNull
    @Override
    public TrackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_tracking_status, parent, false);
        return new TrackingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackingViewHolder holder, int position) {
        ShopTrackingStatus status = statusList.get(position);
        holder.bind(status);
    }

    @Override
    public int getItemCount() {
        return statusList == null ? 0 : statusList.size();
    }

    class TrackingViewHolder extends RecyclerView.ViewHolder {
        ImageView dotActive, dotInactive;
        TextView tvTimestamp, tvStatus;

        public TrackingViewHolder(@NonNull View itemView) {
            super(itemView);
            dotActive = itemView.findViewById(R.id.dotActive);
            dotInactive = itemView.findViewById(R.id.dotInactive);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(ShopTrackingStatus status) {
            tvTimestamp.setText(status.getTimestamp());
            tvStatus.setText(status.getStatusDescription());
            if (status.isActive()) {
                dotActive.setVisibility(View.VISIBLE);
                dotInactive.setVisibility(View.GONE);
                int activeColor = ContextCompat.getColor(context, R.color.highLight5);
                tvTimestamp.setTextColor(activeColor);
                tvStatus.setTextColor(activeColor);
            } else {
                dotActive.setVisibility(View.GONE);
                dotInactive.setVisibility(View.VISIBLE);
                int inactiveColor = ContextCompat.getColor(context, R.color.text_secondary);
                tvTimestamp.setTextColor(inactiveColor);
                tvStatus.setTextColor(inactiveColor);
            }
        }
    }
}
