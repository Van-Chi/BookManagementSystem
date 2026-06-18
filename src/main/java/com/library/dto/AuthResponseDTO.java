package com.library.dto;

import com.library.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dung de tra ve ket qua dang nhap/dang ky thanh cong, bao gom JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String token;

    private String tokenType;

    private String username;

    private Role role;

    private String refreshToken;
}
