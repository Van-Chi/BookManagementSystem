package com.library.service;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface dinh nghia cac nghiep vu (business logic) lien quan den Book.
 * Controller chi duoc phep goi qua interface nay, khong duoc phep truy cap
 * truc tiep BookRepository.
 */
public interface BookService {

    BookResponseDTO createBook(BookRequestDTO requestDTO);

    BookResponseDTO getBookById(Long id);

    /**
     * Tim kiem va loc sach theo keyword (title/author), category, va tinh kha dung.
     * Ket qua duoc phan trang theo Pageable.
     */
    Page<BookResponseDTO> searchBooks(String keyword, String category, Boolean available, Pageable pageable);

    BookResponseDTO updateBook(Long id, BookRequestDTO requestDTO);

    void deleteBook(Long id);
}
