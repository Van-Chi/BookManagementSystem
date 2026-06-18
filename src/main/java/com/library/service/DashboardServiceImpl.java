package com.library.service;

import com.library.dto.DashboardStatsDTO;
import com.library.dto.TopBorrowedBookDTO;
import com.library.entity.BorrowStatus;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int TOP_BORROWED_BOOKS_LIMIT = 5;

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        long totalCopies = bookRepository.sumTotalCopies();
        long totalAvailableCopies = bookRepository.sumAvailableCopies();

        long activeBorrowCount = borrowRecordRepository.countByStatus(BorrowStatus.BORROWED);
        long overdueCount = borrowRecordRepository.countByStatusAndDueDateBefore(BorrowStatus.BORROWED, LocalDate.now());

        List<TopBorrowedBookDTO> topBorrowedBooks =
                borrowRecordRepository.findTopBorrowedBooks(PageRequest.of(0, TOP_BORROWED_BOOKS_LIMIT));

        return DashboardStatsDTO.builder()
                .totalBooks(bookRepository.count())
                .totalCopies(totalCopies)
                .totalAvailableCopies(totalAvailableCopies)
                .totalBorrowedCopies(totalCopies - totalAvailableCopies)
                .totalUsers(userRepository.count())
                .activeBorrowCount(activeBorrowCount)
                .overdueCount(overdueCount)
                .totalReturnedCount(borrowRecordRepository.countByStatus(BorrowStatus.RETURNED))
                .topBorrowedBooks(topBorrowedBooks)
                .build();
    }
}
