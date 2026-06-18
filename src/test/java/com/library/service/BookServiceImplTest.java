package com.library.service;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.entity.Book;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho BookServiceImpl, su dung Mockito de mock BookRepository,
 * tap trung kiem tra logic nghiep vu thuan tuy ma khong can khoi dong
 * Spring Context hay Database thuc te.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;
    private BookRequestDTO sampleRequestDTO;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(5)
                .availableCopies(5)
                .category("Cong nghe thong tin")
                .version(0L)
                .build();

        sampleRequestDTO = BookRequestDTO.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(5)
                .category("Cong nghe thong tin")
                .build();
    }

    @Test
    void createBook_KhiIsbnChuaTonTai_TraVeBookResponseDTO() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponseDTO result = bookService.createBook(sampleRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Clean Code");
        assertThat(result.getTotalCopies()).isEqualTo(5);
        assertThat(result.getAvailableCopies()).isEqualTo(5);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createBook_KhiIsbnDaTonTai_NemDuplicateResourceException() {
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(sampleRequestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(sampleRequestDTO.getIsbn());

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getBookById_KhiTonTai_TraVeBookResponseDTO() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookResponseDTO result = bookService.getBookById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIsbn()).isEqualTo("9780132350884");
    }

    @Test
    void getBookById_KhiKhongTonTai_NemResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllBooks_TraVeDanhSachBookResponseDTO() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<BookResponseDTO> result = bookService.getAllBooks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void updateBook_KhiHopLe_DieuChinhAvailableCopiesTuongUng() {
        // sampleBook: total=5, available=5 -> dang muon 0 cuon
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookRequestDTO updateRequest = BookRequestDTO.builder()
                .title("Clean Code (2nd Edition)")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(10)
                .category("Cong nghe thong tin")
                .build();

        BookResponseDTO result = bookService.updateBook(1L, updateRequest);

        assertThat(result.getTotalCopies()).isEqualTo(10);
        assertThat(result.getAvailableCopies()).isEqualTo(10);
        assertThat(result.getTitle()).isEqualTo("Clean Code (2nd Edition)");
    }

    @Test
    void updateBook_KhiGiamTotalCopiesThapHonSoLuongDangMuon_NemIllegalArgumentException() {
        // Gia lap 5 cuon dang duoc muon: total=5, available=0
        sampleBook.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookRequestDTO updateRequest = BookRequestDTO.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(2)
                .category("Cong nghe thong tin")
                .build();

        assertThatThrownBy(() -> bookService.updateBook(1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_KhiTonTai_GoiRepositoryDelete() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).delete(sampleBook);
    }

    @Test
    void deleteBook_KhiKhongTonTai_NemResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookRepository, never()).delete(any(Book.class));
    }
}
