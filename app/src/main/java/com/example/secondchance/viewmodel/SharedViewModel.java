package com.example.secondchance.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class SharedViewModel extends ViewModel {


    // Dùng MutableLiveData để có thể thay đổi giá trị
    private final MutableLiveData<String> currentTitle = new MutableLiveData<>();

    // Cung cấp LiveData (không thể thay đổi từ bên ngoài) để Activity quan sát
    public LiveData<String> getCurrentTitle() {
        return currentTitle;
    }

    // Phương thức để Fragment cập nhật tiêu đề mới
    public void updateTitle(String newTitle) {
        currentTitle.setValue(newTitle);
    }
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
    private final String[] tabTitles = new String[]{"Xác nhận", "Đang giao", "Đã mua", "Đã hủy", "Hoàn trả"};
    public String[] getTabTitles() {
        return tabTitles;
    }

}