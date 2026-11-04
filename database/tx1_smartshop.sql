SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
-- TX1 MySQL 8.4 export: Ecommerce Shop seven filter groups + twelve featured tiles.
CREATE DATABASE IF NOT EXISTS smartshop_tx1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartshop_tx1;
CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ADMIN',
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    price DECIMAL(14,2) NOT NULL,
    stock INT NOT NULL,
    image_url VARCHAR(400) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY(category_id) REFERENCES categories(id),
    CONSTRAINT chk_product_price CHECK(price > 0),
    CONSTRAINT chk_product_stock CHECK(stock >= 0)
);
CREATE INDEX idx_products_category ON products(category_id);
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(350) NOT NULL,
    note VARCHAR(500),
    total DECIMAL(14,2) NOT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(14,2) NOT NULL,
    line_total DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_orderitems_order FOREIGN KEY(order_id) REFERENCES orders(id),
    CONSTRAINT fk_orderitems_product FOREIGN KEY(product_id) REFERENCES products(id) ON DELETE SET NULL,
    CONSTRAINT chk_orderitem_quantity CHECK(quantity > 0)
);
CREATE INDEX idx_orderitems_order ON order_items(order_id);
-- Seven filter categories on a fresh install.
INSERT IGNORE INTO categories(id,name) VALUES
(1,'Laptop'),(2,'Điện thoại'),(3,'Phụ kiện'),(4,'Điện máy'),
(5,'Máy tính bảng'),(6,'Màn hình'),(7,'PC - Máy tính để bàn');
INSERT IGNORE INTO products(id,name,description,category_id,price,stock,image_url,active) VALUES
(1,'Laptop HP Pavilion 15','Laptop 15 inch cho học tập, văn phòng, RAM 16GB, SSD 512GB',1,15990000,12,'/img/product_img/hp laptop.jpg',1),
(2,'Laptop ASUS VivoBook','Laptop mỏng nhẹ, RAM 8GB, SSD 512GB, làm việc và học trực tuyến',1,13990000,9,'/img/product_img/laptop.jpg',1),
(3,'iPhone 14 128GB','Điện thoại iOS, màn hình OLED, camera kép, lưu trữ 128GB',2,12990000,7,'/img/product_img/iphone 14.jpg',1),
(4,'OnePlus Smartphone','Điện thoại Android, màn hình AMOLED và sạc nhanh',2,8990000,14,'/img/product_img/oneplus mobile.jpg',1),
(7,'Máy giặt gia đình','Máy giặt cửa trước tiết kiệm nước và điện',4,8490000,5,'/img/product_img/washing_machine.png',1),
(8,'Tủ lạnh hai cánh','Tủ lạnh dung tích lớn, làm lạnh nhanh',4,10990000,6,'/img/product_img/fridge.png',1),
(9,'Samsung Galaxy A55 5G','Điện thoại Android 5G. Ảnh minh họa phục vụ TX1.',2,7990000,12,'/img/product_img/mobile.jpg',1),
(10,'Tivi màn hình LED 43 inch','Tivi LED gia đình. Ảnh minh họa.',4,5790000,8,'/img/catalog-products/tv-demo.webp',1),
(11,'Tai nghe chụp tai Bluetooth','Tai nghe Bluetooth cho điện thoại và laptop.',3,690000,20,'/img/catalog-products/headphone-demo.webp',1),
(12,'Chuột không dây','Chuột không dây cho học tập và công việc.',3,249000,32,'/img/catalog-products/mouse-demo.webp',1),
(13,'Cáp sạc USB-C','Cáp USB-C dài một mét, ảnh dựng minh họa.',3,99000,40,'/img/catalog-products/cable-demo.webp',1),
(14,'Loa Bluetooth mini','Loa Bluetooth nhỏ gọn, ảnh dựng minh họa.',3,390000,25,'/img/catalog-products/speaker-demo.webp',1),
(15,'Máy tính bảng Android 11 inch','Máy tính bảng 11 inch, WiFi, bộ nhớ 128GB',5,4990000,8,'/img/category-gallery/may-tinh-bang.webp',1),
(16,'Màn hình IPS 24 inch','Màn hình máy tính IPS 24 inch Full HD',6,2990000,9,'/img/category-gallery/man-hinh.webp',1),
(17,'PC máy tính để bàn Gaming','Máy tính để bàn RAM 16GB SSD 512GB',7,13990000,5,'/img/category-gallery/pc-may-tinh-de-ban.webp',1),
(18,'Máy xay sinh tố cối thủy tinh','Máy xay sinh tố đa tốc độ',4,890000,15,'/img/category-gallery/may-xay-sinh-to.webp',1),
(19,'Robot hút bụi tự động','Robot hút bụi lau nhà tự động',4,3990000,9,'/img/category-gallery/robot-hut-bui.webp',1),
(20,'Máy lọc không khí HEPA','Máy lọc không khí màng lọc HEPA',4,2590000,11,'/img/category-gallery/may-loc-khong-khi.webp',1);
