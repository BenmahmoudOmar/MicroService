package com.esprit.internify.repository;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import com.esprit.internify.entities.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c JOIN c.users u WHERE u.id = :userId")
    List<Conversation> findByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.conversation.id = :conversationId) AND " +
            "(:content IS NULL OR LOWER(m.content) LIKE LOWER(CONCAT('%', :content, '%'))) AND " +
            "(:startOfDay IS NULL OR m.timestamp BETWEEN :startOfDay AND :endOfDay)")
    List<Message> searchMessagesInConversation(
            @Param("conversationId") Long conversationId,
            @Param("content") String content,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COUNT(m), m.messageType FROM Message m WHERE m.conversation.id = :conversationId GROUP BY m.messageType")
    List<Object[]> countMessagesByTypeInConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId")
    Long countTotalMessagesInConversation(@Param("conversationId") Long conversationId);

    @Query("SELECT m.timestamp FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp ASC")
    List<LocalDateTime> getAllSentDatesByConversation(@Param("conversationId") Long conversationId);

}