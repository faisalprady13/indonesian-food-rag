package org.myspring.backend.dto.response;

import lombok.Builder;

@Builder
public record ChatResponse(
        String conversationId,
        String answer
) {
}