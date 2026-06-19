package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dữ liệu tạo đánh giá sách")
public class ReviewRequestDTO {

    @Schema(description = "Điểm đánh giá từ 1 đến 5", example = "4")
    @NotNull(message = "Diem danh gia khong duoc de trong")
    @Min(value = 1, message = "Diem danh gia toi thieu la 1")
    @Max(value = 5, message = "Diem danh gia toi da la 5")
    private Integer rating;

    @Schema(description = "Bình luận (tối đa 1000 ký tự)", example = "Sách rất hay và dễ hiểu!")
    @Size(max = 1000, message = "Binh luan khong duoc vuot qua 1000 ky tu")
    private String comment;
}
