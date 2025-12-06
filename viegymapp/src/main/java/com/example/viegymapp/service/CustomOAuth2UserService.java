package com.example.viegymapp.service;

import com.example.viegymapp.entity.Enum.AuthProvider;
import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.entity.Enum.UserStatus;
import com.example.viegymapp.entity.Role;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.UserRole;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.repository.RoleRepository;
import com.example.viegymapp.repository.UserRoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            logger.info("OAuth2 login attempt for email: {}", email);

            if (email == null || email.isBlank()) {
                logger.error("Email not provided by OAuth2 provider");
                throw new OAuth2AuthenticationException("Email not provided by OAuth2 provider");
            }

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        logger.info("Creating new user for email: {}", email);
                        User newUser = User.builder()
                                .email(email)
                                .fullName(name)
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .status(UserStatus.ACTIVE)
                                .provider(AuthProvider.GOOGLE)
                                .userRoles(new HashSet<>())
                                .build();
                        User savedUser = userRepository.save(newUser);
                        assignDefaultRole(savedUser);
                        logger.info("New user created successfully: {}", email);
                        return savedUser;
                    });

            ensureUserHasRole(user);

            // Cập nhật thời gian đăng nhập cuối cùng
            user.setLastLogin(java.time.Instant.now());
            userRepository.save(user);

            logger.info("OAuth2 authentication successful for email: {}", email);
            return new DefaultOAuth2User(
                    mapAuthorities(user),
                    oauth2User.getAttributes(),
                    "email");
        } catch (OAuth2AuthenticationException e) {
            logger.error("OAuth2 authentication exception: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during OAuth2 authentication: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    private void ensureUserHasRole(User user) {
        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            assignDefaultRole(user);
        }
    }

    private void assignDefaultRole(User user) {
        try {
            Role defaultRole = roleRepository.findByName(PredefinedRole.ROLE_USER)
                    .orElseThrow(() -> {
                        logger.error("Default role ROLE_USER not found in database");
                        return new OAuth2AuthenticationException("Default role ROLE_USER not found. Please contact administrator.");
                    });

            // Kiểm tra xem user đã có role này chưa
            boolean alreadyHasRole = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName() == PredefinedRole.ROLE_USER);

            if (!alreadyHasRole) {
                UserRole userRole = UserRole.builder()
                        .user(user)
                        .role(defaultRole)
                        .assignedBy(user)
                        .build();

                user.getUserRoles().add(userRole);
                userRoleRepository.save(userRole);
                userRepository.save(user);
                logger.info("Assigned ROLE_USER to user: {}", user.getEmail());
            }
        } catch (Exception e) {
            logger.error("Error assigning default role to user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new OAuth2AuthenticationException("Failed to assign default role: " + e.getMessage());
        }
    }

    private List<SimpleGrantedAuthority> mapAuthorities(User user) {
        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().name())
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
