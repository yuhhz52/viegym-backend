package com.example.viegymapp.repository;

import com.example.viegymapp.entity.BookingSession;
import com.example.viegymapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingSessionRepository extends JpaRepository<BookingSession, UUID> {
    
    @Query("SELECT b FROM BookingSession b WHERE b.coach = :coach AND b.deleted = false ORDER BY b.bookingTime DESC")
    List<BookingSession> findByCoach(@Param("coach") User coach);
    
    @Query("SELECT b FROM BookingSession b WHERE b.client = :client AND b.deleted = false ORDER BY b.bookingTime DESC")
    List<BookingSession> findByClient(@Param("client") User client);
    
    @Query("SELECT b FROM BookingSession b WHERE b.coach.id = :coachId AND b.deleted = false ORDER BY b.bookingTime DESC")
    List<BookingSession> findByCoachId(@Param("coachId") UUID coachId);
    
    @Query("SELECT COUNT(b) > 0 FROM BookingSession b WHERE b.timeSlot = :timeSlot AND b.status IN ('PENDING', 'CONFIRMED') AND b.deleted = false")
    boolean existsByTimeSlot(@Param("timeSlot") com.example.viegymapp.entity.CoachTimeSlot timeSlot);
    
    @Query("SELECT b FROM BookingSession b WHERE b.coach.id = :coachId AND b.status IN ('PENDING', 'CONFIRMED') AND b.deleted = false ORDER BY b.bookingTime")
    List<BookingSession> findUpcomingBookingsByCoach(@Param("coachId") UUID coachId);
    
    @Query("SELECT b FROM BookingSession b WHERE b.client.id = :clientId AND b.status IN ('PENDING', 'CONFIRMED') AND b.deleted = false ORDER BY b.bookingTime")
    List<BookingSession> findUpcomingBookingsByClient(@Param("clientId") UUID clientId);
    
    @Query("SELECT b FROM BookingSession b WHERE (b.coach.id = :userId OR b.client.id = :userId) AND b.bookingTime >= :start AND b.bookingTime <= :end AND b.deleted = false ORDER BY b.bookingTime")
    List<BookingSession> findBookingsByUserAndDateRange(@Param("userId") UUID userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(b) FROM BookingSession b WHERE b.coach = :coach AND b.deleted = false")
    long countByCoach(@Param("coach") User coach);
    
    @Query("SELECT COUNT(b) FROM BookingSession b WHERE b.coach = :coach AND b.status = :status AND b.deleted = false")
    long countByCoachAndStatus(@Param("coach") User coach, @Param("status") BookingSession.BookingStatus status);
    
    // Rate limiting queries
    @Query("SELECT COUNT(b) FROM BookingSession b WHERE b.client = :client AND b.createdAt >= :since AND b.deleted = false")
    long countByClientAndCreatedAtAfter(@Param("client") User client, @Param("since") OffsetDateTime since);
    
    @Query("SELECT COUNT(b) FROM BookingSession b WHERE b.client = :client AND b.status = :status AND b.updatedAt >= :since AND b.deleted = false")
    long countByClientAndStatusAndUpdatedAtAfter(@Param("client") User client, @Param("status") BookingSession.BookingStatus status, @Param("since") OffsetDateTime since);
    
    // Scheduled task queries
    @Query("SELECT b FROM BookingSession b WHERE b.status = :status AND b.bookingTime < :cutoffTime AND b.deleted = false")
    List<BookingSession> findByStatusAndBookingTimeBefore(@Param("status") BookingSession.BookingStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT b FROM BookingSession b WHERE b.status = :status AND b.createdAt < :cutoffTime AND b.deleted = false")
    List<BookingSession> findByStatusAndCreatedAtBefore(@Param("status") BookingSession.BookingStatus status, @Param("cutoffTime") Instant cutoffTime);
    
    @Query("SELECT b FROM BookingSession b WHERE b.status = :status AND b.createdAt < :cutoffTime AND b.deleted = false")
    List<BookingSession> findByStatusAndCreatedAtBefore(@Param("status") BookingSession.BookingStatus status, @Param("cutoffTime") OffsetDateTime cutoffTime);
    
    // Query expired PENDING bookings
    @Query("SELECT b FROM BookingSession b WHERE b.status = 'PENDING' AND b.expiredAt < :now AND b.deleted = false")
    List<BookingSession> findExpiredPendingBookings(@Param("now") LocalDateTime now);
    
    // Find completed bookings by coach
    @Query("SELECT b FROM BookingSession b WHERE b.coach.id = :coachId AND b.status = 'COMPLETED' AND b.deleted = false ORDER BY b.bookingTime DESC")
    List<BookingSession> findCompletedBookingsByCoachId(@Param("coachId") UUID coachId);
}
