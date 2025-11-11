package com.example.secondchance.ui.profile;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.example.secondchance.data.model.ShopData;
import com.example.secondchance.data.model.ShopProfileResponse;
import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.data.remote.RetrofitProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SellerViewModel extends AndroidViewModel {

    private final MutableLiveData<String> shopName = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> isSeller = new MutableLiveData<>(false);
    private static final String KEY_IS_SELLER = "is_seller";
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MeApi meApi;

    public SellerViewModel(@NonNull Application application) {
        super(application);
        this.meApi = RetrofitProvider.me();

        boolean saved = PreferenceManager.getDefaultSharedPreferences(application)
                .getBoolean(KEY_IS_SELLER, false);
        isSeller.setValue(saved);

    }

    public LiveData<String> getShopName() {
        return shopName;
    }

    public LiveData<Boolean> getIsSeller() {
        return isSeller;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setSeller(boolean value) {
        isSeller.setValue(value);
        PreferenceManager.getDefaultSharedPreferences(getApplication())
                .edit()
                .putBoolean(KEY_IS_SELLER, value)
                .apply();
    }


    public void fetchSellerProfile() {

        Call<ShopProfileResponse> call = meApi.getShopProfile();

        call.enqueue(new Callback<ShopProfileResponse>() {
            @Override
            public void onResponse(Call<ShopProfileResponse> call, Response<ShopProfileResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    ShopData data = response.body().getData();
                    if (data != null) {
                        shopName.setValue(data.getName());
                        setSeller(true);
                    } else {

                        shopName.setValue("");
                        setSeller(false);
                    }


                } else if (response.code() == 404) {

                    shopName.setValue(""); //
                    setSeller(false);


                } else {
                    shopName.setValue("");
                    setSeller(false);
                    errorMessage.setValue("Lỗi khi kiểm tra shop: " + response.message());
                    Log.e("SellerViewModel", "Lỗi API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ShopProfileResponse> call, Throwable t) {
                shopName.setValue("");
                setSeller(false);
                errorMessage.setValue("Lỗi mạng: " + t.getMessage());
                Log.e("SellerViewModel", "Lỗi mạng: ", t);
            }
        });
    }
}