package org.myspring.backend.repository;

import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findAllByUser(User user);

    List<Conversation> findAllByUserId(Long userId);

    Optional<Conversation> findByIdAndUserId(Long id, Long userId);

    @Query("""
                SELECT c
                FROM Conversation c
                LEFT JOIN FETCH c.chatMessages
                WHERE c.id = :id
                AND c.user.id = :userId
            """)
    Optional<Conversation> findDetailByIdAndUserId(Long id, Long userId);
}
