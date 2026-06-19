package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Mat khau cu khong duoc de trong")
    private String oldPassword;

    @NotBlank(message = "Mat khau moi khong duoc de trong")
    @Size(min = 8, message = "Mat khau moi phai co it nhat 8 ky tu")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Mat khau moi phai chua it nhat mot chu hoa, mot chu thuong va mot chu so"
    )
    private String newPassword;
}
