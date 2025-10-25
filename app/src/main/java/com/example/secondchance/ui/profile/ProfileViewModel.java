package com.example.secondchance.ui.profile;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<List<AddressItem>> addressListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<PaymentMethodItem>> paymentMethodListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Uri> avatarUriLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> phoneLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();


    // ==================== PROFILE INFO ====================
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


    // ==================== ADDRESS METHODS ====================

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

    // ==================== PAYMENT METHOD METHODS ====================

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