package com.example.viegymapp.repository;

import com.example.viegymapp.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<PostComment, UUID> {

    // Lấy tất cả comment của một post, có thể sắp xếp theo createdAt
    List<PostComment> findByPostIdOrderByCreatedAtAsc(UUID postId);

    // Lấy tất cả replies của một comment
    List<PostComment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
}