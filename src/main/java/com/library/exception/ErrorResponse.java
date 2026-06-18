package com.library.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Cau truc JSON dong nhat duoc tra ve cho moi loi xay ra trong he thong,
 * theo dung quy chuan da dinh nghia trong CLAUDE.md:
 * Timestamp, Status, Error, Message, Path.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    /**
     * Danh sach chi tiet loi Validation theo tung field (chi xuat hien khi
     * loi phat sinh tu @Valid tren cac DTO request).
     */
    private Map<String, String> errors;
}
