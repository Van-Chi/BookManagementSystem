package com.library.dto;

import com.library.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dung de tra thong tin nguoi dung ra ngoai cho Client.
 * Khong bao gio chua truong password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private Role role;
}
