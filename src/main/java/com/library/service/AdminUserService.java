package com.library.service;

import com.library.dto.AdminUserResponseDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<AdminUserResponseDTO> getAllUsers(Pageable pageable);

    AdminUserResponseDTO getUserById(Long id);

    Page<BorrowResponseDTO> getUserBorrows(Long userId, Pageable pageable);

    AdminUserResponseDTO changeUserRole(Long userId, Role newRole);

    AdminUserResponseDTO toggleUserEnabled(Long userId);
}
