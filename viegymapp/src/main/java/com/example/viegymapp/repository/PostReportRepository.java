package com.example.viegymapp.repository;

import com.example.viegymapp.entity.PostReport;
import com.example.viegymapp.entity.CommunityPost;
import com.example.viegymapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, UUID> {
    boolean existsByPostAndReporter(CommunityPost post, User reporter);
    long countByPostAndStatus(CommunityPost post, String status);
    void deleteByPost(CommunityPost post);
}
