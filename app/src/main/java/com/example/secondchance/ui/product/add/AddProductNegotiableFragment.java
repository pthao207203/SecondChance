package com.example.secondchance.ui.product.add;

import com.example.secondchance.R;

public class AddProductNegotiableFragment extends BaseAddProductFragment {

    @Override
    protected String getDefaultProductType() {
        return "negotiable";
    }

    @Override
    protected int getSuccessNavigationAction() {
        return R.id.action_addProductNegotiable_to_productList;
    }

    @Override
    protected int getPreviewNavigationAction() {
        return R.id.action_addProductNegotiable_to_preview;
    }
}