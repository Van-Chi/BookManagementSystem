package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin đăng nhập")
public class LoginRequestDTO {

    @Schema(description = "Tên đăng nhập", example = "john_doe")
    @NotBlank(message = "Ten dang nhap (username) khong duoc de trong")
    private String username;

    @Schema(description = "Mật khẩu", example = "secret123")
    @NotBlank(message = "Mat khau (password) khong duoc de trong")
    private String password;
}
