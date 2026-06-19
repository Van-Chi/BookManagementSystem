package com.library.repository;

import com.library.dto.TopBorrowedBookDTO;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository chiu trach nhiem giao tiep voi Database cho Entity BorrowRecord.
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByUserUsername(String username);

    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndStatus(Long userId, BorrowStatus status);

    List<BorrowRecord> findByUserUsernameAndStatus(String username, BorrowStatus status);

    /**
     * Tim phieu muon dang BORROWED cua mot sach cu the boi mot nguoi dung cu the.
     * Dung de kiem tra khi xu ly tra sach.
     */
    Optional<BorrowRecord> findByIdAndUserUsername(Long id, String username);

    /**
     * Dem so phieu muon theo trang thai, dung cho Dashboard (vi du: dang muon, da tra).
     */
    long countByStatus(BorrowStatus status);

    /**
     * Dem so phieu muon dang qua han: status truyen vao (thuong la BORROWED)
     * va dueDate da troi qua so voi ngay truyen vao (thuong la hom nay).
     */
    long countByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);

    /**
     * Kiem tra xem user da tung muon va tra sach nay chua (dieu kien de viet review).
     */
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, BorrowStatus status);

    /**
     * Lay danh sach sach duoc muon nhieu nhat, sap xep giam dan theo so lan muon.
     * Pageable dung de gioi han so luong ket qua tra ve (vi du: top 5).
     */
    @Query("SELECT new com.library.dto.TopBorrowedBookDTO(br.book.id, br.book.title, br.book.author, COUNT(br)) " +
            "FROM BorrowRecord br GROUP BY br.book.id, br.book.title, br.book.author ORDER BY COUNT(br) DESC")
    List<TopBorrowedBookDTO> findTopBorrowedBooks(Pageable pageable);

    /**
     * Tim cac phieu muon co dueDate bang mot ngay cu the va co status cho truoc.
     * Dung boi OverdueScheduler de tim cac phieu sap den han (dueDate = ngay mai).
     */
    List<BorrowRecord> findByDueDateAndStatus(LocalDate dueDate, BorrowStatus status);

    /**
     * Tim cac phieu muon co status cho truoc va dueDate truoc mot ngay cu the.
     * Dung boi OverdueScheduler de tim cac phieu da qua han can cap nhat OVERDUE.
     */
    List<BorrowRecord> findByStatusAndDueDateBefore(BorrowStatus status, LocalDate date);
}
