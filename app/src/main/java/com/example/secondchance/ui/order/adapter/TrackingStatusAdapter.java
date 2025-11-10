package com.example.secondchance.ui.order.adapter;

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
import com.example.secondchance.data.model.TrackingStatus;
import java.util.List;

public class TrackingStatusAdapter extends RecyclerView.Adapter<TrackingStatusAdapter.TrackingViewHolder> {

    private List<TrackingStatus> statusList;

    public TrackingStatusAdapter(List<TrackingStatus> statusList) {

        this.statusList = statusList;
    }

    @NonNull
    @Override
    public TrackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tracking_status, parent, false);
        return new TrackingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackingViewHolder holder, int position) {
        TrackingStatus status = statusList.get(position);
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

        void bind(TrackingStatus status) {
            tvTimestamp.setText(status.getTimestamp());
            tvStatus.setText(status.getStatusDescription());

            Context context = itemView.getContext();

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