package com.library.service;

import com.library.dto.ChangePasswordRequestDTO;
import com.library.dto.UpdateProfileRequestDTO;
import com.library.dto.UserResponseDTO;

public interface UserService {

    UserResponseDTO updateProfile(String username, UpdateProfileRequestDTO requestDTO);

    void changePassword(String username, ChangePasswordRequestDTO requestDTO);
}
