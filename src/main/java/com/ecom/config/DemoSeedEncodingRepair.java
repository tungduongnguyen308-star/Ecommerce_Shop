package com.ecom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Repairs UTF-8 mojibake in the original sample rows without dropping the database.
 * Deliberately leaves user-created products, checkout data, and legitimate edits untouched.
 * A no-op on correctly encoded or already repaired databases.
 */
@Configuration
public class DemoSeedEncodingRepair {
    private static final Logger log = LoggerFactory.getLogger(DemoSeedEncodingRepair.class);

    private record SeedCategory(long id, String name) { }
    private record SeedProduct(long id, String name, String description) { }
    private record StoredText(long id, String name, String description) { }

    private static final List<SeedCategory> CATEGORIES = List.of(
        new SeedCategory(1, "Laptop"),
        new SeedCategory(2, "Điện thoại"),
        new SeedCategory(3, "Phụ kiện"),
        new SeedCategory(4, "Thiết bị gia dụng")
    );
    private static final List<SeedProduct> PRODUCTS = List.of(
        new SeedProduct(1, "Laptop HP Pavilion 15", "Laptop 15 inch cho học tập, văn phòng, RAM 16GB, SSD 512GB"),
        new SeedProduct(2, "Laptop ASUS VivoBook", "Laptop mỏng nhẹ, RAM 8GB, SSD 512GB, làm việc và học trực tuyến"),
        new SeedProduct(3, "iPhone 14 128GB", "Điện thoại iOS, màn hình OLED, camera kép, lưu trữ 128GB"),
        new SeedProduct(4, "OnePlus Smartphone", "Điện thoại Android, màn hình AMOLED và sạc nhanh"),
        new SeedProduct(5, "Màn hình máy tính 24 inch", "Màn hình Full HD cho học tập, làm việc và giải trí"),
        new SeedProduct(6, "Giày thể thao", "Giày nhẹ phù hợp vận động hằng ngày"),
        new SeedProduct(7, "Máy giặt gia đình", "Máy giặt cửa trước tiết kiệm nước và điện"),
        new SeedProduct(8, "Tủ lạnh hai cánh", "Tủ lạnh dung tích lớn, làm lạnh nhanh")
    );

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ApplicationRunner repairOldSeedEncoding(JdbcTemplate jdbc) {
        return args -> {
            int changed = 0;
            for (SeedCategory seed : CATEGORIES) {
                List<String> names = jdbc.query("SELECT name FROM categories WHERE id=?",
                        (rs, index) -> rs.getString("name"), seed.id());
                if (!names.isEmpty() && VietnameseTextEncoding.isMojibake(names.get(0))) {
                    changed += jdbc.update("UPDATE categories SET name=? WHERE id=?", seed.name(), seed.id());
                }
            }
            for (SeedProduct seed : PRODUCTS) {
                List<StoredText> rows = jdbc.query("SELECT id,name,description FROM products WHERE id=?",
                        (rs, index) -> new StoredText(rs.getLong("id"),rs.getString("name"),rs.getString("description")), seed.id());
                if (rows.isEmpty()) continue;
                StoredText stored = rows.get(0);
                if (VietnameseTextEncoding.isMojibake(stored.name())) {
                    changed += jdbc.update("UPDATE products SET name=? WHERE id=?", seed.name(), seed.id());
                }
                if (VietnameseTextEncoding.isMojibake(stored.description())) {
                    changed += jdbc.update("UPDATE products SET description=? WHERE id=?", seed.description(), seed.id());
                }
            }
            if (changed > 0) {
                log.info("Restored Vietnamese UTF-8 in {} sample field(s). Existing orders and custom data were preserved.", changed);
            }
        };
    }
}
