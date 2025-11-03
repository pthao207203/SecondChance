package com.example.secondchance.data.product;

import com.example.secondchance.R;
import com.example.secondchance.ui.product.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SampleProductData {

    private static List<Product> allProducts;
    private static int nextProductId = 10;

    static {
        allProducts = new ArrayList<>();
        initializeProducts();
    }

    private static void initializeProducts() {
        // Initial Fixed Products
        List<String> fixedImages = Arrays.asList(
                String.valueOf(R.drawable.binhhoa),
                String.valueOf(R.drawable.giohoa1),
                String.valueOf(R.drawable.nhan1)
        );
        for (int i = 0; i < 3; i++) {
            Product p = new Product("p" + i, "Sản phẩm cố định " + i, 150000, 1, fixedImages, "2024-01-01", "fixed", "Giá cố định");
            p.setDescription("testing tab 1 fixed - một văn bản ngẫu nhiên");
            p.setSource("testing tab 2 fixed - một văn bản ngẫu nhiên");
            p.setProof("testing tab 3 fixed - một văn bản ngẫu nhiên");
            p.setOtherInfo("testing tab 4 fixed - một văn bản ngẫu nhiên");
            allProducts.add(p);
        }

        // Initial Negotiable Products
        List<String> negotiableImages = Arrays.asList(
                String.valueOf(R.drawable.nhan1),
                String.valueOf(R.drawable.nhan2),
                String.valueOf(R.drawable.nhan3)
        );
        for (int i = 0; i < 3; i++) {
            Product product = new Product(
                    "negotiable_" + i,
                    "Nhẫn kim cương",
                    50000,
                    1,
                    negotiableImages,
                    "17/6/2025",
                    "negotiable",
                    "Giá thương lượng"
            );
            product.setDescription("testing tab 1 negotiable - một văn bản ngẫu nhiên");
            product.setSource("testing tab 2 negotiable - một văn bản ngẫu nhiên");
            product.setProof("testing tab 3 negotiable - một văn bản ngẫu nhiên");
            product.setOtherInfo("testing tab 4 negotiable - một văn bản ngẫu nhiên");
            allProducts.add(product);
        }

        // Initial Auction Products
        List<String> auctionImages1 = Arrays.asList(
                String.valueOf(R.drawable.giohoa1),
                String.valueOf(R.drawable.giohoa2),
                String.valueOf(R.drawable.giohoa3)
        );
        Product product1 = new Product("auction_1", "Vòng hoa hướng dương (còn hạn)", 8500000, 1, auctionImages1, "2024-01-01", "auction", "Đấu giá");
        product1.setEndTime(System.currentTimeMillis() + 3600 * 1000);
        product1.setDescription("testing tab 1 auction - một văn bản ngẫu nhiên");
        product1.setSource("testing tab 2 auction - một văn bản ngẫu nhiên");
        product1.setProof("testing tab 3 auction - một văn bản ngẫu nhiên");
        product1.setOtherInfo("testing tab 4 auction - một văn bản ngẫu nhiên");
        allProducts.add(product1);

        List<String> auctionImages2 = Arrays.asList(String.valueOf(R.drawable.binhhoa));
        Product product2 = new Product("auction_2", "Vòng hoa hướng dương (hết hạn)", 500000, 1, auctionImages2, "2024-01-02", "auction", "Đấu giá");
        product2.setEndTime(System.currentTimeMillis() - 3600 * 1000);
        product2.setDescription("testing tab 1 auction 2 - một văn bản ngẫu nhiên");
        product2.setSource("testing tab 2 auction 2 - một văn bản ngẫu nhiên");
        product2.setProof("testing tab 3 auction 2 - một văn bản ngẫu nhiên");
        product2.setOtherInfo("testing tab 4 auction 2 - một văn bản ngẫu nhiên");
        allProducts.add(product2);

        // Initial Deleted Products
        List<String> deletedImages1 = Arrays.asList(
                String.valueOf(R.drawable.giohoa1),
                String.valueOf(R.drawable.giohoa2),
                String.valueOf(R.drawable.giohoa3)
        );
        Product p1 = new Product("d1", "Giỏ gỗ cắm hoa", 50000, 1, deletedImages1, "", "deleted", "");
        p1.setOriginalStatus("fixed");
        p1.setDeletedDate("17/06/2025");
        p1.setDescription("testing tab 1 deleted - một văn bản ngẫu nhiên");
        p1.setSource("testing tab 2 deleted - một văn bản ngẫu nhiên");
        p1.setProof("testing tab 3 deleted - một văn bản ngẫu nhiên");
        p1.setOtherInfo("testing tab 4 deleted - một văn bản ngẫu nhiên");
        allProducts.add(p1);
    }

    public static List<Product> getFixedProducts() {
        return allProducts.stream().filter(p -> "fixed".equals(p.getStatus())).collect(Collectors.toList());
    }

    public static List<Product> getNegotiableProducts() {
        return allProducts.stream().filter(p -> "negotiable".equals(p.getStatus())).collect(Collectors.toList());
    }

    public static List<Product> getAuctionProducts() {
        return allProducts.stream().filter(p -> "auction".equals(p.getStatus())).collect(Collectors.toList());
    }

    public static List<Product> getDeletedProducts() {
        return allProducts.stream().filter(p -> "deleted".equals(p.getStatus())).collect(Collectors.toList());
    }

    public static Product getProductById(String productId) {
        for (Product product : allProducts) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    public static void addProduct(Product product) {
        // Generate a new unique ID
        product.setId("product_" + nextProductId++);
        allProducts.add(0, product); // Add to the top of the list
    }

    public static void updateProduct(Product productToUpdate) {
        for (int i = 0; i < allProducts.size(); i++) {
            if (allProducts.get(i).getId().equals(productToUpdate.getId())) {
                allProducts.set(i, productToUpdate);
                return;
            }
        }
    }
    
    public static void deleteProduct(String productId) {
        Product product = getProductById(productId);
        if (product != null) {
            product.setOriginalStatus(product.getStatus());
            product.setStatus("deleted");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            product.setDeletedDate(sdf.format(new Date()));
        }
    }
}