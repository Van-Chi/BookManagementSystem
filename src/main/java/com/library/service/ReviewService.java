package com.library.service;

import com.library.dto.BookReviewsResponseDTO;
import com.library.dto.ReviewRequestDTO;
import com.library.dto.ReviewResponseDTO;

public interface ReviewService {

    ReviewResponseDTO createReview(Long bookId, String username, ReviewRequestDTO requestDTO);

    BookReviewsResponseDTO getBookReviews(Long bookId);

    void deleteReview(Long bookId, Long reviewId);
}
