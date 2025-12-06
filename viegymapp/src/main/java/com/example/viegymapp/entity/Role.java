package com.example.viegymapp.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;
import com.example.viegymapp.entity.Enum.PredefinedRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor          
@AllArgsConstructor

@Builder
public class Role extends BaseEntity{
	@Id
    @UuidGenerator
    @Column(name = "role_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PredefinedRole name;

    // Quan hệ 1-N với User qua UserRole
    @OneToMany(mappedBy = "role", orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    public Role(PredefinedRole name) {
        this.name = name;
    }

}
