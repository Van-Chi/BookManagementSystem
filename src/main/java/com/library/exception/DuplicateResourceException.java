package com.library.exception;

/**
 * Custom Exception duoc nem ra khi du lieu tao moi vi pham rang buoc duy nhat
 * (vi du: ISBN da ton tai trong he thong). Duoc GlobalExceptionHandler bat
 * va chuyen thanh HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
