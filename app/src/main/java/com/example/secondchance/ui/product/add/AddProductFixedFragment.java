package com.example.secondchance.ui.product.add;

import com.example.secondchance.R;

public class AddProductFixedFragment extends BaseAddProductFragment {

    @Override
    protected String getDefaultProductType() {
        return "fixed";
    }

    @Override
    protected int getSuccessNavigationAction() {
        return R.id.action_addProductFixed_to_productList;
    }

    @Override
    protected int getPreviewNavigationAction() {
        return R.id.action_addProductFixed_to_preview;
    }
}