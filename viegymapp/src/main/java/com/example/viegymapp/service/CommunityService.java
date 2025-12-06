package com.example.viegymapp.service;

import com.example.viegymapp.dto.request.CommunityPostRequest;
import com.example.viegymapp.dto.request.PostCommentRequest;
import com.example.viegymapp.dto.response.CommunityPostResponse;
import com.example.viegymapp.dto.response.PostCommentResponse;
import com.example.viegymapp.dto.response.PostLikeResponse;

import java.util.List;
import java.util.UUID;

public interface CommunityService {
    List<CommunityPostResponse> getAllPosts();
    CommunityPostResponse getPostById(UUID id);
    CommunityPostResponse createPost(CommunityPostRequest request);
    CommunityPostResponse updatePost(UUID id, CommunityPostRequest request);
    void deletePost(UUID id);
    boolean isPostOwner(UUID postId, String username);

    // Comments
    PostCommentResponse addComment(UUID postId, PostCommentRequest request);
    List<PostCommentResponse> getCommentsByPost(UUID postId);
    PostCommentResponse updateComment(UUID commentId, PostCommentRequest request);
    void deleteComment(UUID commentId);
    boolean isCommentOwner(UUID commentId, String username);

    // Likes
    PostLikeResponse likePost(UUID postId);
    PostLikeResponse unlikePost(UUID postId);

    // Reports
    void reportPost(UUID postId, com.example.viegymapp.dto.request.PostReportRequest request);
    List<com.example.viegymapp.dto.response.PostReportResponse> getPostReports(UUID postId);
    void clearPostReports(UUID postId);

    // Admin actions
    CommunityPostResponse approvePost(UUID postId);
    int activateAllPendingPosts();

}
