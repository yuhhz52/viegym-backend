package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.CommunityPostRequest;
import com.example.viegymapp.dto.request.PostCommentRequest;
import com.example.viegymapp.dto.response.CommunityPostResponse;
import com.example.viegymapp.dto.response.PostCommentResponse;
import com.example.viegymapp.dto.response.PostLikeResponse;
import com.example.viegymapp.entity.*;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.CommunityMapper;
import com.example.viegymapp.repository.CommentRepository;
import com.example.viegymapp.repository.CommunityPostRepository;
import com.example.viegymapp.repository.PostLikeRepository;
import com.example.viegymapp.repository.PostMediaRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.CommentService;
import com.example.viegymapp.service.CommunityService;
import com.example.viegymapp.service.LikeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service("communityService")
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {
    // Lock map để tránh race condition khi toggle like
    private final Map<String, Object> likeLocks = new ConcurrentHashMap<>();
    
    private final CommunityPostRepository postRepo;
    private final CommentRepository commentRepo;
    private final PostLikeRepository likeRepo;
    private final PostMediaRepository mediaRepo;
    private final UserRepository userRepo;
    private final com.example.viegymapp.repository.PostReportRepository reportRepo;
    private final CommunityMapper mapper;
    private final CommentService commentService;
    private final LikeService likeService;

    @Override
    public List<CommunityPostResponse> getAllPosts() {
        User currentUser = getCurrentUserOrNull();
        List<CommunityPost> posts = postRepo.findAllWithLikes();
        
        // Force initialize likes collection để tránh LazyInitializationException
        posts.forEach(post -> {
            if (post.getLikes() != null) {
                post.getLikes().size(); // Trigger lazy loading
            }
        });
        
        return posts.stream()
                .map(post -> mapper.toResponse(post, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    public CommunityPostResponse getPostById(UUID id) {
        CommunityPost post = postRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        User currentUser = getCurrentUserOrNull();
        return mapper.toResponse(post, currentUser);
    }

    @Override
    public CommunityPostResponse createPost(CommunityPostRequest request) {
        CommunityPost post = mapper.toEntity(request);

        // Lấy current user
        User user = getCurrentUser();
        post.setUser(user);

        // Nếu cần kiểm duyệt, có thể check role ở đây
        if (post.getStatus() == null || post.getStatus().isBlank()) {
            post.setStatus("active"); // Thay đổi từ "pending" sang "active"
        }

        if (request.getMediaUrls() != null && !request.getMediaUrls().isEmpty()) {
            for (String url : request.getMediaUrls()) {
                PostMedia media = PostMedia.builder()
                        .post(post)
                        .url(url)
                        .mediaType(detectMediaType(url))
                        .build();
                post.getMedia().add(media);
            }
        }
        post = postRepo.saveAndFlush(post);
        User currentUser = getCurrentUserOrNull();
        return mapper.toResponse(post, currentUser);
    }

    private String detectMediaType(String url) {
        if (url.endsWith(".mp4") || url.endsWith(".mov")) {
            return "VIDEO";
        }
        return "IMAGE";
    }

    @Override
    public CommunityPostResponse updatePost(UUID id, CommunityPostRequest request) {
        CommunityPost post = postRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // Xóa toàn bộ media cũ trong danh sách
        post.getMedia().clear();

        if (request.getMediaUrls() != null && !request.getMediaUrls().isEmpty()) {
            for (String url : request.getMediaUrls()) {
                PostMedia media = PostMedia.builder()
                        .url(url)
                        .mediaType("IMAGE")
                        .post(post)
                        .build();
                post.getMedia().add(media);
            }
        }

        // Hibernate tự xử lý cascade và orphanRemoval
        postRepo.save(post);

        User currentUser = getCurrentUserOrNull();
        return mapper.toResponse(post, currentUser);
    }

    @Override
    public void deletePost(UUID id) {
        CommunityPost post = postRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        mediaRepo.deleteAll(post.getMedia());
        commentRepo.deleteAll(post.getComments());
        likeRepo.deleteAll(post.getLikes());
        postRepo.delete(post);
    }

    @Override
    public boolean isPostOwner(UUID postId, String username) {
        return postRepo.findById(postId)
                .map(post -> post.getUser() != null && username != null
                        && username.equalsIgnoreCase(post.getUser().getEmail()))
                .orElse(false);
    }

    @Override
    public PostCommentResponse addComment(UUID postId, PostCommentRequest request) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        PostComment comment = mapper.toEntity(request);
        comment.setPost(post);

        // Lấy current user
        User user = getCurrentUser();
        comment.setUser(user);

        if (request.getParentCommentId() != null) {
            PostComment parent = commentRepo.findById(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
            comment.setParentComment(parent);
        }

        return commentService.saveAndPublish(comment);
    }

    @Override
    public List<PostCommentResponse> getCommentsByPost(UUID postId) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        return post.getComments().stream()
                .filter(c -> c.getParentComment() == null) // chỉ lấy comment gốc
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostCommentResponse updateComment(UUID commentId, PostCommentRequest request) {
        PostComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new  AppException(ErrorCode.COMMENT_NOT_FOUND));
        comment.setContent(request.getContent());
        return mapper.toResponse(commentRepo.save(comment));
    }

    @Override
    public void deleteComment(UUID commentId) {
        PostComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        commentRepo.delete(comment);
    }

    @Override
    public boolean isCommentOwner(UUID commentId, String username) {
        return commentRepo.findById(commentId)
                .map(comment -> comment.getUser() != null && username != null
                        && username.equalsIgnoreCase(comment.getUser().getEmail()))
                .orElse(false);
    }

    @Override
    public PostLikeResponse likePost(UUID postId) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        User user = getCurrentUser();

        String lockKey = postId + ":" + user.getId();
        Object lock = likeLocks.computeIfAbsent(lockKey, k -> new Object());
        
        synchronized (lock) {
            Optional<PostLike> existing = likeRepo.findAnyByPostAndUser(post, user);
        
            if (existing.isPresent()) {
                PostLike likeRecord = existing.get();
            
                if (likeRecord.getDeleted()) {
                    likeRecord.setDeleted(false);
                    likeRepo.save(likeRecord);
                
                    long likeCount = likeRepo.countActiveByPostId(postId);
                
                    PostLikeResponse response = PostLikeResponse.builder()
                            .postId(postId)
                            .likeCount(likeCount)
                            .isLikedByCurrentUser(true)
                            .build();
                
                    likeService.broadcastLikeUpdate(postId, likeCount);
                    return response;
                } else {
                    likeRecord.setDeleted(true);
                    likeRepo.save(likeRecord);
                
                    long likeCount = likeRepo.countActiveByPostId(postId);
                    
                    PostLikeResponse response = PostLikeResponse.builder()
                            .postId(postId)
                            .likeCount(likeCount)
                            .isLikedByCurrentUser(false)
                            .build();
                
                    likeService.broadcastLikeUpdate(postId, likeCount);
                    return response;
                }
            }

            PostLike like = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            like = likeRepo.save(like);
        
            long likeCount = likeRepo.countActiveByPostId(postId);
            
            PostLikeResponse response = PostLikeResponse.builder()
                    .postId(postId)
                    .likeCount(likeCount)
                    .isLikedByCurrentUser(true)
                    .build();
            
            likeService.broadcastLikeUpdate(postId, likeCount);
            
            return response;
        }
    }

    @Override
    public PostLikeResponse unlikePost(UUID postId) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        User user = getCurrentUser();

        Optional<PostLike> existing = likeRepo.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            PostLike likeToDelete = existing.get();
            likeToDelete.setDeleted(true);
            likeRepo.save(likeToDelete);
        }
        
        long likeCount = likeRepo.countActiveByPostId(postId);
        
        PostLikeResponse response = PostLikeResponse.builder()
                .postId(postId)
                .likeCount(likeCount)
                .isLikedByCurrentUser(false)
                .build();
        
        likeService.broadcastLikeUpdate(postId, likeCount);
        
        return response;
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private User getCurrentUserOrNull() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepo.findByEmail(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void reportPost(UUID postId, com.example.viegymapp.dto.request.PostReportRequest request) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        
        User reporter = getCurrentUser();
        
        // Check if user already reported this post
        if (reportRepo.existsByPostAndReporter(post, reporter)) {
            throw new AppException(ErrorCode.ALREADY_REPORTED);
        }
        
        PostReport report = PostReport.builder()
                .post(post)
                .reporter(reporter)
                .reason(request.getReason())
                .description(request.getDescription())
                .status("pending")
                .build();
        
        reportRepo.save(report);
        
        // Increment report count
        post.setReportCount(post.getReportCount() + 1);
        postRepo.save(post);
    }

    @Override
    public List<com.example.viegymapp.dto.response.PostReportResponse> getPostReports(UUID postId) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        
        return post.getReports().stream()
                .map(report -> com.example.viegymapp.dto.response.PostReportResponse.builder()
                        .id(report.getId())
                        .postId(post.getId())
                        .postTitle(post.getTitle())
                        .reporterId(report.getReporter().getId())
                        .reporterName(report.getReporter().getFullName())
                        .reason(report.getReason())
                        .description(report.getDescription())
                        .status(report.getStatus())
                        .createdAt(report.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearPostReports(UUID postId) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        
        // Xóa tất cả reports của bài viết
        reportRepo.deleteByPost(post);
        
        // Cập nhật reportCount về 0
        post.setReportCount(0);
        postRepo.save(post);
    }

    @Override
    @Transactional
    public CommunityPostResponse approvePost(UUID postId) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        
        post.setStatus("active");
        post = postRepo.save(post);
        
        User currentUser = getCurrentUserOrNull();
        return mapper.toResponse(post, currentUser);
    }

    @Override
    @Transactional
    public int activateAllPendingPosts() {
        List<CommunityPost> pendingPosts = postRepo.findAll().stream()
                .filter(post -> "pending".equals(post.getStatus()))
                .collect(Collectors.toList());
        
        pendingPosts.forEach(post -> post.setStatus("active"));
        postRepo.saveAll(pendingPosts);
        
        return pendingPosts.size();
    }
}

