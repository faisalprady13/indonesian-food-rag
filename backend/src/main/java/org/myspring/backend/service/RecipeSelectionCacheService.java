package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.enums.RecipeListType;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.ConversationContext;
import org.myspring.backend.repository.ConversationContextRepository;
import org.myspring.backend.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Remembers, per conversation, the database IDs behind the last numbered recipe
 * list shown to the user (search results, favorites, ...), so a later turn like
 * "save number 2" can be resolved to the real recipe ID directly - without
 * depending on the LLM recalling or re-deriving the mapping from chat history.
 */
@Service
@RequiredArgsConstructor
public class RecipeSelectionCacheService {

    private final ConversationContextRepository conversationContextRepository;
    private final ConversationRepository conversationRepository;
    private final UserService userService;

    @Transactional
    public void store(String conversationId, RecipeListType listType, List<Long> recipeIds) throws UnauthorizedException {
        Conversation conversation = ownedConversation(conversationId);

        ConversationContext context = conversationContextRepository
                .findByConversationIdAndListType(conversation.getId(), listType)
                .orElseGet(() -> {
                    ConversationContext newContext = new ConversationContext();
                    newContext.setConversation(conversation);
                    newContext.setListType(listType);
                    return newContext;
                });

        context.getRecipeIds().clear();
        context.getRecipeIds().addAll(recipeIds);

        conversationContextRepository.save(context);
    }

    @Transactional(readOnly = true)
    public Long resolvePosition(String conversationId, RecipeListType listType, int position) throws UnauthorizedException {
        Conversation conversation = ownedConversation(conversationId);

        List<Long> recipeIds = conversationContextRepository
                .findByConversationIdAndListType(conversation.getId(), listType)
                .map(ConversationContext::getRecipeIds)
                .orElseThrow(() -> new IllegalStateException(
                        "No recipe list has been shown yet in this conversation. Show a list before selecting by position."
                ));

        if (position < 1 || position > recipeIds.size()) {
            throw new IllegalArgumentException(
                    "Position " + position + " is out of range. The last shown list has " + recipeIds.size() + " recipes."
            );
        }

        return recipeIds.get(position - 1);
    }

    private Conversation ownedConversation(String conversationId) throws UnauthorizedException {
        return conversationRepository.findByIdAndUserId(Long.valueOf(conversationId), userService.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
    }
}
