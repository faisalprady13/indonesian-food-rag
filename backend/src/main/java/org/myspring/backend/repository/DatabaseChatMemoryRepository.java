package org.myspring.backend.repository;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.UserPrincipal;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.myspring.backend.model.ChatMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.myspring.backend.model.User;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DatabaseChatMemoryRepository implements ChatMemoryRepository {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @Override
    public @NonNull List<String> findConversationIds() {
        return conversationRepository.findAllByUserId(currentUserId())
                .stream()
                .map(c -> String.valueOf(c.getId()))
                .toList();
    }

    @Override
    public @NonNull List<Message> findByConversationId(@NonNull String conversationId) {
        return ownedConversation(conversationId)
                .getChatMessages()
                .stream()
                .map(this::convert)
                .toList();
    }

    // MessageWindowChatMemory.add() re-saves the entire accumulated window on every turn.
    // Persistence for the "messages" table is handled by MessageService instead, so this
    // is a no-op to avoid re-inserting the whole history as duplicates on each turn.
    @Override
    public void saveAll(@NonNull String conversationId, @NonNull List<Message> messages) {
    }

    @Override
    public void deleteByConversationId(@NonNull String conversationId) {
        Conversation conversation = ownedConversation(conversationId);

        messageRepository.deleteAll(conversation.getChatMessages());
    }

    // Conversations are scoped to the authenticated user so this component can't be used
    // to read or delete another user's chat history via a spoofed conversationId.
    private Conversation ownedConversation(String conversationId) {
        return conversationRepository.findByIdAndUserId(Long.valueOf(conversationId), currentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
    }

    private Long currentUserId() {
        Object principal = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        if (!(principal instanceof UserPrincipal(User user))) {
            throw new AccessDeniedException("No authenticated user");
        }

        return user.getId();
    }

    private Message convert(ChatMessage chatMessage) {

        return switch (chatMessage.getRole()) {
            case USER -> new UserMessage(chatMessage.getContent());
            case ASSISTANT -> new AssistantMessage(chatMessage.getContent());
            case SYSTEM -> null;
        };
    }

}

