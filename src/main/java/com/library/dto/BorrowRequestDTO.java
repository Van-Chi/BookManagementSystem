package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dung de nhan yeu cau muon sach tu Client.
 * Nguoi muon (user) duoc xac dinh tu thong tin xac thuc (JWT), khong nhan
 * truc tiep tu request de tranh gia mao.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRequestDTO {

    @NotNull(message = "Ma sach (bookId) khong duoc de trong")
    private Long bookId;
}
