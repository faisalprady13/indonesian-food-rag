package org.myspring.backend.dto.request;

public record ChatRequest(
        String conversationId,
        String question
) {
}