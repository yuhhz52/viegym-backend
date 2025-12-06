package com.example.viegymapp.mapper;

import com.example.viegymapp.dto.request.*;
import com.example.viegymapp.dto.response.*;
import com.example.viegymapp.entity.*;
import org.mapstruct.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommunityMapper {

    // -------------------------------
    // CommunityPost
    // -------------------------------
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "media", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "likes", ignore = true)
    CommunityPost toEntity(CommunityPostRequest request);

    @Mapping(target = "authorName", expression = "java(post.getUser() != null && post.getUser().getFullName() != null && !post.getUser().getFullName().isBlank() ? post.getUser().getFullName() : (post.getUser() != null ? post.getUser().getEmail() : null))")
    @Mapping(target = "authorAvatar", expression = "java(post.getUser() != null ? post.getUser().getAvatarUrl() : null)")
    @Mapping(target = "mediaUrls", expression = "java(mapMediaUrls(post.getMedia()))")
    @Mapping(target = "likeCount", expression = "java(countActiveLikes(post.getLikes()))")
    @Mapping(target = "commentCount", expression = "java(safeGetCommentCount(post.getComments()))")
    @Mapping(target = "createdAt", expression = "java(map(post.getCreatedAt()))")
    @Mapping(target = "reportCount", source = "reportCount")
    @Mapping(target = "isLikedByCurrentUser", ignore = true) // Sẽ được set thủ công
    @Mapping(target = "isReportedByCurrentUser", ignore = true) // Sẽ được set thủ công
    CommunityPostResponse toResponse(CommunityPost post);

    default CommunityPostResponse toResponse(CommunityPost post, User currentUser) {
        CommunityPostResponse response = toResponse(post);
        boolean isLiked = false;
        boolean isReported = false;
        
        if (currentUser != null && post.getLikes() != null && !post.getLikes().isEmpty()) {
            isLiked = post.getLikes().stream()
                    .filter(like -> !like.getDeleted()) // CHỈ check likes chưa bị soft-deleted
                    .anyMatch(like -> like.getUser() != null && like.getUser().getId().equals(currentUser.getId()));
        }
        
        if (currentUser != null && post.getReports() != null) {
            try {
                isReported = post.getReports().stream()
                        .anyMatch(report -> report.getReporter() != null && report.getReporter().getId().equals(currentUser.getId()));
            } catch (Exception e) {
                // LazyInitializationException or any other issue - default to false
                isReported = false;
            }
        }
        
        response.setIsLikedByCurrentUser(isLiked);
        response.setIsReportedByCurrentUser(isReported);
        return response;
    }

    default List<String> mapMediaUrls(Set<PostMedia> media) {
        try {
            if (media == null || media.isEmpty()) return List.of();
            return media.stream().map(PostMedia::getUrl).collect(Collectors.toList());
        } catch (Exception e) {
            // LazyInitializationException
            return List.of();
        }
    }

    default long countActiveLikes(Set<PostLike> likes) {
        if (likes == null || likes.isEmpty()) return 0L;
        return likes.stream().filter(like -> !like.getDeleted()).count();
    }

    default long safeGetCommentCount(Set<PostComment> comments) {
        try {
            if (comments == null) return 0L;
            return (long) comments.size();
        } catch (Exception e) {
            // LazyInitializationException - return 0
            return 0L;
        }
    }

    // -------------------------------
    // PostComment
    // -------------------------------
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "replies", ignore = true)
    PostComment toEntity(PostCommentRequest request);

    @Mapping(target = "authorName", expression = "java(comment.getUser() != null && comment.getUser().getFullName() != null && !comment.getUser().getFullName().isBlank() ? comment.getUser().getFullName() : (comment.getUser() != null ? comment.getUser().getEmail() : null))")
    @Mapping(target = "authorAvatar", expression = "java(comment.getUser() != null ? comment.getUser().getAvatarUrl() : null)")
    @Mapping(target = "replies", expression = "java(mapReplies(comment.getReplies()))")
    @Mapping(target = "createdAt", expression = "java(map(comment.getCreatedAt()))")
    @Mapping(target = "parentCommentId", expression = "java(comment.getParentComment() != null ? comment.getParentComment().getId() : null)")
    PostCommentResponse toResponse(PostComment comment);

    default List<PostCommentResponse> mapReplies(Set<PostComment> replies) {
        if (replies == null || replies.isEmpty()) return List.of();
        return replies.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------
    // PostLike
    // -------------------------------
    default PostLikeResponse toLikeResponse(CommunityPost post, User currentUser) {
        if (post == null) return null;
        // Đếm chỉ likes chưa bị xóa (deleted = false)
        long likeCount = post.getLikes() != null 
                ? post.getLikes().stream().filter(like -> !like.getDeleted()).count() 
                : 0;
        boolean isLiked = false;
        if (currentUser != null && post.getLikes() != null) {
            isLiked = post.getLikes().stream()
                    .filter(like -> !like.getDeleted())
                    .anyMatch(like -> like.getUser() != null && like.getUser().getId().equals(currentUser.getId()));
        }
        return PostLikeResponse.builder()
                .postId(post.getId())
                .likeCount(likeCount)
                .isLikedByCurrentUser(isLiked)
                .build();
    }

    // -------------------------------
    // OffsetDateTime -> Instant mapping
    // -------------------------------
    default Instant map(OffsetDateTime value) {
        return value != null ? value.toInstant() : null;
    }
}
