package com.library.scheduler;

import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.repository.BorrowRecordRepository;
import com.library.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueScheduler {

    private final BorrowRecordRepository borrowRecordRepository;
    private final EmailService emailService;

    /**
     * Chạy mỗi ngày lúc 8 giờ sáng:
     * 1. Gửi email nhắc nhở cho các phiếu mượn sắp đến hạn (dueDate = ngày mai).
     * 2. Cập nhật status = OVERDUE cho các phiếu đã quá hạn.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processOverdueAndReminders() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        sendDueTomorrowReminders(tomorrow);
        markOverdueRecords(today);
    }

    private void sendDueTomorrowReminders(LocalDate tomorrow) {
        List<BorrowRecord> dueTomorrow = borrowRecordRepository
                .findByDueDateAndStatus(tomorrow, BorrowStatus.BORROWED);

        log.info("Tìm thấy {} phiếu mượn đến hạn vào ngày mai ({}), gửi email nhắc nhở...",
                dueTomorrow.size(), tomorrow);

        for (BorrowRecord record : dueTomorrow) {
            emailService.sendOverdueReminder(record.getUser(), record.getBook(), record);
        }
    }

    private void markOverdueRecords(LocalDate today) {
        List<BorrowRecord> overdueRecords = borrowRecordRepository
                .findByStatusAndDueDateBefore(BorrowStatus.BORROWED, today);

        if (!overdueRecords.isEmpty()) {
            log.info("Cập nhật {} phiếu mượn sang trạng thái OVERDUE...", overdueRecords.size());
        }

        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowStatus.OVERDUE);
            borrowRecordRepository.save(record);
            log.info("OVERDUE: phiếu id={}, sách='{}', người dùng='{}', hạn trả={}",
                    record.getId(), record.getBook().getTitle(),
                    record.getUser().getUsername(), record.getDueDate());
        }
    }
}
