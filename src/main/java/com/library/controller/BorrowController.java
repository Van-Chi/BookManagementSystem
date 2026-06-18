package com.library.controller;

import com.library.dto.BorrowRequestDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.service.BorrowService;
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

/**
 * REST Controller cung cap API muon/tra sach.
 * Nguoi dung hien tai duoc xac dinh thong qua Authentication (JWT), khong
 * nhan truc tiep tu request body de tranh gia mao danh tinh.
 */
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * Muon sach.
     * POST /api/borrows
     */
    @PostMapping
    public ResponseEntity<BorrowResponseDTO> borrowBook(
            @Valid @RequestBody BorrowRequestDTO requestDTO,
            Authentication authentication) {
        BorrowResponseDTO response = borrowService.borrowBook(requestDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Tra sach.
     * PUT /api/borrows/{id}/return
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowResponseDTO> returnBook(
            @PathVariable Long id,
            Authentication authentication) {
        BorrowResponseDTO response = borrowService.returnBook(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Lay lich su muon sach cua nguoi dung hien tai.
     * GET /api/borrows/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<BorrowResponseDTO>> getMyBorrows(Authentication authentication) {
        List<BorrowResponseDTO> response = borrowService.getMyBorrows(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
