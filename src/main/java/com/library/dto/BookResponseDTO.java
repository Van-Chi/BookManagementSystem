package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dung de tra du lieu sach ra ngoai cho Client.
 * Khong bao gio tra truc tiep Entity Book ra API theo quy chuan cua du an.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDTO {

    private Long id;

    private String title;

    private String author;

    private String isbn;

    private Integer totalCopies;

    private Integer availableCopies;

    private String category;
}
