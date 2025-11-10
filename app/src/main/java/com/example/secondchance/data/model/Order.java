package com.example.secondchance.data.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;
import android.util.Log;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Order extends OrderWrapper implements Serializable {

    @SerializedName("_id")
    public String id;

    @SerializedName("orderItems")
    public List<OrderItem> items;

    @SerializedName("orderTotalAmount")
    public long totalAmount;

    @SerializedName("orderStatus")
    @JsonAdapter(StatusDeserializer.class)
    public int status; // 0, 1, 2, 3, 4

    @SerializedName("orderPaymentMethod")
    public String paymentMethod;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("isReviewed")
    public boolean isReviewed;

    @SerializedName("returnRequest")
    public ReturnRequestData returnRequest;

    public OrderItem getFirstItem() {
        if (items != null && !items.isEmpty()) {
            return items.get(0);
        }
        return null;
    }

    public String getTitle() {
        OrderItem item = getFirstItem();
        return (item != null && item.name != null) ? item.name : "Không có tên";
    }

    public String getPrice() {
        OrderItem item = getFirstItem();
        long price = (item != null) ? item.price : 0;
        if (price == 0 && totalAmount > 0) {
            price = totalAmount;
        }
        return String.format(Locale.GERMAN, "%,d", price);
    }

    public String getQuantity() {
        if (items == null || items.isEmpty()) return "x0";
        int totalQty = 0;
        for (OrderItem item : items) {
            totalQty += item.quantity;
        }
        return "x" + totalQty;
    }

    public String getSubtitle() {
        return (paymentMethod != null && paymentMethod.equals("cod")) ? "Thanh toán COD" : "Thanh toán Wallet";
    }

    public String getDate() {
        if (createdAt == null || createdAt.isEmpty()) {
            return "Không rõ ngày";
        }
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inFormat.parse(createdAt);

            SimpleDateFormat outFormat;

            if (status == 0) { // "Chưa xác nhận"
                outFormat = new SimpleDateFormat("'Đã tạo ngày:' dd/MM/yyyy", Locale.getDefault());
            } else if (status == 2) { // "Đã mua"
                outFormat = new SimpleDateFormat("'Đã giao' dd/MM/yyyy", Locale.getDefault());
            } else if (status == 3) { // "Đã hủy"
                outFormat = new SimpleDateFormat("'Đã hủy' dd/MM/yyyy", Locale.getDefault());
            } else if (status == 4) { // "Hoàn trả"
                outFormat = new SimpleDateFormat("'Đã hoàn trả' dd/MM/yyyy", Locale.getDefault());
            } else { // (Status 1 - Đang giao)
                outFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            }

            outFormat.setTimeZone(TimeZone.getDefault());
            return outFormat.format(date);

        } catch (ParseException e) {
            return "Ngày không hợp lệ";
        }
    }

    public String getStatusText() {
        if (status == 2) { // "Đã mua"
            return isReviewed ? "Đã đánh giá" : "Chưa đánh giá";
        }

        if (status == 4 && returnRequest != null) {
            return returnRequest.getStatusText();
        }

        switch (status) {
            case 0: return "Chưa xác nhận";
            case 1: return "Đang giao";
            case 3: return "Đã hủy";
            case 4: return "Hoàn trả";
            default: return "Không rõ";
        }
    }

    public String getDescription() {
        if (status == 0) return null;
        return "Đơn hàng này không thể hủy";
    }

    public OrderType getType() {
        if (status == 0) return OrderType.UNCONFIRMED;
        return OrderType.CONFIRMED_FIXED;
    }

    public enum OrderType { UNCONFIRMED, CONFIRMED_FIXED, CONFIRMED_AUCTION }
    public enum RefundStatus { NOT_CONFIRMED, CONFIRMED, REJECTED, SUCCESSFUL }
    public enum DeliveryOverallStatus { PACKAGED, AT_POST_OFFICE, DELIVERING, DELIVERED }

    public boolean isEvaluated() {
        return isReviewed;
    }
    public RefundStatus getRefundStatus() {
        if (status != 4 || returnRequest == null) return null;
        return returnRequest.getStatus();
    }
    public DeliveryOverallStatus getDeliveryStatus() {
        OrderWrapper.ShipmentData shipment = new OrderWrapper.ShipmentData();
      
      return switch (shipment.currentStatus) {
        case 2, 3 -> DeliveryOverallStatus.AT_POST_OFFICE;
        case 4 -> DeliveryOverallStatus.DELIVERING;
        case 5 -> DeliveryOverallStatus.DELIVERED;
        default -> DeliveryOverallStatus.PACKAGED;
      };
    }
    public String getId() { return id; }
    public static class ReturnRequestData implements Serializable {
        @SerializedName("status")
        public String status; // "completed", "pending", "rejected"

        public RefundStatus getStatus() {
            if (status == null) return RefundStatus.NOT_CONFIRMED;
            switch (status) {
                case "completed": return RefundStatus.SUCCESSFUL;
                case "approved":  return RefundStatus.CONFIRMED;
                case "rejected":  return RefundStatus.REJECTED;
                default:          return RefundStatus.NOT_CONFIRMED;
            }
        }

        public String getStatusText() {
            switch (getStatus()) {
                case SUCCESSFUL: return "Hoàn trả thành công";
                case CONFIRMED:  return "Đã xác nhận";
                case REJECTED:   return "Đã từ chối";
                default:         return "Chưa xác nhận";
            }
        }
    }

    public static class StatusDeserializer implements JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                if (json.isJsonPrimitive()) {
                    if (json.getAsJsonPrimitive().isNumber()) {
                        return json.getAsInt();
                    } else if (json.getAsJsonPrimitive().isString()) {
                        String statusStr = json.getAsString();
                        if ("pending".equalsIgnoreCase(statusStr)) {
                            return 0;
                        }
                        if ("shipping".equalsIgnoreCase(statusStr)) {
                            return 1;
                        }
                        if ("delivered".equalsIgnoreCase(statusStr)) {
                            return 2;
                        }
                        if ("cancelled".equalsIgnoreCase(statusStr)) {
                            return 3;
                        }
                        if ("returned".equalsIgnoreCase(statusStr)) {
                            return 4;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("StatusDeserializer", "Error parsing status", e);
            }
            return 0;
        }
    }
}
