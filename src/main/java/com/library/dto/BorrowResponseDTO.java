package com.library.dto;

import com.library.entity.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO dung de tra thong tin phieu muon sach ra ngoai cho Client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowResponseDTO {

    private Long id;

    private Long bookId;

    private String bookTitle;

    private String username;

    private LocalDate borrowDate;

    private LocalDate dueDate;

    private LocalDate returnDate;

    private BorrowStatus status;
}
