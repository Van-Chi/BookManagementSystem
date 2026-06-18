package com.library.service;

import com.library.dto.BorrowRequestDTO;
import com.library.dto.BorrowResponseDTO;

import java.util.List;

/**
 * Interface dinh nghia cac nghiep vu muon/tra sach cua he thong.
 */
public interface BorrowService {

    /**
     * Muon sach: giam availableCopies cua Book di 1 va tao moi mot
     * BorrowRecord voi trang thai BORROWED.
     * Nem ResourceNotFoundException neu sach hoac nguoi dung khong ton tai.
     * Nem BookNotAvailableException neu sach da het ban de cho muon.
     */
    BorrowResponseDTO borrowBook(BorrowRequestDTO requestDTO, String username);

    /**
     * Tra sach: tang availableCopies cua Book len 1 va cap nhat
     * BorrowRecord sang trang thai RETURNED.
     * Nem ResourceNotFoundException neu phieu muon khong ton tai.
     * Nem InvalidBorrowOperationException neu phieu muon khong thuoc ve
     * nguoi dung hien tai hoac sach da duoc tra truoc do.
     */
    BorrowResponseDTO returnBook(Long borrowRecordId, String username);

    /**
     * Lay danh sach lich su muon sach cua nguoi dung hien tai.
     */
    List<BorrowResponseDTO> getMyBorrows(String username);
}
