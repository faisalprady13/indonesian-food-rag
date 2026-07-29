package org.myspring.backend.service.rag;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.factory.ChatClientFactory;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.UserSettingService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TitleGeneratorService {

    private final ChatClientFactory chatClientFactory;
    private final UserSettingService userSettingService;
    private final ApiKeyEncryptionService apiKeyEncryptionService;

    @Value("${spring.ai.openai.chat.model}")
    private String model;

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
