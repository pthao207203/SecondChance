package com.example.secondchance.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.example.secondchance.ui.card.ProductCard;

public class HomeFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    View btnSeeAll = view.findViewById(R.id.tvSeeAllAuction);
    if (btnSeeAll != null) {
      btnSeeAll.setOnClickListener(v -> {
        Log.d("NAV_TEST", "Clicked Xem tat ca");
        try {
          NavHostFragment.findNavController(HomeFragment.this)
                  .navigate(R.id.action_home_to_auction);
        } catch (Exception e) {
          Log.e("NAV_TEST", "Navigation error: " + e.getMessage());
        }
      });
    }
    View auctionCard = view.findViewById(R.id.auction_card_home);
    if (auctionCard != null) {
      auctionCard.setOnClickListener(v -> {
        // 1. Tạo ProductCard bằng tay (vì nó là thẻ tĩnh)
        ProductCard product = new ProductCard();
        product.setTitle("Vòng hoa hướng dương vàng");
        product.setPrice("₫8.500.000");
        product.setQuantity(1);
        // product.setImageRes(R.drawable.nhan1);
        // product.setDescription("Mô tả cho vòng hoa...");
        product.setProductType(ProductCard.ProductType.AUCTION);

        // === THÊM DÒNG NÀY VÀO ===
        product.setTimeRemaining("02:39:12"); // <-- Gán thời gian tĩnh vào đây

        // 2. Tạo Bundle
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", product);

        // 3. Điều hướng
        Navigation.findNavController(v).navigate(
                R.id.action_home_navigation_detail_product,
                bundle
        );
      });
    }
  }
}
