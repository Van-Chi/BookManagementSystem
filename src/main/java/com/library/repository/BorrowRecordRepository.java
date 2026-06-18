package com.library.repository;

import com.library.dto.TopBorrowedBookDTO;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
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
     * Lay danh sach sach duoc muon nhieu nhat, sap xep giam dan theo so lan muon.
     * Pageable dung de gioi han so luong ket qua tra ve (vi du: top 5).
     */
    @Query("SELECT new com.library.dto.TopBorrowedBookDTO(br.book.id, br.book.title, br.book.author, COUNT(br)) " +
            "FROM BorrowRecord br GROUP BY br.book.id, br.book.title, br.book.author ORDER BY COUNT(br) DESC")
    List<TopBorrowedBookDTO> findTopBorrowedBooks(Pageable pageable);
}
