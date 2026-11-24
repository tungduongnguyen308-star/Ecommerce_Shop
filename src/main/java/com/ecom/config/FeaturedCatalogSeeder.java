package com.ecom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.List;

/**
 * migrate demo catalog to 7 searchable groups / 12 homepage tiles.
 * Idempotent, does not delete orders, does not replace custom prices or stock.
 */
@Configuration
public class FeaturedCatalogSeeder {
    private static final Logger LOG = LoggerFactory.getLogger(FeaturedCatalogSeeder.class);
    private record DemoProduct(String category, String name, String description,
                               BigDecimal price, int stock, String imageUrl) { }

    private static final List<String> TOP_CATEGORIES = List.of(
            "Điện thoại", "Máy tính bảng", "Laptop", "Màn hình", "PC - Máy tính để bàn", "Phụ kiện", "Điện máy");
    private static final List<String> ELECTRICAL_CATEGORIES = List.of(
            "Thiết bị gia dụng", "Tivi", "Máy giặt", "Tủ lạnh", "Máy xay sinh tố",
            "Máy lọc nước", "Máy lọc không khí", "Robot hút bụi", "Máy lạnh - Điều hòa");
    private static final List<DemoProduct> DEMOS = List.of(
            item("Điện thoại", "Samsung Galaxy A55 5G", "Điện thoại Samsung Galaxy 5G.", "7990000", 12, "dien-thoai"),
            item("Máy tính bảng", "Máy tính bảng Android 11 inch", "Máy tính bảng 11 inch, WiFi, bộ nhớ 128GB.", "4990000", 8, "may-tinh-bang"),
            item("Màn hình", "Màn hình IPS 24 inch", "Màn hình máy tính IPS 24 inch, Full HD.", "2990000", 9, "man-hinh"),
            item("PC - Máy tính để bàn", "PC máy tính để bàn Gaming", "Máy tính để bàn, RAM 16GB, SSD 512GB.", "13990000", 5, "pc-may-tinh-de-ban"),
            item("Điện máy", "Tivi LED 43 inch", "Tivi LED thông minh 43 inch.", "5790000", 8, "tivi"),
            item("Điện máy", "Máy xay sinh tố cối thủy tinh", "Máy xay sinh tố đa tốc độ, cối thủy tinh.", "890000", 15, "may-xay-sinh-to"),
            item("Điện máy", "Robot hút bụi tự động", "Robot hút bụi lau nhà tự động.", "3990000", 9, "robot-hut-bui"),
            item("Điện máy", "Máy lọc không khí HEPA", "Máy lọc không khí dùng màng lọc HEPA.", "2590000", 11, "may-loc-khong-khi"),
            item("Điện máy", "Máy giặt cửa trước", "Máy giặt gia đình cửa trước.", "7490000", 7, "may-giat"),
            item("Điện máy", "Tủ lạnh ngăn đá trên", "Tủ lạnh tiết kiệm điện cho gia đình.", "6990000", 8, "tu-lanh"),
            item("Phụ kiện", "Tai nghe chụp tai Bluetooth", "Tai nghe Bluetooth chụp tai.", "690000", 20, "phu-kien"),
            item("Phụ kiện", "Chuột không dây", "Chuột máy tính không dây.", "249000", 32, "phu-kien"),
            item("Phụ kiện", "Cáp sạc USB-C", "Cáp USB-C sạc nhanh.", "99000", 40, "phu-kien"),
            item("Phụ kiện", "Loa Bluetooth mini", "Loa Bluetooth di động.", "390000", 25, "phu-kien")
    );
    private static DemoProduct item(String cat, String name, String desc, String price, int stock, String image) {
        return new DemoProduct(cat, name, desc, new BigDecimal(price), stock,
                "/img/category-gallery/" + image + ".webp");
    }

    @Bean
    @Order(200)
    @ConditionalOnProperty(name = "demo.featured-catalog-seed.enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner seedCatalog(JdbcTemplate jdbc, TransactionTemplate tx) {
        return args -> tx.executeWithoutResult(status -> {
            // Preserve category id from original database whenever possible.
            Integer hasElectrical = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM categories WHERE name='Điện máy'", Integer.class);
            if (hasElectrical != null && hasElectrical == 0)
                jdbc.update("UPDATE categories SET name='Điện máy' WHERE name='Thiết bị gia dụng'");
            for (String category : TOP_CATEGORIES)
                jdbc.update("INSERT IGNORE INTO categories(name) VALUES(?)", category);
            Long electricalId = categoryId(jdbc, "Điện máy");
            for (String legacyCategory : ELECTRICAL_CATEGORIES) {
                jdbc.update("UPDATE products p JOIN categories c ON c.id=p.category_id " +
                        "SET p.category_id=? WHERE c.name=? AND p.category_id<>?",
                        electricalId, legacyCategory, electricalId);
            }
            // Repair the original monitor sample, only if it is still that exact picture/name.
            Long monitorId = categoryId(jdbc, "Màn hình");
            jdbc.update("UPDATE products SET category_id=?,active=TRUE " +
                            "WHERE id=5 AND image_url='/img/product_img/monitor.jpg' AND name LIKE '%Màn hình%'",
                    monitorId);
            // Original sneaker sample must not be sold in this technology shop.
            jdbc.update("UPDATE products SET active=FALSE " +
                    "WHERE id=6 AND image_url='/img/product_img/white shoe.jfif'");
            // Normalize existing legacy product names before checking for missing catalog items.
            // This preserves product IDs, custom prices, stock and order references.
            jdbc.update("UPDATE products SET name=TRIM(REPLACE(name, '(demo)', '')) " +
                    "WHERE name LIKE '%(demo)%'");
            // Restore known incorrect image paths only, preserving other custom images.
            replaceImage(jdbc, "Tai nghe chụp tai Bluetooth", "/img/catalog-tiles/headset.svg", "/img/catalog-products/headphone-demo.webp");
            replaceImage(jdbc, "Chuột không dây", "/img/catalog-tiles/mouse.svg", "/img/catalog-products/mouse-demo.webp");
            replaceImage(jdbc, "Cáp sạc USB-C", "/img/catalog-tiles/cable.svg", "/img/catalog-products/cable-demo.webp");
            for (DemoProduct item : DEMOS) addIfMissing(jdbc, item);
            LOG.info("Catalog ready with 7 searchable categories and 12 homepage tiles.");
        });
    }

    private static Long categoryId(JdbcTemplate jdbc, String name) {
        return jdbc.queryForObject("SELECT id FROM categories WHERE name=?", Long.class, name);
    }
    private static void replaceImage(JdbcTemplate jdbc, String name, String oldImage, String newImage) {
        jdbc.update("UPDATE products SET image_url=? WHERE name=? AND image_url=?", newImage, name, oldImage);
    }
    private static void addIfMissing(JdbcTemplate jdbc, DemoProduct item) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE name=?", Integer.class, item.name());
        if (count != null && count > 0) return;
        jdbc.update("INSERT INTO products(name,description,category_id,price,stock,image_url,active) " +
                        "VALUES(?,?,?,?,?,?,TRUE)", item.name(), item.description(), categoryId(jdbc, item.category()),
                item.price(), item.stock(), item.imageUrl());
    }
}
