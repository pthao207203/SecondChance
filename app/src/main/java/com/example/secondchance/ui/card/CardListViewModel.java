package com.example.secondchance.ui.card;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardListViewModel extends ViewModel {
    private MutableLiveData<List<ProductCard>> productsLiveData = new MutableLiveData<>();

    public CardListViewModel() {
        productsLiveData.setValue(new ArrayList<>());
    }

    public LiveData<List<ProductCard>> getProducts() {
        return productsLiveData;
    }

    public void setProducts(List<ProductCard> products) {
        if (products != null) {
            List<ProductCard> copy = new ArrayList<>(products);
            productsLiveData.setValue(copy);
        } else {
            productsLiveData.setValue(new ArrayList<>());
        }
    }

    public void addProduct(ProductCard product) {
        List<ProductCard> currentList = productsLiveData.getValue();
        if (currentList != null && product != null) {
            currentList.add(product);
            sortProductsByPostTime(currentList);
            productsLiveData.setValue(currentList);
        }
    }

    public void updateAuctionTime(int position, String newTime) {
        List<ProductCard> currentList = productsLiveData.getValue();
        if (currentList != null && position >= 0 && position < currentList.size() && newTime != null && !newTime.isEmpty()) {
            ProductCard product = currentList.get(position);
            if (product.getProductType() == ProductCard.ProductType.AUCTION) {
                product.setTimeRemaining(newTime);
                productsLiveData.setValue(currentList);
            }
        }
    }

    public void removeProduct(int position) {
        List<ProductCard> currentList = productsLiveData.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            productsLiveData.setValue(currentList);
        }
    }

    public int getProductsCount() {
        List<ProductCard> currentList = productsLiveData.getValue();
        return currentList != null ? currentList.size() : 0;
    }

    public ProductCard getProductAt(int position) {
        List<ProductCard> currentList = productsLiveData.getValue();
        return (currentList != null && position >= 0 && position < currentList.size()) ? currentList.get(position) : null;
    }

    private void sortProductsByPostTime(List<ProductCard> products) {
        Collections.sort(products, (p1, p2) -> p2.getPostTime().compareTo(p1.getPostTime()));
    }

    public void clearProducts() {
        productsLiveData.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        productsLiveData.setValue(null);
    }
}
