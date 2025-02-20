package com.esprit.internify.repository;

import com.esprit.internify.entities.Conversation;
import com.esprit.internify.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}