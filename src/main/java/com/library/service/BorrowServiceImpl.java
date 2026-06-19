package com.library.service;

import com.library.dto.BorrowRequestDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.User;
import com.library.exception.BookNotAvailableException;
import com.library.exception.InvalidBorrowOperationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Trien khai cac nghiep vu muon/tra sach.
 * Cac phuong thuc lam thay doi availableCopies cua Book deu duoc danh dau
 * @Transactional, ket hop voi annotation @Version tren Entity Book de
 * dam bao co che Optimistic Locking, tranh tinh huong Race Condition khi
 * nhieu nguoi dung cung muon mot cuon sach tai mot thoi diem.
 */
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public BorrowResponseDTO borrowBook(BorrowRequestDTO requestDTO, String username) {
        Book book = bookRepository.findById(requestDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", requestDTO.getBookId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException(
                    String.format("Sach '%s' hien khong con ban nao de cho muon", book.getTitle()));
        }

        // Giam availableCopies. Hibernate se tu dong kiem tra truong @Version
        // khi flush/commit; neu co request khac da cap nhat truoc, mot
        // ObjectOptimisticLockingFailureException se duoc nem ra va duoc
        // GlobalExceptionHandler chuyen thanh HTTP 409 Conflict.
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        LocalDate today = LocalDate.now();
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .book(book)
                .user(user)
                .borrowDate(today)
                .dueDate(today.plusDays(DEFAULT_LOAN_PERIOD_DAYS))
                .status(BorrowStatus.BORROWED)
                .build();

        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        emailService.sendBorrowConfirmation(user, book, savedRecord);
        return mapToResponseDTO(savedRecord);
    }

    @Override
    @Transactional
    public BorrowResponseDTO returnBook(Long borrowRecordId, String username) {
        BorrowRecord borrowRecord = borrowRecordRepository.findByIdAndUserUsername(borrowRecordId, username)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord", "id", borrowRecordId));

        if (borrowRecord.getStatus() == BorrowStatus.RETURNED) {
            throw new InvalidBorrowOperationException(
                    String.format("Phieu muon id=%d da duoc tra truoc do", borrowRecordId));
        }

        User user = borrowRecord.getUser();
        Book book = borrowRecord.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        borrowRecord.setReturnDate(LocalDate.now());
        borrowRecord.setStatus(BorrowStatus.RETURNED);
        BorrowRecord updatedRecord = borrowRecordRepository.save(borrowRecord);
        emailService.sendReturnConfirmation(user, book, updatedRecord);
        return mapToResponseDTO(updatedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowResponseDTO> getMyBorrows(String username) {
        return borrowRecordRepository.findByUserUsername(username)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private BorrowResponseDTO mapToResponseDTO(BorrowRecord borrowRecord) {
        return BorrowResponseDTO.builder()
                .id(borrowRecord.getId())
                .bookId(borrowRecord.getBook().getId())
                .bookTitle(borrowRecord.getBook().getTitle())
                .username(borrowRecord.getUser().getUsername())
                .borrowDate(borrowRecord.getBorrowDate())
                .dueDate(borrowRecord.getDueDate())
                .returnDate(borrowRecord.getReturnDate())
                .status(borrowRecord.getStatus())
                .build();
    }
}
