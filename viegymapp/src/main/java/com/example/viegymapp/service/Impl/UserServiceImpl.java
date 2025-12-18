package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.PagingResponse;
import com.example.viegymapp.dto.request.*;
import com.example.viegymapp.dto.response.UserResponse;
import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.entity.Enum.UserStatus;
import com.example.viegymapp.entity.Role;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.UserRole;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.UserMapper;
import com.example.viegymapp.repository.*;
import com.example.viegymapp.service.UserService;
import com.example.viegymapp.service.CloudinaryService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final EntityManager entityManager;

    // ============ GET CURRENT USER ============
    @Override
    public UserResponse getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    // ============ Helper: Get current user entity ============
    private User getCurrentUserEntity() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    // ============ UPDATE PROFILE ============
    @Transactional
    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUserEntity();
        userMapper.updateProfile(user, request);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    // ============ UPDATE AVATAR ============
    @Override
    public UserResponse updateAvatar(MultipartFile file) throws IOException {
        User user = getCurrentUserEntity();

        // Validate file
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }

        try {
            // Upload to Cloudinary
            String avatarUrl = cloudinaryService.uploadFile(file);
            log.info("Uploaded avatar to Cloudinary for user: {}", user.getEmail());

            // Update database
            user.setAvatarUrl(avatarUrl);
            User savedUser = userRepository.save(user);
            
            log.info("Avatar updated successfully for user: {}", user.getEmail());
            return userMapper.toUserResponse(savedUser);
        } catch (IOException e) {
            log.error("Failed to upload avatar: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    // ============ UPDATE SETTINGS ============
    @Transactional
    @Override
    public UserResponse updateSettings(UpdateSettingsRequest request) {
        User user = getCurrentUserEntity();
        userMapper.updateSettings(user, request);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    // ============ UPDATE DAILY GOALS ============
    @Transactional
    @Override
    public UserResponse updateDailyGoals(UpdateDailyGoalsRequest request) {
        User user = getCurrentUserEntity();
        userMapper.updateDailyGoals(user, request);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    // ============ INCREMENT WORKOUT STATS ============
    @Transactional
    @Override
    public void incrementWorkoutStats(Double volume) {
        User user = getCurrentUserEntity();
        user.setTotalWorkouts(user.getTotalWorkouts() + 1);
        user.setTotalVolume(user.getTotalVolume() + volume);
        userRepository.save(user);
    }

    // ============ UPDATE STREAK ============
    @Transactional
    @Override
    public void updateStreak(Integer streakDays) {
        User user = getCurrentUserEntity();
        user.setStreakDays(streakDays);
        userRepository.save(user);
    }

    // ============ EXISTING METHODS (giữ nguyên) ============

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        Role defaultRole = roleRepository.findByName(PredefinedRole.ROLE_USER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(defaultRole)
                .assignedBy(user)
                .build();

        user.getUserRoles().add(userRole);
        defaultRole.getUserRoles().add(userRole);

        user = userRepository.save(user);
        userRoleRepository.save(userRole);

        return userMapper.toUserResponse(user);
    }

    @Transactional
    @Override
    public UserResponse updateCurrentUser(UserUpdateRequest request) {
        User user = getCurrentUserEntity();
        String oldPassword = user.getPassword();

        userMapper.updateUser(user, request);
        user.setPassword(oldPassword);

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_USED);
            }
            user.setEmail(request.getEmail());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Transactional
    @Override
    public UserResponse disableUser(UUID userId) {
        User currentUser = getCurrentUserEntity();
        
        // Prevent disabling yourself
        if (currentUser.getId().equals(userId)) {
            throw new AppException(ErrorCode.CANNOT_DISABLE_SELF);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Check if user being disabled is an admin
        boolean isTargetAdmin = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == PredefinedRole.ROLE_ADMIN 
                        || ur.getRole().getName() == PredefinedRole.ROLE_SUPER_ADMIN);
        
        // Only SUPER_ADMIN can disable ADMIN
        if (isTargetAdmin) {
            boolean isSuperAdmin = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName() == PredefinedRole.ROLE_SUPER_ADMIN);
            if (!isSuperAdmin) {
                throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
            }
        }

        user.setStatus(UserStatus.SUSPENDED);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponse enableUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    @Override
    public UserResponse updateUserById(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_USED);
            }
            user.setEmail(request.getEmail());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @Override
    public PagingResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> result = userRepository.findAll(pageable);
        return new PagingResponse<>(
                result.getContent().stream().map(userMapper::toUserResponse).toList(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Override
    public UserResponse assignRoleToUser(UUID userId, PredefinedRole roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User currentUser = getCurrentUserEntity();

        // Only SUPER_ADMIN can assign ADMIN or SUPER_ADMIN role
        if (roleName == PredefinedRole.ROLE_ADMIN || roleName == PredefinedRole.ROLE_SUPER_ADMIN) {
            boolean isSuperAdmin = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName() == PredefinedRole.ROLE_SUPER_ADMIN);
            if (!isSuperAdmin) {
                throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
            }
        }

        boolean exists = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName().equals(roleName));

        if (!exists) {
            UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(role)
                    .assignedBy(currentUser)
                    .build();

            user.getUserRoles().add(userRole);
            userRoleRepository.save(userRole);
        }
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse removeRoleFromUser(UUID userId, PredefinedRole roleName) {
        User currentUser = getCurrentUserEntity();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Only SUPER_ADMIN can remove ADMIN or SUPER_ADMIN role
        if (roleName == PredefinedRole.ROLE_ADMIN || roleName == PredefinedRole.ROLE_SUPER_ADMIN) {
            boolean isSuperAdmin = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName() == PredefinedRole.ROLE_SUPER_ADMIN);
            if (!isSuperAdmin) {
                throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
            }
        }

        UserRole userRoleToRemove = user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.getUserRoles().remove(userRoleToRemove);
        userRoleRepository.delete(userRoleToRemove);

        return userMapper.toUserResponse(user);
    }

    @Transactional
    @Override
    public void deleteUser(UUID id) {
        User currentUser = getCurrentUserEntity();
        
        // Prevent deleting yourself
        if (currentUser.getId().equals(id)) {
            throw new AppException(ErrorCode.CANNOT_DELETE_SELF);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if user being deleted is an admin
        boolean isTargetAdmin = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == PredefinedRole.ROLE_ADMIN 
                        || ur.getRole().getName() == PredefinedRole.ROLE_SUPER_ADMIN);
        
        // Only SUPER_ADMIN can delete ADMIN
        if (isTargetAdmin) {
            boolean isSuperAdmin = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName() == PredefinedRole.ROLE_SUPER_ADMIN);
            if (!isSuperAdmin) {
                throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION);
            }
        }

        // Delete all related data using native queries to avoid FK constraints
        log.info("Deleting user {} and all related data", user.getEmail());
        
        // Delete user roles
        entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete post likes
        entityManager.createNativeQuery("DELETE FROM post_likes WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete post comments
        entityManager.createNativeQuery("DELETE FROM post_comments WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete community posts
        entityManager.createNativeQuery("DELETE FROM community_posts WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete workout sessions
        entityManager.createNativeQuery("DELETE FROM workout_sessions WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete nutrition logs
        entityManager.createNativeQuery("DELETE FROM nutrition_logs WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete health logs
        entityManager.createNativeQuery("DELETE FROM health_logs WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete saved programs
        entityManager.createNativeQuery("DELETE FROM saved_programs WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete program ratings
        entityManager.createNativeQuery("DELETE FROM program_ratings WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete workout programs created by user
        entityManager.createNativeQuery("DELETE FROM workout_programs WHERE creator_user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete refresh tokens
        entityManager.createNativeQuery("DELETE FROM refresh_tokens WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Delete password reset tokens
        entityManager.createNativeQuery("DELETE FROM password_reset_tokens WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        // Finally delete the user
        entityManager.createNativeQuery("DELETE FROM users WHERE user_id = :userId")
                .setParameter("userId", id)
                .executeUpdate();
        
        entityManager.flush();
        log.info("Successfully deleted user {} and all related data", user.getEmail());
    }

    @Transactional
    @Override
    public UserResponse updateUserStatus(UUID id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus(status);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    // ============ GET ALL COACHES ============
    @Override
    public java.util.List<UserResponse> getAllCoaches() {
        java.util.List<User> coaches = userRepository.findByRoleName(PredefinedRole.ROLE_COACH);
        return coaches.stream()
                .map(userMapper::toUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}