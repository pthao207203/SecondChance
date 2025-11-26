package com.example.secondchance.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Class hỗ trợ thông tin về cơ cấu hành chính Việt Nam 2025
 * Theo Nghị quyết 202/2025/QH15
 */
public class AdministrativeHelper {

    // Thông tin sáp nhập
    private static final Map<String, String> MERGE_INFO = new HashMap<>();

    static {
        // Ghi chú các tỉnh đã sáp nhập
        MERGE_INFO.put("Hải Phòng", "Sáp nhập Hải Dương");
        MERGE_INFO.put("Huế", "Từ Thừa Thiên Huế");
        MERGE_INFO.put("Đà Nẵng", "Sáp nhập Quảng Nam");
        MERGE_INFO.put("Hồ Chí Minh", "Sáp nhập Bà Rịa - Vũng Tàu, Bình Dương");
        MERGE_INFO.put("Cần Thơ", "Sáp nhập Hậu Giang, Kiên Giang, Sóc Trăng");
        MERGE_INFO.put("Tuyên Quang", "Sáp nhập Hà Giang");
        MERGE_INFO.put("Lào Cai", "Sáp nhập Yên Bái");
        MERGE_INFO.put("Thái Nguyên", "Sáp nhập Bắc Kạn");
        MERGE_INFO.put("Phú Thọ", "Sáp nhập Vĩnh Phúc, Hòa Bình");
        MERGE_INFO.put("Bắc Ninh", "Sáp nhập Bắc Giang");
        MERGE_INFO.put("Hưng Yên", "Sáp nhập Thái Bình");
        MERGE_INFO.put("Ninh Bình", "Sáp nhập Hà Nam, Nam Định");
        MERGE_INFO.put("Quảng Trị", "Sáp nhập Quảng Bình");
        MERGE_INFO.put("Quảng Ngãi", "Sáp nhập Kon Tum");
        MERGE_INFO.put("Gia Lai", "Sáp nhập Đắk Nông");
        MERGE_INFO.put("Khánh Hòa", "Sáp nhập Phú Yên, Ninh Thuận");
        MERGE_INFO.put("Lâm Đồng", "Sáp nhập Bình Thuận");
        MERGE_INFO.put("Đắk Lắk", "Sáp nhập một phần Đắk Nông");
        MERGE_INFO.put("Đồng Nai", "Sáp nhập Bình Phước");
        MERGE_INFO.put("Tây Ninh", "Sáp nhập Long An");
        MERGE_INFO.put("Vĩnh Long", "Sáp nhập Trà Vinh");
        MERGE_INFO.put("Đồng Tháp", "Sáp nhập Tiền Giang");
        MERGE_INFO.put("Cà Mau", "Sáp nhập Bạc Liêu");
        MERGE_INFO.put("An Giang", "Sáp nhập Bến Tre");
    }

    // 11 tỉnh/thành phố giữ nguyên
    private static final String[] UNCHANGED_PROVINCES = {
            "Hà Nội", "Huế", "Cao Bằng", "Điện Biên", "Hà Tĩnh",
            "Lai Châu", "Lạng Sơn", "Nghệ An", "Quảng Ninh",
            "Thanh Hóa", "Sơn La"
    };

    /**
     * Thống kê cơ bản về cơ cấu hành chính
     */
    public static class Statistics {
        public static final int TOTAL_PROVINCES = 34;
        public static final int TOTAL_CITIES = 6;
        public static final int TOTAL_PROVINCIAL_UNITS = 28;
        public static final int TOTAL_WARDS = 3321;
        public static final int TOTAL_XA = 2636;
        public static final int TOTAL_PHUONG = 672;
        public static final int TOTAL_SPECIAL_DISTRICTS = 13;
        public static final int UNCHANGED_PROVINCES_COUNT = 11;
        public static final int MERGED_PROVINCES_COUNT = 23;

        // 13 Đặc khu
        public static final String[] SPECIAL_DISTRICTS = {
                "Vân Đồn", "Cô Tô", "Cát Hải", "Bạch Long Vĩ",
                "Cồn Cỏ", "Lý Sơn", "Hoàng Sa", "Trường Sa",
                "Phú Quý", "Côn Đảo", "Kiên Hải", "Phú Quốc", "Thổ Châu"
        };
    }

    /**
     * Lấy thông tin sáp nhập của tỉnh
     */
    public static String getMergeInfo(String province) {
        return MERGE_INFO.getOrDefault(province, "Giữ nguyên");
    }

    /**
     * Kiểm tra tỉnh có giữ nguyên không
     */
    public static boolean isUnchanged(String province) {
        for (String p : UNCHANGED_PROVINCES) {
            if (p.equals(province)) return true;
        }
        return false;
    }

    /**
     * Lấy thông tin chi tiết về tỉnh/thành phố
     */
    public static String getProvinceInfo(String province) {
        StringBuilder info = new StringBuilder();
        info.append(ProvinceProvider.getFullName(province));
        info.append("\n");
        info.append("Loại: ").append(ProvinceProvider.getProvinceType(province));
        info.append("\n");

        String mergeInfo = getMergeInfo(province);
        if (!mergeInfo.equals("Giữ nguyên")) {
            info.append("Sáp nhập: ").append(mergeInfo);
            info.append("\n");
        }

        int wardCount = WardProvider.getWardCount(province);
        if (wardCount > 0) {
            info.append("Số xã/phường/đặc khu: ").append(wardCount);
        }

        return info.toString();
    }

    /**
     * Lấy danh sách các đặc khu
     */
    public static String[] getSpecialDistricts() {
        return Statistics.SPECIAL_DISTRICTS;
    }

    /**
     * Thông tin về thời điểm áp dụng
     */
    public static class EffectiveDate {
        public static final String RESOLUTION_DATE = "12/06/2025";
        public static final String EFFECTIVE_DATE = "01/07/2025";
        public static final String RESOLUTION_NUMBER = "202/2025/QH15";

        public static String getInfo() {
            return "Nghị quyết " + RESOLUTION_NUMBER +
                    "\nNgày ban hành: " + RESOLUTION_DATE +
                    "\nCó hiệu lực: " + EFFECTIVE_DATE;
        }
    }
}