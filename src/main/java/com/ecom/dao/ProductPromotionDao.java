package com.ecom.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ProductPromotionDao {
    private final JdbcTemplate jdbc;
    public ProductPromotionDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @PostConstruct public void initialize() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS product_promotions (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(180) NOT NULL, discount_type VARCHAR(12) NOT NULL, discount_value DECIMAL(14,2) NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        jdbc.execute("CREATE TABLE IF NOT EXISTS product_promotion_items (promotion_id BIGINT NOT NULL, product_id BIGINT NOT NULL, PRIMARY KEY(promotion_id, product_id), CONSTRAINT fk_pp_promotion FOREIGN KEY(promotion_id) REFERENCES product_promotions(id) ON DELETE CASCADE, CONSTRAINT fk_pp_product FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE CASCADE)");
    }

    public record ProductPromotion(long id, String name, String discountType, BigDecimal discountValue,
                                   LocalDate startDate, LocalDate endDate, boolean active, List<Long> productIds) { }

    public List<ProductPromotion> findAll() {
        return jdbc.query("SELECT * FROM product_promotions ORDER BY id DESC", (r, i) -> {
            long id = r.getLong("id");
            List<Long> ids = jdbc.query("SELECT product_id FROM product_promotion_items WHERE promotion_id=? ORDER BY product_id",
                    (x, n) -> x.getLong(1), id);
            return new ProductPromotion(id, r.getString("name"), r.getString("discount_type"), r.getBigDecimal("discount_value"),
                    r.getDate("start_date").toLocalDate(), r.getDate("end_date").toLocalDate(), r.getBoolean("active"), ids);
        });
    }

    public void create(String name, String type, BigDecimal value, LocalDate start, LocalDate end, List<Long> productIds) {
        jdbc.update("INSERT INTO product_promotions(name,discount_type,discount_value,start_date,end_date,active) VALUES(?,?,?,?,?,TRUE)",
                name, type, value, Date.valueOf(start), Date.valueOf(end));
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (productIds != null) for (Long productId : productIds)
            jdbc.update("INSERT IGNORE INTO product_promotion_items(promotion_id,product_id) VALUES(?,?)", id, productId);
    }

    public void delete(long id) { jdbc.update("DELETE FROM product_promotions WHERE id=?", id); }

    public BigDecimal discountFor(long productId, BigDecimal price) {
        List<BigDecimal> discounts = jdbc.query("SELECT pp.discount_type,pp.discount_value FROM product_promotions pp JOIN product_promotion_items pi ON pi.promotion_id=pp.id WHERE pi.product_id=? AND pp.active=TRUE AND CURDATE() BETWEEN pp.start_date AND pp.end_date",
                (r, i) -> {
                    BigDecimal value = r.getBigDecimal("discount_value");
                    BigDecimal amount = "PERCENT".equals(r.getString("discount_type"))
                            ? price.multiply(value).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP) : value;
                    return amount.min(price).max(BigDecimal.ZERO);
                }, productId);
        return discounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }
}
