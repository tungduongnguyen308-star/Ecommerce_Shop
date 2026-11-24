# Ecommerce Shop – hướng dẫn chạy và các lỗi đã sửa

## Các thay đổi trong bản này

1. **Lỗi HTTP 500 tại `/cart`:** bỏ biểu thức Thymeleaf gọi lớp static `T(java.math.BigDecimal)` ở `cart.html` (dòng 43). Giá đơn vị sau khuyến mãi được tính trong `CartLine.getUnitPrice()` và truyền sang template qua `${line.unitPrice}`.
2. **Footer trang giỏ hàng:** giữ bố cục flex theo chiều dọc để footer nằm sát đáy viewport khi nội dung ngắn.
3. **Footer lịch sử mua hàng:** trang lịch sử mua hàng dùng flex layout và footer màu đen nằm cuối trang.
4. **Giao diện giảm giá sản phẩm:** nạp `admin-dashboard.css` trong fragment head dùng chung; bổ sung bố cục banner, form, lựa chọn sản phẩm và bảng chương trình có màu sắc, khoảng cách và trạng thái hover.
5. **Lịch sử mua hàng:** tiếp tục hiển thị riêng email người mua và số điện thoại trong bảng.

## Chạy bằng Docker Desktop

1. Giải nén ZIP.
2. Mở PowerShell tại thư mục `Ecommerce_Shop` (thư mục có file `docker-compose.yml`).
3. Chạy:

```powershell
docker compose up --build
```

4. Mở `http://localhost:8080`.
5. Trang giỏ hàng: `http://localhost:8080/cart`.
6. Trang giảm giá sản phẩm: đăng nhập tài khoản quản trị, sau đó mở `http://localhost:8080/admin/product-promotions`.

Để dừng các container, mở PowerShell khác tại đúng thư mục dự án và chạy:

```powershell
docker compose down
```

**Lưu ý dữ liệu:** không dùng `docker compose down -v` nếu muốn giữ dữ liệu MySQL trong volume.

## Nếu cần xem log lỗi

```powershell
docker compose logs --tail=200 app
```
