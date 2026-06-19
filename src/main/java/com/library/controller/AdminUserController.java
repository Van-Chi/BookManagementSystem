package com.library.controller;

import com.library.dto.AdminUserResponseDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.dto.ChangeRoleRequestDTO;
import com.library.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Quản lý người dùng dành cho ADMIN: xem, thay đổi role, khoá/mở tài khoản")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Lấy danh sách tất cả người dùng có phân trang (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về danh sách người dùng"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN")
    })
    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDTO>> getAllUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getAllUsers(pageable));
    }

    @Operation(summary = "Lấy thông tin chi tiết một người dùng (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về thông tin người dùng"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponseDTO> getUserById(
            @Parameter(description = "ID của người dùng") @PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @Operation(summary = "Lấy lịch sử mượn sách của một người dùng (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về danh sách phiếu mượn của người dùng"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @GetMapping("/{id}/borrows")
    public ResponseEntity<Page<BorrowResponseDTO>> getUserBorrows(
            @Parameter(description = "ID của người dùng") @PathVariable Long id,
            @PageableDefault(size = 10, sort = "borrowDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUserBorrows(id, pageable));
    }

    @Operation(summary = "Thay đổi role của người dùng (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thay đổi role thành công"),
            @ApiResponse(responseCode = "400", description = "Role không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @PutMapping("/{id}/role")
    public ResponseEntity<AdminUserResponseDTO> changeUserRole(
            @Parameter(description = "ID của người dùng") @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequestDTO requestDTO) {
        return ResponseEntity.ok(adminUserService.changeUserRole(id, requestDTO.getRole()));
    }

    @Operation(summary = "Khoá hoặc mở khoá tài khoản người dùng (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thay đổi trạng thái tài khoản thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    @PutMapping("/{id}/disable")
    public ResponseEntity<AdminUserResponseDTO> toggleUserEnabled(
            @Parameter(description = "ID của người dùng") @PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.toggleUserEnabled(id));
    }
}
