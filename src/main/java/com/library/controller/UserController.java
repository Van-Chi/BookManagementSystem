package com.library.controller;

import com.library.dto.ChangePasswordRequestDTO;
import com.library.dto.UpdateProfileRequestDTO;
import com.library.dto.UserResponseDTO;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Quản lý hồ sơ cá nhân và đổi mật khẩu")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Cập nhật họ tên và email của bản thân")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật hồ sơ thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email đã tồn tại"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PutMapping("/me/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDTO updatedUser = userService.updateProfile(userDetails.getUsername(), requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Đổi mật khẩu", description = "Yêu cầu nhập mật khẩu cũ để xác nhận")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(userDetails.getUsername(), requestDTO);
        return ResponseEntity.noContent().build();
    }
}
