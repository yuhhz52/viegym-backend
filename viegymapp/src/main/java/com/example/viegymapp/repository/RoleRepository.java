package com.example.viegymapp.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.viegymapp.entity.Role;
import com.example.viegymapp.entity.Enum.PredefinedRole;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByName(PredefinedRole name);
    
}
