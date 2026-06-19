package com.library.service;

import com.library.dto.AdminUserResponseDTO;
import com.library.dto.BorrowResponseDTO;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            long activeBorrowCount = borrowRecordRepository.countByUserIdAndStatus(
                    user.getId(), BorrowStatus.BORROWED);
            return mapToAdminUserResponseDTO(user, activeBorrowCount);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        long activeBorrowCount = borrowRecordRepository.countByUserIdAndStatus(
                id, BorrowStatus.BORROWED);
        return mapToAdminUserResponseDTO(user, activeBorrowCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowResponseDTO> getUserBorrows(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return borrowRecordRepository.findByUserId(userId, pageable)
                .map(this::mapToBorrowResponseDTO);
    }

    @Override
    @Transactional
    public AdminUserResponseDTO changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);
        long activeBorrowCount = borrowRecordRepository.countByUserIdAndStatus(
                saved.getId(), BorrowStatus.BORROWED);
        return mapToAdminUserResponseDTO(saved, activeBorrowCount);
    }

    @Override
    @Transactional
    public AdminUserResponseDTO toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setEnabled(!user.isEnabled());
        User saved = userRepository.save(user);
        long activeBorrowCount = borrowRecordRepository.countByUserIdAndStatus(
                saved.getId(), BorrowStatus.BORROWED);
        return mapToAdminUserResponseDTO(saved, activeBorrowCount);
    }

    private AdminUserResponseDTO mapToAdminUserResponseDTO(User user, long activeBorrowCount) {
        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .activeBorrowCount(activeBorrowCount)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private BorrowResponseDTO mapToBorrowResponseDTO(BorrowRecord record) {
        return BorrowResponseDTO.builder()
                .id(record.getId())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .username(record.getUser().getUsername())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .build();
    }
}
