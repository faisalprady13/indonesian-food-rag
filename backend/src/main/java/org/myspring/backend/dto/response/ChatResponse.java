package org.myspring.backend.dto.response;

import lombok.Builder;
import org.myspring.backend.enums.MessageRole;

@Builder
public record ChatResponse(
        Long conversationId,
        MessageRole role,
        String content
) {

    public ChatResponse(Long conversationId, String content) {
        this(conversationId, MessageRole.ASSISTANT, content);
    }
}