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
}