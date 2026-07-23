package org.myspring.backend.dto.response;

import org.myspring.backend.enums.MessageRole;
import org.myspring.backend.model.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        String content,
        MessageRole role,
        LocalDateTime createdAt
) {

    public static ChatMessageResponse fromChatMessage(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getContent(),
                message.getRole(),
                message.getCreatedAt()
        );
    }
}