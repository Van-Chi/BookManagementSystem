package com.library.service;

import com.library.dto.BookReviewsResponseDTO;
import com.library.dto.ReviewRequestDTO;
import com.library.dto.ReviewResponseDTO;
import com.library.entity.Book;
import com.library.entity.BorrowStatus;
import com.library.entity.Review;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.DuplicateResourceException;
import com.library.exception.InvalidBorrowOperationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Book sampleBook;
    private User sampleUser;
    private Review sampleReview;
    private ReviewRequestDTO reviewRequest;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("9780132350884")
                .totalCopies(5)
                .availableCopies(4)
                .category("CNTT")
                .version(0L)
                .build();

        sampleUser = User.builder()
                .id(10L)
                .username("member01")
                .password("$2a$10$hashed")
                .email("member@example.com")
                .fullName("Nguyen Van A")
                .role(Role.MEMBER)
                .version(0L)
                .build();

        sampleReview = Review.builder()
                .id(100L)
                .book(sampleBook)
                .user(sampleUser)
                .rating(5)
                .comment("Sach rat hay!")
                .createdAt(LocalDateTime.now())
                .build();

        reviewRequest = ReviewRequestDTO.builder()
                .rating(5)
                .comment("Sach rat hay!")
                .build();
    }

    @Test
    void createReview_KhiHopLe_TraVeReviewResponseDTO() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(borrowRecordRepository.existsByBookIdAndUserIdAndStatus(1L, 10L, BorrowStatus.RETURNED))
                .thenReturn(true);
        when(reviewRepository.existsByBookIdAndUserId(1L, 10L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);

        ReviewResponseDTO result = reviewService.createReview(1L, "member01", reviewRequest);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Sach rat hay!");
        assertThat(result.getUsername()).isEqualTo("member01");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_KhiChuaTraSach_NemInvalidBorrowOperationException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(borrowRecordRepository.existsByBookIdAndUserIdAndStatus(1L, 10L, BorrowStatus.RETURNED))
                .thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(1L, "member01", reviewRequest))
                .isInstanceOf(InvalidBorrowOperationException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_KhiDaDanhGiaSachNay_NemDuplicateResourceException() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(borrowRecordRepository.existsByBookIdAndUserIdAndStatus(1L, 10L, BorrowStatus.RETURNED))
                .thenReturn(true);
        when(reviewRepository.existsByBookIdAndUserId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(1L, "member01", reviewRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Clean Code");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_KhiSachKhongTonTai_NemResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(99L, "member01", reviewRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBookReviews_TraVeBookReviewsResponseDTOHopLe() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(sampleReview));
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(Optional.of(5.0));
        when(reviewRepository.countByBookId(1L)).thenReturn(1L);

        BookReviewsResponseDTO result = reviewService.getBookReviews(1L);

        assertThat(result.getBookId()).isEqualTo(1L);
        assertThat(result.getBookTitle()).isEqualTo("Clean Code");
        assertThat(result.getAverageRating()).isEqualTo(5.0);
        assertThat(result.getReviewCount()).isEqualTo(1L);
        assertThat(result.getReviews()).hasSize(1);
    }

    @Test
    void getBookReviews_KhiChuaCoReview_AverageRatingLaNull() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(reviewRepository.findByBookIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        when(reviewRepository.findAverageRatingByBookId(1L)).thenReturn(Optional.empty());
        when(reviewRepository.countByBookId(1L)).thenReturn(0L);

        BookReviewsResponseDTO result = reviewService.getBookReviews(1L);

        assertThat(result.getAverageRating()).isNull();
        assertThat(result.getReviewCount()).isEqualTo(0L);
        assertThat(result.getReviews()).isEmpty();
    }

    @Test
    void getBookReviews_KhiSachKhongTonTai_NemResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getBookReviews(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteReview_KhiHopLe_XoaThanhCong() {
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(sampleReview));

        reviewService.deleteReview(1L, 100L);

        verify(reviewRepository).delete(sampleReview);
    }

    @Test
    void deleteReview_KhiReviewKhongThuocSach_NemResourceNotFoundException() {
        Book otherBook = Book.builder().id(99L).title("Other Book").build();
        Review reviewOfOtherBook = Review.builder()
                .id(100L)
                .book(otherBook)
                .user(sampleUser)
                .rating(3)
                .build();

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(reviewOfOtherBook));

        assertThatThrownBy(() -> reviewService.deleteReview(1L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void deleteReview_KhiReviewKhongTonTai_NemResourceNotFoundException() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
