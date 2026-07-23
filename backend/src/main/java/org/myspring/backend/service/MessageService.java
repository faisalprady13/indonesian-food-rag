package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.enums.MessageRole;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.model.ChatMessage;
import org.myspring.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    @Transactional
    public void saveUserMessage(Conversation conversation, String content) {
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setContent(content);
        chatMessage.setRole(MessageRole.USER);
        chatMessage.setConversation(conversation);

        messageRepository.save(chatMessage);
    }

    @Transactional
    public void saveAssistantMessage(Conversation conversation, String content) {
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setContent(content);
        chatMessage.setRole(MessageRole.ASSISTANT);
        chatMessage.setConversation(conversation);

        messageRepository.save(chatMessage);
    }
}
