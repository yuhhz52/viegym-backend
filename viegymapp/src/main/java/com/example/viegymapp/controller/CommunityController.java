package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.CommunityPostRequest;
import com.example.viegymapp.dto.request.PostCommentRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.CommunityPostResponse;
import com.example.viegymapp.dto.response.PostCommentResponse;
import com.example.viegymapp.dto.response.PostLikeResponse;
import com.example.viegymapp.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;


    // Posts
    @GetMapping("/posts")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<List<CommunityPostResponse>> getAllPosts() {
        return ApiResponse.<List<CommunityPostResponse>>builder()
                .result(communityService.getAllPosts())
                .build();
    }

    @GetMapping("/posts/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<CommunityPostResponse> getPostById(@PathVariable UUID id) {
        return ApiResponse.<CommunityPostResponse>builder()
                .result(communityService.getPostById(id))
                .build();
    }

    @PostMapping("/posts")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<CommunityPostResponse> createPost(@RequestBody CommunityPostRequest request) {
        return ApiResponse.<CommunityPostResponse>builder()
                .result(communityService.createPost(request))
                .build();
    }

    @PutMapping("/posts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SUPER_ADMIN') or @communityService.isPostOwner(#id, authentication.name)")
    public ApiResponse<CommunityPostResponse> updatePost(@PathVariable UUID id,
                                                         @RequestBody CommunityPostRequest request) {
        return ApiResponse.<CommunityPostResponse>builder()
                .result(communityService.updatePost(id, request))
                .build();
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SUPER_ADMIN') or @communityService.isPostOwner(#id, authentication.name)")
    public ApiResponse<UUID> deletePost(@PathVariable UUID id) {
        communityService.deletePost(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    // Comments
    @PostMapping("/posts/{id}/comments")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<PostCommentResponse> addComment(@PathVariable UUID id,
                                                       @RequestBody PostCommentRequest request) {
        return ApiResponse.<PostCommentResponse>builder()
                .result(communityService.addComment(id, request))
                .build();
    }

    @GetMapping("/posts/{id}/comments")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<List<PostCommentResponse>> getComments(@PathVariable UUID id) {
        return ApiResponse.<List<PostCommentResponse>>builder()
                .result(communityService.getCommentsByPost(id))
                .build();
    }

    @PutMapping("/comments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SUPER_ADMIN') or @communityService.isCommentOwner(#id, authentication.name)")
    public ApiResponse<PostCommentResponse> updateComment(@PathVariable UUID id,
                                                          @RequestBody PostCommentRequest request) {
        return ApiResponse.<PostCommentResponse>builder()
                .result(communityService.updateComment(id, request))
                .build();
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SUPER_ADMIN') or @communityService.isCommentOwner(#id, authentication.name)")
    public ApiResponse<UUID> deleteComment(@PathVariable UUID id) {
        communityService.deleteComment(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }

    // --- Like / Unlike ---
    @PostMapping("/posts/{id}/likes")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<PostLikeResponse> likePost(@PathVariable UUID id) {
        return ApiResponse.<PostLikeResponse>builder()
                .result(communityService.likePost(id))
                .build();
    }

    @DeleteMapping("/posts/{id}/likes")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<PostLikeResponse> unlikePost(@PathVariable UUID id) {
        return ApiResponse.<PostLikeResponse>builder()
                .result(communityService.unlikePost(id))
                .build();
    }

    // --- Report ---
    @PostMapping("/posts/{id}/report")
    @PreAuthorize("hasAnyRole('USER','ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<String> reportPost(@PathVariable UUID id, 
                                          @RequestBody @jakarta.validation.Valid com.example.viegymapp.dto.request.PostReportRequest request) {
        communityService.reportPost(id, request);
        return ApiResponse.<String>builder()
                .result("Báo cáo đã được gửi thành công")
                .build();
    }

    @GetMapping("/posts/{id}/reports")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<List<com.example.viegymapp.dto.response.PostReportResponse>> getPostReports(@PathVariable UUID id) {
        return ApiResponse.<List<com.example.viegymapp.dto.response.PostReportResponse>>builder()
                .result(communityService.getPostReports(id))
                .build();
    }

    @DeleteMapping("/posts/{id}/reports")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<String> clearPostReports(@PathVariable UUID id) {
        communityService.clearPostReports(id);
        return ApiResponse.<String>builder()
                .result("Đã xóa tất cả báo cáo của bài viết")
                .build();
    }

    // --- Admin: Approve/Activate Posts ---
    @PutMapping("/posts/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SUPER_ADMIN')")
    public ApiResponse<CommunityPostResponse> approvePost(@PathVariable UUID id) {
        return ApiResponse.<CommunityPostResponse>builder()
                .result(communityService.approvePost(id))
                .build();
    }

    // --- Admin: Activate ALL pending posts (migration helper) ---
    @PostMapping("/posts/activate-all-pending")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ApiResponse<String> activateAllPendingPosts() {
        int count = communityService.activateAllPendingPosts();
        return ApiResponse.<String>builder()
                .result("Đã kích hoạt " + count + " bài viết pending")
                .build();
    }

}
