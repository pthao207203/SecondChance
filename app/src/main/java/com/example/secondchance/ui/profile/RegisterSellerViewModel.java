package com.example.secondchance.ui.profile;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.secondchance.data.model.ShopProfileResponse;
import com.example.secondchance.data.model.dto.BecomeSellerRequest;
import com.example.secondchance.data.model.dto.PickupAddressRequest;
import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.data.remote.RetrofitProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterSellerViewModel extends ViewModel {

    private final MeApi meApi;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _registrationSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> registrationSuccess = _registrationSuccess;

    public RegisterSellerViewModel() {
        this.meApi = RetrofitProvider.me();
    }

    public void submitRegistration(
            String fullName, String cccd, String shopName,
            String contactName, String contactPhone, String street,
            String province, String city, String postalCode
    ) {
        _isLoading.setValue(true);

        String placeholderFrontUrl = "https://cdn.example.com/id_front.jpg";
        String placeholderBackUrl = "https://cdn.example.com/id_back.jpg";

        PickupAddressRequest address = new PickupAddressRequest(
                street, city, province, postalCode, contactName, contactPhone
        );

        BecomeSellerRequest request = new BecomeSellerRequest(
                fullName, shopName, cccd,
                placeholderFrontUrl, placeholderBackUrl,
                address
        );

        meApi.registerAsSeller(request).enqueue(new Callback<ShopProfileResponse>() {
            @Override
            public void onResponse(Call<ShopProfileResponse> call, Response<ShopProfileResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Thành công!
                    Log.d("RegisterSellerVM", "Đăng ký thành công!");
                    _registrationSuccess.setValue(true);
                } else {
                    // Lỗi từ server
                    String errorMsg = "Đăng ký thất bại: " + response.message();
                    Log.e("RegisterSellerVM", "Lỗi API: " + response.code() + " - " + errorMsg);
                    _errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ShopProfileResponse> call, Throwable t) {
                // Lỗi mạng
                _isLoading.setValue(false);
                Log.e("RegisterSellerVM", "Lỗi mạng: ", t);
                _errorMessage.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void onRegistrationHandled() {
        _registrationSuccess.setValue(false);
    }
}