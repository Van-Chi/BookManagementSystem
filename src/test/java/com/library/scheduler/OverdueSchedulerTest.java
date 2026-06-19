package com.library.scheduler;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.BorrowRecordRepository;
import com.library.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho OverdueScheduler: kiem tra logic gui nhac nho va cap nhat trang thai OVERDUE.
 */
@ExtendWith(MockitoExtension.class)
class OverdueSchedulerTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private EmailService emailService;

    private OverdueScheduler overdueScheduler;

    private User sampleUser;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        overdueScheduler = new OverdueScheduler(borrowRecordRepository, emailService);

        sampleUser = User.builder()
                .id(1L)
                .username("member01")
                .email("member01@example.com")
                .fullName("Nguyen Van A")
                .role(Role.MEMBER)
                .build();

        sampleBook = Book.builder()
                .id(1L)
                .title("The Pragmatic Programmer")
                .author("David Thomas")
                .isbn("9780135957059")
                .totalCopies(2)
                .availableCopies(1)
                .build();
    }

    @Test
    void processOverdueAndReminders_KhiCoBorrowRecordSapHanVaQuaHan_GuiEmailVaCapNhatStatus() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        BorrowRecord dueTomorrowRecord = BorrowRecord.builder()
                .id(1L).user(sampleUser).book(sampleBook)
                .borrowDate(today.minusDays(13)).dueDate(tomorrow)
                .status(BorrowStatus.BORROWED).version(0L).build();

        BorrowRecord overdueRecord = BorrowRecord.builder()
                .id(2L).user(sampleUser).book(sampleBook)
                .borrowDate(today.minusDays(20)).dueDate(today.minusDays(6))
                .status(BorrowStatus.BORROWED).version(0L).build();

        when(borrowRecordRepository.findByDueDateAndStatus(tomorrow, BorrowStatus.BORROWED))
                .thenReturn(List.of(dueTomorrowRecord));
        when(borrowRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, today))
                .thenReturn(List.of(overdueRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        overdueScheduler.processOverdueAndReminders();

        verify(emailService, times(1)).sendOverdueReminder(sampleUser, sampleBook, dueTomorrowRecord);

        ArgumentCaptor<BorrowRecord> captor = ArgumentCaptor.forClass(BorrowRecord.class);
        verify(borrowRecordRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BorrowStatus.OVERDUE);
        assertThat(captor.getValue().getId()).isEqualTo(2L);
    }

    @Test
    void processOverdueAndReminders_KhiKhongCoBorrowRecord_KhongGuiEmailKhongLuu() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        when(borrowRecordRepository.findByDueDateAndStatus(tomorrow, BorrowStatus.BORROWED))
                .thenReturn(List.of());
        when(borrowRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, today))
                .thenReturn(List.of());

        overdueScheduler.processOverdueAndReminders();

        verify(emailService, never()).sendOverdueReminder(any(), any(), any());
        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void processOverdueAndReminders_KhiCoNhieuBorrowRecordQuaHan_CapNhatTatCa() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        BorrowRecord record1 = BorrowRecord.builder()
                .id(10L).user(sampleUser).book(sampleBook)
                .borrowDate(today.minusDays(20)).dueDate(today.minusDays(6))
                .status(BorrowStatus.BORROWED).version(0L).build();

        BorrowRecord record2 = BorrowRecord.builder()
                .id(11L).user(sampleUser).book(sampleBook)
                .borrowDate(today.minusDays(30)).dueDate(today.minusDays(16))
                .status(BorrowStatus.BORROWED).version(0L).build();

        when(borrowRecordRepository.findByDueDateAndStatus(eq(tomorrow), eq(BorrowStatus.BORROWED)))
                .thenReturn(List.of());
        when(borrowRecordRepository.findByStatusAndDueDateBefore(eq(BorrowStatus.BORROWED), eq(today)))
                .thenReturn(List.of(record1, record2));
        when(borrowRecordRepository.save(any(BorrowRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        overdueScheduler.processOverdueAndReminders();

        verify(borrowRecordRepository, times(2)).save(any(BorrowRecord.class));
        assertThat(record1.getStatus()).isEqualTo(BorrowStatus.OVERDUE);
        assertThat(record2.getStatus()).isEqualTo(BorrowStatus.OVERDUE);
    }

    @Test
    void processOverdueAndReminders_KhiGuiEmailThatBai_VanCapNhatOverdueBinhThuong() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        BorrowRecord dueTomorrowRecord = BorrowRecord.builder()
                .id(5L).user(sampleUser).book(sampleBook)
                .borrowDate(today.minusDays(13)).dueDate(tomorrow)
                .status(BorrowStatus.BORROWED).version(0L).build();

        when(borrowRecordRepository.findByDueDateAndStatus(tomorrow, BorrowStatus.BORROWED))
                .thenReturn(List.of(dueTomorrowRecord));
        when(borrowRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.BORROWED, today))
                .thenReturn(List.of());

        // EmailService tu xu ly loi ben trong, khong nem exception ra ngoai
        // nen scheduler van chay binh thuong
        overdueScheduler.processOverdueAndReminders();

        verify(emailService, times(1)).sendOverdueReminder(sampleUser, sampleBook, dueTomorrowRecord);
    }
}
