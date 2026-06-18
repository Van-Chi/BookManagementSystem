package com.library.service;

import com.library.config.JwtUtil;
import com.library.dto.AuthResponseDTO;
import com.library.dto.LoginRequestDTO;
import com.library.dto.RefreshTokenRequestDTO;
import com.library.dto.RegisterRequestDTO;
import com.library.entity.RefreshToken;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.DuplicateResourceException;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Trien khai cac nghiep vu xac thuc: dang ky tai khoan moi va dang nhap,
 * cap JWT token cho Client su dung cho cac request tiep theo.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new DuplicateResourceException(
                    String.format("Ten dang nhap '%s' da duoc su dung", requestDTO.getUsername()));
        }
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException(
                    String.format("Email '%s' da duoc su dung", requestDTO.getEmail()));
        }

        User newUser = User.builder()
                .username(requestDTO.getUsername())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .email(requestDTO.getEmail())
                .fullName(requestDTO.getFullName())
                .role(Role.MEMBER)
                .build();

        User savedUser = userRepository.save(newUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponseDTO.builder()
                .token(accessToken)
                .tokenType("Bearer")
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.getUsername(), requestDTO.getPassword())
        );

        User user = userRepository.findByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Nguoi dung da duoc xac thuc nhung khong tim thay trong Database"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponseDTO.builder()
                .token(accessToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshAccessToken(RefreshTokenRequestDTO requestDTO) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(requestDTO.getRefreshToken());

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtUtil.generateToken(userDetails);

        // Token rotation: cap refresh token moi va huy token cu
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponseDTO.builder()
                .token(newAccessToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequestDTO requestDTO) {
        refreshTokenService.revokeRefreshToken(requestDTO.getRefreshToken());
    }
}
