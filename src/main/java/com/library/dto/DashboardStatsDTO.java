package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO tong hop so lieu thong ke cho man hinh Dashboard cua ADMIN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {

    private long totalBooks;
    private long totalCopies;
    private long totalAvailableCopies;
    private long totalBorrowedCopies;
    private long totalUsers;
    private long activeBorrowCount;
    private long overdueCount;
    private long totalReturnedCount;
    private List<TopBorrowedBookDTO> topBorrowedBooks;
}
