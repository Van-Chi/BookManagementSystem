package com.library.service;

import com.library.dto.ChangePasswordRequestDTO;
import com.library.dto.UpdateProfileRequestDTO;
import com.library.dto.UserResponseDTO;
import com.library.entity.User;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO updateProfile(String username, UpdateProfileRequestDTO requestDTO) {
        User user = findUserOrThrow(username);

        if (!user.getEmail().equalsIgnoreCase(requestDTO.getEmail())
                && userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException(
                    String.format("Email '%s' da duoc su dung boi tai khoan khac", requestDTO.getEmail())
            );
        }

        user.setFullName(requestDTO.getFullName());
        user.setEmail(requestDTO.getEmail());
        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequestDTO requestDTO) {
        User user = findUserOrThrow(username);

        if (!passwordEncoder.matches(requestDTO.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Mat khau cu khong chinh xac");
        }

        user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
        userRepository.save(user);
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}
