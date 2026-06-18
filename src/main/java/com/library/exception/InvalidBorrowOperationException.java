package com.library.exception;

/**
 * Custom Exception duoc nem ra khi thao tac muon/tra sach vi pham quy tac
 * nghiep vu (vi du: tra mot sach khong phai do minh muon, hoac tra mot
 * sach da duoc tra truoc do). Duoc GlobalExceptionHandler bat va chuyen
 * thanh HTTP 400 Bad Request.
 */
public class InvalidBorrowOperationException extends RuntimeException {

    public InvalidBorrowOperationException(String message) {
        super(message);
    }
}
