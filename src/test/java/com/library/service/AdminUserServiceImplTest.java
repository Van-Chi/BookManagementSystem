package com.library.service;

import com.library.dto.AdminUserResponseDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User memberUser;
    private User adminUser;
    private Book book;
    private BorrowRecord borrowRecord;

    @BeforeEach
    void setUp() {
        memberUser = User.builder()
                .id(1L)
                .username("member")
                .password("encoded")
                .email("member@test.com")
                .fullName("Member User")
                .role(Role.MEMBER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .password("encoded")
                .email("admin@test.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build();

        book = Book.builder()
                .id(10L)
                .title("Spring Boot in Action")
                .author("Craig Walls")
                .isbn("9781617292545")
                .totalCopies(5)
                .availableCopies(3)
                .build();

        borrowRecord = BorrowRecord.builder()
                .id(100L)
                .book(book)
                .user(memberUser)
                .borrowDate(LocalDate.now().minusDays(3))
                .dueDate(LocalDate.now().plusDays(11))
                .status(BorrowStatus.BORROWED)
                .build();
    }

    @Test
    void getAllUsers_KhiCoUsers_TraVePageCoThongTinDung() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(memberUser, adminUser), pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(2L);
        when(borrowRecordRepository.countByUserIdAndStatus(2L, BorrowStatus.BORROWED)).thenReturn(0L);

        Page<AdminUserResponseDTO> result = adminUserService.getAllUsers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        AdminUserResponseDTO firstUser = result.getContent().get(0);
        assertThat(firstUser.getUsername()).isEqualTo("member");
        assertThat(firstUser.getActiveBorrowCount()).isEqualTo(2L);
        assertThat(firstUser.isEnabled()).isTrue();

        AdminUserResponseDTO secondUser = result.getContent().get(1);
        assertThat(secondUser.getUsername()).isEqualTo("admin");
        assertThat(secondUser.getActiveBorrowCount()).isEqualTo(0L);
    }

    @Test
    void getUserById_KhiUserTonTai_TraVeUserDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(memberUser));
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(1L);

        AdminUserResponseDTO result = adminUserService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("member");
        assertThat(result.getEmail()).isEqualTo("member@test.com");
        assertThat(result.getRole()).isEqualTo(Role.MEMBER);
        assertThat(result.getActiveBorrowCount()).isEqualTo(1L);
    }

    @Test
    void getUserById_KhiUserKhongTonTai_NemResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUserBorrows_KhiUserTonTai_TraVePageBorrows() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BorrowRecord> borrowPage = new PageImpl<>(List.of(borrowRecord), pageable, 1);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(borrowRecordRepository.findByUserId(1L, pageable)).thenReturn(borrowPage);

        Page<BorrowResponseDTO> result = adminUserService.getUserBorrows(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        BorrowResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getBookTitle()).isEqualTo("Spring Boot in Action");
        assertThat(dto.getUsername()).isEqualTo("member");
        assertThat(dto.getStatus()).isEqualTo(BorrowStatus.BORROWED);
    }

    @Test
    void getUserBorrows_KhiUserKhongTonTai_NemResourceNotFoundException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminUserService.getUserBorrows(99L, pageable))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(borrowRecordRepository, never()).findByUserId(any(), any());
    }

    @Test
    void changeUserRole_KhiUserTonTai_DoiRoleThanhCong() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(memberUser));
        when(userRepository.save(memberUser)).thenReturn(memberUser);
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(0L);

        AdminUserResponseDTO result = adminUserService.changeUserRole(1L, Role.ADMIN);

        assertThat(memberUser.getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(memberUser);
    }

    @Test
    void changeUserRole_KhiUserKhongTonTai_NemResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.changeUserRole(99L, Role.ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleUserEnabled_KhiUserDangEnabled_VoHieuHoaTaiKhoan() {
        assertThat(memberUser.isEnabled()).isTrue();
        when(userRepository.findById(1L)).thenReturn(Optional.of(memberUser));
        when(userRepository.save(memberUser)).thenReturn(memberUser);
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(0L);

        AdminUserResponseDTO result = adminUserService.toggleUserEnabled(1L);

        assertThat(memberUser.isEnabled()).isFalse();
        assertThat(result.isEnabled()).isFalse();
        verify(userRepository).save(memberUser);
    }

    @Test
    void toggleUserEnabled_KhiUserDangDisabled_KichHoatLaiTaiKhoan() {
        memberUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(memberUser));
        when(userRepository.save(memberUser)).thenReturn(memberUser);
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.BORROWED)).thenReturn(0L);

        AdminUserResponseDTO result = adminUserService.toggleUserEnabled(1L);

        assertThat(memberUser.isEnabled()).isTrue();
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void toggleUserEnabled_KhiUserKhongTonTai_NemResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.toggleUserEnabled(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}
