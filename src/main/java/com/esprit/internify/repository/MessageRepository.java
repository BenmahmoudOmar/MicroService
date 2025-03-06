package com.esprit.internify.repository;

import com.esprit.internify.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT COUNT(m), m.messageType FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId GROUP BY m.messageType")
    List<Object[]> countTotalMessagesAndTypesByUser (@Param("userId") Long userId);

    @Query("SELECT COUNT(m), m.messageType FROM Message m WHERE m.sender.id = :userId GROUP BY m.messageType")
    List<Object[]> countTotalSentMessagesAndTypesByUser (@Param("userId") Long userId);

    @Query("SELECT COUNT(m), m.messageType FROM Message m WHERE m.receiver.id = :userId GROUP BY m.messageType")
    List<Object[]> countTotalReceivedMessagesAndTypesByUser (@Param("userId") Long userId);

    @Query("SELECT COUNT(m), m.messageType FROM Message m GROUP BY m.messageType")
    List<Object[]> countTotalMessagesAndTypes ();
}
