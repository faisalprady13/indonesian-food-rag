package org.myspring.backend.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myspring.backend.factory.ChatClientFactory;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.ConversationService;
import org.myspring.backend.service.UserSettingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TitleGeneratorService {

    private final ChatClientFactory chatClientFactory;
    private final UserSettingService userSettingService;
    private final ApiKeyEncryptionService apiKeyEncryptionService;
    private final ConversationService conversationService;

    @Value("${spring.ai.openai.chat.model}")
    private String model;

    @Async("titleGenerationExecutor")
    public void generateAndApply(Long userId, Long conversationId, String firstMessage) {
        try {
            String title = generate(userId, firstMessage);
            conversationService.updateGeneratedTitle(conversationId, title);
        } catch (Exception e) {
            log.warn("Title generation failed for conversation {}", conversationId, e);
        }
    }

    public String generate(Long userId, String firstMessage) {

        String apiKey = apiKeyEncryptionService.decrypt(
                userSettingService.findByUserId(userId).getApiKey()
        );

        ChatClient titleClient = chatClientFactory.createChatClientBuilder(apiKey, model)
                .defaultSystem("""
                            Generate a short title for a cooking conversation.
                            Rules:
                            - Maximum 5 words
                            - No quotes
                            - Return only the title
                        """)
                .build();

        return titleClient.prompt()
                .user(firstMessage)
                .call()
                .content();
    }
}
