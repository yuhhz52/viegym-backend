package com.example.viegymapp.repository;

import com.example.viegymapp.entity.SessionExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface SessionExerciseLogRepository extends JpaRepository<SessionExerciseLog, UUID> {
    
    @Query("SELECT log FROM SessionExerciseLog log " +
           "LEFT JOIN FETCH log.exercise " +
           "WHERE log.session.id = :sessionId " +
           "ORDER BY log.setNumber ASC")
    List<SessionExerciseLog> findBySessionId(@Param("sessionId") UUID sessionId);
}
