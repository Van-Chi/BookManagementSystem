package com.library.controller;

import com.library.dto.AuthResponseDTO;
import com.library.dto.LoginRequestDTO;
import com.library.dto.RefreshTokenRequestDTO;
import com.library.dto.RegisterRequestDTO;
import com.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký, đăng nhập, refresh token, logout và OAuth2")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản mới")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công, trả về JWT token"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc username/email đã tồn tại")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        AuthResponseDTO response = authService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Đăng nhập, nhận JWT access token và refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @ApiResponse(responseCode = "401", description = "Sai username hoặc password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        AuthResponseDTO response = authService.login(requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Làm mới access token từ refresh token còn hiệu lực")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về access token mới"),
            @ApiResponse(responseCode = "403", description = "Refresh token không hợp lệ hoặc đã hết hạn")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        AuthResponseDTO response = authService.refreshAccessToken(requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Đăng xuất, thu hồi refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "400", description = "Refresh token không hợp lệ")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        authService.logout(requestDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Chuyển hướng đến trang đăng nhập Google (OAuth2)")
    @ApiResponse(responseCode = "302", description = "Redirect đến Google OAuth2")
    @GetMapping("/oauth2/google")
    public void redirectToGoogleLogin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }
}
