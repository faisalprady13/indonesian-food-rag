package org.myspring.backend.service.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TitleGeneratorService {
    private final ChatClient titleClient;

    public TitleGeneratorService(ChatClient.Builder builder) {

        this.titleClient = builder
                .defaultSystem("""
                            Generate a short title for a cooking conversation.
                            Rules:
                            - Maximum 5 words
                            - No quotes
                            - Return only the title
                        """)
                .build();
    }

    public String generate(String firstMessage) {

        return titleClient.prompt()
                .user(firstMessage)
                .call()
                .content();
    }
}
