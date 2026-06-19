package com.library.service;

import com.library.dto.ChangePasswordRequestDTO;
import com.library.dto.UpdateProfileRequestDTO;
import com.library.dto.UserResponseDTO;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("member01")
                .password("$2a$10$hashedPassword")
                .email("member@example.com")
                .fullName("Nguyen Van A")
                .role(Role.MEMBER)
                .version(0L)
                .build();
    }

    @Test
    void updateProfile_KhiEmailKhongThayDoi_CapNhatThanhCong() {
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .fullName("Nguyen Van B")
                .email("member@example.com")
                .build();

        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = userService.updateProfile("member01", request);

        assertThat(result.getFullName()).isEqualTo("Nguyen Van B");
        assertThat(result.getEmail()).isEqualTo("member@example.com");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateProfile_KhiEmailMoiChuaDuocDung_CapNhatThanhCong() {
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .fullName("Nguyen Van C")
                .email("newemail@example.com")
                .build();

        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = userService.updateProfile("member01", request);

        assertThat(result.getFullName()).isEqualTo("Nguyen Van C");
        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    void updateProfile_KhiEmailMoiDaTonTai_NemDuplicateResourceException() {
        UpdateProfileRequestDTO request = UpdateProfileRequestDTO.builder()
                .fullName("Nguyen Van C")
                .email("other@example.com")
                .build();

        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile("member01", request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("other@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_KhiUserKhongTonTai_NemResourceNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile("ghost",
                UpdateProfileRequestDTO.builder().fullName("X").email("x@x.com").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePassword_KhiMatKhauCuDung_DoiMatKhauThanhCong() {
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("OldPass1")
                .newPassword("NewPass1!")
                .build();

        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("OldPass1", sampleUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword("member01", request);

        verify(userRepository).save(any(User.class));
        assertThat(sampleUser.getPassword()).isEqualTo("$2a$10$newHashedPassword");
    }

    @Test
    void changePassword_KhiMatKhauCuSai_NemBadCredentialsException() {
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .oldPassword("WrongPass1")
                .newPassword("NewPass1!")
                .build();

        when(userRepository.findByUsername("member01")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPass1", sampleUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("member01", request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_KhiUserKhongTonTai_NemResourceNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword("ghost",
                ChangePasswordRequestDTO.builder().oldPassword("a").newPassword("b").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
