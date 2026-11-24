# ĐẶC TẢ USE CASE – ECOMMERCE_SHOP

> Mẫu trình bày theo các trường chuẩn: mã/tên, tác nhân, mục tiêu, tiền điều kiện, hậu điều kiện, luồng chính, luồng thay thế, ngoại lệ và quy tắc nghiệp vụ. Nếu giảng viên có mẫu BTL riêng, nhóm cần chuyển nội dung sang mẫu chính thức.

## UC-01 – Quản lý sản phẩm (CRUD)

| Trường | Nội dung |
|---|---|
| Mã Use Case | UC-01 |
| Tác nhân chính | Quản trị viên |
| Mục tiêu | Thêm, xem, sửa và xóa sản phẩm trên MySQL qua DAO/JDBC |
| Tiền điều kiện | Admin đăng nhập; bảng `categories`, `products` đã được khởi tạo |
| Kích hoạt | Admin truy cập `/admin/products` |
| Hậu điều kiện thành công | Dữ liệu sản phẩm được thêm/sửa/xóa trong MySQL |

**Luồng chính (thêm sản phẩm):** (1) Admin mở trang quản lý → (2) Chọn “Thêm sản phẩm” → (3) Hệ thống mở form Bootstrap → (4) Admin nhập tên, danh mục, mô tả, giá, tồn kho, ảnh → (5) Controller kiểm tra `@Valid` → (6) `ProductServiceImpl` kiểm tra quy tắc nghiệp vụ → (7) `JdbcProductDao.insert()` lưu dữ liệu bằng `PreparedStatement` → (8) Chuyển về danh sách với thông báo thành công.

**Luồng thay thế:** Admin chọn Sửa → hệ thống nạp dữ liệu theo ID → cập nhật; chọn Xóa → yêu cầu POST có CSRF token → DAO xóa sản phẩm và trả kết quả; chọn Xem → `findAll` trả sản phẩm.

**Ngoại lệ:** Tên rỗng, giá ≤ 0, tồn kho âm hoặc không chọn danh mục → giữ lại form và báo lỗi; ID không tồn tại → báo lỗi nghiệp vụ; MySQL từ chối khóa ngoại → báo lỗi dữ liệu. Dữ liệu đơn hàng đã đặt vẫn giữ snapshot sản phẩm khi xóa.

**Quy tắc:** Giá > 0; tồn kho ≥ 0; tên không quá 200 ký tự; truy cập admin bắt buộc ROLE_ADMIN.

## UC-02 – Đặt hàng

| Trường | Nội dung |
|---|---|
| Mã Use Case | UC-02 |
| Tác nhân chính | Khách hàng |
| Mục tiêu | Đặt hàng từ giỏ hàng, giảm tồn kho an toàn và lưu hóa đơn |
| Tiền điều kiện | Có sản phẩm đang bán và giỏ hàng không rỗng |
| Kích hoạt | Khách hàng xác nhận `/checkout` |
| Hậu điều kiện thành công | Tạo dòng trong `orders`, `order_items`; giảm `products.stock`; xóa giỏ session |

**Luồng chính:** (1) Xem sản phẩm → (2) Chọn số lượng và thêm vào giỏ → (3) Vào giỏ xem tổng tiền → (4) Chọn “Tiến hành đặt hàng” → (5) Nhập tên, email, điện thoại, địa chỉ → (6) Controller kiểm tra Bean Validation → (7) Service bắt đầu transaction và khóa trừ tồn kho bằng điều kiện `stock >= quantity` → (8) Tính tổng từ giá MySQL, không tin giá phía client → (9) DAO lưu đơn hàng và các dòng chi tiết → (10) COMMIT, xóa giỏ và thông báo mã đơn.

**Luồng thay thế:** Người dùng thay đổi ô số lượng ngay trên trang `/cart` → JavaScript gọi `POST /cart/api/update` với CSRF → `CartService` cập nhật session → giao diện hiển thị thành tiền và tạm tính mới. Khi nhấn nút ×, gửi số lượng 0 để xóa dòng giỏ. Nếu JavaScript bị tắt, form POST `/cart/update` có thể dùng để xóa sản phẩm.

**Ngoại lệ:** Giỏ rỗng → không tạo đơn; email/điện thoại/địa chỉ sai → báo lỗi tại form; hết hàng hoặc sản phẩm ngừng bán → rollback transaction, không tạo đơn và giữ giỏ; lỗi DB → rollback.

**Quy tắc:** Số lượng mỗi dòng từ 1–100; tổng = ∑ (đơn giá từ MySQL × số lượng); không có thanh toán online trong demo.

## UC-03 – Tra cứu sản phẩm

| Trường | Nội dung |
|---|---|
| Mã Use Case | UC-03 |
| Tác nhân chính | Khách hàng; Admin (đồng bộ Qdrant) |
| Mục tiêu | Tìm theo từ khóa, danh mục; tùy chọn tìm tương đồng bằng Qdrant |
| Tiền điều kiện | MySQL hoạt động; riêng Qdrant cần container và index đã đồng bộ |
| Kích hoạt | Khách hàng mở `/products` và lọc/tìm kiếm |
| Hậu điều kiện thành công | Hiển thị danh sách sản phẩm phù hợp, không thay đổi tồn kho |

**Luồng chính (MySQL):** (1) Mở trang sản phẩm → (2) Nhập từ khóa/chọn danh mục → (3) Chọn MySQL JDBC → (4) Controller gọi Service → (5) DAO truy vấn SELECT dùng parameter binding → (6) Hiển thị các sản phẩm đang bán.

**Luồng thay thế (Qdrant):** (1) Admin bấm “Đồng bộ tìm kiếm Qdrant” tại dashboard → (2) Service đọc sản phẩm từ MySQL và gửi vector hash (256 chiều) vào Qdrant → (3) Khách chọn “Qdrant vector” và nhập từ khóa → (4) Service truy xuất ID từ Qdrant → (5) Đối chiếu ID với danh sách sản phẩm còn hàng trên MySQL → (6) Hiển thị kết quả, lấy giá/tồn kho trực tiếp từ MySQL.

**Ngoại lệ:** Qdrant không chạy hoặc chưa đồng bộ → hiển thị cảnh báo và chuyển sang tìm kiếm MySQL; không có từ khóa → dùng danh sách mặc định; không có kết quả → hiển thị thông báo.

**Quy tắc:** Trong TX1 chỉ có gợi ý bằng vector dựa trên hashing từ khóa; chưa tích hợp chatbot, LLM, Ollama hoặc embedding ngữ nghĩa.

## UC-04 – Cập nhật trạng thái đơn (bổ sung)

Admin đăng nhập → mở `/admin/orders` → chọn trạng thái → service khóa bản ghi với `SELECT ... FOR UPDATE` → đối chiếu transition hợp lệ → cập nhật. Trạng thái: `PENDING` → `CONFIRMED` hoặc `CANCELLED`; `CONFIRMED` → `SHIPPED` hoặc `CANCELLED`; `SHIPPED` → `COMPLETED`. Khi hủy trước giao hàng, cộng trả tồn kho. Không cho cập nhật từ trạng thái cuối (`CANCELLED`, `COMPLETED`).


## UC-05 – Quản lý giỏ hàng tự động (bổ sung theo phiên bản giao diện hiện tại)

| Trường | Nội dung |
|---|---|
| Mã | UC-05 |
| Tác nhân | Khách hàng |
| Mục tiêu | Sửa số lượng, xóa sản phẩm, xem thành tiền và tạm tính mới |
| Tiền điều kiện | Giỏ session có ít nhất một dòng sản phẩm |
| Kích hoạt | Thay số lượng hoặc nhấn nút × tại `/cart` |
| Hậu điều kiện | Giỏ session và tiền hiển thị đồng bộ với nhau |

**Luồng chính:** (1) Trang `/cart` hiển thị sản phẩm, giá, số lượng và tổng; (2) khách thay số lượng hợp lệ từ 1–100; (3) JavaScript gửi AJAX kèm CSRF đến `/cart/api/update`; (4) Controller gọi `CartService.update`; (5) Service xác minh tồn kho; (6) trả JSON kết quả và tổng giá trị; (7) JS cập nhật thành tiền, tạm tính; (8) có thể tiếp tục mua sắm từ `/products`.

**Luồng xóa:** Nhấn × → gửi `quantity=0` → xóa dòng sản phẩm khỏi giỏ session → cập nhật giao diện.

**Ngoại lệ:** Số lượng âm / quá 100, hết hàng, request lỗi mạng → không nhận thay đổi, hiển thị lỗi và khôi phục giá trị trước đó. Khi tắt JavaScript có thể dùng POST `/cart/update` để xóa.

**Quy tắc:** Giá lấy từ sản phẩm do backend quản lý, không tin giá tự truyền từ trình duyệt; không ghi giỏ hàng vào MySQL khi chưa xác nhận đặt hàng.

## UC-06 – Tra cứu khách hàng (bổ sung)

Admin đăng nhập → chọn `Khách hàng` ở sidebar → controller tổng hợp danh sách khách hàng từ các đơn hàng đã lưu trong MySQL → hiển thị trên dashboard. Đây **không phải** chức năng quản lý tài khoản khách hàng có đăng ký/đăng nhập.
