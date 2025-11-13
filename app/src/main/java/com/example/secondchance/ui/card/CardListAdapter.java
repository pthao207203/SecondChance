package com.example.secondchance.ui.card;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemProductAuctioncardBinding;
import com.example.secondchance.databinding.ItemProductFixedcardBinding;
import com.example.secondchance.databinding.ItemProductNegotiationcardBinding;

import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardViewHolder> {

    private List<ProductCard> products;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(ProductCard product);
    }

    public CardListAdapter(Context context, List<ProductCard> products, OnItemClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (products == null || position < 0 || position >= products.size()) {
            Log.w("CardAdapter", "Invalid position: " + position + " | products size: " + (products != null ? products.size() : "null"));
            return R.layout.item_product_fixedcard; // Fallback
        }
        ProductCard product = products.get(position);
        Log.d("CardAdapter", "productType: " + product.getProductType());
        if (product.getProductType() == ProductCard.ProductType.FIXED) {
            Log.d("CardAdapter", "Position " + position + ": FIXED");
            return R.layout.item_product_fixedcard;
        } else if (product.getProductType() == ProductCard.ProductType.AUCTION) {
            Log.d("CardAdapter", "Position " + position + ": AUCTION");
            return R.layout.item_product_auctioncard;
        } else if (product.getProductType() == ProductCard.ProductType.NEGOTIATION) {
            Log.d("CardAdapter", "Position " + position + ": NEGOTIATION");
            return R.layout.item_product_negotiationcard;
        } else {
            Log.w("CardAdapter", "Position " + position + ": DEFAULT -> FIXED");
            return R.layout.item_product_fixedcard;
        }
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Log.d("CardAdapter", "Creating ViewHolder for viewType: " + viewType);

        if (viewType == R.layout.item_product_fixedcard) {
            ItemProductFixedcardBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
            Log.d("CardAdapter", "✅ FIXED Binding created");
            return new FixedCardViewHolder(binding);
        } else if (viewType == R.layout.item_product_auctioncard) {
            ItemProductAuctioncardBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
            Log.d("CardAdapter", "✅ AUCTION Binding created");
            return new AuctionCardViewHolder(binding);
        } else if (viewType == R.layout.item_product_negotiationcard) {
            ItemProductNegotiationcardBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
            Log.d("CardAdapter", "✅ NEGOTIATION Binding created");
            return new NegotiationCardViewHolder(binding);
        }

        Log.e("CardAdapter", "❌ Unknown viewType: " + viewType + " | Using FIXED as fallback");
        ItemProductFixedcardBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_product_fixedcard, parent, false);
        return new FixedCardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (products == null || position < 0 || position >= products.size()) {
            Log.w("CardAdapter", "Invalid bind position: " + position);
            return;
        }
        Log.d("CardAdapter", "Binding position: " + position);
        ProductCard product = products.get(position);
        holder.bind(product);
        setupClickListener(holder, product);
    }

    private void setupClickListener(CardViewHolder holder, ProductCard product) {
        holder.itemView.setOnClickListener(v -> {
            Log.d("CardAdapter", "Clicked product ID: " + product.getId() + " | Title: " + product.getTitle());
            if (listener != null) listener.onItemClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateData(List<ProductCard> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    public abstract static class CardViewHolder extends RecyclerView.ViewHolder {
        public CardViewHolder(androidx.databinding.ViewDataBinding binding) {
            super(binding.getRoot());
        }
        public abstract void bind(ProductCard product);
    }

    public static class FixedCardViewHolder extends CardViewHolder {
        private final ItemProductFixedcardBinding binding;

        public FixedCardViewHolder(ItemProductFixedcardBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        public void bind(ProductCard product) {
            binding.setProduct(product);
            binding.executePendingBindings();
        }
    }

    public static class AuctionCardViewHolder extends CardViewHolder {
        private final ItemProductAuctioncardBinding binding;

        public AuctionCardViewHolder(ItemProductAuctioncardBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        public void bind(ProductCard product) {
            binding.setProduct(product);
            binding.executePendingBindings();
        }
    }

    public static class NegotiationCardViewHolder extends CardViewHolder {
        private final ItemProductNegotiationcardBinding binding;

        public NegotiationCardViewHolder(ItemProductNegotiationcardBinding binding) {
            super(binding);
            this.binding = binding;
        }

        @Override
        public void bind(ProductCard product) {
            binding.setProduct(product);
            binding.executePendingBindings();
        }
    }
}
