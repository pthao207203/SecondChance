// File: SellerViewModel.java
package com.example.secondchance.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;


public class SellerViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> isSeller = new MutableLiveData<>(false);
    private static final String KEY_IS_SELLER = "is_seller";

    public SellerViewModel(@NonNull Application application) {
        super(application);
        // Khôi phục trạng thái từ SharedPreferences
        boolean saved = PreferenceManager.getDefaultSharedPreferences(application)
                .getBoolean(KEY_IS_SELLER, false);
        isSeller.setValue(saved);
    }

    public LiveData<Boolean> getIsSeller() {
        return isSeller;
    }

    public void setSeller(boolean value) {
        isSeller.setValue(value);
        // Lưu vào SharedPreferences để giữ trạng thái sau khi thoát app
        PreferenceManager.getDefaultSharedPreferences(getApplication())
                .edit()
                .putBoolean(KEY_IS_SELLER, value)
                .apply();
    }
}
