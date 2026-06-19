package com.library.controller;

import com.library.dto.BorrowRequestDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
@Tag(name = "Borrows", description = "Mượn sách, trả sách và xem lịch sử mượn")
public class BorrowController {

    private final BorrowService borrowService;

    @Operation(summary = "Mượn sách", description = "Người dùng hiện tại mượn sách theo bookId. Người dùng được xác định từ JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mượn sách thành công"),
            @ApiResponse(responseCode = "400", description = "Sách không còn bản để mượn hoặc user đang mượn sách này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sách")
    })
    @PostMapping
    public ResponseEntity<BorrowResponseDTO> borrowBook(
            @Valid @RequestBody BorrowRequestDTO requestDTO,
            Authentication authentication) {
        BorrowResponseDTO response = borrowService.borrowBook(requestDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Trả sách", description = "Trả sách theo ID phiếu mượn. Chỉ người mượn hoặc ADMIN mới được trả.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả sách thành công"),
            @ApiResponse(responseCode = "400", description = "Phiếu mượn không ở trạng thái BORROWED"),
            @ApiResponse(responseCode = "403", description = "Không có quyền trả phiếu mượn này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phiếu mượn")
    })
    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowResponseDTO> returnBook(
            @Parameter(description = "ID của phiếu mượn") @PathVariable Long id,
            Authentication authentication) {
        BorrowResponseDTO response = borrowService.returnBook(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xem lịch sử mượn sách của bản thân")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về danh sách phiếu mượn của user hiện tại")
    })
    @GetMapping("/my")
    public ResponseEntity<List<BorrowResponseDTO>> getMyBorrows(Authentication authentication) {
        List<BorrowResponseDTO> response = borrowService.getMyBorrows(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
