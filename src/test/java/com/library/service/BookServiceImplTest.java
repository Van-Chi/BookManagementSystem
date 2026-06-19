package com.library.service;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.entity.Book;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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

    @Mock
    private ReviewRepository reviewRepository;

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

        // mapToResponseDTO goi reviewRepository de lay averageRating va reviewCount
        lenient().when(reviewRepository.findAverageRatingByBookId(anyLong())).thenReturn(Optional.empty());
        lenient().when(reviewRepository.countByBookId(anyLong())).thenReturn(0L);
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
    void searchBooks_KhongCoFilter_TraVeToanBoSach() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook), pageable, 1));

        Page<BookResponseDTO> result = bookService.searchBooks(null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void searchBooks_VoiKeyword_ChiTraVeSachKhop() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook), pageable, 1));

        Page<BookResponseDTO> result = bookService.searchBooks("clean", null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void searchBooks_VoiCategory_ChiTraVeSachCungTheLoai() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook), pageable, 1));

        Page<BookResponseDTO> result = bookService.searchBooks(null, "Cong nghe thong tin", null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("Cong nghe thong tin");
    }

    @Test
    void searchBooks_VoiAvailableTrue_ChiTraVeSachConKhaDung() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook), pageable, 1));

        Page<BookResponseDTO> result = bookService.searchBooks(null, null, true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAvailableCopies()).isGreaterThan(0);
    }

    @Test
    void searchBooks_KhiKhongCoBanGhi_TraVePageRong() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        Page<BookResponseDTO> result = bookService.searchBooks("xyz_khong_ton_tai", null, null, pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchBooks_VoiPhanTrang_TraVeMetadataDung() {
        Pageable pageable = PageRequest.of(0, 2);
        Book book2 = Book.builder().id(2L).title("Effective Java").author("Joshua Bloch")
                .isbn("9780134685991").totalCopies(3).availableCopies(3).category("CNTT").version(0L).build();
        when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleBook, book2), pageable, 5));

        Page<BookResponseDTO> result = bookService.searchBooks(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
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
