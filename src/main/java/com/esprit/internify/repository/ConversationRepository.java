package com.esprit.internify.repository;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c JOIN c.users u WHERE u.id = :userId")
    List<Conversation> findByUserId(@Param("userId") Long userId);
}