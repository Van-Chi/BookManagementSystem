package com.library.controller;

import com.library.dto.AuthResponseDTO;
import com.library.dto.LoginRequestDTO;
import com.library.dto.RefreshTokenRequestDTO;
import com.library.dto.RegisterRequestDTO;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller cung cap API xac thuc: dang ky tai khoan moi va dang nhap.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Dang ky tai khoan moi.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        AuthResponseDTO response = authService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Dang nhap, tra ve JWT access token va refresh token.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        AuthResponseDTO response = authService.login(requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Cap access token moi tu refresh token con hieu luc.
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        AuthResponseDTO response = authService.refreshAccessToken(requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Thu hoi refresh token, ket thuc phien dang nhap hien tai.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        authService.logout(requestDTO);
        return ResponseEntity.noContent().build();
    }
}
