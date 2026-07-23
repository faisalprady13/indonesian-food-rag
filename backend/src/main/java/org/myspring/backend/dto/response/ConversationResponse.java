package org.myspring.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.myspring.backend.model.Conversation;

import java.util.ArrayList;
import java.util.List;

public record ConversationResponse(
        Long id,
        String title,
        List<ChatMessageResponse> messages
) {

    // With messages
    public static ConversationResponse fromConversation(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
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
                new ArrayList<>()
        );
    }
}