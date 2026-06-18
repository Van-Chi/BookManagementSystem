package com.library.service;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;

import java.util.List;

/**
 * Interface dinh nghia cac nghiep vu (business logic) lien quan den Book.
 * Controller chi duoc phep goi qua interface nay, khong duoc phep truy cap
 * truc tiep BookRepository.
 */
public interface BookService {

    /**
     * Tao moi mot dau sach. availableCopies se duoc tu dong gan bang
     * totalCopies tai thoi diem tao.
     */
    BookResponseDTO createBook(BookRequestDTO requestDTO);

    /**
     * Lay thong tin chi tiet mot dau sach theo id.
     * Nem ResourceNotFoundException neu khong tim thay.
     */
    BookResponseDTO getBookById(Long id);

    /**
     * Lay danh sach toan bo dau sach hien co trong he thong.
     */
    List<BookResponseDTO> getAllBooks();

    /**
     * Cap nhat thong tin mot dau sach theo id.
     * Nem ResourceNotFoundException neu khong tim thay.
     */
    BookResponseDTO updateBook(Long id, BookRequestDTO requestDTO);

    /**
     * Xoa mot dau sach theo id.
     * Nem ResourceNotFoundException neu khong tim thay.
     */
    void deleteBook(Long id);
}
