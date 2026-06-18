package com.library.exception;

/**
 * Custom Exception duoc nem ra khi mot sach khong con ban nao de cho muon
 * (availableCopies = 0). Duoc GlobalExceptionHandler bat va chuyen thanh
 * HTTP 409 Conflict.
 */
public class BookNotAvailableException extends RuntimeException {

    public BookNotAvailableException(String message) {
        super(message);
    }
}
