package org.myspring.backend.dto.request;

public record ChatRequest(
        Long conversationId,
        String content
) {
}