package com.library.dto;

import com.library.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRoleRequestDTO {

    @NotNull(message = "Role khong duoc de trong")
    private Role role;
}
