package com.example.secondchance.ui.profile;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.secondchance.data.model.AddressData;
import com.example.secondchance.data.model.UserData;
import com.example.secondchance.data.model.UserProfileResponse;
import com.example.secondchance.dto.request.AddressRequest;
import com.example.secondchance.dto.request.BankRequest;
import com.example.secondchance.dto.request.PasswordUpdateRequest;
import com.example.secondchance.dto.request.ProfileUpdateRequest;
import com.example.secondchance.dto.response.AddressItemResponse;
import com.example.secondchance.dto.response.AddressListResponse;
import com.example.secondchance.dto.response.BankItemResponse;
import com.example.secondchance.dto.response.BankListResponse;

import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.data.remote.RetrofitProvider;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {
    private static final String TAG = "ProfileViewModel";

    private final MutableLiveData<List<AddressItem>> addressListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<AddressItem> addressDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<PaymentMethodItem>> paymentMethodListLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Uri> avatarUriLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> phoneLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    // Add LiveData to notify success/failure of add/edit operations
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    // LiveData for bank detail (when edit)
    private final MutableLiveData<PaymentMethodItem> bankDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> balanceLiveData = new MutableLiveData<>();

    private MeApi meApi;

    public ProfileViewModel() {
        this.meApi = RetrofitProvider.me();
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public void resetOperationSuccess() {
        operationSuccess.setValue(null);
    }

    public LiveData<PaymentMethodItem> getBankDetail() {
        return bankDetailLiveData;
    }

    public LiveData<AddressItem> getAddressDetail() {
        return addressDetailLiveData;
    }

    public LiveData<String> getBalance() {
        return balanceLiveData;
    }

    public void fetchUserProfile() {
        Log.d(TAG, "fetchUserProfile: Bắt đầu gọi API lấy profile...");
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

                    nameLiveData.setValue(data.getName());
                    phoneLiveData.setValue(data.getPhone());
                    emailLiveData.setValue(data.getMail());

                    // Set avatar
                    if (data.getAvatar() != null) {
                        avatarUriLiveData.setValue(Uri.parse(data.getAvatar()));
                    }

                    // Format balance
                    long balance = data.getWalletBalance();
                    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                    balanceLiveData.setValue(nf.format(balance));

                    // Fetch addresses
                    fetchAddressList();

                    // Gọi fetchBankList không tham số
                    fetchBankList();
                } else {
                    errorMessage.setValue("Không thể tải hồ sơ: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    public void updateUserProfile(String name, String phone, String email) {
        ProfileUpdateRequest request = new ProfileUpdateRequest(name, phone, email);
        Call<UserProfileResponse> call = meApi.updateProfile(request);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserData data = response.body().getData();
                    if (data != null) {
                        nameLiveData.setValue(data.getName());
                        phoneLiveData.setValue(data.getPhone());
                        emailLiveData.setValue(data.getMail());
                    }
                    operationSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Cập nhật hồ sơ thất bại: " + response.message());
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi cập nhật hồ sơ: " + t.getMessage());
                operationSuccess.setValue(false);
            }
        });
    }

    public void updatePassword(String oldPassword, String newPassword) {
        PasswordUpdateRequest request = new PasswordUpdateRequest(oldPassword, newPassword);
        Call<UserProfileResponse> call = meApi.updatePassword(request);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    operationSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Cập nhật mật khẩu thất bại: " + response.message());
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi cập nhật mật khẩu: " + t.getMessage());
                operationSuccess.setValue(false);
            }
        });
    }

    public void fetchAddressList() {
        Call<AddressListResponse> call = meApi.getAddresses();
        call.enqueue(new Callback<AddressListResponse>() {
            @Override
            public void onResponse(Call<AddressListResponse> call, Response<AddressListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    AddressListResponse.Data data = response.body().data;
                    if (data != null && data.items != null) {
                        List<AddressItem> uiList = new ArrayList<>();
                        for (AddressData item : data.items) {
                            AddressData.Location loc = item.getLocation();
                            double lat = loc != null ? loc.lat : 0;
                            double lng = loc != null ? loc.lng : 0;

                            uiList.add(new AddressItem(
                                    item.getId(),
                                    item.getName(),
                                    item.getPhone(),
                                    item.getStreet(),
                                    item.getWard(),
                                    item.getProvince(),
                                    item.getCountry(),
                                    item.getLabel(),
                                    item.isDefault(),
                                    lat,
                                    lng
                            ));
                        }
                        addressListLiveData.setValue(uiList);
                    }
                } else {
                    errorMessage.setValue("Không thể tải danh sách địa chỉ");
                }
            }

            @Override
            public void onFailure(Call<AddressListResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi tải danh sách địa chỉ: " + t.getMessage());
            }
        });
    }

    public void fetchAddressDetail(String addressId) {
        Call<AddressItemResponse> call = meApi.getAddressDetail(addressId);
        call.enqueue(new Callback<AddressItemResponse>() {
            @Override
            public void onResponse(Call<AddressItemResponse> call, Response<AddressItemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    AddressData data = response.body().data;
                    if (data != null) {
                         AddressData.Location loc = data.getLocation();
                         double lat = loc != null ? loc.lat : 0;
                         double lng = loc != null ? loc.lng : 0;
                         AddressItem item = new AddressItem(
                                data.getId(),
                                data.getName(),
                                data.getPhone(),
                                data.getStreet(),
                                data.getWard(),
                                data.getProvince(),
                                data.getCountry(),
                                data.getLabel(),
                                data.isDefault(),
                                lat,
                                lng
                         );
                         addressDetailLiveData.setValue(item);
                    }
                } else {
                    errorMessage.setValue("Không thể lấy chi tiết địa chỉ");
                }
            }
            @Override
            public void onFailure(Call<AddressItemResponse> call, Throwable t) {
                 errorMessage.setValue("Lỗi mạng khi lấy chi tiết địa chỉ: " + t.getMessage());
            }
        });
    }

    public void addAddress(AddressItem item) {
        // Use default lat/lng for now as per requirement snippet or hardcoded
        double lat = item.getLat() != 0 ? item.getLat() : 10.776889;
        double lng = item.getLng() != 0 ? item.getLng() : 106.700806;

        AddressRequest request = new AddressRequest(
                item.getName(),
                item.getPhone(),
                item.getStreet(),
                item.getWard(),
                item.getProvince(),
                item.getCountry(),
                item.getLabel(),
                item.isDefault(),
                lat,
                lng
        );

        Call<AddressItemResponse> call = meApi.addAddress(request);
        call.enqueue(new Callback<AddressItemResponse>() {
            @Override
            public void onResponse(Call<AddressItemResponse> call, Response<AddressItemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    operationSuccess.setValue(true);
                    fetchAddressList();
                } else {
                    errorMessage.setValue("Thêm địa chỉ thất bại");
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<AddressItemResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi thêm địa chỉ");
                operationSuccess.setValue(false);
            }
        });
    }

    public void updateAddress(String addressId, AddressItem item) {
        double lat = item.getLat() != 0 ? item.getLat() : 10.776888;
        double lng = item.getLng() != 0 ? item.getLng() : 106.700806;

        AddressRequest request = new AddressRequest(
                item.getName(),
                item.getPhone(),
                item.getStreet(),
                item.getWard(),
                item.getProvince(),
                item.getCountry(),
                item.getLabel(),
                item.isDefault(),
                lat,
                lng
        );

        Call<AddressItemResponse> call = meApi.updateAddress(addressId, request);
        call.enqueue(new Callback<AddressItemResponse>() {
            @Override
            public void onResponse(Call<AddressItemResponse> call, Response<AddressItemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    operationSuccess.setValue(true);
                    fetchAddressList();
                } else {
                    errorMessage.setValue("Cập nhật địa chỉ thất bại");
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<AddressItemResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi cập nhật địa chỉ");
                operationSuccess.setValue(false);
            }
        });
    }

    public void removeAddress(String addressId) {
        Call<AddressListResponse> call = meApi.deleteAddress(addressId);
        call.enqueue(new Callback<AddressListResponse>() {
            @Override
            public void onResponse(Call<AddressListResponse> call, Response<AddressListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    AddressListResponse.Data data = response.body().data;
                    if (data != null && data.items != null) {
                        List<AddressItem> uiList = new ArrayList<>();
                        for (AddressData item : data.items) {
                            AddressData.Location loc = item.getLocation();
                            double lat = loc != null ? loc.lat : 0;
                            double lng = loc != null ? loc.lng : 0;

                            uiList.add(new AddressItem(
                                    item.getId(),
                                    item.getName(),
                                    item.getPhone(),
                                    item.getStreet(),
                                    item.getWard(),
                                    item.getProvince(),
                                    item.getCountry(),
                                    item.getLabel(),
                                    item.isDefault(),
                                    lat,
                                    lng
                            ));
                        }
                        addressListLiveData.setValue(uiList);
                    } else {
                        fetchAddressList();
                    }
                    operationSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Xóa địa chỉ thất bại");
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<AddressListResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi xóa địa chỉ");
                operationSuccess.setValue(false);
            }
        });
    }

    public void fetchBankList() {
        Log.d(TAG, "fetchBankList: Bắt đầu gọi API bank (không body)");
        Call<BankListResponse> call = meApi.getBanks();

        call.enqueue(new Callback<BankListResponse>() {
            @Override
            public void onResponse(Call<BankListResponse> call, Response<BankListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    BankListResponse.Data data = response.body().data;
                    if (data != null && data.items != null) {
                        List<PaymentMethodItem> uiList = new ArrayList<>();
                        for (BankListResponse.BankItem item : data.items) {
                            boolean isDef = item.isDefault;
                            uiList.add(new PaymentMethodItem(
                                    item.accountHolder,
                                    item.bankName,
                                    item.accountNumber,
                                    isDef
                            ));
                        }
                        paymentMethodListLiveData.setValue(uiList);
                    }
                } else {
                    Log.e(TAG, "fetchBankList failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<BankListResponse> call, Throwable t) {
                Log.e(TAG, "fetchBankList error: " + t.getMessage(), t);
            }
        });
    }

    // Fetch single bank detail
    public void fetchBankDetail(String bankName) {
        Call<BankItemResponse> call = meApi.getBankDetail(bankName);
        call.enqueue(new Callback<BankItemResponse>() {
            @Override
            public void onResponse(Call<BankItemResponse> call, Response<BankItemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    BankListResponse.BankItem item = response.body().data;
                    if (item != null) {
                         boolean isDef = item.isDefault;
                         PaymentMethodItem pItem = new PaymentMethodItem(
                                    item.accountHolder,
                                    item.bankName,
                                    item.accountNumber,
                                    isDef
                         );
                         bankDetailLiveData.setValue(pItem);
                    }
                } else {
                    errorMessage.setValue("Không lấy được chi tiết ngân hàng");
                }
            }

            @Override
            public void onFailure(Call<BankItemResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi lấy chi tiết ngân hàng: " + t.getMessage());
            }
        });
    }

    public void addPaymentMethod(PaymentMethodItem item) {
        String isDefaultStr = item.isDefault() ? "true" : "false";
        BankRequest request = new BankRequest(item.getBankName(), item.getAccountNumber(), item.getAccountHolderName(), isDefaultStr);
        Call<BankItemResponse> call = meApi.addBank(request);
        call.enqueue(new Callback<BankItemResponse>() {
            @Override
            public void onResponse(Call<BankItemResponse> call, Response<BankItemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    operationSuccess.setValue(true);
                    fetchBankList(); // Refresh list
                } else {
                    errorMessage.setValue("Thêm ngân hàng thất bại: " + response.message());
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<BankItemResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi thêm ngân hàng: " + t.getMessage());
                operationSuccess.setValue(false);
            }
        });
    }

    public void updatePaymentMethod(String oldBankName, PaymentMethodItem item) {
        // Using bankName as ID as per user requirement (API path parameter)
        String isDefaultStr = item.isDefault() ? "true" : "false";
        BankRequest request = new BankRequest(item.getBankName(), item.getAccountNumber(), item.getAccountHolderName(), isDefaultStr);
        // Note: In real implementation, usually we need a unique ID, but user specified bankName in path
        Call<BankItemResponse> call = meApi.updateBank(oldBankName, request);
        call.enqueue(new Callback<BankItemResponse>() {
            @Override
            public void onResponse(Call<BankItemResponse> call, Response<BankItemResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    operationSuccess.setValue(true);
                    fetchBankList();
                } else {
                    errorMessage.setValue("Cập nhật ngân hàng thất bại");
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<BankItemResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi cập nhật ngân hàng");
                operationSuccess.setValue(false);
            }
        });
    }

    public void removePaymentMethod(String bankName) {
        Call<BankListResponse> call = meApi.deleteBank(bankName);
        call.enqueue(new Callback<BankListResponse>() {
            @Override
            public void onResponse(Call<BankListResponse> call, Response<BankListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    // API delete returns updated list (as per prompt description for DELETE)
                     BankListResponse.Data data = response.body().data;
                    if (data != null && data.items != null) {
                        List<PaymentMethodItem> uiList = new ArrayList<>();
                        for (BankListResponse.BankItem item : data.items) {
                            boolean isDef = item.isDefault;
                            uiList.add(new PaymentMethodItem(
                                    item.accountHolder,
                                    item.bankName,
                                    item.accountNumber,
                                    isDef
                            ));
                        }
                        paymentMethodListLiveData.setValue(uiList);
                    } else {
                         // If empty list or format different, just re-fetch
                         fetchBankList();
                    }
                    operationSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Xóa ngân hàng thất bại");
                    operationSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<BankListResponse> call, Throwable t) {
                errorMessage.setValue("Lỗi mạng khi xóa ngân hàng");
                operationSuccess.setValue(false);
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
    public LiveData<String> getName() { return nameLiveData; }
    public void setName(String name) { nameLiveData.setValue(name); }

    public LiveData<String> getPhone() { return phoneLiveData; }
    public void setPhone(String phone) { phoneLiveData.setValue(phone); }

    public LiveData<String> getEmail() { return emailLiveData; }
    public void setEmail(String email) { emailLiveData.setValue(email); }

    public LiveData<Uri> getAvatarUri() { return avatarUriLiveData; }
    public void setAvatarUri(Uri uri) { avatarUriLiveData.setValue(uri); }

    public LiveData<String> getErrorMessage() { return errorMessage; }

    //ADDRESS METHODS
    public LiveData<List<AddressItem>> getAddressList() { return addressListLiveData; }

    public AddressItem getDefaultAddress() {
        List<AddressItem> currentList = addressListLiveData.getValue();
        if (currentList == null) return null;
        for (AddressItem item : currentList) {
            if (item.isDefault()) return item;
        }
        return currentList.isEmpty() ? null : currentList.get(0);
    }

    // PAYMENT METHOD METHODS
    public LiveData<List<PaymentMethodItem>> getPaymentMethodList() { return paymentMethodListLiveData; }

    public PaymentMethodItem getDefaultPaymentMethod() {
        List<PaymentMethodItem> currentList = paymentMethodListLiveData.getValue();
        if (currentList == null) return null;
        for (PaymentMethodItem item : currentList) {
            if (item.isDefault()) return item;
        }
        return currentList.isEmpty() ? null : currentList.get(0);
    }
}
