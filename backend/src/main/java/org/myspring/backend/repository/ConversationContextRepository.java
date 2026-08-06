package org.myspring.backend.repository;

import org.myspring.backend.enums.RecipeListType;
import org.myspring.backend.model.ConversationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationContextRepository extends JpaRepository<ConversationContext, Long> {

    Optional<ConversationContext> findByConversationIdAndListType(Long conversationId, RecipeListType listType);

    void deleteByConversationId(Long conversationId);
}
