package com.example.secondchance.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.repo.HomeRepository;

import java.util.List;

public class HomeViewModel extends ViewModel {
  private final HomeRepository repo = new HomeRepository();
  
  private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
  private final MutableLiveData<String>  error   = new MutableLiveData<>(null);
  
  private final MutableLiveData<HomeApi.FeaturedAuction> featured = new MutableLiveData<>();
  private final MutableLiveData<List<HomeApi.Category>>  categories = new MutableLiveData<>();
  private final MutableLiveData<List<HomeApi.SuggestionItem>> suggestions = new MutableLiveData<>();
  
  public LiveData<Boolean> getLoading() { return loading; }
  public LiveData<String>  getError()   { return error; }
  public LiveData<HomeApi.FeaturedAuction> getFeatured() { return featured; }
  public LiveData<List<HomeApi.Category>>  getCategories() { return categories; }
  public LiveData<List<HomeApi.SuggestionItem>> getSuggestions() { return suggestions; }
  
  public void loadHome() {
    loading.setValue(true);
    error.setValue(null);
    repo.fetchHome(new HomeRepository.HomeCallback() {
      @Override public void onSuccess(HomeApi.HomeEnvelope.Data data) {
        featured.postValue(data.featuredAuction);
        categories.postValue(data.categories);
        suggestions.postValue(data.suggestions != null ? data.suggestions.items : null);
        loading.postValue(false);
      }
      @Override public void onError(String message) {
        error.postValue(message);
        loading.postValue(false);
      }
    });
  }
}
