package com.example.viegymapp.repository;

import com.example.viegymapp.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :userId OR m.receiver.id = :userId) ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesByUser(@Param("userId") UUID userId);
    
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id) ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversationBetweenUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessagesByUser(@Param("userId") UUID userId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.receiver.id = :userId AND m.isRead = false")
    List<ChatMessage> findUnreadMessagesByUser(@Param("userId") UUID userId);
    
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE (m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR (m.sender.id = :user2Id AND m.receiver.id = :user1Id)")
    void deleteConversationBetweenUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
}
