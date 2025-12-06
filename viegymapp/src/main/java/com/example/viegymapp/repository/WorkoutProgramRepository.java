package com.example.viegymapp.repository;

import com.example.viegymapp.entity.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface WorkoutProgramRepository extends JpaRepository<WorkoutProgram, UUID> {
    // Get all programs: user's programs + public programs from others
    @Query("SELECT DISTINCT p FROM WorkoutProgram p LEFT JOIN p.creator c LEFT JOIN c.userRoles ur LEFT JOIN ur.role r " +
           "WHERE (p.creator.id = :userId) OR " +
           "(UPPER(p.visibility) = 'PUBLIC' AND (r.name = 'ROLE_ADMIN' OR r.name = 'ROLE_SUPER_ADMIN'))")
    List<WorkoutProgram> findUserAndPublicPrograms(@Param("userId") UUID userId);
    
    // Get only public programs from ADMIN/SUPER_ADMIN (for explore page)
    @Query("SELECT DISTINCT p FROM WorkoutProgram p JOIN p.creator c JOIN c.userRoles ur JOIN ur.role r " +
           "WHERE UPPER(p.visibility) = 'PUBLIC' AND (r.name = 'ROLE_ADMIN' OR r.name = 'ROLE_SUPER_ADMIN')")
    List<WorkoutProgram> findAllPublicPrograms();
    
    // Get programs by creator
    @Query("SELECT p FROM WorkoutProgram p WHERE p.creator.id = :creatorId")
    List<WorkoutProgram> findByCreatorId(@Param("creatorId") UUID creatorId);
    
    // Count programs by creator
    long countByCreatorId(UUID creatorId);
}
