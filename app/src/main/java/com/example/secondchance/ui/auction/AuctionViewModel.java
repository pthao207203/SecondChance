package com.example.secondchance.ui.auction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.secondchance.dto.response.AuctionListResponse;

import java.util.List;

public class AuctionViewModel extends ViewModel {
  private final MutableLiveData<List<AuctionListResponse.Item>> _items = new MutableLiveData<>();
  public LiveData<List<AuctionListResponse.Item>> items = _items;
  
  void setItems(List<AuctionListResponse.Item> newItems) {
    _items.postValue(newItems);
  }
}
