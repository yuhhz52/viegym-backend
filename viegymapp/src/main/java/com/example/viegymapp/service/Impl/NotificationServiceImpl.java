package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.request.NotificationPreferenceRequest;
import com.example.viegymapp.dto.request.NotificationRequest;
import com.example.viegymapp.dto.response.NotificationPreferenceResponse;
import com.example.viegymapp.dto.response.NotificationResponse;
import com.example.viegymapp.entity.Notification;
import com.example.viegymapp.entity.NotificationPreference;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.NotificationMapper;
import com.example.viegymapp.mapper.NotificationPreferenceMapper;
import com.example.viegymapp.repository.NotificationPreferenceRepository;
import com.example.viegymapp.repository.NotificationRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepo;
    private final NotificationPreferenceRepository preferenceRepo;
    private final UserRepository userRepo;
    private final NotificationMapper notificationMapper;
    private final NotificationPreferenceMapper preferenceMapper;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@viegym.com}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public NotificationResponse createNotification(UUID userId, NotificationRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        Notification notification = notificationMapper.toEntity(request);
        notification.setUser(user);
        notification = notificationRepo.save(notification);
        
        // Get preferences
        NotificationPreference prefs = getOrCreatePreferences(userId);
        
        // Send via channels based on preferences
        if (prefs.getPushEnabled() && shouldSendPush(notification.getType(), prefs)) {
            sendPushNotification(userId, notification);
        }
        
        if (prefs.getEmailEnabled() && shouldSendEmail(notification.getType(), prefs)) {
            sendEmailNotification(userId, notification);
        }
        
        return notificationMapper.toResponse(notification);
    }

    @Override
    public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepo.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepo.countUnreadByUserId(userId);
    }

    @Override
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepo.save(notification);
        
        return notificationMapper.toResponse(notification);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        notificationRepo.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        notificationRepo.delete(notification);
    }

    @Override
    public void deleteAllNotifications(UUID userId) {
        notificationRepo.deleteAllByUserId(userId);
    }

    @Override
    public void generateWorkoutCompletionNotification(UUID userId, int durationMinutes, double volume) {
        String message = String.format("💪 Tuyệt vời! Bạn đã hoàn thành buổi tập %d phút với tổng volume %.0fkg", 
                durationMinutes, volume);
        
        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.WORKOUT)
                .title("Hoàn thành buổi tập")
                .message(message)
                .link("/workout-sessions")
                .build();
        
        createNotification(userId, request);
    }

    @Override
    public void generateAchievementNotification(UUID userId, String achievementType, Object... params) {
        String message = switch (achievementType) {
            case "FIRST_WORKOUT" -> "🎉 Chúc mừng buổi tập đầu tiên! Hành trình fitness bắt đầu từ đây!";
            case "WORKOUT_MILESTONE" -> {
                int count = (int) params[0];
                if (count == 5) yield "⭐ Tuyệt vời! Bạn đã hoàn thành 5 buổi tập!";
                else if (count == 10) yield "💪 Milestone: 10 buổi tập! Bạn đang xây dựng thói quen tốt!";
                else if (count == 50) yield "🚀 Chiến binh thực sự! 50 buổi tập là một thành tựu lớn!";
                else yield String.format("🏆 Không thể tin được! %d buổi tập hoàn thành!", count);
            }
            case "VOLUME_MILESTONE" -> {
                double volume = (double) params[0];
                yield String.format("💎 Thành tựu Volume: Đã nâng được %.0fkg tổng cộng!", volume);
            }
            case "CONSISTENCY" -> {
                int days = (int) params[0];
                yield String.format("📈 Tuyệt vời! Bạn đã tập %d/7 ngày trong tuần này!", days);
            }
            default -> "🏆 Thành tựu mới đã được mở khóa!";
        };
        
        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.ACHIEVEMENT)
                .title("Thành tựu mới")
                .message(message)
                .link("/progress")
                .build();
        
        createNotification(userId, request);
    }

    @Override
    public void generateStreakNotification(UUID userId, int streakDays) {
        String message;
        if (streakDays == 7) {
            message = "🔥 Thành tựu mở khóa: 7 ngày tập liên tiếp!";
        } else if (streakDays == 14) {
            message = "🔥 Thành tựu mở khóa: 14 ngày tập liên tiếp!";
        } else if (streakDays == 30) {
            message = "🔥 Thành tựu mở khóa: 30 ngày tập liên tiếp!";
        } else if (streakDays >= 100) {
            message = String.format("🔥 Thành tựu mở khóa: %d ngày tập liên tiếp! Phi thường!", streakDays);
        } else {
            message = String.format("🔥 Chuỗi %d ngày! Hãy duy trì!", streakDays);
        }
        
        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.STREAK)
                .title("Chuỗi ngày tập")
                .message(message)
                .link("/dashboard")
                .build();
        
        createNotification(userId, request);
    }

    @Override
    public void generateReminderNotification(UUID userId, String message) {
        NotificationRequest request = NotificationRequest.builder()
                .type(Notification.NotificationType.REMINDER)
                .title("Nhắc nhở")
                .message(message)
                .link("/training")
                .build();
        
        createNotification(userId, request);
    }

    @Override
    public void generateBookingNotification(UUID userId, String coachName, String timeSlot, Notification.NotificationType type) {
        String message = type == Notification.NotificationType.BOOKING_CONFIRMED
                ? String.format("✅ Đã xác nhận booking với %s lúc %s", coachName, timeSlot)
                : String.format("❌ Booking với %s lúc %s đã bị hủy", coachName, timeSlot);
        
        NotificationRequest request = NotificationRequest.builder()
                .type(type)
                .title(type == Notification.NotificationType.BOOKING_CONFIRMED ? "Booking đã xác nhận" : "Booking đã hủy")
                .message(message)
                .link("/booking")
                .build();
        
        createNotification(userId, request);
    }

    @Override
    public NotificationPreferenceResponse getPreferences(UUID userId) {
        NotificationPreference prefs = getOrCreatePreferences(userId);
        return preferenceMapper.toResponse(prefs);
    }

    @Override
    public NotificationPreferenceResponse updatePreferences(UUID userId, NotificationPreferenceRequest request) {
        NotificationPreference prefs = getOrCreatePreferences(userId);
        preferenceMapper.updateFromRequest(request, prefs);
        prefs = preferenceRepo.save(prefs);
        return preferenceMapper.toResponse(prefs);
    }

    @Override
    public void sendEmailNotification(UUID userId, Notification notification) {
        try {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("[VieGym] " + notification.getTitle());
            message.setText(buildEmailContent(notification, user));
            
            mailSender.send(message);
            
            notification.setEmailSent(true);
            notificationRepo.save(notification);
            
            log.info("Email notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
        }
    }

    @Override
    public void sendPushNotification(UUID userId, Notification notification) {
        try {
            // Send via WebSocket
            NotificationResponse response = notificationMapper.toResponse(notification);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    response
            );
            
            notification.setPushSent(true);
            notificationRepo.save(notification);
            
            log.info("Push notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
        }
    }
    
    // Helper methods
    private NotificationPreference getOrCreatePreferences(UUID userId) {
        return preferenceRepo.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                    NotificationPreference prefs = NotificationPreference.builder()
                            .user(user)
                            .emailEnabled(true)
                            .emailAchievements(true)
                            .emailWorkouts(false)
                            .emailReminders(true)
                            .emailSocial(true)
                            .emailCoach(true)
                            .emailBooking(true)
                            .pushEnabled(true)
                            .pushAchievements(true)
                            .pushWorkouts(true)
                            .pushReminders(true)
                            .pushSocial(true)
                            .pushCoach(true)
                            .pushBooking(true)
                            .dailyReminder(false)
                            .build();
                    return preferenceRepo.save(prefs);
                });
    }
    
    private boolean shouldSendEmail(Notification.NotificationType type, NotificationPreference prefs) {
        if (!prefs.getEmailEnabled()) return false;
        
        return switch (type) {
            case ACHIEVEMENT -> prefs.getEmailAchievements();
            case WORKOUT -> prefs.getEmailWorkouts();
            case REMINDER -> prefs.getEmailReminders();
            case SOCIAL -> prefs.getEmailSocial();
            case COACH_MESSAGE -> prefs.getEmailCoach();
            case BOOKING_CONFIRMED, BOOKING_CANCELLED -> prefs.getEmailBooking();
            default -> false;
        };
    }
    
    private boolean shouldSendPush(Notification.NotificationType type, NotificationPreference prefs) {
        if (!prefs.getPushEnabled()) return false;
        
        return switch (type) {
            case ACHIEVEMENT -> prefs.getPushAchievements();
            case WORKOUT -> prefs.getPushWorkouts();
            case REMINDER -> prefs.getPushReminders();
            case SOCIAL -> prefs.getPushSocial();
            case COACH_MESSAGE -> prefs.getPushCoach();
            case BOOKING_CONFIRMED, BOOKING_CANCELLED -> prefs.getPushBooking();
            default -> true;
        };
    }
    
    private String buildEmailContent(Notification notification, User user) {
        return String.format("""
                Xin chào %s,
                
                %s
                
                %s
                
                ---
                Bạn nhận được email này vì bạn đã đăng ký nhận thông báo từ VieGym.
                Để thay đổi cài đặt thông báo, vui lòng truy cập: %s/settings
                
                Trân trọng,
                Đội ngũ VieGym
                """,
                user.getFullName(),
                notification.getTitle(),
                notification.getMessage(),
                frontendUrl
        );
    }
}
