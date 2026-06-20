# BookManagementSystem

Hệ thống Quản lý Thư viện (Library Management System) xây dựng bằng **Spring Boot 3**, cung cấp REST API cho việc quản lý sách, mượn/trả sách, đánh giá sách, người dùng và thống kê dành cho quản trị viên.

## Tính năng chính

- **Xác thực & phân quyền**: Đăng ký, đăng nhập bằng JWT (access token + refresh token), đăng xuất, đăng nhập qua Google OAuth2. Phân quyền theo role (USER/ADMIN) với Spring Security.
- **Quản lý sách**: Tạo, sửa, xóa sách (ADMIN); tìm kiếm và lọc sách theo từ khóa, thể loại, tình trạng còn sách, có phân trang.
- **Mượn/trả sách**: Mượn sách, trả sách, xem lịch sử mượn của bản thân. Xử lý đồng thời an toàn bằng `@Transactional` và Optimistic Locking (`@Version`) trên số lượng sách khả dụng.
- **Đánh giá sách**: Người dùng đã mượn và trả sách có thể đánh giá (rating + nhận xét); ADMIN có thể xóa đánh giá vi phạm.
- **Hồ sơ cá nhân**: Cập nhật thông tin cá nhân, đổi mật khẩu.
- **Quản trị người dùng (ADMIN)**: Xem danh sách/chi tiết người dùng, xem lịch sử mượn của từng người dùng, thay đổi role, khóa/mở khóa tài khoản.
- **Dashboard thống kê (ADMIN)**: Tổng số sách, số bản còn/đã mượn, số người dùng, số phiếu mượn theo trạng thái, top sách được mượn nhiều nhất.
- **Bảo mật**: Mật khẩu băm bằng `BCryptPasswordEncoder`, rate limiting bằng Bucket4j, gửi email qua SMTP.
- **API docs**: Tích hợp Swagger/OpenAPI (springdoc) để xem và thử API.

## Công nghệ sử dụng

- Java 21, Spring Boot 3.3.4, Maven
- Spring Data JPA, Spring Security, Spring OAuth2 Client
- JWT (jjwt), Bucket4j (rate limiting), Lombok
- PostgreSQL (chính), hỗ trợ MySQL, H2 cho test
- Springdoc OpenAPI (Swagger UI)

## Kiến trúc

Mã nguồn tuân theo kiến trúc phân lớp (layered architecture), nằm trong `src/main/java/com/library/`:

```
controller/   REST Controller - nhận request, trả ResponseEntity
service/      Business logic (interface + implementation)
repository/   Spring Data JPA repository
entity/       Entity ánh xạ với database
dto/          Data Transfer Object (Request/Response)
exception/    Custom exception và @ControllerAdvice xử lý lỗi tập trung
config/       Cấu hình Spring Security, JWT, Beans...
```

## Yêu cầu hệ thống

- JDK 21
- Maven (hoặc dùng `mvnw`/`mvnw.cmd` đi kèm)
- PostgreSQL (hoặc MySQL) đang chạy

## Cấu hình

Ứng dụng đọc cấu hình từ `src/main/resources/application.yml`, các giá trị nhạy cảm được cấu hình qua biến môi trường:

| Biến môi trường | Mô tả | Giá trị mặc định |
|---|---|---|
| `DB_URL` | JDBC URL của database | `jdbc:postgresql://localhost:5432/library_db` |
| `DB_USERNAME` | Username database | *(bắt buộc)* |
| `DB_PASSWORD` | Password database | *(bắt buộc)* |
| `JWT_SECRET` | Secret key ký JWT | *(nên đặt riêng cho production)* |
| `JWT_EXPIRATION_MS` | Thời hạn access token (ms) | `86400000` |
| `JWT_REFRESH_EXPIRATION_MS` | Thời hạn refresh token (ms) | `604800000` |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | OAuth2 Google | `disabled` |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | Cấu hình SMTP gửi mail | `smtp.gmail.com` / `587` |
| `OAUTH2_REDIRECT_URL` | URL redirect sau khi OAuth2 login | `http://localhost:3000/oauth2/callback` |

## Cách chạy dự án

```bash
# Clean và build
./mvnw clean package

# Chạy ứng dụng (local)
./mvnw spring-boot:run

# Chạy toàn bộ unit test
./mvnw test

# Chạy một test cụ thể
./mvnw test -Dtest=ClassName
```

Ứng dụng mặc định chạy ở cổng `8080`. Sau khi khởi động, có thể xem và thử API qua Swagger UI tại:

```
http://localhost:8080/swagger-ui.html
```

## Tổng quan API

| Nhóm | Base path | Mô tả |
|---|---|---|
| Authentication | `/api/auth` | Đăng ký, đăng nhập, refresh token, logout, OAuth2 Google |
| Books | `/api/books` | Tìm kiếm, tạo, sửa, xóa sách |
| Borrows | `/api/borrows` | Mượn sách, trả sách, lịch sử mượn |
| Reviews | `/api/books/{bookId}/reviews` | Đánh giá sách |
| User Profile | `/api/users` | Cập nhật hồ sơ, đổi mật khẩu |
| Admin - User Management | `/api/admin/users` | Quản lý người dùng (ADMIN) |
| Admin - Dashboard | `/api/dashboard` | Thống kê tổng quan (ADMIN) |

## Quy chuẩn dự án

Xem chi tiết quy chuẩn coding và bảo mật tại [CLAUDE.md](CLAUDE.md).
