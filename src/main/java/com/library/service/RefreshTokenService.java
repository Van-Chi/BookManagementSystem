package com.library.service;

import com.library.entity.RefreshToken;
import com.library.entity.User;

public interface RefreshTokenService {

    /**
     * Tao refresh token moi cho user (huy tat ca token cu cua user do truoc).
     */
    RefreshToken createRefreshToken(User user);

    /**
     * Xac thuc refresh token: kiem tra ton tai, chua bi thu hoi va chua het han.
     * Nem InvalidTokenException neu bat ky dieu kien nao khong thoa man.
     */
    RefreshToken verifyRefreshToken(String token);

    /**
     * Thu hoi (revoke) mot refresh token cu the.
     */
    void revokeRefreshToken(String token);

    /**
     * Thu hoi tat ca refresh token cua mot user (dung khi logout khoi tat ca thiet bi).
     */
    void revokeAllUserTokens(User user);
}
