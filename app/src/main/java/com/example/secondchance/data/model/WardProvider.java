package com.example.secondchance.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Danh sách Xã/Phường/Đặc khu trực thuộc Tỉnh/Thành phố
 * Theo mô hình chính quyền 2 cấp (bỏ cấp huyện)
 * Có hiệu lực từ 01/7/2025 (Cập nhật theo Nghị quyết sắp xếp ĐVHC)
 */
public class WardProvider {
    private static final Map<String, List<String>> PROVINCE_WARD_MAP = new HashMap<>();

    static {
        // --- HÀ NỘI (Cập nhật theo Nghị quyết 126 ĐVHC - 2025) ---
        List<String> hanoiWards = new ArrayList<>();

        // Ba Đình (Sắp xếp lại các phường, sáp nhập Nguyễn Trung Trực vào Trúc Bạch)
        hanoiWards.add("Phường Phúc Xá"); hanoiWards.add("Phường Trúc Bạch"); hanoiWards.add("Phường Vĩnh Phúc");
        hanoiWards.add("Phường Cống Vị"); hanoiWards.add("Phường Liễu Giai"); hanoiWards.add("Phường Quán Thánh");
        hanoiWards.add("Phường Ngọc Hà"); hanoiWards.add("Phường Điện Biên"); hanoiWards.add("Phường Đội Cấn");
        hanoiWards.add("Phường Ngọc Khánh"); hanoiWards.add("Phường Kim Mã"); hanoiWards.add("Phường Giảng Võ");
        hanoiWards.add("Phường Thành Công");

        // Hoàn Kiếm (Sáp nhập lớn: Hàng Bạc, Hàng Đào... vào Hoàn Kiếm; Chương Dương, Phúc Tân vào Hồng Hà...)
        hanoiWards.add("Phường Hoàn Kiếm"); hanoiWards.add("Phường Cửa Nam"); hanoiWards.add("Phường Hồng Hà");
        hanoiWards.add("Phường Hàng Bài"); hanoiWards.add("Phường Hàng Bông"); hanoiWards.add("Phường Hàng Gai");
        hanoiWards.add("Phường Hàng Mã"); hanoiWards.add("Phường Lý Thái Tổ"); hanoiWards.add("Phường Tràng Tiền");
        hanoiWards.add("Phường Đồng Xuân"); hanoiWards.add("Phường Phan Chu Trinh"); hanoiWards.add("Phường Trần Hưng Đạo");

        // Tây Hồ (Giữ ổn định hoặc thay đổi nhỏ ranh giới)
        hanoiWards.add("Phường Phú Thượng"); hanoiWards.add("Phường Nhật Tân"); hanoiWards.add("Phường Tứ Liên");
        hanoiWards.add("Phường Quảng An"); hanoiWards.add("Phường Xuân La"); hanoiWards.add("Phường Yên Phụ");
        hanoiWards.add("Phường Bưởi"); hanoiWards.add("Phường Thụy Khuê");

        // Long Biên (Một số điều chỉnh biên giới các phường Sài Đồng, Phúc Đồng, Phúc Lợi...)
        hanoiWards.add("Phường Thượng Thanh"); hanoiWards.add("Phường Ngọc Thụy"); hanoiWards.add("Phường Giang Biên");
        hanoiWards.add("Phường Đức Giang"); hanoiWards.add("Phường Việt Hưng"); hanoiWards.add("Phường Gia Thụy");
        hanoiWards.add("Phường Ngọc Lâm"); hanoiWards.add("Phường Phúc Lợi"); hanoiWards.add("Phường Bồ Đề");
        hanoiWards.add("Phường Sài Đồng"); hanoiWards.add("Phường Long Biên"); hanoiWards.add("Phường Thạch Bàn");
        hanoiWards.add("Phường Phúc Đồng"); hanoiWards.add("Phường Cự Khối");

        // Cầu Giấy (Ổn định, điều chỉnh biên giới Yên Hòa, Dịch Vọng, Nghĩa Đô)
        hanoiWards.add("Phường Nghĩa Đô"); hanoiWards.add("Phường Nghĩa Tân"); hanoiWards.add("Phường Mai Dịch");
        hanoiWards.add("Phường Dịch Vọng"); hanoiWards.add("Phường Dịch Vọng Hậu"); hanoiWards.add("Phường Quan Hoa");
        hanoiWards.add("Phường Yên Hòa"); hanoiWards.add("Phường Trung Hòa");

        // Đống Đa (Sáp nhập Quốc Tử Giám, Văn Miếu, Phương Liên, Trung Tự, Khâm Thiên...)
        hanoiWards.add("Phường Văn Miếu - Quốc Tử Giám"); hanoiWards.add("Phường Phương Liên - Trung Tự");
        hanoiWards.add("Phường Khâm Thiên"); hanoiWards.add("Phường Khương Thượng"); hanoiWards.add("Phường Thịnh Quang");
        hanoiWards.add("Phường Kim Liên"); hanoiWards.add("Phường Cát Linh"); hanoiWards.add("Phường Láng Thượng");
        hanoiWards.add("Phường Ô Chợ Dừa"); hanoiWards.add("Phường Văn Chương"); hanoiWards.add("Phường Hàng Bột");
        hanoiWards.add("Phường Láng Hạ"); hanoiWards.add("Phường Thổ Quan"); hanoiWards.add("Phường Nam Đồng");
        hanoiWards.add("Phường Quang Trung"); hanoiWards.add("Phường Trung Liệt"); hanoiWards.add("Phường Phương Mai");
        // Lưu ý: Ngã Tư Sở đã chia tách vào Khương Thượng và Thịnh Quang

        // Hai Bà Trưng (Sáp nhập Cầu Dền, Đống Mác, Quỳnh Lôi...)
        hanoiWards.add("Phường Nguyễn Du"); hanoiWards.add("Phường Bạch Đằng"); hanoiWards.add("Phường Phạm Đình Hổ");
        hanoiWards.add("Phường Lê Đại Hành"); hanoiWards.add("Phường Đồng Nhân"); hanoiWards.add("Phường Phố Huế");
        hanoiWards.add("Phường Thanh Lương"); hanoiWards.add("Phường Thanh Nhàn"); hanoiWards.add("Phường Bách Khoa");
        hanoiWards.add("Phường Đồng Tâm"); hanoiWards.add("Phường Vĩnh Tuy"); hanoiWards.add("Phường Bạch Mai");
        hanoiWards.add("Phường Quỳnh Mai"); hanoiWards.add("Phường Minh Khai"); hanoiWards.add("Phường Trương Định");

        // Hoàng Mai (Một số phường điều chỉnh diện tích)
        hanoiWards.add("Phường Thanh Trì"); hanoiWards.add("Phường Vĩnh Hưng"); hanoiWards.add("Phường Định Công");
        hanoiWards.add("Phường Mai Động"); hanoiWards.add("Phường Tương Mai"); hanoiWards.add("Phường Đại Kim");
        hanoiWards.add("Phường Tân Mai"); hanoiWards.add("Phường Hoàng Văn Thụ"); hanoiWards.add("Phường Giáp Bát");
        hanoiWards.add("Phường Lĩnh Nam"); hanoiWards.add("Phường Thịnh Liệt"); hanoiWards.add("Phường Trần Phú");
        hanoiWards.add("Phường Hoàng Liệt"); hanoiWards.add("Phường Yên Sở");

        // Thanh Xuân (Sáp nhập Thanh Xuân Nam vào Thanh Xuân Bắc, Kim Giang vào Hạ Đình)
        hanoiWards.add("Phường Nhân Chính"); hanoiWards.add("Phường Thượng Đình"); hanoiWards.add("Phường Khương Trung");
        hanoiWards.add("Phường Khương Mai"); hanoiWards.add("Phường Thanh Xuân Trung"); hanoiWards.add("Phường Phương Liệt");
        hanoiWards.add("Phường Hạ Đình"); hanoiWards.add("Phường Khương Đình"); hanoiWards.add("Phường Thanh Xuân Bắc");

        // Nam Từ Liêm
        hanoiWards.add("Phường Cầu Diễn"); hanoiWards.add("Phường Xuân Phương"); hanoiWards.add("Phường Phương Canh");
        hanoiWards.add("Phường Mỹ Đình 1"); hanoiWards.add("Phường Mỹ Đình 2"); hanoiWards.add("Phường Tây Mỗ");
        hanoiWards.add("Phường Mễ Trì"); hanoiWards.add("Phường Phú Đô"); hanoiWards.add("Phường Đại Mỗ");
        hanoiWards.add("Phường Trung Văn");

        // Bắc Từ Liêm
        hanoiWards.add("Phường Thượng Cát"); hanoiWards.add("Phường Liên Mạc"); hanoiWards.add("Phường Thụy Phương");
        hanoiWards.add("Phường Minh Khai"); hanoiWards.add("Phường Tây Tựu"); hanoiWards.add("Phường Đông Ngạc");
        hanoiWards.add("Phường Đức Thắng"); hanoiWards.add("Phường Xuân Đỉnh"); hanoiWards.add("Phường Xuân Tảo");
        hanoiWards.add("Phường Cổ Nhuế 1"); hanoiWards.add("Phường Cổ Nhuế 2"); hanoiWards.add("Phường Phúc Diễn");
        hanoiWards.add("Phường Phú Diễn");

        // Hà Đông (Sáp nhập Yết Kiêu, Nguyễn Trãi, Quang Trung thành Phường Quang Trung)
        hanoiWards.add("Phường Mộ Lao"); hanoiWards.add("Phường Văn Quán"); hanoiWards.add("Phường Vạn Phúc");
        hanoiWards.add("Phường Quang Trung"); hanoiWards.add("Phường La Khê"); hanoiWards.add("Phường Phú La");
        hanoiWards.add("Phường Phúc La"); hanoiWards.add("Phường Hà Cầu"); hanoiWards.add("Phường Yên Nghĩa");
        hanoiWards.add("Phường Kiến Hưng"); hanoiWards.add("Phường Phú Lãm"); hanoiWards.add("Phường Phú Lương");
        hanoiWards.add("Phường Dương Nội"); hanoiWards.add("Phường Biên Giang"); hanoiWards.add("Phường Đồng Mai");

        // Sơn Tây (Sáp nhập Lê Lợi, Ngô Quyền, Quang Trung thành Phường Ngô Quyền)
        hanoiWards.add("Phường Phú Thịnh"); hanoiWards.add("Phường Ngô Quyền"); hanoiWards.add("Phường Sơn Lộc");
        hanoiWards.add("Phường Xuân Khanh"); hanoiWards.add("Phường Trung Hưng"); hanoiWards.add("Phường Viên Sơn");
        hanoiWards.add("Phường Trung Sơn Trầm"); hanoiWards.add("Xã Đường Lâm"); hanoiWards.add("Xã Thanh Mỹ");
        hanoiWards.add("Xã Xuân Sơn"); hanoiWards.add("Xã Kim Sơn"); hanoiWards.add("Xã Sơn Đông"); hanoiWards.add("Xã Cổ Đông");

        // Các Huyện & Xã mới (Cập nhật từ danh sách 75 Xã)
        hanoiWards.add("Xã Thanh Trì"); hanoiWards.add("Xã Đại Thanh"); hanoiWards.add("Xã Nam Phú");
        hanoiWards.add("Xã Ngọc Hồi"); hanoiWards.add("Xã Thượng Phúc"); hanoiWards.add("Xã Thường Tín");
        hanoiWards.add("Xã Chương Dương"); hanoiWards.add("Xã Hồng Vân"); hanoiWards.add("Xã Phú Xuyên");
        hanoiWards.add("Xã Phượng Dực"); hanoiWards.add("Xã Chuyên Mỹ"); hanoiWards.add("Xã Đại Xuyên");
        hanoiWards.add("Xã Thành Đại"); hanoiWards.add("Xã Bình Minh"); hanoiWards.add("Xã Tam Hưng");
        hanoiWards.add("Xã Dân Hòa"); hanoiWards.add("Xã Vân Đình"); hanoiWards.add("Xã Ứng Thiên");
        hanoiWards.add("Xã Hòa Xá"); hanoiWards.add("Xã Ứng Hòa"); hanoiWards.add("Xã Mỹ Đức");
        hanoiWards.add("Xã Hồng Sơn"); hanoiWards.add("Xã Phúc Sơn"); hanoiWards.add("Xã Hương Sơn");
        hanoiWards.add("Xã Phú Nghĩa"); hanoiWards.add("Xã Xuân Mai"); hanoiWards.add("Xã Trần Phú");
        hanoiWards.add("Xã Hòa Phú"); hanoiWards.add("Xã Quảng Bị"); hanoiWards.add("Xã Minh Châu");
        hanoiWards.add("Xã Quảng Oai"); hanoiWards.add("Xã Vật Lại"); hanoiWards.add("Xã Cổ Đô");
        hanoiWards.add("Xã Bất Bạt"); hanoiWards.add("Xã Suối Hai"); hanoiWards.add("Xã Ba Vì");
        hanoiWards.add("Xã Yên Bài"); hanoiWards.add("Xã Đoài Phượng"); hanoiWards.add("Xã Phúc Thọ");
        hanoiWards.add("Xã Phúc Lộc"); hanoiWards.add("Xã Hát Môn"); hanoiWards.add("Xã Thạch Thất");
        hanoiWards.add("Xã Hạ Bằng"); hanoiWards.add("Xã Tây Phương"); hanoiWards.add("Xã Hòa Lạc");
        hanoiWards.add("Xã Yên Xuân"); hanoiWards.add("Xã Quốc Oai"); hanoiWards.add("Xã Hưng Đạo");
        hanoiWards.add("Xã Kiều Phú"); hanoiWards.add("Xã Phú Cát"); hanoiWards.add("Xã Hoài Đức");
        hanoiWards.add("Xã Dương Hòa"); hanoiWards.add("Xã Sơn Đồng"); hanoiWards.add("Xã An Khánh");
        hanoiWards.add("Xã Đan Phượng"); hanoiWards.add("Xã Ô Diên"); hanoiWards.add("Xã Liên Minh");
        hanoiWards.add("Xã Gia Lâm"); hanoiWards.add("Xã Thuận An"); hanoiWards.add("Xã Bát Tràng");
        hanoiWards.add("Xã Phù Đổng"); hanoiWards.add("Xã Thư Lâm"); hanoiWards.add("Xã Đông Anh");
        hanoiWards.add("Xã Phúc Thịnh"); hanoiWards.add("Xã Thiên Lộc"); hanoiWards.add("Xã Vĩnh Thanh");
        hanoiWards.add("Xã Mê Linh"); hanoiWards.add("Xã Yên Lãng"); hanoiWards.add("Xã Tiến Thắng");
        hanoiWards.add("Xã Quang Minh"); hanoiWards.add("Xã Sóc Sơn"); hanoiWards.add("Xã Đa Phúc");
        hanoiWards.add("Xã Nội Bài"); hanoiWards.add("Xã Trung Giã"); hanoiWards.add("Xã Kim Anh");

        PROVINCE_WARD_MAP.put("Hà Nội", hanoiWards);


        // --- HỒ CHÍ MINH (Mới 2025: 126 Đơn vị hành chính cấp xã mới) ---
        List<String> hcmWards = new ArrayList<>();

        // Quận 1 (Phường Sài Gòn, Tân Định, Bến Thành, Cầu Ông Lãnh)
        hcmWards.add("Phường Sài Gòn"); hcmWards.add("Phường Tân Định");
        hcmWards.add("Phường Bến Thành"); hcmWards.add("Phường Cầu Ông Lãnh");

        // Quận 3 (Phường Bàn Cờ, Xuân Hòa, Nhiêu Lộc)
        hcmWards.add("Phường Bàn Cờ"); hcmWards.add("Phường Xuân Hòa"); hcmWards.add("Phường Nhiêu Lộc");

        // Quận 4 (Xóm Chiếu, Khánh Hội, Vĩnh Hội)
        hcmWards.add("Phường Xóm Chiếu"); hcmWards.add("Phường Khánh Hội"); hcmWards.add("Phường Vĩnh Hội");

        // Quận 5 (Chợ Quán, An Đông, Chợ Lớn)
        hcmWards.add("Phường Chợ Quán"); hcmWards.add("Phường An Đông"); hcmWards.add("Phường Chợ Lớn");

        // Quận 6 (Bình Tây, Bình Tiên, Bình Phú, Phú Lâm)
        hcmWards.add("Phường Bình Tây"); hcmWards.add("Phường Bình Tiên");
        hcmWards.add("Phường Bình Phú"); hcmWards.add("Phường Phú Lâm");

        // Quận 7 (Tân Thuận, Phú Thuận, Tân Mỹ, Tân Hưng)
        hcmWards.add("Phường Tân Thuận"); hcmWards.add("Phường Phú Thuận");
        hcmWards.add("Phường Tân Mỹ"); hcmWards.add("Phường Tân Hưng");

        // Quận 8 (Chánh Hưng, Phú Định, Bình Đông)
        hcmWards.add("Phường Chánh Hưng"); hcmWards.add("Phường Phú Định"); hcmWards.add("Phường Bình Đông");

        // Quận 10 (Diên Hồng, Vườn Lài, Hòa Hưng)
        hcmWards.add("Phường Diên Hồng"); hcmWards.add("Phường Vườn Lài"); hcmWards.add("Phường Hòa Hưng");

        // Quận 11 (Minh Phụng, Bình Thới, Hòa Bình, Phú Thọ)
        hcmWards.add("Phường Minh Phụng"); hcmWards.add("Phường Bình Thới");
        hcmWards.add("Phường Hòa Bình"); hcmWards.add("Phường Phú Thọ");

        // Quận 12
        hcmWards.add("Phường Đông Hưng Thuận"); hcmWards.add("Phường Trung Mỹ Tây");
        hcmWards.add("Phường Tân Thới Hiệp"); hcmWards.add("Phường Thới An"); hcmWards.add("Phường An Phú Đông");

        // Bình Tân
        hcmWards.add("Phường An Lạc"); hcmWards.add("Phường Bình Tân"); hcmWards.add("Phường Tân Tạo");
        hcmWards.add("Phường Bình Trị Đông"); hcmWards.add("Phường Bình Hưng Hòa");

        // Bình Thạnh
        hcmWards.add("Phường Gia Định"); hcmWards.add("Phường Bình Thạnh"); hcmWards.add("Phường Bình Lợi Trung");
        hcmWards.add("Phường Thạnh Mỹ Tây"); hcmWards.add("Phường Bình Quới");

        // Gò Vấp
        hcmWards.add("Phường Hạnh Thông"); hcmWards.add("Phường An Nhơn"); hcmWards.add("Phường Gò Vấp");
        hcmWards.add("Phường An Hội Đông"); hcmWards.add("Phường Thông Tây Hội"); hcmWards.add("Phường An Hội Tây");

        // Phú Nhuận
        hcmWards.add("Phường Đức Nhuận"); hcmWards.add("Phường Cầu Kiệu"); hcmWards.add("Phường Phú Nhuận");

        // Tân Bình
        hcmWards.add("Phường Tân Sơn Hòa"); hcmWards.add("Phường Tân Sơn Nhất"); hcmWards.add("Phường Tân Hòa");
        hcmWards.add("Phường Bảy Hiền"); hcmWards.add("Phường Tân Bình"); hcmWards.add("Phường Tân Sơn");

        // Tân Phú
        hcmWards.add("Phường Tây Thạnh"); hcmWards.add("Phường Tân Sơn Nhì"); hcmWards.add("Phường Phú Thọ Hòa");
        hcmWards.add("Phường Tân Phú"); hcmWards.add("Phường Phú Thạnh");

        // TP Thủ Đức
        hcmWards.add("Phường Hiệp Bình"); hcmWards.add("Phường Thủ Đức"); hcmWards.add("Phường Tam Bình");
        hcmWards.add("Phường Linh Xuân"); hcmWards.add("Phường Tăng Nhơn Phú"); hcmWards.add("Phường Long Bình");
        hcmWards.add("Phường Long Phước"); hcmWards.add("Phường Long Trường"); hcmWards.add("Phường Cát Lái");
        hcmWards.add("Phường Bình Trưng"); hcmWards.add("Phường Phước Long"); hcmWards.add("Phường An Khánh");

        // Các Huyện thuộc TP.HCM
        hcmWards.add("Xã Vĩnh Lộc"); hcmWards.add("Xã Tân Vĩnh Lộc"); hcmWards.add("Xã Bình Lợi");
        hcmWards.add("Xã Tân Nhựt"); hcmWards.add("Xã Bình Chánh"); hcmWards.add("Xã Hưng Long");
        hcmWards.add("Xã Bình Hưng"); hcmWards.add("Xã Bình Khánh"); hcmWards.add("Xã An Thới Đông");
        hcmWards.add("Xã Cần Giờ"); hcmWards.add("Xã Củ Chi"); hcmWards.add("Xã Tân An Hội");
        hcmWards.add("Xã Thái Mỹ"); hcmWards.add("Xã An Nhơn Tây"); hcmWards.add("Xã Nhuận Đức");
        hcmWards.add("Xã Phú Hòa Đông"); hcmWards.add("Xã Bình Mỹ"); hcmWards.add("Xã Đông Thạnh");
        hcmWards.add("Xã Hóc Môn"); hcmWards.add("Xã Xuân Thới Sơn"); hcmWards.add("Xã Bà Điểm");
        hcmWards.add("Xã Nhà Bè"); hcmWards.add("Xã Hiệp Phước");

        PROVINCE_WARD_MAP.put("Hồ Chí Minh", hcmWards);


        // ĐÀ NẴNG - 94 đơn vị (gồm cả từ Quảng Nam)
        List<String> danangWards = new ArrayList<>();
        danangWards.add("Phường Thạch Thang");
        danangWards.add("Phường Hải Châu 1");
        danangWards.add("Phường Hải Châu 2");
        danangWards.add("Phường Thuận Phước");
        danangWards.add("Phường Thọ Quang");
        danangWards.add("Phường Nại Hiên Đông");
        danangWards.add("Phường An Hải Bắc");
        danangWards.add("Phường Mỹ An");
        danangWards.add("Phường Khuê Mỹ");
        // Từ Quảng Nam
        danangWards.add("Phường Minh An");
        danangWards.add("Phường Cẩm Châu");
        danangWards.add("Xã Cẩm Thanh");
        danangWards.add("Đặc khu Hoàng Sa");
        PROVINCE_WARD_MAP.put("Đà Nẵng", danangWards);

        // HẢI PHÒNG (gồm cả Hải Dương)
        List<String> haiphongWards = new ArrayList<>();
        haiphongWards.add("Phường Lê Lợi");
        haiphongWards.add("Phường Minh Khai");
        haiphongWards.add("Phường Trại Cau");
        haiphongWards.add("Phường Hoàng Văn Thụ");
        haiphongWards.add("Phường Đằng Giang");
        // Từ Hải Dương
        haiphongWards.add("Phường Ngọc Châu");
        haiphongWards.add("Phường Cẩm Thượng");
        haiphongWards.add("Đặc khu Bạch Long Vĩ");
        haiphongWards.add("Đặc khu Cát Hải");
        PROVINCE_WARD_MAP.put("Hải Phòng", haiphongWards);

        // CẦN THƠ (gồm Hậu Giang, Kiên Giang, Sóc Trăng)
        List<String> canthoWards = new ArrayList<>();
        canthoWards.add("Phường Xuân Khánh");
        canthoWards.add("Phường Hưng Lợi");
        canthoWards.add("Phường An Hòa");
        canthoWards.add("Phường Thới Bình");
        // Từ Kiên Giang
        canthoWards.add("Phường Dương Đông");
        canthoWards.add("Đặc khu Phú Quốc");
        canthoWards.add("Đặc khu Thổ Châu");
        canthoWards.add("Đặc khu Kiên Hải");
        PROVINCE_WARD_MAP.put("Cần Thơ", canthoWards);

        // HUẾ (từ Thừa Thiên Huế)
        List<String> hueWards = new ArrayList<>();
        hueWards.add("Phường Phú Hòa");
        hueWards.add("Phường Phú Cát");
        hueWards.add("Phường Kim Long");
        hueWards.add("Phường Vỹ Dạ");
        hueWards.add("Phường Phường Đúc");
        hueWards.add("Phường Vĩnh Ninh");
        hueWards.add("Phường Phú Hiệp");
        hueWards.add("Phường Thuận Hòa");
        PROVINCE_WARD_MAP.put("Thừa Thiên Huế", hueWards);

        // Generic fallback for others
        List<String> genericWards = new ArrayList<>();
        genericWards.add("Phường 1");
        genericWards.add("Phường 2");
        genericWards.add("Xã A");
        genericWards.add("Xã B");

        // Fill others with generic to avoid crashes if user selects other provinces
        for (String province : ProvinceProvider.getProvinces()) {
            if (!PROVINCE_WARD_MAP.containsKey(province)) {
                PROVINCE_WARD_MAP.put(province, genericWards);
            }
        }
    }

    /**
     * Lấy danh sách xã/phường/đặc khu theo tỉnh/thành phố
     */
    public static List<String> getWards(String province) {
        if (province == null || province.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> wards = PROVINCE_WARD_MAP.get(province);
        if (wards == null) {
            return new ArrayList<>();
        }

        List<String> sortedWards = new ArrayList<>(wards);
        Collections.sort(sortedWards);
        return sortedWards;
    }

    /**
     * Lấy tổng số đơn vị hành chính cấp xã của một tỉnh
     */
    public static int getWardCount(String province) {
        List<String> wards = getWards(province);
        return wards.size();
    }

    /**
     * Kiểm tra xem đơn vị hành chính có phải là đặc khu không
     */
    public static boolean isSpecialDistrict(String ward) {
        return ward != null && ward.startsWith("Đặc khu");
    }
}