-- Chạy một lần trên database smartshop_tx1 đã được tạo trước khi cập nhật phiên bản này.
USE smartshop_tx1;
ALTER TABLE app_users
    ADD COLUMN full_name VARCHAR(150) NULL,
    ADD COLUMN phone VARCHAR(30) NULL,
    ADD COLUMN address VARCHAR(350) NULL;
ALTER TABLE orders
    ADD COLUMN customer_user_id BIGINT NULL,
    ADD CONSTRAINT fk_orders_customer FOREIGN KEY(customer_user_id) REFERENCES app_users(id) ON DELETE SET NULL;
