package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO the hien mot dau sach trong danh sach "sach duoc muon nhieu nhat".
 * Duoc khoi tao truc tiep boi JPQL constructor expression trong BorrowRecordRepository,
 * nen thu tu va kieu du lieu cua cac field phai khop voi cau query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopBorrowedBookDTO {

    private Long bookId;
    private String title;
    private String author;
    private Long borrowCount;
}
