package org.myspring.backend.dto.response;

import org.myspring.backend.model.Conversation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record ConversationResponse(
        Long id,
        String title,
        Boolean pinned,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ChatMessageResponse> messages
) {

    // With messages
    public static ConversationResponse fromConversation(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getPinned(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getChatMessages()
                        .stream()
                        .map(ChatMessageResponse::fromChatMessage)
                        .toList()
        );
    }

    // Without messages
    public static ConversationResponse fromConversationWithoutMessages(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getPinned(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                new ArrayList<>()
        );
    }
}