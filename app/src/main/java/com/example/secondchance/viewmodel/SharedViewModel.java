package com.example.secondchance.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.viewpager2.widget.ViewPager2;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<String> currentTitle = new MutableLiveData<>();
    public LiveData<String> getCurrentTitle() {
        return currentTitle;
    }
    public void updateTitle(String newTitle) {
        currentTitle.setValue(newTitle);
    }

    // Xử lý yêu cầu chuyển Tab
    private final MutableLiveData<Integer> requestedTab = new MutableLiveData<>();
    public LiveData<Integer> getRequestedTab() {
        return requestedTab;
    }
    public void requestTabChange(Integer tabIndex) {
        requestedTab.setValue(tabIndex);
    }
    public void clearTabRequest() {
        requestedTab.setValue(null);
    }

    // Xử lý Refresh danh sách
    private final MutableLiveData<Boolean> refreshLists = new MutableLiveData<>();
    public LiveData<Boolean> getRefreshLists() {
        return refreshLists;
    }
    public void refreshOrderLists() {
        refreshLists.setValue(true);
    }
    public void clearRefreshRequest() {
        refreshLists.setValue(null);
    }

    // Quản lý ViewPager
    private final MutableLiveData<ViewPager2> viewPagerReady = new MutableLiveData<>();
    private final String[] tabTitles = new String[]{"Xác nhận", "Đang giao", "Đã mua", "Đã hủy", "Hoàn trả"};

    public LiveData<ViewPager2> getViewPager() {
        return viewPagerReady;
    }
    public void setViewPager(ViewPager2 viewPager) {
        viewPagerReady.setValue(viewPager);
    }
    public String[] getTabTitles() {
        return tabTitles;
    }
    public void clearViewPager() {
        viewPagerReady.setValue(null);
    }
}
