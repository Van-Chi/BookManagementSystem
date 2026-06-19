package com.library.controller;

import com.library.dto.BookReviewsResponseDTO;
import com.library.dto.ReviewRequestDTO;
import com.library.dto.ReviewResponseDTO;
import com.library.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Đánh giá sách: tạo, xem và xóa review")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Tạo đánh giá cho sách", description = "Chỉ user đã mượn và trả sách mới được đánh giá. Mỗi user chỉ đánh giá một lần cho mỗi sách.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đánh giá thành công"),
            @ApiResponse(responseCode = "400", description = "User chưa từng mượn/trả sách này hoặc đã đánh giá rồi"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sách")
    })
    @PostMapping("/{bookId}/reviews")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Parameter(description = "ID của sách") @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReviewResponseDTO review = reviewService.createReview(bookId, userDetails.getUsername(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @Operation(summary = "Lấy danh sách đánh giá của một cuốn sách kèm rating trung bình")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về danh sách review và rating trung bình"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sách")
    })
    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<BookReviewsResponseDTO> getBookReviews(
            @Parameter(description = "ID của sách") @PathVariable Long bookId) {
        BookReviewsResponseDTO reviews = reviewService.getBookReviews(bookId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Xóa review vi phạm (ADMIN)", description = "Yêu cầu role ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa review thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy review")
    })
    @DeleteMapping("/{bookId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID của sách") @PathVariable Long bookId,
            @Parameter(description = "ID của review") @PathVariable Long reviewId) {
        reviewService.deleteReview(bookId, reviewId);
        return ResponseEntity.noContent().build();
    }
}
