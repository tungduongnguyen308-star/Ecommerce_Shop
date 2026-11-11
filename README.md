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
3. Mở Docker Desktop, chờ Docker Engine chạy.
4. Mở Terminal/PowerShell tại thư mục `Ecommerce_Shop` (nơi có `docker-compose.yml`) và chạy:

   ```powershell
   docker compose up -d --build
   docker compose ps
   ```

   Lệnh trên tự build image Spring Boot và khởi động cả website, MySQL và Qdrant. Chờ container `smartshop-app` ở trạng thái `Up`; MySQL phải `healthy`.
5. Mở website tại http://localhost:8080. Không cần chạy `EcommerceApplication.java` trong IntelliJ khi đã chạy bằng Docker.

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


### Cập nhật tên sản phẩm từ bản cũ (bỏ `(demo)`)

Ở bản source này, **tên sản phẩm trong SQL khởi tạo và trong `FeaturedCatalogSeeder` đều không có `(demo)`**. Seeder chuẩn hóa tên cũ khi được bật (mặc định bật) trước khi bổ sung sản phẩm, nên không tự sinh lại tên demo do thay đổi tên. Nó không thay đổi mã sản phẩm, giá hay tồn kho.

**Với MySQL đang chạy và đã có dữ liệu**, không import lại toàn bộ `tx1_smartshop.sql` vì tệp đó dùng để khởi tạo database mới. Bạn có thể chạy một lần trong Terminal IntelliJ:

```powershell
docker exec smartshop-mysql mysql -usmartshop -psmartshop123 smartshop_tx1 -e "UPDATE products SET name=TRIM(REPLACE(name, '(demo)', '')) WHERE name LIKE '%(demo)%';"
```

Kiểm tra tên sau khi cập nhật:

```powershell
docker exec smartshop-mysql mysql -usmartshop -psmartshop123 smartshop_tx1 -e "SELECT id, name FROM products WHERE name LIKE '%(demo)%';"
```

Truy vấn thứ hai không trả bản ghi nào là đạt. Ngoài ra có thể dùng `database/CAP_NHAT_TEN_SAN_PHAM_DANG_CHAY.sql` để chạy lệnh trên qua MySQL client. Nếu trước đó bạn đặt `demo.featured-catalog-seed.enabled=false`, SQL cập nhật trực tiếp vẫn hoạt động. **Đừng chạy `docker compose down -v`** vì nó sẽ xóa volume MySQL.

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

## 8. Tài liệu TX1 và những giới hạn

- 1 Use Case tổng quát; ít nhất 3 Use Case chi tiết; Sequence cho đặt hàng, CRUD và giỏ hàng; sơ đồ kiến trúc tại `docs/uml/`.
- Đặc tả luồng chính/ngoại lệ tại `docs/USE_CASE_SPECIFICATIONS.md`.
- Đối chiếu rubric tại `docs/RUBRIC_CHECKLIST.md`.
- Bản báo cáo và slide dự thảo, nếu có trong `deliverables/`, **cần cập nhật theo mẫu BTL chính thức của trường**; không coi đó là bản chốt đã kiểm chứng.
- Chưa tích hợp cổng thanh toán thật hoặc chatbot AI; khách hàng sử dụng tài khoản đăng ký trong hệ thống.
- Phần làm sạch này chưa thể xác nhận Maven build, Docker integration test hoặc chạy giao diện trong môi trường hiện tại: **hãy chạy và kiểm tra trên IntelliJ trước khi nộp**.


## Customer accounts, checkout authentication, and catalog filters

New customer features in this version:
- Customer registration with BCrypt password hashing and unique email validation.
- Login/logout through Spring Security; the existing administrator role remains protected under `/admin/**`.
- Product browsing and cart operations remain public. `/checkout` requires authentication; Spring Security stores the original checkout URL and returns the customer there after successful login. The cart remains in the HTTP session during authentication.
- Customer profile data is stored in `app_users`; orders created by signed-in customers store `orders.customer_user_id`.
- Customers can open `/account/orders` to view their own order history and `/account/orders/{id}` to view order items and delivery details. The server checks order ownership by both order ID and customer user ID; customer routes require the `CUSTOMER` role.
- Product listing supports minimum/maximum price and sorting by price or name, in addition to existing category and keyword filters.

### Database update
For a **new database**, create it using `database/tx1_smartshop.sql` as usual.
For an **existing database**, take a backup and run `database/migration_customer_accounts.sql` once against `smartshop_tx1` before starting the updated application. The migration adds customer profile columns and the nullable customer reference on orders.

### Run locally
Use Java 17 and MySQL 8+. Configure `MYSQL_URL`, `MYSQL_USER`, and `MYSQL_PASSWORD` if your local credentials differ, then run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). The Maven wrapper needs network access the first time it downloads Maven dependencies.


## Chạy toàn bộ website bằng Docker

**Yêu cầu:** Docker Desktop đã cài đặt và đang chạy.

Tại thư mục chứa `docker-compose.yml`, chạy:

```powershell
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

Truy cập website: http://localhost:8080

Các lệnh quản lý:

```powershell
# Dừng các container, giữ nguyên dữ liệu database
docker compose down

# Khởi động lại
docker compose up -d

# Xem log ứng dụng
docker compose logs -f app

# Build lại ứng dụng sau khi sửa source code
docker compose up -d --build app
```

**Lưu ý dữ liệu:** Không dùng `docker compose down -v` nếu muốn giữ dữ liệu MySQL. File SQL khởi tạo chỉ được chạy khi volume MySQL được tạo lần đầu. Cấu hình tài khoản demo chỉ dành cho môi trường local.
