package com.example.viegymapp.repository;

import com.example.viegymapp.entity.CommunityPost;
import com.example.viegymapp.entity.PostLike;
import com.example.viegymapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    // Tìm like chưa bị xóa (deleted = false)
    @Query("SELECT pl FROM PostLike pl WHERE pl.post = :post AND pl.user = :user AND pl.deleted = false")
    Optional<PostLike> findByPostAndUser(@Param("post") CommunityPost post, @Param("user") User user);
    
    // Tìm bất kỳ like nào (kể cả đã soft-deleted) - dùng cho toggle logic
    @Query("SELECT pl FROM PostLike pl WHERE pl.post = :post AND pl.user = :user")
    Optional<PostLike> findAnyByPostAndUser(@Param("post") CommunityPost post, @Param("user") User user);
    
    // Đếm số likes chưa bị xóa
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.deleted = false")
    long countActiveByPostId(@Param("postId") UUID postId);
    
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
}
