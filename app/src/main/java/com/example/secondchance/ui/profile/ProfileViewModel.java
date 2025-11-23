package com.example.secondchance.ui.profile;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.secondchance.data.model.AddressData;
import com.example.secondchance.data.model.UserData;
import com.example.secondchance.data.model.UserProfileResponse;

import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.data.remote.RetrofitProvider;

import com.example.secondchance.ui.profile.AddressItem;
import com.example.secondchance.ui.profile.PaymentMethodItem;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<List<AddressItem>> addressListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<PaymentMethodItem>> paymentMethodListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Uri> avatarUriLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> shopNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> phoneLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private MeApi meApi;

    public ProfileViewModel() {
        this.meApi = RetrofitProvider.me();
    }


    public void fetchUserProfile() {
        Call<UserProfileResponse> call = meApi.getUserProfile();

        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    UserData data = response.body().getData();
                    if (data == null) {
                        errorMessage.setValue("Không tìm thấy dữ liệu người dùng.");
                        return;
                    }

                    // Cập nhật LiveData
                    nameLiveData.setValue(data.getName());
                    phoneLiveData.setValue(data.getPhone());
                    emailLiveData.setValue(data.getMail());

                    // Xử lý địa chỉ
                    if (data.getAddress() != null) {
                        AddressData apiAddress = data.getAddress();

                        String formattedAddress = formatAddress(apiAddress);
                        AddressItem uiAddress = new AddressItem(
                                apiAddress.getName(),
                                apiAddress.getPhone(),
                                formattedAddress,
                                apiAddress.isDefault()
                        );

                        List<AddressItem> newList = new ArrayList<>();
                        newList.add(uiAddress);
                        addressListLiveData.setValue(newList);
                    } else {
                        addressListLiveData.setValue(new ArrayList<>());
                    }

                } else {
                    Log.e("ProfileViewModel", "Lỗi API: " + response.code() + " - " + response.message());
                    errorMessage.setValue("Không thể tải hồ sơ: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e("ProfileViewModel", "Lỗi mạng: " + t.getMessage(), t);
                errorMessage.setValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    private String formatAddress(AddressData address) {
        if (address == null) {
            return "Chưa có địa chỉ";
        }

        StringBuilder sb = new StringBuilder();

        if (address.getStreet() != null && !address.getStreet().isEmpty()) {
            sb.append(address.getStreet()).append(", ");
        }
        if (address.getWard() != null && !address.getWard().isEmpty()) {
            sb.append(address.getWard()).append(", ");
        }
        // Data 1 của bạn có district, data 2 không có, nên tôi thêm "district" vào đây
        if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
            sb.append(address.getDistrict()).append(", ");
        }
        if (address.getProvince() != null && !address.getProvince().isEmpty()) {
            sb.append(address.getProvince());
        }

        String result = sb.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        return result.isEmpty() ? "Chưa có địa chỉ" : result;
    }


    // PROFILE INFO
    public LiveData<String> getName() {
        return nameLiveData;
    }

    public void setName(String name) {
        nameLiveData.setValue(name);
    }

    public LiveData<String> getPhone() {
        return phoneLiveData;
    }

    public void setPhone(String phone) {
        phoneLiveData.setValue(phone);
    }

    public LiveData<String> getEmail() {
        return emailLiveData;
    }

    public void setEmail(String email) {
        emailLiveData.setValue(email);
    }

    public LiveData<Uri> getAvatarUri() {
        return avatarUriLiveData;
    }

    public void setAvatarUri(Uri uri) {
        avatarUriLiveData.setValue(uri);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    //ADDRESS METHODS
    public LiveData<List<AddressItem>> getAddressList() {
        return addressListLiveData;
    }

    public void addAddress(AddressItem newAddress) {
        List<AddressItem> currentList = addressListLiveData.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }

        if (newAddress.isDefault()) {
            for (AddressItem item : currentList) {
                item.setDefault(false);
            }
        }

        currentList.add(newAddress);
        addressListLiveData.setValue(currentList);
    }

    public void updateAddress(int position, AddressItem updatedAddress) {
        List<AddressItem> currentList = addressListLiveData.getValue();
        if (currentList == null || position < 0 || position >= currentList.size()) {
            return;
        }

        if (updatedAddress.isDefault()) {
            for (int i = 0; i < currentList.size(); i++) {
                if (i != position) {
                    currentList.get(i).setDefault(false);
                }
            }
        }

        currentList.set(position, updatedAddress);
        addressListLiveData.setValue(currentList);
    }

    public void removeAddress(int position) {
        List<AddressItem> currentList = addressListLiveData.getValue();
        if (currentList == null || position < 0 || position >= currentList.size()) {
            return;
        }

        currentList.remove(position);
        addressListLiveData.setValue(currentList);
    }

    public AddressItem getDefaultAddress() {
        List<AddressItem> currentList = addressListLiveData.getValue();
        if (currentList == null) {
            return null;
        }

        for (AddressItem item : currentList) {
            if (item.isDefault()) {
                return item;
            }
        }
        return currentList.isEmpty() ? null : currentList.get(0);
    }

    // PAYMENT METHOD METHODS
    public LiveData<List<PaymentMethodItem>> getPaymentMethodList() {
        return paymentMethodListLiveData;
    }

    public void addPaymentMethod(PaymentMethodItem newPaymentMethod) {
        List<PaymentMethodItem> currentList = paymentMethodListLiveData.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }

        if (newPaymentMethod.isDefault()) {
            for (PaymentMethodItem item : currentList) {
                item.setDefault(false);
            }
        }

        currentList.add(newPaymentMethod);
        paymentMethodListLiveData.setValue(currentList);
    }

    public void updatePaymentMethod(int position, PaymentMethodItem updatedPaymentMethod) {
        List<PaymentMethodItem> currentList = paymentMethodListLiveData.getValue();
        if (currentList == null || position < 0 || position >= currentList.size()) {
            return;
        }

        if (updatedPaymentMethod.isDefault()) {
            for (int i = 0; i < currentList.size(); i++) {
                if (i != position) {
                    currentList.get(i).setDefault(false);
                }
            }
        }

        currentList.set(position, updatedPaymentMethod);
        paymentMethodListLiveData.setValue(currentList);
    }

    public void removePaymentMethod(int position) {
        List<PaymentMethodItem> currentList = paymentMethodListLiveData.getValue();
        if (currentList == null || position < 0 || position >= currentList.size()) {
            return;
        }

        currentList.remove(position);
        paymentMethodListLiveData.setValue(currentList);
    }

    public PaymentMethodItem getDefaultPaymentMethod() {
        List<PaymentMethodItem> currentList = paymentMethodListLiveData.getValue();
        if (currentList == null) {
            return null;
        }

        for (PaymentMethodItem item : currentList) {
            if (item.isDefault()) {
                return item;
            }
        }
        return currentList.isEmpty() ? null : currentList.get(0);
    }
}