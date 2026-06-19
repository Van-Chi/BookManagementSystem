package com.library.service;

import com.library.dto.BookReviewsResponseDTO;
import com.library.dto.ReviewRequestDTO;
import com.library.dto.ReviewResponseDTO;
import com.library.entity.Book;
import com.library.entity.BorrowStatus;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.exception.DuplicateResourceException;
import com.library.exception.InvalidBorrowOperationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(Long bookId, String username, ReviewRequestDTO requestDTO) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        boolean hasReturnedBook = borrowRecordRepository
                .existsByBookIdAndUserIdAndStatus(bookId, user.getId(), BorrowStatus.RETURNED);

        if (!hasReturnedBook) {
            throw new InvalidBorrowOperationException(
                    "Ban chi co the danh gia sach ma ban da muon va tra ve"
            );
        }

        if (reviewRepository.existsByBookIdAndUserId(bookId, user.getId())) {
            throw new DuplicateResourceException(
                    String.format("Ban da danh gia sach '%s' roi", book.getTitle())
            );
        }

        Review review = Review.builder()
                .book(book)
                .user(user)
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        return mapToResponseDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public BookReviewsResponseDTO getBookReviews(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        List<Review> reviews = reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        Double averageRating = reviewRepository.findAverageRatingByBookId(bookId).orElse(null);
        long reviewCount = reviewRepository.countByBookId(bookId);

        List<ReviewResponseDTO> reviewDTOs = reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return BookReviewsResponseDTO.builder()
                .bookId(bookId)
                .bookTitle(book.getTitle())
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .reviews(reviewDTOs)
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Long bookId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getBook().getId().equals(bookId)) {
            throw new ResourceNotFoundException("Review", "id", reviewId);
        }

        reviewRepository.delete(review);
    }

    private ReviewResponseDTO mapToResponseDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .bookId(review.getBook().getId())
                .username(review.getUser().getUsername())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
