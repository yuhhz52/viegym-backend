package com.example.viegymapp.repository;

import com.example.viegymapp.entity.ProgramMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ProgramMediaRepository extends JpaRepository<ProgramMedia, UUID> {
    @Transactional
    @Modifying
    void deleteByProgramId(UUID programId);
}
