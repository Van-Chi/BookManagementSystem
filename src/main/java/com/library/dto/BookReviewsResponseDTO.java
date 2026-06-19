package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReviewsResponseDTO {

    private Long bookId;
    private String bookTitle;
    private Double averageRating;
    private Long reviewCount;
    private List<ReviewResponseDTO> reviews;
}
