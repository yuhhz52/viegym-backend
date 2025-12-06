package com.example.viegymapp.repository;

import com.example.viegymapp.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, UUID> {
    // Query để lấy posts active với user và media
    @Query("SELECT DISTINCT p FROM CommunityPost p " +
           "LEFT JOIN FETCH p.user " +
           "LEFT JOIN FETCH p.media " +
           "WHERE p.status = 'active' " +
           "ORDER BY p.createdAt DESC")
    List<CommunityPost> findAllWithLikes();
    
    List<CommunityPost> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}

