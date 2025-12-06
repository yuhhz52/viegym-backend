package com.example.viegymapp.service.Impl;

import com.example.viegymapp.entity.Enum.UserStatus;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userrepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userrepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new DisabledException("Tài khoản đã bị khóa");
        }

        log.debug("Loading user: {} with {} roles", username, user.getUserRoles().size());
        
        return UserDetailsImpl.build(user);
    }
}
