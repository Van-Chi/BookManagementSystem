package com.library.service;

import com.library.dto.BorrowRequestDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.BookNotAvailableException;
import com.library.exception.InvalidBorrowOperationException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test cho BorrowServiceImpl, su dung Mockito de mock cac Repository,
 * tap trung kiem tra logic nghiep vu muon/tra sach.
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    private BorrowServiceImpl borrowService;

    private Book sampleBook;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        borrowService = new BorrowServiceImpl(bookRepository, userRepository, borrowRecordRepository);

        sampleBook = Book.builder()
                .id(1L)
                .title("Clean Architecture")
                .author("Robert C. Martin")
                .isbn("9780134494166")
                .totalCopies(3)
                .availableCopies(3)
                .category("Cong nghe thong tin")
                .version(0L)
                .build();

        sampleUser = User.builder()
                .id(1L)
                .username("memberuser")
                .password("hashed-password")
                .email("member@example.com")
                .fullName("Thanh Vien Test")
                .role(Role.MEMBER)
                .version(0L)
                .build();
    }

    @Test
    void borrowBook_KhiSachConBan_GiamAvailableCopiesVaTaoBorrowRecord() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(userRepository.findByUsername("memberuser")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(invocation -> {
            BorrowRecord record = invocation.getArgument(0);
            record.setId(100L);
            return record;
        });

        BorrowResponseDTO result = borrowService.borrowBook(
                BorrowRequestDTO.builder().bookId(1L).build(), "memberuser");

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getBookId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BorrowStatus.BORROWED);
        assertThat(sampleBook.getAvailableCopies()).isEqualTo(2);
        verify(bookRepository, times(1)).save(sampleBook);
    }

    @Test
    void borrowBook_KhiSachKhongTonTai_NemResourceNotFoundException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.borrowBook(
                BorrowRequestDTO.builder().bookId(99L).build(), "memberuser"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void borrowBook_KhiSachHetBan_NemBookNotAvailableException() {
        sampleBook.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(userRepository.findByUsername("memberuser")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> borrowService.borrowBook(
                BorrowRequestDTO.builder().bookId(1L).build(), "memberuser"))
                .isInstanceOf(BookNotAvailableException.class);

        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void returnBook_KhiDangMuon_TangAvailableCopiesVaCapNhatStatus() {
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .id(100L)
                .book(sampleBook)
                .user(sampleUser)
                .status(BorrowStatus.BORROWED)
                .version(0L)
                .build();
        sampleBook.setAvailableCopies(2);

        when(borrowRecordRepository.findByIdAndUserUsername(100L, "memberuser"))
                .thenReturn(Optional.of(borrowRecord));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BorrowResponseDTO result = borrowService.returnBook(100L, "memberuser");

        assertThat(result.getStatus()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(sampleBook.getAvailableCopies()).isEqualTo(3);
    }

    @Test
    void returnBook_KhiDaTraTruocDo_NemInvalidBorrowOperationException() {
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .id(100L)
                .book(sampleBook)
                .user(sampleUser)
                .status(BorrowStatus.RETURNED)
                .version(1L)
                .build();

        when(borrowRecordRepository.findByIdAndUserUsername(100L, "memberuser"))
                .thenReturn(Optional.of(borrowRecord));

        assertThatThrownBy(() -> borrowService.returnBook(100L, "memberuser"))
                .isInstanceOf(InvalidBorrowOperationException.class);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void returnBook_KhiKhongTimThayPhieuMuonCuaNguoiDung_NemResourceNotFoundException() {
        when(borrowRecordRepository.findByIdAndUserUsername(eq(999L), eq("memberuser")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> borrowService.returnBook(999L, "memberuser"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMyBorrows_TraVeDanhSachPhieuMuonCuaNguoiDung() {
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .id(100L)
                .book(sampleBook)
                .user(sampleUser)
                .status(BorrowStatus.BORROWED)
                .build();

        when(borrowRecordRepository.findByUserUsername("memberuser")).thenReturn(List.of(borrowRecord));

        List<BorrowResponseDTO> result = borrowService.getMyBorrows("memberuser");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookTitle()).isEqualTo("Clean Architecture");
    }
}
