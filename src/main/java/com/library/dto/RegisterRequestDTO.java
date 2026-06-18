package com.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dung de nhan du lieu dang ky tai khoan moi tu Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    @NotBlank(message = "Ten dang nhap (username) khong duoc de trong")
    @Size(min = 4, max = 50, message = "Ten dang nhap phai co tu 4 den 50 ky tu")
    private String username;

    @NotBlank(message = "Mat khau (password) khong duoc de trong")
    @Size(min = 6, message = "Mat khau phai co it nhat 6 ky tu")
    private String password;

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong dung dinh dang")
    private String email;

    @NotBlank(message = "Ho ten (fullName) khong duoc de trong")
    private String fullName;
}
