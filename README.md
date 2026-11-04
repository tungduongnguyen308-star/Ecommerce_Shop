# Ecommerce_Shop

Hệ thống thương mại điện tử xây dựng theo **kiến trúc phân tầng** với Java Spring Boot, MySQL/JDBC và Qdrant. Đây là phiên bản demo cho **TX1 – Nhóm 6**. **Chưa có chatbot AI/LLM**: Qdrant chỉ được dùng cho tìm kiếm sản phẩm bằng vector hash từ khóa (không phải embedding ngữ nghĩa).

## 1. Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 17, Spring Boot 3.3.5, Spring MVC, Spring Security |
| Giao diện | Thymeleaf, Bootstrap 5, HTML/CSS/JavaScript |
| Tầng dữ liệu | DAO interfaces + Spring `JdbcTemplate` (JDBC), MySQL 8.4 |
| Tìm kiếm mở rộng | Qdrant 1.12.4, vector hash 256 chiều, tùy chọn |
| Kiểm thử | JUnit 5, Mockito, Spring Boot Test |
| Khởi chạy dịch vụ | Docker Compose |

## 2. Chức năng hiện có

**Khách hàng:** xem sản phẩm, chọn danh mục, tìm kiếm theo từ khóa, xem chi tiết; thêm/xóa sản phẩm trong giỏ; đổi số lượng, cập nhật tổng tiền; điền thông tin và đặt hàng. Giỏ hàng lưu trong HTTP session.

**Quản trị viên:** đăng nhập, dashboard sidebar, quản lý sản phẩm (thêm/xem/sửa/xóa), quản lý đơn hàng và trạng thái, xem khách hàng tổng hợp từ đơn hàng, đồng bộ chỉ mục Qdrant.

**Danh mục ở bộ lọc:** Điện thoại, Máy tính bảng, Laptop, Màn hình, PC – Máy tính để bàn, Phụ kiện, Điện máy. Tivi, tủ lạnh, máy giặt, robot hút bụi, máy xay sinh tố và máy lọc không khí thuộc Điện máy. Trang chủ dùng các ô nổi bật để tìm sản phẩm theo nhóm và từ khóa.

## 3. Chạy bằng IntelliJ IDEA (Windows)

**Yêu cầu:** JDK 17 hoặc 21, IntelliJ IDEA, Docker Desktop với Docker Compose. Maven có thể chạy qua Maven tool window trong IntelliJ.

1. Giải nén và mở **thư mục `Ecommerce_Shop` chứa `pom.xml`** trong IntelliJ (File → Open).
2. File → Project Structure → Project SDK: chọn Java 17/21; Maven → Reload All Maven Projects.
3. Mở Docker Desktop. Mở Terminal trong thư mục dự án:

   ```powershell
   docker compose up -d
   docker compose ps
   ```

   Chờ MySQL ở trạng thái `healthy`. `docker compose ps` cần hiện mapping `0.0.0.0:3306->3306/tcp` và `0.0.0.0:6333->6333/tcp`. Nếu cổng 3306 đã bị phần mềm khác sử dụng, thay mapping trong Compose và JDBC URL tương ứng.

4. Mở `src/main/java/com/ecom/EcommerceApplication.java` → Run hàm `main()`.
5. Truy cập http://localhost:8080.

**Các địa chỉ thường dùng:**

| Địa chỉ | Chức năng |
|---|---|
| `/` | Trang chủ |
| `/products` | Tất cả sản phẩm và bộ lọc |
| `/cart` | Giỏ hàng |
| `/checkout` | Thanh toán/đặt hàng (demo) |
| `/login` | Đăng nhập admin |
| `/admin` | Dashboard |
| `/admin/products` | Quản lý sản phẩm |
| `/admin/orders` | Quản lý đơn hàng |
| `/admin/customers` | Danh sách khách hàng từ đơn hàng |

### Tài khoản quản trị demo

- Email mặc định: `admin@smartshop.local`
- Mật khẩu mặc định: `Admin@123`

**Chỉ dùng để demo cục bộ.** Hãy thay mật khẩu trước khi đưa ứng dụng lên mạng. AdminSeeder tạo tài khoản mặc định khi tài khoản chưa tồn tại; sửa cấu hình `demo.admin-password` **không tự đổi** mật khẩu đã lưu trong MySQL. Password được mã hóa BCrypt.

## 4. MySQL và Docker (quan trọng nếu từng đổi tên thư mục)

Docker Compose đã cố định `name: smartshop-tx1` để đổi tên thư mục từ `SmartShop-TX1` sang `Ecommerce_Shop` **không tự tạo Compose project/volume mới** đối với project cũ có tên `smartshop-tx1`. Trước khi thao tác trên máy từng chạy project cũ, kiểm tra `docker compose ls -a` và `docker ps` để xác nhận project/container đang dùng.

**Để tránh làm mất đơn hàng:** không chạy `docker compose down -v`, không xóa Docker volume, không import đè SQL vào MySQL đã có dữ liệu.

Cấu hình mặc định vẫn dùng **database `smartshop_tx1`, user `smartshop`, password `smartshop123`** để tương thích dữ liệu cũ. Đổi tên website không đồng nghĩa đổi tên database. Cấu hình JDBC nằm tại `src/main/resources/application.properties`.

Kiểm tra dữ liệu (Terminal ở thư mục dự án):

```powershell
docker compose exec mysql mysql -usmartshop -psmartshop123 smartshop_tx1 -e "SHOW TABLES; SELECT id, name, price, stock FROM products LIMIT 10;"
```

File `database/tx1_smartshop.sql` cung cấp schema và dữ liệu mẫu cho **database mới**. Script trong `/docker-entrypoint-initdb.d` chỉ chạy khi tạo volume MySQL lần đầu. Các lớp seeder cũng có thể bổ sung/chỉnh dữ liệu mẫu khi khởi động: nên backup database trước khi thử trên dữ liệu thật.


## 5. Qdrant (tùy chọn, không có chatbot)

- Dashboard Qdrant: http://localhost:6333/dashboard
- Tại `/admin`, dùng thao tác đồng bộ Qdrant để tạo/cập nhật index.
- Tìm kiếm JDBC hoạt động độc lập với Qdrant. Khi dùng Qdrant mà dịch vụ lỗi hoặc chưa đồng bộ, hệ thống chuyển về tìm kiếm MySQL.
- Vector tạo từ hashing từ khóa, **không phải chatbot, không sử dụng LLM**.

## 6. Chạy kiểm thử

Trong Maven tool window: Lifecycle → `test`, hoặc Terminal Windows (nếu Maven cài sẵn):

```powershell
mvn test
```

Nếu Maven Wrapper tải được Maven qua mạng, có thể dùng `./mvnw.cmd test`. Unit test được đặt trong `src/test/java/com/ecom/`. Cần kiểm tra kết quả thực tế trong IntelliJ trước khi nộp TX1.

## 7. Kiến trúc phân tầng

```text
Trình duyệt (Bootstrap/Thymeleaf/JavaScript)
  └── Spring MVC Controller + Spring Security + Bean Validation
       └── Service Interface / Implementation (@Transactional)
            ├── DAO Interface / JdbcTemplate ──> MySQL
            └── CatalogSearchService ──> QdrantGateway ──> Qdrant (tùy chọn)
```

- `controller/`: xử lý HTTP và trả về trang/JSON.
- `dto/`: dữ liệu form và validation.
- `service/`, `service/impl/`: nghiệp vụ, giỏ hàng, tồn kho và đơn hàng.
- `dao/`: giao diện và lớp truy cập MySQL bằng JDBC.
- `model/`: đối tượng dữ liệu.
- `config/`: Security, cấu hình và dữ liệu demo.
- `ai/`: tạo vector hash và kết nối Qdrant (không chatbot).
- `src/main/resources/templates/`: trang web Thymeleaf; `static/`: CSS, JS, ảnh.
- `docs/uml/`: sơ đồ PlantUML; `docs/USE_CASE_SPECIFICATIONS.md`: đặc tả; `docs/DEMO_SCRIPT.md`: kịch bản demo.


