package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dữ liệu tạo mới hoặc cập nhật sách")
public class BookRequestDTO {

    @Schema(description = "Tên sách", example = "Lập trình Java hiện đại")
    @NotBlank(message = "Ten sach (title) khong duoc de trong")
    private String title;

    @Schema(description = "Tác giả", example = "Nguyen Van A")
    @NotBlank(message = "Ten tac gia (author) khong duoc de trong")
    private String author;

    @Schema(description = "Mã ISBN (10 hoặc 13 chữ số)", example = "9781234567897")
    @NotBlank(message = "Ma ISBN khong duoc de trong")
    @Pattern(
            regexp = "^(?:\\d{10}|\\d{13})$",
            message = "Ma ISBN phai gom 10 hoac 13 chu so"
    )
    private String isbn;

    @Schema(description = "Tổng số bản sách", example = "5")
    @NotNull(message = "Tong so luong sach (totalCopies) khong duoc de trong")
    @Min(value = 0, message = "Tong so luong sach phai lon hon hoac bang 0")
    private Integer totalCopies;

    @Schema(description = "Thể loại sách", example = "Công nghệ thông tin")
    @NotBlank(message = "The loai sach (category) khong duoc de trong")
    private String category;
}
