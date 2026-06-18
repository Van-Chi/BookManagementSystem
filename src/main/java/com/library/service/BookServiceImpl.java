package com.library.service;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.entity.Book;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Trien khai cac nghiep vu CRUD cho Book.
 * Cac phuong thuc lam thay doi du lieu (create/update/delete) deu duoc
 * danh dau @Transactional theo dung quy chuan cua du an, dam bao tinh
 * toan ven du lieu, dac biet la voi availableCopies.
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final String RESOURCE_NAME = "Book";

    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BookResponseDTO createBook(BookRequestDTO requestDTO) {
        if (bookRepository.existsByIsbn(requestDTO.getIsbn())) {
            throw new DuplicateResourceException(
                    String.format("Sach voi ISBN '%s' da ton tai trong he thong", requestDTO.getIsbn())
            );
        }

        Book book = Book.builder()
                .title(requestDTO.getTitle())
                .author(requestDTO.getAuthor())
                .isbn(requestDTO.getIsbn())
                .totalCopies(requestDTO.getTotalCopies())
                .availableCopies(requestDTO.getTotalCopies())
                .category(requestDTO.getCategory())
                .build();

        Book savedBook = bookRepository.save(book);
        return mapToResponseDTO(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long id) {
        Book book = findBookOrThrow(id);
        return mapToResponseDTO(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO requestDTO) {
        Book existingBook = findBookOrThrow(id);

        // Neu ISBN thay doi, kiem tra xem ISBN moi co bi trung voi sach khac khong.
        if (!existingBook.getIsbn().equals(requestDTO.getIsbn())
                && bookRepository.existsByIsbn(requestDTO.getIsbn())) {
            throw new DuplicateResourceException(
                    String.format("Sach voi ISBN '%s' da ton tai trong he thong", requestDTO.getIsbn())
            );
        }

        // So sach dang duoc muon = totalCopies cu - availableCopies cu.
        // Khi cap nhat totalCopies, availableCopies duoc dieu chinh tuong ung
        // de dam bao so luong sach dang duoc muon khong doi.
        int borrowedCopies = existingBook.getTotalCopies() - existingBook.getAvailableCopies();
        int newAvailableCopies = requestDTO.getTotalCopies() - borrowedCopies;

        if (newAvailableCopies < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Khong the cap nhat tong so luong sach xuong %d vi hien dang co %d ban duoc muon",
                            requestDTO.getTotalCopies(), borrowedCopies
                    )
            );
        }

        existingBook.setTitle(requestDTO.getTitle());
        existingBook.setAuthor(requestDTO.getAuthor());
        existingBook.setIsbn(requestDTO.getIsbn());
        existingBook.setTotalCopies(requestDTO.getTotalCopies());
        existingBook.setAvailableCopies(newAvailableCopies);
        existingBook.setCategory(requestDTO.getCategory());

        Book updatedBook = bookRepository.save(existingBook);
        return mapToResponseDTO(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);
        bookRepository.delete(book);
    }

    /**
     * Tim sach theo id, nem ResourceNotFoundException neu khong tim thay.
     */
    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, "id", id));
    }

    /**
     * Chuyen doi Entity Book sang BookResponseDTO de tra ve cho Client.
     */
    private BookResponseDTO mapToResponseDTO(Book book) {
        return BookResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .category(book.getCategory())
                .build();
    }
}
