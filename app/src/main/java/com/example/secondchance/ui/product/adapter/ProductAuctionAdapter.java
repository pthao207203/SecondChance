package com.example.secondchance.ui.product.adapter;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.ui.product.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProductAuctionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_AUCTION = 1;
    private static final int VIEW_TYPE_AUCTION_EXPIRED = 2;

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAuctionAdapter(OnProductClickListener listener) {
        this.productList = new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Product product = productList.get(position);
        if (product.getEndTime() < System.currentTimeMillis()) {
            return VIEW_TYPE_AUCTION_EXPIRED;
        } else {
            return VIEW_TYPE_AUCTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_AUCTION) {
            View auctionView = inflater.inflate(R.layout.item_product_auction, parent, false);
            return new AuctionViewHolder(auctionView);
        } else {
            View expiredView = inflater.inflate(R.layout.item_product_auction_expired, parent, false);
            return new AuctionExpiredViewHolder(expiredView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Product product = productList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_AUCTION) {
            ((AuctionViewHolder) holder).bind(product, listener, this, position);
        } else {
            ((AuctionExpiredViewHolder) holder).bind(product, listener);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof AuctionViewHolder) {
            ((AuctionViewHolder) holder).cancelTimer();
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class AuctionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvHours, tvMinutes, tvSeconds;
        CountDownTimer timer;

        public AuctionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.auctionImage);
            tvName = itemView.findViewById(R.id.product_name);
            tvPrice = itemView.findViewById(R.id.currentPrice);
            tvHours = itemView.findViewById(R.id.hours_text);
            tvMinutes = itemView.findViewById(R.id.minutes_text);
            tvSeconds = itemView.findViewById(R.id.seconds_text);
        }

        public void bind(Product product, OnProductClickListener listener, ProductAuctionAdapter adapter, int position) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format("%,.0f", product.getPrice()));

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                String imageUrl = product.getImageUrls().get(0);
                try {
                    int resId = Integer.parseInt(imageUrl);
                    imgProduct.setImageResource(resId);
                } catch (NumberFormatException e) {
                    imgProduct.setImageURI(Uri.parse(imageUrl));
                }
            }

            long remainingTime = product.getEndTime() - System.currentTimeMillis();

            if (timer != null) {
                timer.cancel();
            }

            timer = new CountDownTimer(remainingTime, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                    tvHours.setText(String.format(Locale.getDefault(), "%02d", hours));
                    tvMinutes.setText(String.format(Locale.getDefault(), "%02d", minutes));
                    tvSeconds.setText(String.format(Locale.getDefault(), "%02d", seconds));
                }

                @Override
                public void onFinish() {
                    adapter.notifyItemChanged(position);
                }
            }.start();

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
        
        public void cancelTimer() {
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    static class AuctionExpiredViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;

        public AuctionExpiredViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.auctionImage);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }

        public void bind(Product product, OnProductClickListener listener) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format("%,.0f", product.getPrice()));

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                String imageUrl = product.getImageUrls().get(0);
                try {
                    int resId = Integer.parseInt(imageUrl);
                    imgProduct.setImageResource(resId);
                } catch (NumberFormatException e) {
                    imgProduct.setImageURI(Uri.parse(imageUrl));
                }
            }

            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("productId", product.getId());
                bundle.putString("productName", product.getName());
                bundle.putFloat("price", (float) product.getPrice());
                bundle.putInt("quantity", product.getQuantity());
                bundle.putStringArrayList("imageUrls", new ArrayList<>(product.getImageUrls()));
                bundle.putString("description", product.getDescription());
                bundle.putString("source", product.getSource());
                bundle.putString("proof", product.getProof());
                bundle.putString("otherInfo", product.getOtherInfo());
                bundle.putString("productType", "auction"); // Restore AS an auction

                Navigation.findNavController(v).navigate(R.id.action_productTab_to_editAuction, bundle);
            });
        }
    }
}