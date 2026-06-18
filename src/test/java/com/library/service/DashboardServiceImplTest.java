package com.library.service;

import com.library.dto.DashboardStatsDTO;
import com.library.dto.TopBorrowedBookDTO;
import com.library.entity.BorrowStatus;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit test cho DashboardServiceImpl, su dung Mockito de mock cac Repository,
 * tap trung kiem tra logic tong hop so lieu thong ke.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(bookRepository, userRepository, borrowRecordRepository);
    }

    @Test
    void getStats_TongHopDayDuSoLieuTuCacRepository() {
        when(bookRepository.count()).thenReturn(10L);
        when(bookRepository.sumTotalCopies()).thenReturn(30L);
        when(bookRepository.sumAvailableCopies()).thenReturn(18L);
        when(userRepository.count()).thenReturn(7L);
        when(borrowRecordRepository.countByStatus(BorrowStatus.BORROWED)).thenReturn(12L);
        when(borrowRecordRepository.countByStatus(BorrowStatus.RETURNED)).thenReturn(40L);
        when(borrowRecordRepository.countByStatusAndDueDateBefore(eq(BorrowStatus.BORROWED), any()))
                .thenReturn(3L);

        TopBorrowedBookDTO topBook = TopBorrowedBookDTO.builder()
                .bookId(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .borrowCount(9L)
                .build();
        when(borrowRecordRepository.findTopBorrowedBooks(any())).thenReturn(List.of(topBook));

        DashboardStatsDTO result = dashboardService.getStats();

        assertThat(result.getTotalBooks()).isEqualTo(10L);
        assertThat(result.getTotalCopies()).isEqualTo(30L);
        assertThat(result.getTotalAvailableCopies()).isEqualTo(18L);
        assertThat(result.getTotalBorrowedCopies()).isEqualTo(12L);
        assertThat(result.getTotalUsers()).isEqualTo(7L);
        assertThat(result.getActiveBorrowCount()).isEqualTo(12L);
        assertThat(result.getOverdueCount()).isEqualTo(3L);
        assertThat(result.getTotalReturnedCount()).isEqualTo(40L);
        assertThat(result.getTopBorrowedBooks()).hasSize(1);
        assertThat(result.getTopBorrowedBooks().get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void getStats_KhiChuaCoDuLieu_TraVeTatCaBangKhong() {
        when(bookRepository.count()).thenReturn(0L);
        when(bookRepository.sumTotalCopies()).thenReturn(0L);
        when(bookRepository.sumAvailableCopies()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(borrowRecordRepository.countByStatus(any())).thenReturn(0L);
        when(borrowRecordRepository.countByStatusAndDueDateBefore(any(), any())).thenReturn(0L);
        when(borrowRecordRepository.findTopBorrowedBooks(any())).thenReturn(List.of());

        DashboardStatsDTO result = dashboardService.getStats();

        assertThat(result.getTotalBooks()).isZero();
        assertThat(result.getTotalBorrowedCopies()).isZero();
        assertThat(result.getTopBorrowedBooks()).isEmpty();
    }
}
