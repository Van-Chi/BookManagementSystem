package com.library.service;

import com.library.entity.RefreshToken;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.InvalidTokenException;
import com.library.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test cho RefreshTokenServiceImpl, su dung Mockito de mock RefreshTokenRepository,
 * kiem tra toan bo logic tao moi, xac thuc va thu hoi refresh token.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationMs", 604800000L);

        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.MEMBER)
                .build();
    }

    // --- createRefreshToken ---

    @Test
    void createRefreshToken_KhiTaoMoi_XoaTokenCuVaTaoTokenMoi() {
        RefreshToken savedToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .token("uuid-token")
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken result = refreshTokenService.createRefreshToken(sampleUser);

        verify(refreshTokenRepository, times(1)).deleteByUser(sampleUser);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(sampleUser);
        assertThat(result.isRevoked()).isFalse();
    }

    @Test
    void createRefreshToken_KhiTaoMoi_ExpiryDateNamTrongTuongLai() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        when(refreshTokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.createRefreshToken(sampleUser);

        RefreshToken captured = captor.getValue();
        assertThat(captured.getExpiryDate()).isAfter(Instant.now());
        assertThat(captured.getToken()).isNotBlank();
        assertThat(captured.isRevoked()).isFalse();
    }

    // --- verifyRefreshToken ---

    @Test
    void verifyRefreshToken_KhiTokenHopLe_TraVeRefreshToken() {
        RefreshToken validToken = RefreshToken.builder()
                .token("valid-token")
                .user(sampleUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        RefreshToken result = refreshTokenService.verifyRefreshToken("valid-token");

        assertThat(result).isEqualTo(validToken);
    }

    @Test
    void verifyRefreshToken_KhiTokenKhongTonTai_NemInvalidTokenException() {
        when(refreshTokenRepository.findByToken("not-exist")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("not-exist"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("khong hop le hoac khong ton tai");
    }

    @Test
    void verifyRefreshToken_KhiTokenDaBiRevoke_NemInvalidTokenException() {
        RefreshToken revokedToken = RefreshToken.builder()
                .token("revoked-token")
                .user(sampleUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("revoked-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("da bi thu hoi");
    }

    @Test
    void verifyRefreshToken_KhiTokenHetHan_NemInvalidTokenException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(sampleUser)
                .expiryDate(Instant.now().minusSeconds(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("expired-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("da het han");
    }

    // --- revokeRefreshToken ---

    @Test
    void revokeRefreshToken_KhiTokenTonTai_DatRevokedThanhTrue() {
        RefreshToken activeToken = RefreshToken.builder()
                .token("active-token")
                .user(sampleUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("active-token")).thenReturn(Optional.of(activeToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(activeToken);

        refreshTokenService.revokeRefreshToken("active-token");

        assertThat(activeToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(activeToken);
    }

    @Test
    void revokeRefreshToken_KhiTokenKhongTonTai_NemInvalidTokenException() {
        when(refreshTokenRepository.findByToken("ghost-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken("ghost-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    // --- revokeAllUserTokens ---

    @Test
    void revokeAllUserTokens_KhiGoiVoiUser_XoaTatCaTokenCuaUser() {
        refreshTokenService.revokeAllUserTokens(sampleUser);

        verify(refreshTokenRepository, times(1)).deleteByUser(sampleUser);
        verify(refreshTokenRepository, never()).save(any());
    }
}
