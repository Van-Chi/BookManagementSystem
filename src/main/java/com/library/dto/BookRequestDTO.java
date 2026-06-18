package com.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dung de nhan du lieu tu Client khi tao moi hoac cap nhat sach.
 * Tach biet hoan toan voi Entity de tranh expose cau truc Database ra ngoai
 * va de kiem soat chat che du lieu dau vao thong qua Validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequestDTO {

    @NotBlank(message = "Ten sach (title) khong duoc de trong")
    private String title;

    @NotBlank(message = "Ten tac gia (author) khong duoc de trong")
    private String author;

    @NotBlank(message = "Ma ISBN khong duoc de trong")
    @Pattern(
            regexp = "^(?:\\d{10}|\\d{13})$",
            message = "Ma ISBN phai gom 10 hoac 13 chu so"
    )
    private String isbn;

    @NotNull(message = "Tong so luong sach (totalCopies) khong duoc de trong")
    @Min(value = 0, message = "Tong so luong sach phai lon hon hoac bang 0")
    private Integer totalCopies;

    @NotBlank(message = "The loai sach (category) khong duoc de trong")
    private String category;
}
