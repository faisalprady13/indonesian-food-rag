package org.myspring.backend.service.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.request.ChatRequest;
import org.myspring.backend.dto.response.ChatResponse;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.service.ConversationService;
import org.myspring.backend.service.MessageService;
import org.myspring.backend.tool.RecipeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeChatServiceTest {

    @Mock(answer = Answers.RETURNS_SELF)
    private ChatClient.Builder builder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private ConversationService conversationService;

    @Mock
    private MessageService messageService;

    @Mock
    private RecipeTools recipeTools;

    @Mock
    private ChatMemory chatMemory;

    private RecipeChatService recipeChatService;

    @BeforeEach
    void setUp() {
        when(builder.build()).thenReturn(chatClient);
        recipeChatService = new RecipeChatService(builder, conversationService, messageService, recipeTools, chatMemory);
    }

    @SuppressWarnings("unchecked")
    private void stubChatClientCall(String userQuestion, String assistantResponse) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(userQuestion)).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(assistantResponse);
    }

    @Test
    void askQuestion_returnsAssistantResponse_andPersistsBothMessages() {
        Conversation conversation = new Conversation();
        conversation.setId(10L);
        ChatRequest request = new ChatRequest(null, "What is rendang?");

        when(conversationService.getOrCreateConversation(1L, request, "What is rendang?"))
                .thenReturn(conversation);
        stubChatClientCall("What is rendang?", "Rendang is a spicy Indonesian meat dish.");

        ChatResponse response = recipeChatService.askQuestion(1L, request, "What is rendang?");

        assertThat(response.conversationId()).isEqualTo(10L);
        assertThat(response.content()).isEqualTo("Rendang is a spicy Indonesian meat dish.");
        verify(messageService).saveUserMessage(conversation, "What is rendang?");
        verify(messageService).saveAssistantMessage(conversation, "Rendang is a spicy Indonesian meat dish.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void askQuestion_passesConversationIdToAdvisorParams() {
        Conversation conversation = new Conversation();
        conversation.setId(42L);
        ChatRequest request = new ChatRequest(42L, "How do I make soto?");

        when(conversationService.getOrCreateConversation(2L, request, "How do I make soto?"))
                .thenReturn(conversation);
        stubChatClientCall("How do I make soto?", "Soto recipe details.");

        recipeChatService.askQuestion(2L, request, "How do I make soto?");

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> advisorCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(requestSpec).advisors(advisorCaptor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class, Answers.RETURNS_SELF);
        advisorCaptor.getValue().accept(advisorSpec);

        verify(advisorSpec).param("chat_memory_conversation_id", "42");
    }
}
