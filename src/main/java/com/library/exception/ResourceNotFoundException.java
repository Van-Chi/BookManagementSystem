package com.library.exception;

/**
 * Custom Exception duoc nem ra khi mot tai nguyen (vi du: Book) duoc yeu cau
 * truy van/cap nhat/xoa nhung khong ton tai trong Database.
 * Duoc GlobalExceptionHandler bat va chuyen thanh HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s khong tim thay voi %s = '%s'", resourceName, fieldName, fieldValue));
    }
}
