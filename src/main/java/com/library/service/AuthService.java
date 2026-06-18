package com.library.service;

import com.library.dto.AuthResponseDTO;
import com.library.dto.LoginRequestDTO;
import com.library.dto.RefreshTokenRequestDTO;
import com.library.dto.RegisterRequestDTO;

/**
 * Interface dinh nghia cac nghiep vu xac thuc (Authentication) cua he thong:
 * dang ky tai khoan moi va dang nhap.
 */
public interface AuthService {

    /**
     * Dang ky mot tai khoan moi voi vai tro MEMBER.
     * Mat khau duoc bam bang BCryptPasswordEncoder truoc khi luu.
     * Nem DuplicateResourceException neu username hoac email da ton tai.
     */
    AuthResponseDTO register(RegisterRequestDTO requestDTO);

    /**
     * Dang nhap bang username/password, tra ve JWT access token va refresh token.
     */
    AuthResponseDTO login(LoginRequestDTO requestDTO);

    /**
     * Cap access token moi tu refresh token hop le.
     * Token cu van con hieu luc den khi het han hoac bi revoke.
     */
    AuthResponseDTO refreshAccessToken(RefreshTokenRequestDTO requestDTO);

    /**
     * Thu hoi refresh token, ket thuc phien lam viec cua user.
     */
    void logout(RefreshTokenRequestDTO requestDTO);
}
