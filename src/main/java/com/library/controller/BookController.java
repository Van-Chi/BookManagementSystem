package com.library.controller;

import com.library.dto.BookRequestDTO;
import com.library.dto.BookResponseDTO;
import com.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller cung cap cac API quan ly sach (Book) cho he thong
 * Quan ly Thu vien. Chi nhan request va tra ve ResponseEntity, toan bo
 * logic nghiep vu duoc giao cho BookService xu ly.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Tao moi mot dau sach.
     * POST /api/books
     */
    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO createdBook = bookService.createBook(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**
     * Lay danh sach toan bo sach.
     * GET /api/books
     */
    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        List<BookResponseDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Lay thong tin chi tiet mot sach theo id.
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * Cap nhat thong tin mot sach theo id.
     * PUT /api/books/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO updatedBook = bookService.updateBook(id, requestDTO);
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Xoa mot sach theo id.
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
