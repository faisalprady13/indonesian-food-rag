package org.myspring.backend.service.rag;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.ChatRequest;
import org.myspring.backend.dto.response.ChatResponse;
import org.myspring.backend.factory.ChatClientFactory;
import org.myspring.backend.model.Conversation;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.ConversationService;
import org.myspring.backend.service.MessageService;
import org.myspring.backend.service.UserSettingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class RecipeChatService {

    private final UserSettingService userSettingService;
    private final ConversationService conversationService;
    private final ApiKeyEncryptionService apiKeyEncryptionService;
    private final MessageService messageService;
    private final ChatClientFactory chatClientFactory;

    @Value("${spring.ai.openai.chat.model}")
    private String model;

    @Transactional
    public ChatResponse askQuestion(
            Long userId,
            ChatRequest request,
            String userQuestion
    ) {

        Conversation conversation = conversationService.getOrCreateConversation(
                userId,
                request,
                userQuestion
        );

        String apiKey = apiKeyEncryptionService.decrypt(
                userSettingService
                        .findByUserId(userId)
                        .getApiKey()
        );

        ChatClient chatClient = chatClientFactory.create(apiKey, model);
        String response = chatClient.prompt()
                .user(userQuestion)
                .advisors(advisorSpec ->
                        advisorSpec.param(
                                "chat_memory_conversation_id",
                                conversation.getId().toString()
                        )
                )
                .toolContext(Map.of("conversationId", conversation.getId().toString()))
                .call()
                .content();


        messageService.saveUserMessage(
                conversation,
                userQuestion
        );

        messageService.saveAssistantMessage(
                conversation,
                response
        );

        return new ChatResponse(
                conversation.getId(),
                response
        );
    }
}