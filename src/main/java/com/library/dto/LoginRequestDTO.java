package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dung de nhan du lieu dang nhap tu Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "Ten dang nhap (username) khong duoc de trong")
    private String username;

    @NotBlank(message = "Mat khau (password) khong duoc de trong")
    private String password;
}
