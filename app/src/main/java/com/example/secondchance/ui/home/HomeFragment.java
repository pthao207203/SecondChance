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
import com.google.android.material.card.MaterialCardView;
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

    MaterialCardView cardNegotiation = view.findViewById(R.id.card_negotiation);

    if (cardNegotiation != null) {
      cardNegotiation.setOnClickListener(v -> {
        try {
          NavController navController = Navigation.findNavController(v);
          navController.navigate(R.id.action_homeFragment_to_negotiationFragment);
        } catch (Exception e) {
          Log.e("HomeFragment", "Lỗi điều hướng: ", e);
        }
      });
    } else {
      Log.w("HomeFragment", "Không tìm thấy View với ID: card_negotiation");
    }

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
        ProductCard product = new ProductCard();
        product.setTitle("Vòng hoa hướng dương vàng");
        product.setPrice("₫8.500.000");
        product.setQuantity(1);
        product.setProductType(ProductCard.ProductType.AUCTION);
        product.setTimeRemaining("02:39:12"); //

        Bundle bundle = new Bundle();
        bundle.putSerializable("product", product);

        Navigation.findNavController(v).navigate(
                R.id.action_home_navigation_detail_product,
                bundle
        );
      });
    }

    if (cardNegotiation != null) {
      cardNegotiation.setOnClickListener(v -> {
        try {
          NavController navController = Navigation.findNavController(v);
          navController.navigate(R.id.action_homeFragment_to_negotiationFragment);
        } catch (Exception e) {
          Log.e("HomeFragment", "Lỗi điều hướng: ", e);
        }
      });
    } else {
      Log.w("HomeFragment", "Không tìm thấy View với ID: card_negotiation");
    }
  }
}
