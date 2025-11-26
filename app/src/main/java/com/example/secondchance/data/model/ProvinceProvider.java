package com.example.secondchance.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Danh sách 34 tỉnh thành Việt Nam theo Nghị quyết 202/2025/QH15
 * Có hiệu lực từ ngày 12/6/2025, chính thức hoạt động từ 01/7/2025
 */
public class ProvinceProvider {
    public static List<String> getProvinces() {
        List<String> provinces = new ArrayList<>();

        // 6 THÀNH PHỐ TRỰC THUỘC TRUNG ƯƠNG
        provinces.add("Hà Nội");
        provinces.add("Hải Phòng");
        provinces.add("Huế");
        provinces.add("Đà Nẵng");
        provinces.add("Hồ Chí Minh");
        provinces.add("Cần Thơ");

        // 28 TỈNH
        // Miền Bắc
        provinces.add("Lai Châu");
        provinces.add("Điện Biên");
        provinces.add("Sơn La");
        provinces.add("Lạng Sơn");
        provinces.add("Cao Bằng");
        provinces.add("Tuyên Quang");
        provinces.add("Lào Cai");
        provinces.add("Thái Nguyên");
        provinces.add("Phú Thọ");
        provinces.add("Bắc Ninh");
        provinces.add("Hưng Yên");
        provinces.add("Ninh Bình");
        provinces.add("Quảng Ninh");
        provinces.add("Thanh Hóa");
        provinces.add("Nghệ An");
        provinces.add("Hà Tĩnh");

        // Miền Trung
        provinces.add("Quảng Trị");
        provinces.add("Quảng Ngãi");
        provinces.add("Gia Lai");
        provinces.add("Khánh Hòa");
        provinces.add("Lâm Đồng");
        provinces.add("Đắk Lắk");

        // Miền Nam
        provinces.add("Đồng Nai");
        provinces.add("Tây Ninh");
        provinces.add("Vĩnh Long");
        provinces.add("Đồng Tháp");
        provinces.add("Cà Mau");
        provinces.add("An Giang");

        Collections.sort(provinces);
        return provinces;
    }

    /**
     * Kiểm tra xem tỉnh có phải là Thành phố trực thuộc TW hay không
     */
    public static boolean isCentralCity(String province) {
        return province.equals("Hà Nội") ||
                province.equals("Hải Phòng") ||
                province.equals("Huế") ||
                province.equals("Đà Nẵng") ||
                province.equals("Hồ Chí Minh") ||
                province.equals("Cần Thơ");
    }

    /**
     * Lấy loại đơn vị hành chính
     */
    public static String getProvinceType(String province) {
        return isCentralCity(province) ? "Thành phố trực thuộc TW" : "Tỉnh";
    }

    /**
     * Lấy tên đầy đủ của tỉnh/thành phố
     */
    public static String getFullName(String province) {
        if (isCentralCity(province)) {
            return "Thành phố " + province;
        }
        return "Tỉnh " + province;
    }
}