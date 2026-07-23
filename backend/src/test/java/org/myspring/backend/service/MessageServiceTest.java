package org.myspring.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.enums.MessageRole;
import org.myspring.backend.model.ChatMessage;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.repository.MessageRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void saveUserMessage_savesMessageWithUserRoleAndContent() {
        Conversation conversation = new Conversation();
        conversation.setId(10L);

        messageService.saveUserMessage(conversation, "What is rendang?");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(captor.capture());

        ChatMessage saved = captor.getValue();
        assertThat(saved.getContent()).isEqualTo("What is rendang?");
        assertThat(saved.getRole()).isEqualTo(MessageRole.USER);
        assertThat(saved.getConversation()).isEqualTo(conversation);
    }

    @Test
    void saveAssistantMessage_savesMessageWithAssistantRoleAndContent() {
        Conversation conversation = new Conversation();
        conversation.setId(10L);

        messageService.saveAssistantMessage(conversation, "Rendang is a spicy meat dish.");

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(captor.capture());

        ChatMessage saved = captor.getValue();
        assertThat(saved.getContent()).isEqualTo("Rendang is a spicy meat dish.");
        assertThat(saved.getRole()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(saved.getConversation()).isEqualTo(conversation);
    }
}
