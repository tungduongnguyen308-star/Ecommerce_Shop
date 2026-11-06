# ĐỐI CHIẾU RUBRIC TX1

| Điều kiện trong PDF TX1 | Minh chứng trong bản demo |
|---|---|
| ≥1 Use Case tổng quát | `docs/uml/01-use-case-overview.puml` |
| ≥3 Use Case chi tiết | `docs/uml/02...puml`, `03...puml`, `04...puml` |
| ≥3 Sequence Diagram | `docs/uml/05-sequence-checkout.puml`, `06-sequence-crud-product.puml`, `08-sequence-cart-update.puml` |
| Đặc tả luồng chính/ngoại lệ | `docs/USE_CASE_SPECIFICATIONS.md` |
| Bootstrap responsive | Templates trong `src/main/resources/templates/`; Bootstrap 5 |
| Trang chủ, form nhập liệu, validation, menu, dashboard | `/`, `/admin/products/new`, `/products`, `/admin` |
| CSDL, SQL export | `database/tx1_smartshop.sql` (schema + seed mẫu) |
| JDBC CRUD | `dao/JdbcProductDao.java` và `ProductDao.java` |
| Interface và nghiệp vụ | `service/ProductService.java`, `OrderService.java`, các `impl/` |
| ≥2 test case | các unit test JUnit ở `src/test/java/com/ecom/service/` |
| Live demo DB, logic, test | `docs/DEMO_SCRIPT.md`, `README.md` |
| Đăng nhập quản trị | `config/SecurityConfig.java`, mật khẩu BCrypt, CSRF |
| Qdrant phù hợp tên dự án | Tìm kiếm vector, đồng bộ index; **chưa có chatbot** |

**Chưa thể xác nhận chạy thực tế trong phiên tạo file:** môi trường build không có Maven và không tải được dependency Internet. Chạy `mvn test` trên máy có Maven/IntelliJ để kiểm tra và chụp kết quả. Cần dùng mẫu báo cáo BTL chính thức của trường nếu mẫu được cung cấp riêng.
