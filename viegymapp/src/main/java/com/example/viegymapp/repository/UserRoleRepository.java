package com.example.viegymapp.repository;

import com.example.viegymapp.entity.Enum.UserStatus;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {



}
