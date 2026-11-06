-- Ecommerce_Shop: cap nhat ten san pham da co trong MySQL ma khong xoa du lieu.
-- Chay mot lan tren database smartshop_tx1; khong chay lai file khoi tao tren database dang su dung.
-- Khong thay doi id, gia, ton kho, don hang hoac ten san pham da khong co (demo).
SET NAMES utf8mb4;
USE smartshop_tx1;
START TRANSACTION;
UPDATE products
SET name = TRIM(REPLACE(name, '(demo)', ''))
WHERE name LIKE '%(demo)%';
COMMIT;
SELECT id, name FROM products WHERE name LIKE '%(demo)%';
