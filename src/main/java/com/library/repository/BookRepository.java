package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository chiu trach nhiem giao tiep voi Database cho Entity Book.
 * Ke thua JpaRepository de duoc cung cap san cac phuong thuc CRUD co ban
 * (save, findById, findAll, deleteById, ...).
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Tim sach theo ma ISBN, dung de kiem tra trung lap khi tao moi sach.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Kiem tra nhanh su ton tai cua mot ISBN trong he thong.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Tong so luong ban sach (totalCopies) cua tat ca dau sach, dung cho Dashboard.
     */
    @Query("SELECT COALESCE(SUM(b.totalCopies), 0) FROM Book b")
    long sumTotalCopies();

    /**
     * Tong so luong ban sach con san (availableCopies) cua tat ca dau sach, dung cho Dashboard.
     */
    @Query("SELECT COALESCE(SUM(b.availableCopies), 0) FROM Book b")
    long sumAvailableCopies();
}
