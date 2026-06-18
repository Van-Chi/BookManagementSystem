# Quy chuẩn Dự án Quản lý Thư viện (Spring Boot)

## Lệnh thông dụng (Development Commands)
- Clean và Build dự án: `./mvnw clean package` (hoặc `mvn clean package`)
- Chạy ứng dụng local: `./mvnw spring-boot:run`
- Chạy toàn bộ Unit Test: `./mvnw test`
- Chạy một test cụ thể: `./mvnw test -Dtest=ClassName`

## Kiến trúc & Cấu trúc Thư mục (Layered Architecture)
Mã nguồn phải tuân thủ cấu trúc package chuẩn sau (nằm trong `src/main/java/com/library/`):
- `controller/`: Chứa các REST Controller, chịu trách nhiệm nhận request và trả về ResponseEntity.
- `service/`: Chứa interface và class triển khai logic nghiệp vụ (Business Logic).
- `repository/`: Chứa các interface kế thừa `JpaRepository` từ Spring Data JPA.
- `entity/`: Chứa các đối tượng ánh xạ trực tiếp với bảng trong Database.
- `dto/`: Chứa các Data Transfer Object (Request/Response) độc lập với Entity.
- `exception/`: Chứa các custom exception và class xử lý lỗi tập trung `@ControllerAdvice`.
- `config/`: Chứa cấu hình hệ thống (Spring Security, JWT, Beans...).

## Quy chuẩn viết code & Bảo mật (Coding Standards)
- **Frameworks**: Sử dụng Java 17/21, Spring Boot 3.x, Maven, Spring Data JPA, Lombok.
- **Dữ liệu**: Không bao giờ trả trực tiếp Entity ra ngoài API. Bắt buộc phải map qua DTO.
- **Bảo mật**: Mật khẩu người dùng trong Database bắt buộc phải được băm bằng `BCryptPasswordEncoder`. Tuyệt đối không lưu plain text.
- **Đồng thời (Concurrency)**: Các hàm thay đổi số lượng sách (`availableCopies`) phải có annotation `@Transactional`. Sử dụng cơ chế Optimistic Locking (`@Version`) trên Entity để tránh tranh chấp dữ liệu (Race Condition).
- **Format**: Đặt tên biến theo kiểu `camelCase`, tên Class theo kiểu `PascalCase`. Sử dụng Lombok (`@Data`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) để giảm bớt code boilerplate.
- **Xử lý lỗi**: Trả về cấu trúc JSON lỗi đồng nhất (`Timestamp`, `Status`, `Error`, `Message`, `Path`).