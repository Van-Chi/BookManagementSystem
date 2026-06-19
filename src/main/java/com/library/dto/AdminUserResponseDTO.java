package com.library.dto;

import com.library.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponseDTO {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
    private long activeBorrowCount;
    private LocalDateTime createdAt;
}
