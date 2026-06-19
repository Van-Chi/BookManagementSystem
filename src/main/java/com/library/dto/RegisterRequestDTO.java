package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin đăng ký tài khoản mới")
public class RegisterRequestDTO {

    @Schema(description = "Tên đăng nhập (4-50 ký tự)", example = "john_doe")
    @NotBlank(message = "Ten dang nhap (username) khong duoc de trong")
    @Size(min = 4, max = 50, message = "Ten dang nhap phai co tu 4 den 50 ky tu")
    private String username;

    @Schema(description = "Mật khẩu (tối thiểu 6 ký tự)", example = "secret123")
    @NotBlank(message = "Mat khau (password) khong duoc de trong")
    @Size(min = 6, message = "Mat khau phai co it nhat 6 ky tu")
    private String password;

    @Schema(description = "Địa chỉ email", example = "john@example.com")
    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    private String email;

    @Schema(description = "Họ và tên đầy đủ", example = "Nguyễn Văn A")
    @NotBlank(message = "Ho ten (fullName) khong duoc de trong")
    private String fullName;
}
