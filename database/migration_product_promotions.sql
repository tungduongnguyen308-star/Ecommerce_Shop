-- Chạy một lần nếu muốn tạo schema thủ công; ứng dụng cũng tự tạo các bảng này khi khởi động.
CREATE TABLE IF NOT EXISTS product_promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    discount_type VARCHAR(12) NOT NULL,
    discount_value DECIMAL(14,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_promotion_items (
    promotion_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (promotion_id, product_id),
    CONSTRAINT fk_pp_promotion FOREIGN KEY (promotion_id)
        REFERENCES product_promotions(id) ON DELETE CASCADE,
    CONSTRAINT fk_pp_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE
);

ALTER TABLE orders ADD COLUMN discount_total DECIMAL(14,2) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(40) NULL;
