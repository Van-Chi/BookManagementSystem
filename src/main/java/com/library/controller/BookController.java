package com.library.controller;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Quản lý sách: tìm kiếm, thêm, sửa, xóa")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Tạo sách mới (ADMIN)", description = "Yêu cầu role ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo sách thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN")
    })
    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO createdBook = bookService.createBook(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @Operation(summary = "Tìm kiếm và lọc sách có phân trang")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về danh sách sách phân trang"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(
            @Parameter(description = "Từ khóa tìm kiếm theo tên sách hoặc tác giả")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo thể loại")
            @RequestParam(required = false) String category,
            @Parameter(description = "Lọc sách còn bản để mượn")
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<BookResponseDTO> books = bookService.searchBooks(keyword, category, available, pageable);
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Lấy thông tin chi tiết một sách theo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về thông tin sách"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sách")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(
            @Parameter(description = "ID của sách") @PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Cập nhật thông tin sách (ADMIN)", description = "Yêu cầu role ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sách")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(
            @Parameter(description = "ID của sách") @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO updatedBook = bookService.updateBook(id, requestDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Xóa sách (ADMIN)", description = "Yêu cầu role ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền ADMIN"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sách")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID của sách") @PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
