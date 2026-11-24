USE smartshop_tx1;
CREATE TABLE IF NOT EXISTS promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    discount_type VARCHAR(12) NOT NULL,
    discount_value DECIMAL(14,2) NOT NULL,
    min_order_value DECIMAL(14,2) NOT NULL DEFAULT 0,
    max_discount DECIMAL(14,2) NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    usage_limit INT NULL,
    used_count INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
