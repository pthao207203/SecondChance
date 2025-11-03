package com.example.secondchance.ui.product.add;

import com.example.secondchance.R;

public class AddProductAuctionFragment extends BaseAddProductFragment {

    @Override
    protected String getDefaultProductType() {
        return "auction";
    }

    @Override
    protected int getSuccessNavigationAction() {
        return R.id.action_addProductAuction_to_productList;
    }

    @Override
    protected int getPreviewNavigationAction() {
        return R.id.action_addProductAuction_to_preview;
    }
}