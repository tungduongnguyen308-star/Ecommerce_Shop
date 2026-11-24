# KỊCH BẢN DEMO VÀ VẤN ĐÁP – 20 PHÚT

## Phân chia thời lượng theo PDF

**10 phút báo cáo (gợi ý)**: 2' giới thiệu đề tài và yêu cầu; 3' Use Case + Sequence + kiến trúc Controller–Service–DAO; 2' bảng MySQL; 3' giải thích JDBC CRUD, tính tổng đơn và transaction. **5 phút vấn đáp**: câu hỏi về JDBC vs JPA, `@Transactional`, SQL injection, BCrypt, Qdrant. **5 phút live demo**: 1' chạy Docker và website; 1' thêm/sửa/xóa sản phẩm; 1' giỏ/checkout; 1' kiểm chứng SQL; 1' chạy JUnit test.

## Bước kiểm tra trực tiếp

1. IntelliJ: chạy `EcommerceApplication.java`; trình duyệt `http://localhost:8080`.
2. Docker: `docker compose ps`; MySQL khỏe; Qdrant có thể chưa chạy nếu chưa demo tìm vector.
3. Đăng nhập `http://localhost:8080/login` bằng tài khoản demo trong README (chỉ localhost).
4. Admin → Quản lý sản phẩm → Thêm sản phẩm: tên “Laptop TX1”, mô tả “Máy tính học tập”, danh mục Laptop, giá 12000000, tồn 5, ảnh `/img/product_img/laptop.jpg`.
5. Sửa giá thành 11500000, kiểm tra danh sách. Xóa sản phẩm khi chưa có đơn; kiểm tra hết trong bảng.
6. Website → Sản phẩm → Thêm 2 sản phẩm vào giỏ → Checkout → Nhập thông tin người nhận → Xác nhận đặt hàng.
7. Terminal: `docker compose exec mysql mysql -usmartshop -psmartshop123 smartshop_tx1 -e "SELECT id,name,price,stock FROM products; SELECT id,customer_name,total,status FROM orders; SELECT order_id,product_name,quantity,line_total FROM order_items;"`
8. IntelliJ Maven → Lifecycle → `test`; chụp màn hình các test xanh. Nếu đang dùng Terminal Windows: `mvnw.cmd test`.
9. **Qdrant tùy chọn:** Admin → Đồng bộ tìm kiếm Qdrant → `/products` → tìm “laptop học tập” với chế độ “Qdrant vector”. Không có hộp chat.

## Vấn đáp gợi ý

- **Kiến trúc?** Controller điều hướng/validation → Service Interface xử lý nghiệp vụ → DAO truy cập MySQL với JdbcTemplate; riêng Qdrant có gateway tìm vector độc lập.
- **Tại sao JDBC?** PDF TX1 yêu cầu DAO/JDBC và CRUD; `JdbcTemplate` giảm boilerplate nhưng thực chất dùng JDBC `PreparedStatement`.
- **Chống SQL injection?** Parameter binding (`?`) thay vì nối chuỗi đầu vào thành SQL.
- **Tồn kho đồng thời?** SQL `UPDATE ... WHERE stock >= ?` kiểm tra số bản ghi ảnh hưởng, kết hợp transaction để rollback toàn bộ đơn khi lỗi.
- **Giao dịch?** `@Transactional` tại `OrderServiceImpl.place()`: chèn đơn, chi tiết và trừ tồn kho cùng giao dịch.
- **Tại sao có Qdrant mà chưa chatbot?** Mới cài đặt mô-đun truy xuất sản phẩm qua vector để phát triển AI sau. Hash vector KHÔNG phải embedding ngữ nghĩa.
- **Lỗ hổng chưa làm?** Thanh toán thật, tài khoản khách hàng, email xác thực, khôi phục mật khẩu, giỏ hàng lưu lâu dài, rate limiting và giám sát chưa nằm trong phạm vi TX1.
